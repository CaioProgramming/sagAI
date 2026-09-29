package com.ilustris.sagai.ui.theme.components.mascot

import androidx.compose.ui.graphics.Path
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * The dragon's head: one closed outline, drawn as a single flat shape.
 *
 * Horns, crest teeth, cheek spikes and frills are vertices of the same polygon, not parts glued on
 * top, and every valley between them has a rounded corner — that is what makes the spikes read as
 * growing out of the head. Units are the head's half-width (`1f`), eye level is `y = 0`, y grows down.
 *
 * The valleys never move; only the tips sway, lift with [posture][com.ilustris.sagai.core.services.model.MascotExpression.posture]
 * and shift against the face, so the outline never tears open.
 */
internal object DragonHead {
    private enum class Tip { NONE, HORN, CREST, FRILL }

    private class Vertex(
        val x: Float,
        val y: Float,
        val corner: Float,
        val tip: Tip = Tip.NONE,
        val pivotX: Float = 0f,
        val pivotY: Float = 0f,
        val phase: Float = 0f,
        val sway: Float = 0f,
        val parallax: Float = 0f,
        val side: Int = 1,
    )

    private fun valley(
        x: Float,
        y: Float,
        corner: Float,
    ) = Vertex(x, y, corner)

    private fun tip(
        kind: Tip,
        x: Float,
        y: Float,
        corner: Float,
        pivotX: Float,
        pivotY: Float,
        phase: Float,
        sway: Float,
        parallax: Float,
    ) = Vertex(x, y, corner, kind, pivotX, pivotY, phase, sway, parallax)

    private fun horn(
        x: Float,
        y: Float,
        corner: Float,
    ) = tip(Tip.HORN, x, y, corner, HORN_PIVOT_X, HORN_PIVOT_Y, HORN_PHASE, HORN_SWAY, HORN_PARALLAX)

    // Right half, from the centre spike down to the chin. Both horns curve inward.
    private val rightHalf =
        listOf(
            tip(Tip.CREST, 0f, -1.23f, .010f, 0f, -.90f, 0f, .03f, .05f),
            valley(.23f, -.906f, .040f),
            tip(Tip.CREST, .275f, -1.08f, .012f, .30f, -.91f, .7f, .035f, .05f),
            valley(.385f, -.92f, .050f),
            horn(.49f, -1.10f, .300f),
            horn(.48f, -1.53f, .015f),
            horn(.75f, -1.22f, .350f),
            valley(.80f, -.775f, .050f),
            tip(Tip.CREST, .915f, -1.06f, .012f, .90f, -.80f, 1.6f, .035f, .04f),
            valley(1.00f, -.80f, .100f),
            valley(.995f, -.52f, .220f),
            valley(.94f, -.40f, .030f),
            tip(Tip.FRILL, 1.055f, -.35f, .012f, .93f, -.27f, .4f, .04f, 0f),
            valley(.92f, -.13f, .050f),
            tip(Tip.FRILL, 1.17f, -.075f, .012f, .90f, .06f, 1.7f, .04f, 0f),
            valley(.84f, .22f, .050f),
            valley(.66f, .29f, .200f),
            valley(.40f, .48f, .300f),
            valley(0f, .62f, .250f),
        )

    // The left half is the right one mirrored, minus the two vertices on the axis.
    private val outline: List<Vertex> =
        rightHalf +
            rightHalf.subList(1, rightHalf.size - 1).asReversed().map {
                Vertex(-it.x, it.y, it.corner, it.tip, -it.pivotX, it.pivotY, it.phase, it.sway, it.parallax, -1)
            }

    /** Bounding box of the resting outline plus a margin, for fitting it into a canvas. */
    const val LEFT = -1.22f
    const val RIGHT = 1.22f
    const val TOP = -1.58f
    const val BOTTOM = .66f

