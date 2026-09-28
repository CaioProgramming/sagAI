package com.ilustris.sagai.features.saga.chat.ui.components.audio

import android.content.Context
import com.ilustris.sagai.core.media.MediaPlayerManager
import com.ilustris.sagai.core.media.MediaPlayerManagerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Plays one chat bubble's audio at a time and reports it as [AudioPlaybackState], which is what
 * [AudioMessagePlayer] draws: tapping the playing bubble pauses/resumes it, tapping another one
 * switches. Owned by a screen's ViewModel ([scope]); call [release] when it's cleared.
 *
 * Same behavior as the saga chat's playback in ChatViewModel, pulled out so other conversations
 * (the epilogue) don't copy it.
 */
class BubbleAudioPlayer(
    context: Context,
    private val scope: CoroutineScope,
) {
    private val manager: MediaPlayerManager = MediaPlayerManagerImpl(context)
    private val _state = MutableStateFlow<AudioPlaybackState?>(null)
    val state: StateFlow<AudioPlaybackState?> = _state.asStateFlow()
    private var progressJob: Job? = null

    fun toggle(
        messageId: Int,
        audioPath: String,
    ) {
        val current = _state.value
        if (current?.messageId == messageId) {
            if (current.isPlaying) pause() else resume()
            return
        }
        stop()
        start(messageId, audioPath)
    }

    fun stop() {
        manager.stop()
        stopProgressUpdates()
        _state.value = null
    }

    fun release() {
        stopProgressUpdates()
        manager.release()
        _state.value = null
    }

    private fun start(
        messageId: Int,
        audioPath: String,
    ) {
        manager.prepareDataSource(
            path = audioPath,
            looping = false,
            onPrepared = {
                val duration = runCatching { manager.mediaPlayer?.duration?.toLong() }.getOrNull() ?: 0L
                _state.value =
                    AudioPlaybackState(
                        messageId = messageId,
                        isPlaying = true,
                        duration = duration,
                        audioPath = audioPath,
                    )
                manager.play()
                startProgressUpdates(messageId)
            },
            onError = { exception ->
                Timber.w(exception, "Bubble audio playback error")
                _state.value = null
            },
            onCompletion = {
                _state.update { it?.copy(isPlaying = false, currentPosition = it.duration) }
                stopProgressUpdates()
            },
        )
    }

    private fun pause() {
        manager.pause()
        _state.update { it?.copy(isPlaying = false) }
        stopProgressUpdates()
    }

    private fun resume() {
        manager.play()
        _state.update { it?.copy(isPlaying = true) }
        _state.value?.messageId?.let(::startProgressUpdates)
    }

    private fun startProgressUpdates(messageId: Int) {
        stopProgressUpdates()
        progressJob =
            scope.launch(Dispatchers.Main) {
                while (_state.value?.isPlaying == true && _state.value?.messageId == messageId) {
                    val position = runCatching { manager.mediaPlayer?.currentPosition?.toLong() }.getOrNull() ?: 0L
                    _state.update { it?.copy(currentPosition = position) }
                    delay(PROGRESS_TICK_MS)
                }
            }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private companion object {
        const val PROGRESS_TICK_MS = 100L
    }
}
