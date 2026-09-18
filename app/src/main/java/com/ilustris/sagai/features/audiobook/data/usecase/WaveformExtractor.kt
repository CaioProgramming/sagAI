package com.ilustris.sagai.features.audiobook.data.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sqrt

/**
 * Loudness envelope of the narration clips already on disk, for drawing the player's seek bar as
 * a waveform. The clips are raw 24 kHz / 16-bit mono PCM behind a 44-byte WAV header (same layout
 * BookAudioUseCaseImpl assumes for durations), so no decoder or Visualizer/mic permission is
 * needed — just RMS over fixed windows.
 */
object WaveformExtractor {
    const val STEP_MS = 100

    private const val HEADER_BYTES = 44
    private const val BYTES_PER_MS = 48
    private const val STEP_BYTES = STEP_MS * BYTES_PER_MS
    private const val FULL_SCALE = 32768f

    /** RMS loudness (0..1) of each [STEP_MS] window of one clip. */
    fun envelope(wav: ByteArray): FloatArray {
        val pcmBytes = (wav.size - HEADER_BYTES).coerceAtLeast(0)
        val windows = (pcmBytes + STEP_BYTES - 1) / STEP_BYTES
        return FloatArray(windows) { window ->
            val start = HEADER_BYTES + window * STEP_BYTES
            val end = minOf(start + STEP_BYTES, wav.size)
            var sumSquares = 0.0
            var samples = 0
            var i = start
            while (i + 1 < end) {
                val sample = (wav[i + 1].toInt() shl 8) or (wav[i].toInt() and 0xFF)
                sumSquares += sample.toDouble() * sample
                samples++
                i += 2
            }
            if (samples == 0) 0f else (sqrt(sumSquares / samples) / FULL_SCALE).toFloat()
        }
    }

    /** Envelope of a section's clips back to back, in the order they play. */
    suspend fun extract(paths: List<String>): FloatArray =
        withContext(Dispatchers.IO) {
            val parts = paths.map { path -> runCatching { envelope(File(path).readBytes()) }.getOrDefault(FloatArray(0)) }
            FloatArray(parts.sumOf { it.size }).also { out ->
                var offset = 0
                parts.forEach { part ->
                    part.copyInto(out, offset)
                    offset += part.size
                }
            }
        }
}
