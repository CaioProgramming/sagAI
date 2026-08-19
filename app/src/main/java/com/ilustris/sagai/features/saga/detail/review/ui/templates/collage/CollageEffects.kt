package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import kotlin.random.Random
import kotlinx.coroutines.delay

private const val ASSEMBLY_STEPS = 7
private const val ASSEMBLY_STEP_MS = 90L
private const val IDLE_TREMOR_STEP_MS = 240L
private const val IDLE_JITTER_FRACTION = 0.14f
private const val ENTRANCE_SLIDE_PX = 46f

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

/** Black text on light chips, white text on dark ones — used by every colored insert/label across the Collage template. */
fun Color.readableTextColor() = if (luminance() > 0.5f) Color.Black else Color.White

/**
 * A counter that ticks in discrete steps, for effects that should redraw at the template's
 * stop-motion cadence rather than every frame. Feed it into anything whose drawing reads it (e.g.
 * [imageStroke]'s `jitterFrame`) to make that drawing re-randomise once per step.
 */
@Composable
fun rememberStopMotionFrame(stepMs: Long = IDLE_TREMOR_STEP_MS): Int {
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
 * [com.ilustris.sagai.ui.animations.comicExtrude]'s front-face outline, without the extrusion body
 * or pop animation. Used for the poster's character stroke and, per-sticker, on the Characters page.
 *
 * Pass a ticking [jitterFrame] (see [rememberStopMotionFrame]) with a non-zero [jitterAmountPx] to
 * make the outline tremble in step with the rest of the template: each frame re-rolls a small
 * whole-stroke offset *and* a per-direction thickness wobble, so the edge breathes like a
 * hand-redrawn line instead of sliding around rigidly.
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

/**
 * One collage element that jump-cuts through [ASSEMBLY_STEPS] discrete frames ([ASSEMBLY_STEP_MS]
 * apart, decelerating) as it slides/rotates into place — a "glued into place" stop-motion feel
 * rather than a continuously eased tween. Once settled, it keeps a faint perpetual tremor (unless
 * [idleTremor] is false) — same low-fps jitter idea as
 * [com.ilustris.sagai.ui.components.RansomLetter]'s letters, so every Collage page reads as one
 * consistent stop-motion identity instead of a one-shot entrance. Positioning is entirely the
 * caller's [modifier] (e.g. `Modifier.align(...).padding(...)`, scoped to whatever container
 * — [Box] or [androidx.compose.foundation.layout.Column] — it's actually placed in) so this stays
 * reusable across pages.
 *
 * [entranceOffset] is where the piece starts relative to its resting spot, decaying to zero as it
 * settles — the default is a small vertical-only slide (the original "pops up into place" feel
 * used by the poster/characters pages); pass a larger, directional offset (e.g. from a screen
 * corner) for pieces that should read as "dragged in" from further away, like the text-page inserts.
 *
 * [scaleFrom] is the scale the piece grows from. Pass `1f` for anything meant to span the full
 * width — scaling an edge-to-edge strip up from a fraction visibly pulls it off both screen edges,
 * which breaks the "torn across the whole page" read.
 */
@Composable
fun AssemblingPiece(
    modifier: Modifier = Modifier,
    rotation: Float,
    delayMs: Long,
    canAnimate: Boolean,
    seed: Int,
    idleTremor: Boolean = true,
    entranceOffset: Offset = Offset(0f, ENTRANCE_SLIDE_PX),
    scaleFrom: Float = 0.6f,
    content: @Composable () -> Unit,
) {
    var step by remember { mutableIntStateOf(if (canAnimate) 0 else ASSEMBLY_STEPS) }

    LaunchedEffect(canAnimate) {
        if (canAnimate) {
            delay(delayMs)
            repeat(ASSEMBLY_STEPS) {
                step++
                delay(ASSEMBLY_STEP_MS)
            }
        } else {
            step = ASSEMBLY_STEPS
        }
        if (idleTremor) {
            while (true) {
                delay(IDLE_TREMOR_STEP_MS)
                step++
            }
        }
    }

    val settleStep = step.coerceAtMost(ASSEMBLY_STEPS)
    val t = settleStep / ASSEMBLY_STEPS.toFloat()
    val eased = 1f - (1f - t) * (1f - t)
    val isSettled = step >= ASSEMBLY_STEPS
    val jitterDecay = if (isSettled) IDLE_JITTER_FRACTION else (1f - t)
    val jitter = Random(seed * 131 + step)
    val jitterX = (jitter.nextFloat() - 0.5f) * 40f * jitterDecay
    val jitterY = (jitter.nextFloat() - 0.5f) * 40f * jitterDecay
    val jitterRot = (jitter.nextFloat() - 0.5f) * 26f * jitterDecay
    val entranceX = entranceOffset.x * (1f - eased)
    val entranceY = entranceOffset.y * (1f - eased)
    val scale = scaleFrom + (1f - scaleFrom) * eased
    val visible = step > 0

    Box(
        modifier.graphicsLayer {
            translationX = entranceX + jitterX
            translationY = entranceY + jitterY
            rotationZ = rotation + jitterRot
            scaleX = scale
            scaleY = scale
            alpha = if (visible) 1f else 0f
        },
    ) {
        content()
    }
}

