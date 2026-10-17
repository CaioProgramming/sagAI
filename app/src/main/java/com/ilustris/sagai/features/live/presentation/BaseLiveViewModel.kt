package com.ilustris.sagai.features.live.presentation

import android.Manifest
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.AudioGenClient
import com.ilustris.sagai.core.ai.QuotaExhaustedException
import com.ilustris.sagai.core.ai.key.QuotaStatus
import com.ilustris.sagai.core.media.SagaPlaybackService
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.permissions.PermissionStatus
import com.ilustris.sagai.core.utils.AudioUtils
import com.ilustris.sagai.features.audiobook.data.usecase.WaveformExtractor
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.live.data.LiveAudioPlayer
import com.ilustris.sagai.features.live.data.LiveBackgroundWork
import com.ilustris.sagai.features.live.data.VoiceRecorder
import com.ilustris.sagai.features.live.data.VoiceRecording
import com.ilustris.sagai.features.saga.chat.data.voicing.MessageBlocks
import com.ilustris.sagai.features.saga.chat.data.voicing.VoicedClip
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * The choreography every live conversation shares: hold → record → audio gate → (the subclass
 * sends the turn) → the player's corrected line on stage while the reply is voiced → the reply
 * played with karaoke captions → back to idle. Barge-in, the silent fallback when voices are
 * resting, ducking the saga's music and ending everything on the way to the background live here.
 *
 * Subclasses own only what differs: how a turn is sent and where the reply comes from — the saga's
 * chat pipeline ([LiveConversationViewModel]) or a single character after the story ended
 * ([EpilogueLiveViewModel]).
 */
