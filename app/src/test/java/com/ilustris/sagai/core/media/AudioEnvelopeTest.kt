package com.ilustris.sagai.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioEnvelopeTest {
    private fun windows(
        windowSamples: Int,
        samples: List<Int>,
    ): FloatArray {
        val windower = AudioEnvelope.Windower(windowSamples)
        samples.forEach { windower.add(it.toShort()) }
        return windower.finish()
    }

    @Test
    fun `rms is taken per window, in order`() {
        // Window 1 is silence, window 2 a constant half-scale signal.
        val rms = windows(4, List(4) { 0 } + List(4) { 16384 })
        assertEquals(2, rms.size)
        assertEquals(0f, rms[0], 1e-6f)
        assertEquals(0.5f, rms[1], 1e-3f)
    }

    @Test
    fun `the last partial window still counts`() {
        val rms = windows(4, List(4) { 0 } + List(2) { 32767 })
        assertEquals(2, rms.size)
        assertEquals(1f, rms[1], 1e-3f)
    }

    @Test
    fun `negative samples have the same loudness as positive ones`() {
        val rms = windows(2, listOf(-16384, -16384))
        assertEquals(0.5f, rms[0], 1e-3f)
    }

    @Test
    fun `normalizing maps the loud end of any track to full scale`() {
        val quiet = AudioEnvelope.normalize(FloatArray(100) { 0.01f * (it + 1) / 100f })
        val hot = AudioEnvelope.normalize(FloatArray(100) { 0.9f * (it + 1) / 100f })
        assertEquals(1f, quiet.max(), 1e-6f)
        assertEquals(1f, hot.max(), 1e-6f)
        assertTrue(quiet.all { it in 0f..1f })
    }

    @Test
    fun `one spike does not flatten the rest`() {
        val rms = FloatArray(100) { 0.1f }
        rms[50] = 1f
        val normalized = AudioEnvelope.normalize(rms)
        assertEquals(1f, normalized[0], 1e-6f) // the reference is the common loudness, not the spike
        assertEquals(1f, normalized[50], 1e-6f)
    }

    @Test
    fun `silence and empty stay silent`() {
        assertTrue(AudioEnvelope.normalize(FloatArray(10)).all { it == 0f })
        assertEquals(0, AudioEnvelope.normalize(FloatArray(0)).size)
    }

    @Test
    fun `an envelope survives the disk cache unchanged`() {
        val envelope = floatArrayOf(0f, 0.25f, 1f, 0.123456f, 0.9999f)
        assertTrue(envelope.contentEquals(AudioEnvelope.fromBytes(AudioEnvelope.toBytes(envelope))))
    }

    @Test
    fun `an empty or truncated cache file reads as an empty or shorter envelope, never a crash`() {
        assertEquals(0, AudioEnvelope.fromBytes(ByteArray(0)).size)
        assertEquals(1, AudioEnvelope.fromBytes(ByteArray(7)).size) // a torn trailing float is dropped
    }
}
