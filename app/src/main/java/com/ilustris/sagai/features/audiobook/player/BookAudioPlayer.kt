package com.ilustris.sagai.features.audiobook.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackTrack(
    val segmentId: Long,
    val bookId: Long,
    val sectionKey: String,
    val audioPath: String,
    val durationMs: Long,
)

data class PlaybackState(
    val tracks: List<PlaybackTrack>,
    val index: Int,
    /** Position inside the current track. */
    val positionMs: Long,
    val isPlaying: Boolean,
) {
    val current get() = tracks.getOrNull(index)
}

/**
 * Plays narrated segments as one gapless queue. The player lives in the app process so the reader
 * can sample its position for the synced highlight; [BookAudioPlaybackService] only wraps it in a
 * media session for the lock screen and background playback.
 */
@Singleton
class BookAudioPlayer
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private var ticker: Job? = null
        private var controller: ListenableFuture<MediaController>? = null

        private val _state = MutableStateFlow<PlaybackState?>(null)
        val state: StateFlow<PlaybackState?> = _state.asStateFlow()

        private var tracks: List<PlaybackTrack> = emptyList()

        val player: ExoPlayer by lazy {
            ExoPlayer
                .Builder(context)
                .setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                        .build(),
                    true,
                ).setHandleAudioBecomingNoisy(true)
                .build()
                .apply {
                    addListener(
                        object : Player.Listener {
                            override fun onEvents(
                                player: Player,
                                events: Player.Events,
                            ) {
                                publish()
                                if (player.isPlaying) startTicker() else ticker?.cancel()
                            }
                        },
                    )
                }
        }

        /** Must be called on the main thread. */
        fun play(
            queue: List<PlaybackTrack>,
            startIndex: Int,
            startPositionMs: Long,
            title: String,
            subtitle: String,
        ) {
            val playable = queue.filter { File(it.audioPath).exists() }
            if (playable.isEmpty()) return
            val sameQueue = playable.map { it.segmentId } == tracks.map { it.segmentId }
            tracks = playable
            val index = playable.indexOfFirst { it.segmentId == queue.getOrNull(startIndex)?.segmentId }.coerceAtLeast(0)

            if (!sameQueue) {
                val metadata =
                    MediaMetadata
                        .Builder()
                        .setTitle(title)
                        .setArtist(subtitle)
                        .build()
                player.setMediaItems(
                    playable.map { track ->
                        MediaItem
                            .Builder()
                            .setMediaId(track.segmentId.toString())
                            .setUri(Uri.fromFile(File(track.audioPath)))
                            .setMediaMetadata(metadata)
                            .build()
                    },
                    index,
                    startPositionMs,
                )
                player.prepare()
            } else {
                player.seekTo(index, startPositionMs)
            }
            player.play()
            connectSession()
        }

        fun toggle() {
            if (player.isPlaying) player.pause() else player.play()
        }

        fun seekTo(
            segmentId: Long,
            positionMs: Long,
        ) {
            val index = tracks.indexOfFirst { it.segmentId == segmentId }
            if (index < 0) return
            player.seekTo(index, positionMs)
            publish()
        }

        fun stop() {
            ticker?.cancel()
            player.stop()
            player.clearMediaItems()
            tracks = emptyList()
            _state.value = null
            controller?.let(MediaController::releaseFuture)
            controller = null
        }

        /** Binding a controller starts the session service, which owns the media notification. */
        private fun connectSession() {
            if (controller != null) return
            val token = SessionToken(context, ComponentName(context, BookAudioPlaybackService::class.java))
            controller = MediaController.Builder(context, token).buildAsync()
        }

        private fun startTicker() {
            if (ticker?.isActive == true) return
            ticker =
                scope.launch {
                    while (isActive) {
                        publish()
                        delay(TICK_MS)
                    }
                }
        }

        private fun publish() {
            if (tracks.isEmpty()) return
            _state.value =
                PlaybackState(
                    tracks = tracks,
                    index = player.currentMediaItemIndex,
                    positionMs = player.currentPosition,
                    isPlaying = player.isPlaying,
                )
        }

        companion object {
            private const val TICK_MS = 50L
        }
    }
