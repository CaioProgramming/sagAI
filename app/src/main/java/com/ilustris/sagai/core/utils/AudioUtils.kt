package com.ilustris.sagai.core.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utility to wrap raw PCM audio data into a WAV container.
 * Gemini API returns audio/L16;codec=pcm;rate=24000 which is raw binary.
 */
object AudioUtils {
    private const val TAG = "AudioUtils"

    fun wrapPcmInWav(
        pcmData: ByteArray,
        sampleRate: Int = 24000,
    ): ByteArray {
        val channels = 1 // Mono
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val dataSize = pcmData.size
        val chunkSize = 36 + dataSize

        val buffer = ByteBuffer.allocate(44 + dataSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buffer.put("RIFF".toByteArray())
        buffer.putInt(chunkSize)
        buffer.put("WAVE".toByteArray())

        // fmt chunk
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // SubchunkSize
        buffer.putShort(1) // AudioFormat (1 = PCM)
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(bitsPerSample.toShort())

        // data chunk
        buffer.put("data".toByteArray())
        buffer.putInt(dataSize)
        buffer.put(pcmData)

        return buffer.array()
    }
}
