package com.ilustris.sagai.features.voicepicker.data

import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.core.file.FileCacheService
import com.ilustris.sagai.features.audiobook.data.usecase.WaveformExtractor
import com.ilustris.sagai.features.live.data.LiveAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Plays a voice's hosted greeting for the picker. The clip is fetched once into the file cache,
 * and its loudness envelope (the same one live mode and the audiobook waveform use) turns the
 * playback position into [level], so the orb can swell with the voice.
 */
class VoiceSamplePlayer
    @Inject
    constructor(
        private val fileCacheService: FileCacheService,
        private val player: LiveAudioPlayer,
    ) {
        private val _level = MutableStateFlow(0f)

        /** 0..1 loudness of what is playing now; 0 when nothing is. */
        val level: StateFlow<Float> = _level.asStateFlow()

        private val envelopes = mutableMapOf<String, FloatArray>()

        /** True when the clip played to the end; false when it has none, couldn't be fetched, or was stopped. */
        suspend fun play(voice: Voice): Boolean {
            val url = voice.sampleUrl?.takeIf { it.isNotBlank() } ?: return false
            val file = fileCacheService.getFile(url, "wav") ?: return false
            val envelope =
                envelopes.getOrPut(file.absolutePath) {
                    withContext(Dispatchers.IO) {
                        runCatching { WaveformExtractor.envelope(file.readBytes()) }.getOrDefault(FloatArray(0))
                    }
                }
            val peak = envelope.maxOrNull()?.takeIf { it > 0f } ?: 1f
            return try {
                player.play(file.absolutePath) { position ->
                    val window = (position / WaveformExtractor.STEP_MS).toInt()
                    _level.value = ((envelope.getOrNull(window) ?: 0f) / peak).coerceIn(0f, 1f)
                }
            } finally {
                _level.value = 0f
            }
        }

        fun stop() {
            player.stop()
            _level.value = 0f
        }
    }
