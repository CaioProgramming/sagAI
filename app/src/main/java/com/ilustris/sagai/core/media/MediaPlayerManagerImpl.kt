package com.ilustris.sagai.core.media

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

class MediaPlayerManagerImpl
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
    ) : MediaPlayerManager {
        var mediaPlayer: MediaPlayer? = null
        private val _isPlaying = MutableStateFlow(false)
        override val isPlaying: StateFlow<Boolean> = _isPlaying

        companion object {
            private const val TAG = "MediaPlayerManager"
        }

        override fun prepareDataSource(
            file: File,
            looping: Boolean,
            onPrepared: (() -> Unit)?,
            onError: ((Exception) -> Unit)?,
            onCompletion: (() -> Unit)?,
        ) {
            try {
                stop() // Stop any previous playback and reset
                mediaPlayer =
                    MediaPlayer().apply {
                        setDataSource(context, file.toUri())
                        isLooping = looping
                        setOnPreparedListener {
                            Log.i(TAG, "MediaPlayer prepared for: ${file.name}")
                            onPrepared?.invoke()
                        }
                        setOnErrorListener { _, what, extra ->
                            Log.e(TAG, "MediaPlayer Error: what: $what, extra: $extra for file: ${file.name}")
                            _isPlaying.value = false
                            onError?.invoke(Exception("MediaPlayer Error: what: $what, extra: $extra"))
                            true
                        }
                        setOnCompletionListener {
                            Log.i(TAG, "MediaPlayer playback completed for: ${file.name}")
                            if (!isLooping) {
                                _isPlaying.value = false
                            }
                            onCompletion?.invoke()
                        }
                        prepareAsync()
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error preparing MediaPlayer for file: ${file.name}", e)
                _isPlaying.value = false
                onError?.invoke(e)
            }
        }

        override fun play() {
            mediaPlayer?.let {
                if (it.isPlaying.not()) {
                    try {
                        it.start()
                        _isPlaying.value = true
                        Log.i(TAG, "MediaPlayer playback started.")
                    } catch (e: IllegalStateException) {
                        Log.e(TAG, "Error starting MediaPlayer: ${e.message}")
                        _isPlaying.value = false
                    }
                }
            }
        }

        override fun pause() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    try {
                        it.pause()
                        _isPlaying.value = false
                        Log.i(TAG, "MediaPlayer playback paused.")
                    } catch (e: IllegalStateException) {
                        Log.e(TAG, "Error pausing MediaPlayer: ${e.message}")
                    }
                }
            }
        }

        override fun stop() {
            mediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.reset()
                    _isPlaying.value = false
                    Log.i(TAG, "MediaPlayer stopped and reset.")
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "Error stopping/resetting MediaPlayer: ${e.message}")
                }
            }
        }

        override fun release() {
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
            Log.i(TAG, "MediaPlayer released.")
        }

        override fun resume() {
            TODO("Not yet implemented")
        }
    }
