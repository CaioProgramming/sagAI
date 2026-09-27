package com.ilustris.sagai.features.live.presentation

import MessageStatus
import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.AudioGenClient
import com.ilustris.sagai.core.ai.QuotaExhaustedException
import com.ilustris.sagai.core.ai.key.QuotaStatus
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.permissions.PermissionStatus
import com.ilustris.sagai.core.utils.AudioUtils
import com.ilustris.sagai.features.audiobook.data.usecase.WaveformExtractor
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.live.data.LiveAudioPlayer
import com.ilustris.sagai.features.live.data.LiveBackgroundWork
import com.ilustris.sagai.features.live.data.LiveSessionTracker
import com.ilustris.sagai.features.live.data.VoiceRecorder
import com.ilustris.sagai.features.live.data.VoiceRecording
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.data.model.ChatGenerationOutcome
import com.ilustris.sagai.features.saga.chat.data.model.InputMode
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.usecase.ChatGenerationService
import com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.data.voicing.MessageBlocks
import com.ilustris.sagai.features.saga.chat.data.voicing.MessageVoicingUseCase
import com.ilustris.sagai.features.saga.chat.data.voicing.VoiceCastingUseCase
import com.ilustris.sagai.features.saga.chat.data.voicing.VoicedMessage
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.repository.ReactionRepository
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Runs a live conversation: hold → record → send the audio as a voice turn → the reply (text
 * saved in the background by [ChatGenerationService], like the chat) → voice it → play it.
 *
 * Everything the story produces goes through the same pipeline and the same messages table as the
 * chat. What this owns is the turn's choreography and playback — which is why leaving the screen
 * ([onCleared], [pause]) stops recording and playback for good, while a reply or voicing already in
 * flight finishes silently in the background.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LiveConversationViewModel
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val messageUseCase: MessageUseCase,
        private val chatGenerationService: ChatGenerationService,
        private val messageVoicingUseCase: MessageVoicingUseCase,
        private val voiceCastingUseCase: VoiceCastingUseCase,
        private val reactionRepository: ReactionRepository,
        private val sagaContentManager: SagaContentManager,
        private val audioGenClient: AudioGenClient,
        private val permissionService: PermissionService,
        private val fileHelper: FileHelper,
        private val recorder: VoiceRecorder,
        private val player: LiveAudioPlayer,
        private val sessionTracker: LiveSessionTracker,
        private val backgroundWork: LiveBackgroundWork,
    ) : ViewModel() {
        private val _state = MutableStateFlow(LiveUiState())
        val state: StateFlow<LiveUiState> = _state.asStateFlow()

        /** Mic level while listening, playback loudness while speaking. Read it in the draw phase. */
        private val _level = MutableStateFlow(0f)
        val level: StateFlow<Float> = _level.asStateFlow()

        private val _reactions = MutableSharedFlow<LiveReaction>(extraBufferCapacity = 16)
        val reactions: SharedFlow<LiveReaction> = _reactions.asSharedFlow()

        private var sagaId: Int? = null
        private val sessionStart = System.currentTimeMillis()
        private val sessionMessageIds = MutableStateFlow<Set<Int>>(emptySet())
        private val seenReactionIds = mutableSetOf<Int>()
        private var pendingUserMessage: Message? = null
        private var failedUserMessage: Message? = null
        private var sessionVoicesSpent = false

        private var listenJob: Job? = null
        private var turnJob: Job? = null

        fun start(sagaId: Int) {
            if (this.sagaId == sagaId) return
            this.sagaId = sagaId
            observeSaga(sagaId)
            observeOutcomes(sagaId)
            observeReasoning(sagaId)
            observeVoiceQuota()
            observeMilestones()
            observeReactions()
        }

        fun hasMicPermission() = permissionService.getPermissionStatus(Manifest.permission.RECORD_AUDIO) == PermissionStatus.GRANTED

        fun selectSpeaker(characterId: Int) {
            if (_state.value.phase != LivePhase.Idle && _state.value.phase !is LivePhase.Speaking) return
            _state.update { it.copy(selectedSpeakerId = characterId) }
        }

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

        private suspend fun send(recording: VoiceRecording) {
            val saga = _state.value.saga ?: return toIdle(LiveHint.REPLY_FAILED)
            val speaker = _state.value.selectedSpeaker ?: return toIdle(LiveHint.REPLY_FAILED)
            val timeline = saga.getCurrentTimeLine()
            if (timeline == null) {
                recording.file.delete()
                sagaContentManager.checkNarrativeProgression(saga)
                return toIdle(LiveHint.MILESTONE)
            }

            setTurnInFlight(true)
            _state.update {
                it.copy(
                    phase = LivePhase.Thinking,
                    focus = LiveFocus(speaker),
                    caption = null,
                    playerLinePending = true,
                    hint = LiveHint.WAITING_REPLY,
                )
            }

            val message =
                withContext(Dispatchers.IO) {
                    val saved =
                        messageUseCase
                            .saveMessage(
                                saga,
                                Message(
                                    text = "",
                                    senderType = SenderType.USER,
                                    speakerName = speaker.name,
                                    characterId = speaker.id,
                                    sagaId = saga.data.id,
                                    timelineId = timeline.data.id,
                                    status = MessageStatus.LOADING,
                                    inputMode = InputMode.VOICE,
                                    audible = true,
                                ),
                                isFromUser = true,
                            ).getSuccess() ?: return@withContext null
                    val audio =
                        fileHelper.saveBinaryFile(
                            recording.file.readBytes(),
                            path = "sagas/${saga.data.id}/audios",
                            fileName = "message_${saved.id}_audio",
                            extension = "wav",
                        )
                    recording.file.delete()
                    val withAudio = saved.copy(audioPath = audio?.absolutePath)
                    messageUseCase.updateMessage(withAudio).getSuccess() ?: withAudio
                }
            if (message == null) {
                setTurnInFlight(false)
                return toIdle(LiveHint.REPLY_FAILED)
            }

            generate(saga, message, speaker)
        }

        private fun generate(
            saga: SagaMetadata,
            message: Message,
            speaker: Character?,
        ) {
            pendingUserMessage = message
            sessionMessageIds.update { it + message.id }
            chatGenerationService.generate(
                saga,
                MessageContent(message = message, character = speaker, reactions = emptyList()),
                saga.getCurrentTimeLine()?.data?.sceneSummary,
            )
        }

        fun retry() {
            val saga = _state.value.saga ?: return
            val failed = failedUserMessage ?: return
            failedUserMessage = null
            setTurnInFlight(true)
            _state.update {
                it.copy(
                    phase = LivePhase.Thinking,
                    focus = LiveFocus(saga.findCharacter(failed.characterId)),
                    canRetry = false,
                    playerLinePending = failed.text.isBlank(),
                    hint = LiveHint.WAITING_REPLY,
                )
            }
            viewModelScope.launch(Dispatchers.IO) {
                val retrying = failed.copy(status = MessageStatus.LOADING)
                messageUseCase.updateMessage(retrying)
                generate(saga, retrying, saga.findCharacter(failed.characterId))
            }
        }

        private fun onReply(outcome: ChatGenerationOutcome.Success) {
            val pending = pendingUserMessage ?: return
            if (outcome.userMessage.id != pending.id) return
            pendingUserMessage = null
            val saga = _state.value.saga ?: return
            val reply = outcome.reply.message
            sessionMessageIds.update { it + reply.id }

            val replyFocus =
                LiveFocus(
                    reply.characterId
                        ?.takeIf { reply.senderType != SenderType.NARRATOR }
                        ?.let { id -> saga.findCharacter(id) },
                )
            // The reply text is here; its audio isn't yet. The player's corrected line takes the
            // stage meanwhile — the reply's own text stays hidden until it's heard.
            _state.update {
                it.copy(
                    phase = LivePhase.Voicing,
                    nextFocus = replyFocus,
                    playerLinePending = false,
                    reasoning = null,
                    caption = LiveCaption(MessageBlocks.split(outcome.userMessage.text), isPlayerLine = true),
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
                            voice(saga, reply)
                        }
                    if (voiced != null) {
                        speakAudio(voiced, replyFocus)
                    } else {
                        speakSilently(reply, replyFocus)
                    }
                    // A barge-in has already moved on to Listening; anything else (the clip ended,
                    // audio focus was lost) closes the turn here.
                    if (_state.value.phase is LivePhase.Speaking) finishTurn()
                }
        }

        /**
         * Voicing runs in [LiveBackgroundWork] so that once started it finishes even if the player
         * leaves; this only awaits it. A spent voice quota switches the rest of the session to
         * captions.
         */
        private suspend fun voice(
            saga: SagaMetadata,
            reply: Message,
        ): VoicedMessage? {
            val work = backgroundWork.scope.async { messageVoicingUseCase.voice(saga, reply) }
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
            voiced: VoicedMessage,
            focus: LiveFocus,
        ) {
            val (envelope, durationMs) =
                withContext(Dispatchers.IO) {
                    val file = File(voiced.audioPath)
                    runCatching { WaveformExtractor.envelope(file.readBytes()) }.getOrDefault(FloatArray(0)) to
                        (AudioUtils.wavDurationMs(file) ?: 0L)
                }
            val timeline = CaptionTimeline.from(voiced.script, durationMs)
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
                    _state.update { state ->
                        state.copy(
                            caption = state.caption?.let { caption -> caption.copy(current = if (line.block >= 0) line.block else caption.current) },
                            focus = if (line.isNarrator) LiveFocus(null) else focus,
                        )
                    }
                }
            _level.value = 0f
            if (!completed) Timber.d("Live playback stopped before the end")
        }

        private suspend fun speakSilently(
            reply: Message,
            focus: LiveFocus,
        ) {
            val blocks = MessageBlocks.split(reply.text)
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
                _state.update { it.copy(caption = it.caption?.copy(current = block.index)) }
                delay((block.text.length * SILENT_MS_PER_CHAR).coerceAtLeast(SILENT_MIN_BLOCK_MS))
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

        // region Observers

        private fun observeSaga(sagaId: Int) {
            viewModelScope.launch {
                var castDone = false
                sagaRepository.getSagaMetadata(sagaId).filterNotNull().collect { saga ->
                    val speakers =
                        listOfNotNull(saga.mainCharacter) +
                            saga.characters.filter { it.id != saga.mainCharacter?.id }.sortedBy { it.joinedAt }
                    _state.update {
                        it.copy(
                            saga = saga,
                            speakers = speakers,
                            selectedSpeakerId = it.selectedSpeakerId ?: saga.mainCharacter?.id,
                        )
                    }
                    if (!castDone) {
                        castDone = true
                        // Cast the scene before the first turn, so voicing rarely waits on it.
                        backgroundWork.scope.launch {
                            val present =
                                saga
                                    .getCurrentTimeLine()
                                    ?.data
                                    ?.sceneSummary
                                    ?.charactersPresent
                                    ?.mapNotNull { saga.findCharacter(it.name) }
                                    .orEmpty()
                            runCatching { voiceCastingUseCase.narratorVoice(saga) }
                            voiceCastingUseCase.castAll(saga, present.filter { it.id != saga.mainCharacter?.id })
                        }
                    }
                }
            }
        }

        private fun observeOutcomes(sagaId: Int) {
            viewModelScope.launch {
                chatGenerationService.outcomes.collect { outcome ->
                    when (outcome) {
                        is ChatGenerationOutcome.Success -> {
                            if (outcome.sagaId == sagaId) onReply(outcome)
                        }

                        is ChatGenerationOutcome.Error -> {
                            if (outcome.sagaId != sagaId || pendingUserMessage == null) return@collect
                            failedUserMessage = pendingUserMessage
                            pendingUserMessage = null
                            setTurnInFlight(false)
                            _state.update {
                                it.copy(
                                    phase = LivePhase.Recovering,
                                    reasoning = null,
                                    playerLinePending = false,
                                    canRetry = true,
                                    hint = LiveHint.REPLY_FAILED,
                                )
                            }
                        }

                        is ChatGenerationOutcome.GuardrailBlocked -> {
                            if (outcome.sagaId != sagaId || pendingUserMessage == null) return@collect
                            pendingUserMessage = null
                            setTurnInFlight(false)
                            toIdle(LiveHint.GUARDRAIL)
                        }
                    }
                }
            }
        }

        private fun observeReasoning(sagaId: Int) {
            viewModelScope.launch {
                chatGenerationService.activeGenerations.collect { active ->
                    val reasoning = active[sagaId]?.reasoning
                    if (_state.value.phase == LivePhase.Thinking) _state.update { it.copy(reasoning = reasoning) }
                }
            }
        }

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

        private fun observeMilestones() {
            viewModelScope.launch {
                sagaContentManager.milestoneUpdate.collect { milestone ->
                    val blocking =
                        milestone is SagaMilestone.Loading ||
                            milestone is SagaMilestone.NewEvent ||
                            milestone is SagaMilestone.ChapterFinished ||
                            milestone is SagaMilestone.ActFinished
                    _state.update {
                        it.copy(
                            inputBlocked = blocking,
                            hint = if (blocking && it.phase == LivePhase.Idle) LiveHint.MILESTONE else it.hint,
                        )
                    }
                }
            }
        }

        private fun observeReactions() {
            viewModelScope.launch {
                sessionMessageIds
                    .flatMapLatest { ids ->
                        if (ids.isEmpty()) emptyFlow() else reactionRepository.observeReactions(ids.toList(), sessionStart)
                    }.collectLatest { reactions ->
                        reactions
                            .filter { seenReactionIds.add(it.data.id) }
                            .forEach { reaction ->
                                _reactions.tryEmit(
                                    LiveReaction(
                                        id = reaction.data.id,
                                        emoji = reaction.data.emoji,
                                        thought = reaction.data.thought,
                                        character = reaction.character,
                                    ),
                                )
                            }
                    }
            }
        }

        // endregion

        /** The app went to the background: nothing keeps talking or listening. */
        fun pause() {
            if (_state.value.phase == LivePhase.Listening) {
                listenJob?.cancel()
                recorder.cancel()
                toIdle(LiveHint.CANCELLED)
            }
            if (_state.value.phase is LivePhase.Speaking) {
                interruptSpeaking()
                finishTurn()
            }
        }

        private fun toIdle(hint: LiveHint) {
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

        private fun setHint(hint: LiveHint) = _state.update { it.copy(hint = hint) }

        private fun setTurnInFlight(inFlight: Boolean) {
            sagaId?.let { sessionTracker.setTurnInFlight(it, inFlight) }
        }

        override fun onCleared() {
            recorder.cancel()
            player.stop()
            setTurnInFlight(false)
            super.onCleared()
        }

        companion object {
            private const val SILENT_VOICING_BEAT_MS = 900L
            private const val SILENT_MS_PER_CHAR = 55L
            private const val SILENT_MIN_BLOCK_MS = 900L
        }
    }
