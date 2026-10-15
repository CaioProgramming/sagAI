package com.ilustris.sagai.features.share.ui.card

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import java.io.Closeable
import java.io.File

/**
 * Writes a sequence of bitmaps to an H.264 MP4, one bitmap per frame at a fixed [fps].
 *
 * The frames are drawn onto the encoder's input surface. A canvas posts them with the wall clock as
 * their time, so the real capture time never reaches the file: the muxer is given each sample's time
 * as its position in the sequence instead. That only holds if the encoder never reorders frames,
 * which is why the profile is Baseline (no B-frames).
 *
 * Call [addFrame] for every frame, then [finish], then [close]. A file left by a failure is the
 * caller's to delete.
 */
class CardVideoEncoder(
    file: File,
    private val width: Int,
    private val height: Int,
    private val fps: Int,
) : Closeable {
    private val codec: MediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    private val muxer = MediaMuxer(file.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    private val info = MediaCodec.BufferInfo()
    private val inputSurface: Surface
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val destination = RectF(0f, 0f, width.toFloat(), height.toFloat())
    private var track = -1
    private var muxerStarted = false
    private var samples = 0

    init {
        val format =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
                // Variable bitrate overshot the target by almost two times on a foil-heavy card; constant holds it.
                setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline)
            }
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = codec.createInputSurface()
        codec.start()
    }

    fun addFrame(bitmap: Bitmap) {
        val canvas = inputSurface.lockHardwareCanvas()
        try {
            canvas.drawColor(BACKGROUND)
            canvas.drawBitmap(bitmap, null, destination, paint)
        } finally {
            inputSurface.unlockCanvasAndPost(canvas)
        }
        drain(endOfStream = false)
    }

    fun finish() {
        codec.signalEndOfInputStream()
        drain(endOfStream = true)
    }

    private fun drain(endOfStream: Boolean) {
        var idle = 0
        while (true) {
            val index = codec.dequeueOutputBuffer(info, if (endOfStream) POLL_US else 0L)
            when {
                index == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) return
                    // The encoder still owes frames; give it a bounded time to flush them.
                    if (++idle > MAX_IDLE_POLLS) error("The encoder never signalled the end of the stream")
                }

                index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    track = muxer.addTrack(codec.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }

                index >= 0 -> {
                    val buffer = checkNotNull(codec.getOutputBuffer(index))
                    if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) info.size = 0
                    if (info.size > 0 && muxerStarted) {
                        info.presentationTimeUs = samples * MICROS / fps
                        samples++
                        buffer.position(info.offset)
                        buffer.limit(info.offset + info.size)
                        muxer.writeSampleData(track, buffer, info)
                    }
                    codec.releaseOutputBuffer(index, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                }
            }
        }
    }

    override fun close() {
        runCatching { codec.stop() }
        runCatching { codec.release() }
        runCatching { if (muxerStarted) muxer.stop() }
        runCatching { muxer.release() }
        runCatching { inputSurface.release() }
    }

    private companion object {
        const val BIT_RATE = 5_000_000
        const val BACKGROUND = 0xFF08080B.toInt()
        const val MICROS = 1_000_000L
        const val POLL_US = 10_000L
        const val MAX_IDLE_POLLS = 500
    }
}
