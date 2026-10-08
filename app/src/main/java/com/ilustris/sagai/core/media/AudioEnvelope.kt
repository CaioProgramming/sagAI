package com.ilustris.sagai.core.media

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import timber.log.Timber
import java.io.File
import java.nio.ByteOrder
import kotlin.math.sqrt

/**
 * Loudness over time of a compressed track (the genre's ambient music is an m4a), for drawing
 * things that move with it. Decodes the file once with [MediaCodec] and keeps only the RMS of each
 * [STEP_MS] window, normalized so any track — quiet or mastered hot — spans 0..1. No Visualizer, so
 * no microphone permission.
 */
object AudioEnvelope {
    const val STEP_MS = 100

    /** Past this the rest of the track is ignored; an ambient loop is never this long. */
    private const val MAX_DURATION_MS = 10 * 60 * 1000L
    private const val TIMEOUT_US = 10_000L
    private const val MAX_IDLE_TURNS = 500

    /** The loudness this share of windows stay under is taken as "full", so one spike can't flatten the rest. */
    private const val REFERENCE_PERCENTILE = 0.95f

    /** Null when the file can't be decoded; callers then just have nothing to react to. */
    fun decode(file: File): FloatArray? {
        val startedAt = System.currentTimeMillis()
        return runCatching { decodeOrThrow(file) }
            .onSuccess {
                Timber.d("AudioEnvelope: ${file.name} -> ${it.size} windows (~${it.size * STEP_MS / 1000}s) in ${System.currentTimeMillis() - startedAt}ms")
            }.onFailure { Timber.w(it, "AudioEnvelope: couldn't decode ${file.name}") }
            .getOrNull()
    }

    /**
     * Folds interleaved 16-bit PCM into per-window RMS. [windowSamples] is per-window sample count
     * across all channels; kept apart from the codec so it can be tested on plain arrays.
     */
    internal class Windower(
        private val windowSamples: Int,
    ) {
        private val rms = ArrayList<Float>()
        private var sumSquares = 0.0
        private var count = 0

        fun add(sample: Short) {
            sumSquares += sample.toDouble() * sample
            if (++count == windowSamples) flush()
        }

        fun finish(): FloatArray {
            if (count > 0) flush()
            return rms.toFloatArray()
        }

        private fun flush() {
            rms.add((sqrt(sumSquares / count) / FULL_SCALE).toFloat())
            sumSquares = 0.0
            count = 0
        }

        val windows get() = rms.size
    }

    internal fun normalize(rms: FloatArray): FloatArray {
        if (rms.isEmpty()) return rms
        val reference = rms.sorted()[((rms.size - 1) * REFERENCE_PERCENTILE).toInt()]
        if (reference <= 1e-4f) return FloatArray(rms.size)
        return FloatArray(rms.size) { (rms[it] / reference).coerceIn(0f, 1f) }
    }

    private fun decodeOrThrow(file: File): FloatArray {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            extractor.setDataSource(file.absolutePath)
            val track =
                (0 until extractor.trackCount).firstOrNull {
                    extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
                } ?: error("no audio track")
            extractor.selectTrack(track)
            val format = extractor.getTrackFormat(track)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: error("no mime")

            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            var windower: Windower? = null
            var windowsLimit = Int.MAX_VALUE
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var idleTurns = 0

            while (!outputDone) {
                if (!inputDone) {
                    val index = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (index >= 0) {
                        val buffer = codec.getInputBuffer(index)!!
                        val size = extractor.readSampleData(buffer, 0)
                        if (size < 0) {
                            codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(index, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val out = codec.dequeueOutputBuffer(info, TIMEOUT_US)
                idleTurns = if (out == MediaCodec.INFO_TRY_AGAIN_LATER) idleTurns + 1 else 0
                // A decoder that stops answering must not hang the caller.
                if (idleTurns > MAX_IDLE_TURNS) error("decoder stalled")
                when {
                    out >= 0 -> {
                        // The output format is read here, not from the track: decoders may change
                        // the channel count or sample rate (e.g. HE-AAC upsampling).
                        val outFormat = codec.outputFormat
                        if (windower == null) {
                            val rate = outFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            val channels = outFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                            windower = Windower(rate * channels * STEP_MS / 1000)
                            windowsLimit = (MAX_DURATION_MS / STEP_MS).toInt()
                        }
                        val samples =
                            codec
                                .getOutputBuffer(out)!!
                                .order(ByteOrder.nativeOrder())
                                .asShortBuffer()
                        samples.limit(info.size / 2)
                        while (samples.hasRemaining() && windower.windows < windowsLimit) {
                            windower.add(samples.get())
                        }
                        codec.releaseOutputBuffer(out, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0 || windower.windows >= windowsLimit) {
                            outputDone = true
                        }
                    }
                }
            }
            return normalize(windower?.finish() ?: error("decoder produced no audio"))
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            extractor.release()
        }
    }

    private const val FULL_SCALE = 32768f
}
