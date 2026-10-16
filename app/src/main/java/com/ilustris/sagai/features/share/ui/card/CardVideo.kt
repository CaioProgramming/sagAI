package com.ilustris.sagai.features.share.ui.card

import android.graphics.RectF
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.layer.GraphicsLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Frames per second of the loop. */
private const val FPS = 24

/** Three seconds at [FPS]. */
private const val FRAMES = 72

/**
 * The clip is 9:16, the shape of Reels, TikTok, Stories and WhatsApp status: the card sits in the
 * middle of it. Both sides are multiples of 16, which encoders like.
 */
private const val VIDEO_WIDTH = 720
private const val VIDEO_HEIGHT = 1280

/** Space left on each side of the card, in video pixels. */
private const val SIDE_MARGIN = 60

/**
 * The most of the height the card may take. Those apps lay their caption, name and buttons over the
 * top and the bottom of the screen, so the card stays in the middle band they leave alone.
 */
private const val MAX_HEIGHT_SHARE = 0.72f

/** How far the card leans along each axis during the loop, as a share of full tilt. */
private const val TILT_X = 0.85f
private const val TILT_Y = 0.4f

/**
 * Renders [layer], the card with its 3D rotation, to a 9:16 MP4 loop of it turning in a slow ellipse:
 * the tilt goes round once over the clip, so the last frame leads into the first and the loop has no
 * seam. The card is fitted into the middle of the frame, over a dark background with a glow in [glow].
 *
 * The card is driven through [tilt] (see [CardTiltState.script]) and each frame is captured once the
 * card has drawn it, so the foil, the parallax and the fringe all move exactly as on screen. Runs on
 * the composition's clock and takes a few seconds; [onProgress] reports 0..1.
 */
suspend fun renderCardVideo(
    layer: GraphicsLayer,
    tilt: CardTiltState,
    file: File,
    glow: Color,
    onProgress: (Float) -> Unit,
) {
    val source = layer.size
    require(source.width > 0 && source.height > 0) { "The card has not been laid out yet" }
    val fit =
        minOf(
            (VIDEO_WIDTH - 2 * SIDE_MARGIN).toFloat() / source.width,
            MAX_HEIGHT_SHARE * VIDEO_HEIGHT / source.height,
        )
    val cardWidth = source.width * fit
    val cardHeight = source.height * fit
    val destination =
        RectF(
            (VIDEO_WIDTH - cardWidth) / 2f,
            (VIDEO_HEIGHT - cardHeight) / 2f,
            (VIDEO_WIDTH + cardWidth) / 2f,
            (VIDEO_HEIGHT + cardHeight) / 2f,
        )

    CardVideoEncoder(file, VIDEO_WIDTH, VIDEO_HEIGHT, FPS, glow.toArgb()).use { encoder ->
        try {
            repeat(FRAMES) { index ->
                val angle = index.toFloat() / FRAMES * 2f * PI.toFloat()
                tilt.script(Offset(sin(angle) * TILT_X, cos(angle) * TILT_Y))
                // One frame to apply the new tilt, one so every layer and shader has drawn it.
                withFrameNanos { }
                withFrameNanos { }
                val frame = layer.toImageBitmap().asAndroidBitmap()
                withContext(Dispatchers.Default) { encoder.addFrame(frame, destination) }
                frame.recycle()
                onProgress((index + 1).toFloat() / FRAMES)
            }
            withContext(Dispatchers.Default) { encoder.finish() }
        } finally {
            tilt.script(null)
        }
    }
}