    // Skin width of the face (frills excluded) by height: what keeps the eyes and nose inside the head.
    private val face =
        arrayOf(
            floatArrayOf(-1.0f, .98f),
            floatArrayOf(-.5f, .995f),
            floatArrayOf(-.2f, .93f),
            floatArrayOf(.05f, .93f),
            floatArrayOf(.22f, .84f),
            floatArrayOf(.29f, .68f),
            floatArrayOf(.48f, .40f),
            floatArrayOf(.60f, .10f),
            floatArrayOf(.62f, 0f),
        )

    /** Half-width of the face at height [y]. */
    fun faceHalfWidth(y: Float): Float {
        if (y <= face[0][0]) return face[0][1]
        for (i in 1 until face.size) {
            if (y <= face[i][0]) {
                val (y0, w0) = face[i - 1]
                val (y1, w1) = face[i]
                return w0 + (w1 - w0) * (y - y0) / (y1 - y0)
            }
        }
        return 0f
    }

    /**
     * Rebuilds [path] for this frame. [posture] lifts or drops crest, horns and frills, [yaw] shifts the
     * tips against the face (they sit behind it), [beat] sways them.
     */
    fun buildPath(
        path: Path,
        posture: Float,
        yaw: Float,
        beat: Float,
    ) {
        val scratch = FloatArray(outline.size * 2)
        outline.forEachIndexed { i, v ->
            var x = v.x
            var y = v.y
            if (v.tip != Tip.NONE) {
                val scale =
                    when (v.tip) {
                        Tip.HORN -> 1f + HORN_LIFT * posture
                        Tip.CREST -> 1f + CREST_LIFT * posture
                        else -> 1f
                    }
                if (scale != 1f) {
                    x = v.pivotX + (x - v.pivotX) * scale
                    y = v.pivotY + (y - v.pivotY) * scale
                }
                var angle = sin(beat * SWAY_SPEED + v.phase + if (v.side > 0) 0f else SWAY_LEFT_LAG) * v.sway
                if (v.tip == Tip.FRILL) angle += -posture * FRILL_SPREAD * v.side
                if (angle != 0f) {
                    val dx = x - v.pivotX
                    val dy = y - v.pivotY
                    val c = cos(angle)
                    val s = sin(angle)
                    x = v.pivotX + dx * c - dy * s
                    y = v.pivotY + dx * s + dy * c
                }
                x += -yaw * v.parallax
            }
            scratch[i * 2] = x
            scratch[i * 2 + 1] = y
        }

        path.rewind()
        val n = outline.size
        for (i in 0 until n) {
            val prev = (i + n - 1) % n
            val next = (i + 1) % n
            val px = scratch[i * 2]
            val py = scratch[i * 2 + 1]
            val v1x = scratch[prev * 2] - px
            val v1y = scratch[prev * 2 + 1] - py
            val v2x = scratch[next * 2] - px
            val v2y = scratch[next * 2 + 1] - py
            val l1 = hypot(v1x, v1y).takeIf { it > 0f } ?: 1f
            val l2 = hypot(v2x, v2y).takeIf { it > 0f } ?: 1f
            val t = min(outline[i].corner, min(l1 * .5f, l2 * .5f))
            val ax = px + v1x / l1 * t
            val ay = py + v1y / l1 * t
            if (i == 0) path.moveTo(ax, ay) else path.lineTo(ax, ay)
            path.quadraticTo(px, py, px + v2x / l2 * t, py + v2y / l2 * t)
        }
        path.close()
    }

    private const val HORN_PIVOT_X = .57f
    private const val HORN_PIVOT_Y = -.95f
    private const val HORN_PHASE = 2.0f
    private const val HORN_SWAY = .02f
    private const val HORN_PARALLAX = .07f
    private const val HORN_LIFT = .10f
    private const val CREST_LIFT = .14f
    private const val FRILL_SPREAD = .30f
    private const val SWAY_SPEED = 1.6f
    private const val SWAY_LEFT_LAG = .9f
}
