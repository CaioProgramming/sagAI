package com.ilustris.sagai.core.media

import com.ilustris.sagai.core.theme.SagaThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * How loud the saga's ambient music is right now, 0..1, for visuals that breathe with it. The
 * track in [SagaThemeManager.ambientMusicFile] is decoded once into an [AudioEnvelope] (kept per
 * file); [level] then samples that envelope at the player's position, scaled by its volume so a
 * ducked track (live mode, the audiobook) calms whatever is following it.
 *
 * [level] is meant to be read in a draw phase every frame: it allocates nothing and does no I/O.
 */
@Singleton
class AmbientMusicPulse
    @Inject
    constructor(
        themeManager: SagaThemeManager,
        private val player: MediaPlayerManager,
    ) {
        private class Decoded(
            val path: String,
            val modifiedAt: Long,
            val envelope: FloatArray,
        )

        @Volatile
        private var decoded: Decoded? = null

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        /** Whether the music is playing: the visual only needs to run (and redraw) while it is. */
        val isPlaying: StateFlow<Boolean> get() = player.isPlaying

        init {
            scope.launch {
                themeManager.ambientMusicFile.collectLatest { file -> decodeIfNeeded(file) }
            }
        }

        private fun decodeIfNeeded(file: File?) {
            if (file == null || !file.exists()) {
                decoded = null
                return
            }
            val current = decoded
            if (current?.path == file.absolutePath && current.modifiedAt == file.lastModified()) return
            // Dropped first: a new track must not borrow the old one's envelope while it decodes.
            decoded = null
            AudioEnvelope.decode(file)?.let { decoded = Decoded(file.absolutePath, file.lastModified(), it) }
        }

        /** 0 when nothing is playing or decoded. */
        fun level(): Float {
            val envelope = decoded?.envelope ?: return 0f
            if (!player.isPlaying.value) return 0f
            val position = player.positionMs() ?: return 0f
            val loudness = envelope.getOrElse((position / AudioEnvelope.STEP_MS).toInt()) { 0f }
            return loudness * player.volume
        }
    }
