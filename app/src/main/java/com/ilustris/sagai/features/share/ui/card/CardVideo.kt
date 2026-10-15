package com.ilustris.sagai.features.share.ui.card

import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Frames per second of the loop. */
private const val FPS = 24

/** Three seconds at [FPS]. */
private const val FRAMES = 72

/** Output width in pixels; the height follows the card, rounded to a multiple of 16 for the encoder. */
private const val VIDEO_WIDTH = 720

/** How far the card leans along each axis during the loop, as a share of full tilt. */
private const val TILT_X = 0.85f
private const val TILT_Y = 0.4f

/**
 * Renders [layer], the card with its 3D rotation, to an MP4 loop of it turning in a slow ellipse: the
 * tilt goes round once over the clip, so the last frame leads into the first and the loop has no seam.
 *
 * The card is driven through [tilt] (see [CardTiltState.script]) and each frame is captured once the
 * card has drawn it, so the foil, the parallax and the fringe all move exactly as on screen. Runs on
 * the composition's clock and takes a few seconds; [onProgress] reports 0..1.
 */
suspend fun renderCardVideo(
    layer: GraphicsLayer,
    tilt: CardTiltState,
    file: File,
    onProgress: (Float) -> Unit,
) {
    val source = layer.size
    require(source.width > 0 && source.height > 0) { "The card has not been laid out yet" }
    val height = ((VIDEO_WIDTH.toFloat() * source.height / source.width) / 16f).roundToInt().coerceAtLeast(1) * 16

    CardVideoEncoder(file, VIDEO_WIDTH, height, FPS).use { encoder ->
        try {
            repeat(FRAMES) { index ->
                val angle = index.toFloat() / FRAMES * 2f * PI.toFloat()
                tilt.script(Offset(sin(angle) * TILT_X, cos(angle) * TILT_Y))
                // One frame to apply the new tilt, one so every layer and shader has drawn it.
                withFrameNanos { }
                withFrameNanos { }
                val frame = layer.toImageBitmap().asAndroidBitmap()
                withContext(Dispatchers.Default) { encoder.addFrame(frame) }
                frame.recycle()
                onProgress((index + 1).toFloat() / FRAMES)
            }
            withContext(Dispatchers.Default) { encoder.finish() }
        } finally {
            tilt.script(null)
        }
    }
}
