package com.ilustris.sagai.core.media

import android.content.Context
import com.ilustris.sagai.core.theme.SagaThemeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * How loud the saga's ambient music is right now, 0..1, for visuals that breathe with it. The
 * track in [SagaThemeManager.ambientMusicFile] is decoded into an [AudioEnvelope] (seconds of work,
 * so it is done once per file ever: kept in memory for the process and on disk across launches);
 * [level] then samples that envelope at the player's position, scaled by its volume so a ducked
 * track (live mode, the audiobook) calms whatever is following it.
 *
 * [level] is meant to be read in a draw phase every frame: it allocates nothing and does no I/O.
 */
@Singleton
class AmbientMusicPulse
    @Inject
    constructor(
        themeManager: SagaThemeManager,
        private val player: MediaPlayerManager,
        @ApplicationContext private val context: Context,
    ) {
        /** Envelopes already known, by track. Never cleared on a theme change: coming back is instant. */
        private val envelopes = ConcurrentHashMap<String, FloatArray>()
        private val decoding = ConcurrentHashMap.newKeySet<String>()

        /** The track playing now, as a key into [envelopes]; null when there's no ambient track. */
        @Volatile
        private var currentKey: String? = null

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        /** Whether the music is playing: the visual only needs to run (and redraw) while it is. */
        val isPlaying: StateFlow<Boolean> get() = player.isPlaying

        init {
            scope.launch {
                themeManager.ambientMusicFile.collectLatest { file -> prepare(file) }
            }
        }

        private fun prepare(file: File?) {
            if (file == null || !file.exists()) {
                currentKey = null
                return
            }
            // The modification time makes a re-downloaded track a different key.
            val key = "${file.name}_${file.lastModified()}"
            currentKey = key
            if (envelopes.containsKey(key) || !decoding.add(key)) return
            try {
                val cached = diskFile(key)
                val envelope =
                    cached
                        .takeIf { it.exists() }
                        ?.let { runCatching { AudioEnvelope.fromBytes(it.readBytes()) }.getOrNull() }
                        ?.takeIf { it.isNotEmpty() }
                        ?: AudioEnvelope.decode(file)?.also { decoded ->
                            runCatching { cached.parentFile?.mkdirs(); cached.writeBytes(AudioEnvelope.toBytes(decoded)) }
                                .onFailure { Timber.w(it, "AmbientMusicPulse: couldn't cache the envelope") }
                        }
                if (envelope != null) envelopes[key] = envelope
            } finally {
                decoding.remove(key)
            }
        }

        private fun diskFile(key: String) = File(context.cacheDir, "music_envelopes/$key.env")

        /** 0 when nothing is playing or decoded yet. */
        fun level(): Float {
            val envelope = currentKey?.let { envelopes[it] } ?: return 0f
            if (!player.isPlaying.value) return 0f
            val position = player.positionMs() ?: return 0f
            val loudness = envelope.getOrElse((position / AudioEnvelope.STEP_MS).toInt()) { 0f }
            return loudness * player.volume
        }
    }
