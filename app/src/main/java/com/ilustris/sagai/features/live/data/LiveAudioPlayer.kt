package com.ilustris.sagai.features.live.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Plays one reply clip for live mode and reports its position so the captions and the radial
 * wave can follow it. Holds transient audio focus while playing and stops if it's lost (a call,
 * another app). Owned by the live screen only: nothing else in the app plays these clips.
 */
class LiveAudioPlayer
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        private var player: MediaPlayer? = null
        private var finished: CompletableDeferred<Boolean>? = null
        private var focusRequest: AudioFocusRequest? = null

        private val attributes =
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

        /**
         * Plays [path] to the end. Returns true when it finished on its own, false when it was
         * stopped (barge-in, leaving the screen, focus loss) or failed.
         */
        suspend fun play(
            path: String,
            onPosition: (Long) -> Unit,
        ): Boolean =
            withContext(Dispatchers.Main) {
                stop()
                val done = CompletableDeferred<Boolean>()
                finished = done
                val mediaPlayer =
                    runCatching {
                        MediaPlayer().apply {
                            setAudioAttributes(attributes)
                            setDataSource(path)
                            prepare()
                        }
                    }.onFailure { Timber.w(it, "LiveAudioPlayer: couldn't prepare $path") }
                        .getOrNull() ?: return@withContext false
                player = mediaPlayer
                mediaPlayer.setOnCompletionListener { done.complete(true) }
                mediaPlayer.setOnErrorListener { _, what, extra ->
                    Timber.w("LiveAudioPlayer error $what/$extra")
                    done.complete(false)
                    true
                }
                requestFocus()
                mediaPlayer.start()
                coroutineScope {
                    val ticker =
                        launch {
                            while (isActive) {
                                runCatching { onPosition(mediaPlayer.currentPosition.toLong()) }
                                delay(POSITION_TICK_MS)
                            }
                        }
                    val completed = done.await()
                    ticker.cancel()
                    release(mediaPlayer)
                    completed
                }
            }

        fun stop() {
            finished?.complete(false)
            finished = null
            player?.let(::release)
        }

        private fun release(mediaPlayer: MediaPlayer) {
            runCatching { if (mediaPlayer.isPlaying) mediaPlayer.stop() }
            mediaPlayer.release()
            if (player === mediaPlayer) player = null
            abandonFocus()
        }

        private fun requestFocus() {
            val request =
                AudioFocusRequest
                    .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(attributes)
                    .setOnAudioFocusChangeListener { change ->
                        if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) stop()
                    }.build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        }

        private fun abandonFocus() {
            focusRequest?.let(audioManager::abandonAudioFocusRequest)
            focusRequest = null
        }

        companion object {
            private const val POSITION_TICK_MS = 40L
        }
    }
