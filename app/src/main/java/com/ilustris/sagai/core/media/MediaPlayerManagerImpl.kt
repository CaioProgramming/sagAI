package com.ilustris.sagai.core.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.net.URI
import javax.inject.Inject

class MediaPlayerManagerImpl
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
    ) : MediaPlayerManager {
        override var mediaPlayer: MediaPlayer? = null
            private set
        private val _isPlaying = MutableStateFlow(false)
        override val isPlaying: StateFlow<Boolean> = _isPlaying

        companion object {
            private const val TAG = "MediaPlayerManager"
        }

        override fun prepareDataSource(
            path: String,
            looping: Boolean,
            onPrepared: (() -> Unit)?,
            onError: ((Exception) -> Unit)?,
            onCompletion: (() -> Unit)?,
        ) {
            val file = File(path)
            Log.i(TAG, "prepareDataSource: Playing file ${file.absolutePath}")
            mediaPlayer?.let {
                Log.i(TAG, "Releasing existing MediaPlayer before preparing new data source.")
                it.stop()
                it.reset()
                it.release()
            }
            if (file.exists().not()) {
                Log.e(TAG, "Audio file does not exist at path: $path")
                onError?.invoke(Exception("Audio file does not exist at path: $path"))
                return
            }

            try {
                if (mediaPlayer == null) {
                    Log.i(TAG, "Creating new MediaPlayer instance.")
                    mediaPlayer = MediaPlayer()
                }

                mediaPlayer =
                    mediaPlayer?.apply {
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
                            // Release the player on error
                            release()
                            true
                        }

                        setOnCompletionListener {
                            Log.i(TAG, "MediaPlayer playback completed for: ${file.name}")
                            if (!isLooping) {
                                _isPlaying.value = false
                            }
                            onCompletion?.invoke()
                            // Release the player on completion
                            release()
                            mediaPlayer = null
                        }
                        prepareAsync()
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error preparing MediaPlayer for file: ${file.name}", e)
                _isPlaying.value = false
                onError?.invoke(e)
                release()
                mediaPlayer = null
            }
        }

        override fun play() {
            try {
                mediaPlayer?.let {
                    if (!it.isPlaying) {
                        try {
                            it.start()
                            _isPlaying.value = true
                            Log.i(TAG, "MediaPlayer playback started.")
                        } catch (e: IllegalStateException) {
                            Log.e(TAG, "Error starting MediaPlayer: ${e.message}")
                            _isPlaying.value = false
                            release()
                        }
                    } else {
                        it.pause()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mediaPlayer?.reset()
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
                        release()
                    }
                }
            }
        }

        override fun stop() {
            release()
        }

        override fun release() {
            mediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error releasing MediaPlayer: ${e.message}")
                } finally {
                    mediaPlayer = null
                    if (_isPlaying.value) {
                        _isPlaying.value = false
                    }
                    Log.i(TAG, "MediaPlayer released.")
                }
            }
        }

        override fun resume() {
            play()
        }
    }
