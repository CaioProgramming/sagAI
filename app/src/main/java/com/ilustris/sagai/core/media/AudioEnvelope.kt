package com.ilustris.sagai.core.media

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
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

    /**
     * Only every Nth sample feeds the RMS: a 100ms window holds thousands, so the loudness is the
     * same to the eye and the per-sample work (the slow part, ~14M samples for a 3 min stereo track)
     * drops by this factor. Odd, so both channels of an interleaved stream are still sampled.
     */
    private const val SAMPLE_STRIDE = 7

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

    /** For the on-disk cache: a decoded envelope is a few KB, but costs seconds to make. */
    fun toBytes(envelope: FloatArray): ByteArray {
        val buffer = ByteBuffer.allocate(envelope.size * Float.SIZE_BYTES)
        buffer.asFloatBuffer().put(envelope)
        return buffer.array()
    }

    fun fromBytes(bytes: ByteArray): FloatArray {
        val floats = FloatArray(bytes.size / Float.SIZE_BYTES)
        ByteBuffer.wrap(bytes, 0, floats.size * Float.SIZE_BYTES).asFloatBuffer().get(floats)
        return floats
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
            var pcm = ShortArray(0)

            while (!outputDone) {
                // Feed everything the codec will take right now, without waiting on it.
                while (!inputDone) {
                    val index = codec.dequeueInputBuffer(0)
                    if (index < 0) break
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

                // Then drain everything it has ready; only the first read may wait.
                var timeout = TIMEOUT_US
                while (!outputDone) {
                    val out = codec.dequeueOutputBuffer(info, timeout)
                    if (out == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // A decoder that stops answering must not hang the caller.
                        if (++idleTurns > MAX_IDLE_TURNS) error("decoder stalled")
                        break
                    }
                    idleTurns = 0
                    timeout = 0
                    if (out < 0) continue // format / buffers changed: nothing to read

                    // The output format is read here, not from the track: decoders may change the
                    // channel count or sample rate (e.g. HE-AAC upsampling).
                    if (windower == null) {
                        val outFormat = codec.outputFormat
                        val rate = outFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        val channels = outFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        windower = Windower(rate * channels * STEP_MS / 1000 / SAMPLE_STRIDE)
                        windowsLimit = (MAX_DURATION_MS / STEP_MS).toInt()
                    }
                    val shorts = info.size / 2
                    if (pcm.size < shorts) pcm = ShortArray(shorts)
                    codec
                        .getOutputBuffer(out)!!
                        .order(ByteOrder.nativeOrder())
                        .asShortBuffer()
                        .get(pcm, 0, shorts)
                    var i = 0
                    while (i < shorts && windower.windows < windowsLimit) {
                        windower.add(pcm[i])
                        i += SAMPLE_STRIDE
                    }
                    codec.releaseOutputBuffer(out, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0 || windower.windows >= windowsLimit) {
                        outputDone = true
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
