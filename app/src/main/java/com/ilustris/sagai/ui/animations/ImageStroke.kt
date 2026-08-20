package com.ilustris.sagai.ui.animations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlin.random.Random
import kotlinx.coroutines.delay

/** Eight compass directions — enough for the offset copies to close into a continuous ring. */
private val STROKE_RING_OFFSETS =
    listOf(
        0f to -1f,
        0.707f to -0.707f,
        1f to 0f,
        0.707f to 0.707f,
        0f to 1f,
        -0.707f to 0.707f,
        -1f to 0f,
        -0.707f to -0.707f,
    )

/** Default cadence for [rememberStopMotionFrame] — slow enough to read as redrawn, not as noise. */
private const val STOP_MOTION_STEP_MS = 240L

/**
 * A counter that ticks in discrete steps, for effects that should redraw at a stop-motion cadence
 * rather than every frame. Feed it into anything whose drawing reads it (e.g. [imageStroke]'s
 * `jitterFrame`) to make that drawing re-randomise once per step.
 */
@Composable
fun rememberStopMotionFrame(stepMs: Long = STOP_MOTION_STEP_MS): Int {
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(stepMs) {
        while (true) {
            delay(stepMs)
            frame++
        }
    }
    return frame
}

/**
 * Solid-color outline hugging the content's actual alpha shape (not its bounding box) — stamps
 * [STROKE_RING_OFFSETS] copies offset by [widthPx], flattens each to a flat [color] silhouette via
 * `saveLayer` + `SrcIn`, then draws the real content on top. Same "offset ghost" technique as
 * [comicExtrude]'s front-face outline, without the extrusion body or pop animation.
 *
 * Because it follows alpha rather than bounds, it is really a *cutout* stroke: pair it with a
 * background-removed subject (a segmented foreground, a sticker) and it traces the subject itself.
 * Chaining two — a wider accent inside a narrower ground colour — reads as a subject lifted off
 * the scene behind it.
 *
 * Pass a ticking [jitterFrame] (see [rememberStopMotionFrame]) with a non-zero [jitterAmountPx] to
 * make the outline tremble: each frame re-rolls a small whole-stroke offset *and* a per-direction
 * thickness wobble, so the edge breathes like a hand-redrawn line instead of sliding around
 * rigidly. Leave the jitter at zero for a still, clean outline.
 */
fun Modifier.imageStroke(
    color: Color,
    widthPx: Float,
    jitterFrame: Int = 0,
    jitterAmountPx: Float = 0f,
) = drawWithContent {
    fun drawGhost(
        dx: Float,
        dy: Float,
    ) {
        drawIntoCanvas { canvas ->
            val paint = Paint()
            canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
            canvas.translate(dx, dy)
            drawContent()
            drawRect(color = color, blendMode = BlendMode.SrcIn)
            canvas.restore()
        }
    }

    val trembles = jitterAmountPx > 0f
    val random = Random(jitterFrame * 977)
    val shiftX = if (trembles) (random.nextFloat() - 0.5f) * jitterAmountPx else 0f
    val shiftY = if (trembles) (random.nextFloat() - 0.5f) * jitterAmountPx else 0f

    STROKE_RING_OFFSETS.forEach { (rx, ry) ->
        val wobble = if (trembles) 1f + (random.nextFloat() - 0.5f) * 0.4f else 1f
        drawGhost(rx * widthPx * wobble + shiftX, ry * widthPx * wobble + shiftY)
    }
    drawContent()
}
