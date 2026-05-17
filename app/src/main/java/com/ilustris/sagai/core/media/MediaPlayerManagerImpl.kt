package com.ilustris.sagai.core.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.HapticGenerator
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MediaPlayerManagerImpl
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
    ) : MediaPlayerManager {
        override var mediaPlayer: MediaPlayer? = null
            private set

        private var hapticGenerator: HapticGenerator? = null

        private val _isPlaying = MutableStateFlow(false)
        override val isPlaying: StateFlow<Boolean> = _isPlaying

        override fun prepareDataSource(
            path: String,
            looping: Boolean,
            onPrepared: (() -> Unit)?,
            onError: ((Exception) -> Unit)?,
            onCompletion: (() -> Unit)?,
        ) {
            val file = File(path)
            Timber.i("prepareDataSource: Playing file ${file.absolutePath}")
            if (mediaPlayer != null) {
                Timber.i("Releasing existing MediaPlayer before preparing new data source.")
                release()
            }
            if (file.exists().not()) {
                Timber.e("Audio file does not exist at path: $path")
                onError?.invoke(Exception("Audio file does not exist at path: $path"))
                return
            }

            try {
                if (mediaPlayer == null) {
                    Timber.i("Creating new MediaPlayer instancefor: ${file.name}")
                    mediaPlayer =
                        MediaPlayer().apply {
                            val audioAttributes =
                                AudioAttributes
                                    .Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .apply {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            setHapticChannelsMuted(false)
                                        }
                                    }.build()
                            setAudioAttributes(audioAttributes)
                        }
                }

                mediaPlayer?.apply {
                    Timber.d("Resetting MediaPlayer.")
                    reset()

                    // Initialize HapticGenerator if supported
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && HapticGenerator.isAvailable()) {
                        try {
                            hapticGenerator?.release()
                            hapticGenerator =
                                HapticGenerator.create(audioSessionId).apply {
                                    enabled = true
                                }
                            Timber.i("HapticGenerator initialized for session: $audioSessionId")
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to initialize HapticGenerator")
                        }
                    }

                    Timber.d("Setting data source: ${file.absolutePath}")
                    setDataSource(file.absolutePath)
                    isLooping = looping
                    setOnPreparedListener {
                        Timber.i("MediaPlayer prepared for: ${file.name}")
                        onPrepared?.invoke()
                    }
                    setOnErrorListener { _, what, extra ->
                        Timber.e(
                            "MediaPlayer Error: what: $what, extra: $extra for file: ${file.name}",
                        )
                        _isPlaying.value = false
                        onError?.invoke(Exception("MediaPlayer Error: what: $what, extra: $extra"))
                        release()
                        true
                    }

                    setOnCompletionListener {
                        Timber.i("MediaPlayer playback completed for: ${file.name}")
                        if (!isLooping) {
                            _isPlaying.value = false
                        }
                        onCompletion?.invoke()
                        release()
                        mediaPlayer = null
                    }
                    Timber.d("Calling prepareAsync.")
                    prepareAsync()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error preparing MediaPlayer for file: ${file.name}")
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
                            Timber.i("MediaPlayer playback started.")
                        } catch (e: IllegalStateException) {
                            Timber.e("Error starting MediaPlayer: ${e.message}")
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
                        Timber.i("MediaPlayer playback paused.")
                    } catch (e: IllegalStateException) {
                        Timber.e("Error pausing MediaPlayer: ${e.message}")
                        release()
                    }
                }
            }
        }

        override fun stop() {
            release()
        }

        override fun release() {
            hapticGenerator?.release()
            hapticGenerator = null
            mediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                } catch (e: Exception) {
                    Timber.e("Error releasing MediaPlayer: ${e.message}")
                } finally {
                    mediaPlayer = null
                    if (_isPlaying.value) {
                        _isPlaying.value = false
                    }
                    Timber.i("MediaPlayer released.")
                }
            }
        }

        override fun resume() {
            play()
        }
    }