abstract class BaseLiveViewModel(
    private val permissionService: PermissionService,
    private val recorder: VoiceRecorder,
    private val player: LiveAudioPlayer,
    private val audioGenClient: AudioGenClient,
    protected val backgroundWork: LiveBackgroundWork,
    private val context: Context,
) : ViewModel() {
    protected val _state = MutableStateFlow(LiveUiState())
    val state: StateFlow<LiveUiState> = _state.asStateFlow()

    /** Mic level while listening, playback loudness while speaking. Read it in the draw phase. */
    private val _level = MutableStateFlow(0f)
    val level: StateFlow<Float> = _level.asStateFlow()

    /**
     * How much of the caption's current block has been spoken, 0..1. Ticks with playback, so it's
     * its own flow: only the caption's words read it, and only to flip spoken/unspoken.
     */
    private val _captionProgress = MutableStateFlow(0f)
    val captionProgress: StateFlow<Float> = _captionProgress.asStateFlow()

    protected val _reactions = MutableSharedFlow<LiveReaction>(extraBufferCapacity = 16)
    val reactions: SharedFlow<LiveReaction> = _reactions.asSharedFlow()

    /** Whether the player can pick who they speak as. A one-on-one conversation can't. */
    open val canSwitchSpeaker: Boolean = true

    private var sessionVoicesSpent = false
    private var listenJob: Job? = null
    private var turnJob: Job? = null

    /** Call once the subclass starts its session. */
    protected fun startSharedObservers() {
        observeVoiceQuota()
        observeMusicDucking()
    }

    fun hasMicPermission() = permissionService.getPermissionStatus(Manifest.permission.RECORD_AUDIO) == PermissionStatus.GRANTED

    open fun selectSpeaker(characterId: Int) {
        if (!canSwitchSpeaker) return
        if (_state.value.phase != LivePhase.Idle && _state.value.phase !is LivePhase.Speaking) return
        _state.update { it.copy(selectedSpeakerId = characterId) }
    }

    open fun retry() = Unit

    // region Hold to talk

    fun onPressStart() {
        val current = _state.value
        if (current.inputBlocked) return setHint(LiveHint.MILESTONE)
        if (!hasMicPermission()) return setHint(LiveHint.PERMISSION_NEEDED)
        if (current.phase == LivePhase.Thinking || current.phase == LivePhase.Voicing || current.phase == LivePhase.Listening) return
        if (current.phase is LivePhase.Speaking) interruptSpeaking()
        if (!recorder.start()) return setHint(LiveHint.MIC_UNAVAILABLE)

        _state.update {
            it.copy(
                phase = LivePhase.Listening,
                focus = LiveFocus(it.selectedSpeaker),
                caption = null,
                reasoning = null,
                canRetry = false,
                hint = LiveHint.RELEASE_TO_SEND,
            )
        }
        listenJob?.cancel()
        listenJob =
            viewModelScope.launch {
                launch { recorder.level.collect { _level.value = it } }
                recorder.maxReached.first { it }
                onPressEnd(cancel = false)
            }
    }

    fun onCancelArmed(armed: Boolean) {
        if (_state.value.phase != LivePhase.Listening) return
        setHint(if (armed) LiveHint.RELEASE_TO_CANCEL else LiveHint.RELEASE_TO_SEND)
    }

    fun onPressEnd(cancel: Boolean) {
        if (_state.value.phase != LivePhase.Listening) return
        listenJob?.cancel()
        _level.value = 0f
        if (cancel) {
            recorder.cancel()
            return toIdle(LiveHint.CANCELLED)
        }
        viewModelScope.launch {
            val recording = recorder.stop()
            if (recording == null || !recording.passesGate) {
                recording?.file?.delete()
                return@launch toIdle(LiveHint.TOO_SHORT)
            }
            send(recording)
        }
    }

    // endregion

    // region Turn

    /** A recording passed the gate: send it as the player's turn. */
    protected abstract suspend fun send(recording: VoiceRecording)

    /**
     * The app went to the background while a turn was out: the subclass lets it go, so its reply
     * lands silently instead of playing in the background.
     */
    protected open fun abandonPendingTurn() = Unit

    /** A turn started or ended — the saga uses it to hold the milestone until the line is heard. */
    protected open fun onTurnInFlight(inFlight: Boolean) = Unit

    /** The recording was accepted: the sender stays in focus while the reply is written. */
    protected fun enterThinking(speaker: Character?) {
        setTurnInFlight(true)
        _state.update {
            it.copy(
                phase = LivePhase.Thinking,
                focus = LiveFocus(speaker),
                caption = null,
                playerLinePending = true,
                canRetry = false,
                hint = LiveHint.WAITING_REPLY,
            )
        }
    }

    /**
     * The reply's text landed. Its audio isn't made yet, so the player's corrected [playerLine]
     * takes the stage meanwhile — the reply's own text stays hidden until it's heard. Then [voice]
     * runs (in [LiveBackgroundWork], so once started it finishes even if the player leaves) and the
     * reply plays; without audio (voices resting, TTS failed) its [replyText] plays as captions.
     */
    protected fun performReply(
        playerLine: String,
        replyText: String,
        replyFocus: LiveFocus,
        voice: suspend () -> VoicedClip?,
    ) {
        _state.update {
            it.copy(
                phase = LivePhase.Voicing,
                nextFocus = replyFocus,
                playerLinePending = false,
                reasoning = null,
                caption = LiveCaption(MessageBlocks.split(playerLine), isPlayerLine = true),
                hint = if (it.voicesRestingUntil != null) LiveHint.SPEAKING_SILENTLY else LiveHint.PREPARING_VOICE,
            )
        }

        turnJob?.cancel()
        turnJob =
            viewModelScope.launch {
                val voiced =
                    if (_state.value.voicesRestingUntil != null) {
                        delay(SILENT_VOICING_BEAT_MS)
                        null
                    } else {
                        voiceInBackground(voice)
                    }
                if (voiced != null) {
                    speakAudio(voiced, replyFocus)
                } else {
                    speakSilently(replyText, replyFocus)
                }
                // A barge-in has already moved on to Listening; anything else (the clip ended,
                // audio focus was lost) closes the turn here.
                if (_state.value.phase is LivePhase.Speaking) finishTurn()
            }
    }

    /** The reply failed: show it, offer a retry, and let the next hold start over. */
    protected fun failTurn(hint: LiveHint = LiveHint.REPLY_FAILED) {
        setTurnInFlight(false)
        _state.update {
            it.copy(
                phase = LivePhase.Recovering,
                reasoning = null,
                playerLinePending = false,
                canRetry = true,
                hint = hint,
            )
        }
    }

    /** A spent voice quota switches the rest of the session to captions. */
    private suspend fun voiceInBackground(voice: suspend () -> VoicedClip?): VoicedClip? {
        val work = backgroundWork.scope.async { voice() }
        return try {
            work.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: QuotaExhaustedException) {
            sessionVoicesSpent = true
            _state.update { it.copy(voicesRestingUntil = e.until) }
            null
        } catch (e: Exception) {
            Timber.w(e, "Live voicing failed; speaking this turn through captions")
            null
        }
    }

    private suspend fun speakAudio(
        voiced: VoicedClip,
        focus: LiveFocus,
    ) {
        val (envelope, durationMs) =
            withContext(Dispatchers.IO) {
                val file = File(voiced.audioPath)
                runCatching { WaveformExtractor.envelope(file.readBytes()) }.getOrDefault(FloatArray(0)) to
                    (AudioUtils.wavDurationMs(file) ?: 0L)
            }
        val timeline = CaptionTimeline.from(voiced.script, durationMs)
        _captionProgress.value = 0f
        _state.update {
            it.copy(
                phase = LivePhase.Speaking(silent = false),
                focus = focus,
                nextFocus = null,
                caption = LiveCaption(voiced.blocks),
                hint = LiveHint.INTERRUPT,
            )
        }
        val peak = envelope.maxOrNull()?.takeIf { it > 0f } ?: 1f
        val completed =
            player.play(voiced.audioPath) { position ->
                val window = (position / WaveformExtractor.STEP_MS).toInt()
                _level.value = ((envelope.getOrNull(window) ?: 0f) / peak).coerceIn(0f, 1f)
                val line = timeline.lineAt(position) ?: return@play
                if (line.block >= 0) _captionProgress.value = timeline.progressAt(position)
                _state.update { state ->
                    state.copy(
                        caption =
                            state.caption?.let { caption ->
                                caption.copy(current = if (line.block >= 0) line.block else caption.current)
                            },
                        focus = if (line.isNarrator) LiveFocus(null) else focus,
                    )
                }
            }
        _level.value = 0f
        if (!completed) Timber.d("Live playback stopped before the end")
    }

    private suspend fun speakSilently(
        text: String,
        focus: LiveFocus,
    ) {
        val blocks = MessageBlocks.split(text)
        _state.update {
            it.copy(
                phase = LivePhase.Speaking(silent = true),
                focus = focus,
                nextFocus = null,
                caption = LiveCaption(blocks),
                hint = LiveHint.SPEAKING_SILENTLY,
            )
        }
        blocks.forEach { block ->
            _captionProgress.value = 0f
            _state.update { it.copy(caption = it.caption?.copy(current = block.index)) }
            // No audio to follow: the fill advances at reading pace instead.
            val blockMs = (block.text.length * SILENT_MS_PER_CHAR).coerceAtLeast(SILENT_MIN_BLOCK_MS)
            var elapsed = 0L
            while (elapsed < blockMs) {
                delay(SILENT_TICK_MS)
                elapsed += SILENT_TICK_MS
                _captionProgress.value = (elapsed.toFloat() / blockMs).coerceAtMost(1f)
            }
        }
    }

    private fun finishTurn() {
        _state.update {
            it.copy(
                phase = LivePhase.Idle,
                caption = it.caption?.copy(allDone = true, current = -1),
                hint = if (it.inputBlocked) LiveHint.MILESTONE else LiveHint.HOLD_TO_TALK,
            )
        }
        setTurnInFlight(false)
    }

    /** Barge-in: stops playback only. The reply keeps its full audio, playable from the chat. */
    private fun interruptSpeaking() {
        turnJob?.cancel()
        turnJob = null
        player.stop()
        _level.value = 0f
        setTurnInFlight(false)
    }

    // endregion

    // region Shared observers

    private fun observeVoiceQuota() {
        viewModelScope.launch {
            audioGenClient.quotaStatus().collect { status ->
                val until = (status as? QuotaStatus.DailyExhausted)?.until
                _state.update {
                    it.copy(voicesRestingUntil = until ?: it.voicesRestingUntil.takeIf { sessionVoicesSpent })
                }
            }
        }
    }

    /**
     * Lowers the saga's music while the player records (less of it leaks into the mic) and while a
     * voice plays, so neither competes with it — same duck the audiobook uses.
     */
    private fun observeMusicDucking() {
        viewModelScope.launch {
            state
                .map { it.phase == LivePhase.Listening || it.phase == LivePhase.Speaking(silent = false) }
                .distinctUntilChanged()
                .collect { ducked -> setMusicDucked(ducked) }
        }
    }

    private fun setMusicDucked(ducked: Boolean) {
        val action = if (ducked) SagaPlaybackService.ACTION_DUCK else SagaPlaybackService.ACTION_UNDUCK
        SagaPlaybackService.startSafely(context, SagaPlaybackService.playbackIntent(context, action))
    }

    // endregion

    /**
     * The app went to the background: the session ends. Nothing keeps talking or listening, and a
     * turn still in flight is let go — its reply lands silently (voicing that already started
     * finishes in [LiveBackgroundWork]; voicing that hadn't is never started).
     */
    fun pause() {
        when (_state.value.phase) {
            LivePhase.Listening -> {
                listenJob?.cancel()
                recorder.cancel()
                toIdle(LiveHint.CANCELLED)
            }

            LivePhase.Thinking -> {
                abandonPendingTurn()
                setTurnInFlight(false)
                toIdle(LiveHint.HOLD_TO_TALK)
            }

            LivePhase.Voicing, is LivePhase.Speaking -> {
                interruptSpeaking()
                finishTurn()
            }

            else -> Unit
        }
    }

    protected fun toIdle(hint: LiveHint) {
        _level.value = 0f
        _state.update {
            it.copy(
                phase = LivePhase.Idle,
                focus = null,
                nextFocus = null,
                playerLinePending = false,
                reasoning = null,
                hint = hint,
            )
        }
    }

    protected fun setHint(hint: LiveHint) = _state.update { it.copy(hint = hint) }

    protected fun setTurnInFlight(inFlight: Boolean) = onTurnInFlight(inFlight)

    override fun onCleared() {
        setMusicDucked(false)
        recorder.cancel()
        player.stop()
        setTurnInFlight(false)
        super.onCleared()
    }

    private companion object {
        const val SILENT_VOICING_BEAT_MS = 900L
        const val SILENT_MS_PER_CHAR = 55L
        const val SILENT_MIN_BLOCK_MS = 900L
        const val SILENT_TICK_MS = 80L
    }
}
