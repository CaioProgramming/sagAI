package com.ilustris.sagai.features.audiobook

import com.ilustris.sagai.features.audiobook.data.usecase.WaveformExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WaveformExtractorTest {
    private val windowSamples = WaveformExtractor.STEP_MS * 24

    private fun wav(vararg windows: Pair<Int, Int>): ByteArray {
        val out = ArrayList<Byte>()
        repeat(44) { out.add(0) }
        windows.forEach { (samples, amplitude) ->
            repeat(samples) {
                out.add((amplitude and 0xFF).toByte())
                out.add(((amplitude shr 8) and 0xFF).toByte())
            }
        }
        return out.toByteArray()
    }

    @Test
    fun `one level per step window, loud vs silent`() {
        val levels = WaveformExtractor.envelope(wav(windowSamples to 16384, windowSamples to 0))

        assertEquals(2, levels.size)
        assertEquals(0.5f, levels[0], 0.01f)
        assertEquals(0f, levels[1], 0.0001f)
    }

    @Test
    fun `a trailing partial window still counts as a level`() {
        val levels = WaveformExtractor.envelope(wav(windowSamples to 8192, windowSamples / 2 to 32767))

        assertEquals(2, levels.size)
        assertTrue(levels[1] > levels[0])
    }

    @Test
    fun `negative samples measure loudness too`() {
        val levels = WaveformExtractor.envelope(wav(windowSamples to -16384))

        assertEquals(0.5f, levels[0], 0.01f)
    }

    @Test
    fun `header-only or empty input has no levels`() {
        assertEquals(0, WaveformExtractor.envelope(ByteArray(44)).size)
        assertEquals(0, WaveformExtractor.envelope(ByteArray(0)).size)
    }
}
