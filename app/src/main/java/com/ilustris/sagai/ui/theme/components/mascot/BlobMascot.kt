package com.ilustris.sagai.ui.theme.components.mascot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.ilustris.sagai.core.services.model.MascotExpression
import com.ilustris.sagai.ui.theme.rememberReduceMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.tan

/**
 * The mascot: a flat baby-dragon head with hollow eyes and nose.
 *
 * The silhouette is one outline ([DragonHead]). The face — eyes and nose — is a plane laid over it:
 * when the gaze moves, both slide together (the nose a little further, it is nearer), the eye on the
 * side being turned to narrows, the head itself shifts and rolls a touch, and horns and crest shift
 * the other way, as things behind the face. Each eye is then checked against the width of the head
 * at its height and pulled back in if any of it would cross the edge, so nothing ever leaves the head.
 *
 * Eyes and nose are holes, not a colour: they are cut out of the body, so whatever is painted behind
 * the mascot — parchment grain, a halftone page, a photo — shows through them, on every surface,
 * without anyone having to guess a matching colour.
 *
 * Emotion is the tone's [MascotExpression]: eye shape and slope, lid, cheek, crest posture, head
 * roll and dip, nose, and where the gaze rests. Nothing else changes between tones.
 *
 * Motion is Playful, and elastic: a spring entrance that drops in, lands with a squash and only
 * then opens its eyes (the body is the hero, the face follows); glances that dart and settle
 * instead of drifting on a sine; blinks that do not arrive on a metronome. All of it runs on the
 * tone's `tempo`, so an anxious dragon glances fast and a sad one slowly. Nothing moves when
 * [animate] is false or the system has animations removed.
 *
 * Draws nothing when [expression] is null: a tone that is not configured in Remote Config
 * has no mascot, by design.
 *
 * @param look where the mascot is looking, each axis in `-1f..1f`; null glances around on its own.
 *   Taken as a lambda so a value that changes every frame — [rememberTiltLook], a drag — is read
 *   while drawing and repaints without recomposing anything.
 * @param pokeable squashes and widens its eyes when tapped. Off by default because it consumes the
 *   tap: a blob sitting inside a clickable card would swallow the card's click.
 * @param entranceDelayMillis how long to wait before arriving, for a blob composed at once but
 *   revealed later by a wrapper that fades or assembles it — otherwise the arrival plays out
 *   while it is still invisible and the reader only ever sees it already landed.
 */
@Composable
fun BlobMascot(
    expression: MascotExpression?,
    color: Color,
    modifier: Modifier = Modifier,
    look: () -> Offset? = { null },
    animate: Boolean = true,
    pokeable: Boolean = false,
    entranceDelayMillis: Long = 0L,
) {
    if (expression == null) return

    val alive = animate && !rememberReduceMotion()
    val time by produceState(0f, alive) {
        if (!alive) return@produceState
        var origin = 0L
        while (true) {
            withInfiniteAnimationFrameMillis { millis ->
                if (origin == 0L) origin = millis
                value = (millis - origin) / 1000f
            }
        }
    }

    // 0 -> 1 with a deliberate overshoot; the overshoot is the landing.
    val entrance = remember { Animatable(if (alive) 0f else 1f) }
    LaunchedEffect(alive) {
        if (alive) {
            entrance.snapTo(0f)
            delay(entranceDelayMillis)
            entrance.animateTo(
                1f,
                spring(dampingRatio = ENTRANCE_DAMPING, stiffness = Spring.StiffnessMediumLow),
            )
        } else {
            entrance.snapTo(1f)
        }
    }

    // 1 the instant it is pressed, then rings back through zero — the negative half is the rebound.
    val poke = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Rebuilt every frame; kept here so a frame allocates none of them.
    val head = remember { Path() }
    val nose = remember { Path() }
    val eye = remember { Path() }
    val lidClip = remember { Path() }
    val cheekClip = remember { Path() }

    Canvas(
        modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .then(
                if (pokeable && alive) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures {
                            scope.launch {
                                poke.snapTo(1f)
                                poke.animateTo(
                                    0f,
                                    spring(dampingRatio = POKE_DAMPING, stiffness = Spring.StiffnessMedium),
                                )
                            }
                        }
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        // One head half-width in pixels. The outline is drawn edge to edge of its own box, so [FIT]
        // is the headroom the breathing, bobbing and entrance overshoot need before the canvas clips it.
        val unit =
            min(
                size.width / (DragonHead.RIGHT - DragonHead.LEFT),
                size.height / (DragonHead.BOTTOM - DragonHead.TOP),
            ) * FIT
        val beat = time * expression.tempo
        val landed = entrance.value
        val pressed = poke.value

        // Volume-preserving: stretched tall while it is still arriving, wide as it overshoots.
        val landing = (landed - 1f) * LANDING_SQUASH
        val breath = sin(beat * BREATH_SPEED) * BREATH_AMOUNT
        val grow = ENTRANCE_FROM + (1f - ENTRANCE_FROM) * landed
        val scaleX = grow * (1f + breath + landing + pressed * POKE_X)
        val scaleY = grow * (1f - breath - landing - pressed * POKE_Y)

        // The eyes get there first and the head follows a moment later, gliding: a look that leads
        // the head is what makes the turn read as attention rather than as a slide.
        val pointed = look()?.let { Offset(it.x.coerceIn(-1f, 1f), it.y.coerceIn(-1f, 1f)) }
        val gaze = pointed ?: expression.idleLook(beat, EYES_TRAVEL, delay = 0f, gliding = false)
        val headGaze = pointed ?: expression.idleLook(beat, HEAD_TRAVEL, delay = HEAD_LAG, gliding = true)
        val yaw = gaze.x
        val pitch = gaze.y

        val shake = expression.jit
        val center =
            Offset(
                x = size.width / 2f +
                    sin(beat * SHAKE_X_SPEED) * unit * SHAKE_X * shake +
                    headGaze.x * unit * HEAD_SHIFT_X,
                y = size.height / 2f - (DragonHead.TOP + DragonHead.BOTTOM) / 2f * unit +
                    sin(beat * BOB_SPEED) * unit * BOB +
                    cos(beat * SHAKE_Y_SPEED) * unit * SHAKE_Y * shake +
                    (headGaze.y * HEAD_SHIFT_Y + expression.headDip) * unit +
                    (landed - 1f) * unit * ENTRANCE_DROP +
                    pressed * unit * POKE_DROP,
            )
        val roll = headGaze.x * HEAD_ROLL + expression.headTilt

        val eyesOpen = smoothstep((landed - EYES_AFTER) / EYES_SPAN)
        val blink = blinkAt(beat) * eyesOpen
        val widen = 1f + abs(pressed).coerceAtMost(1f) * POKE_EYES

        DragonHead.buildPath(head, expression.posture, headGaze.x, beat)

        withTransform({
            translate(center.x, center.y)
            rotate(degrees = roll * RAD_TO_DEG, pivot = Offset.Zero)
            scale(unit * scaleX, unit * scaleY, Offset.Zero)
        }) {
            drawPath(head, color, alpha = (landed / FADE_IN_BY).coerceIn(0f, 1f))
            drawNose(nose, expression, yaw, pitch, beat)
            drawEyes(expression, yaw, pitch, blink, widen, eye, lidClip, cheekClip)
        }
    }
}

/** Where a face feature ends up, in head units. */
private class PlacedEye(
    val side: Int,
    val x: Float,
    val y: Float,
    val scaleX: Float,
    val scaleY: Float,
    val rot: Float,
    val halfWidth: Float,
    val halfHeight: Float,
    val lid: Float,
)

/**
 * Eye position is the face plane plus the tone, then checked against the head: the eye's outline is
 * sampled and, if any of it would pass the skin edge (with a margin), the eye moves in by what is
 * missing — and up, when it has already reached the limit between the eyes.
 */
private fun placeEye(
    side: Int,
    expression: MascotExpression,
    yaw: Float,
    pitch: Float,
    blink: Float,
    widen: Float,
): PlacedEye {
    val halfWidth = EYE_HALF_WIDTH * widen
    val halfHeight = EYE_HALF_HEIGHT * widen
    // The eye on the side being turned to narrows.
    val far = (1f - FAR_EYE_NARROWING * yaw * side).coerceIn(FAR_EYE_MIN, FAR_EYE_MAX)
    val scaleX = far * expression.w
    val scaleY = expression.h * (1f + side * expression.asym / 2f) * blink
    val rot = side * expression.rot

    var x = side * EYE_GAP + yaw * FACE_SHIFT_X
    var y = (EYE_LEVEL + expression.dy + pitch * FACE_SHIFT_Y).coerceIn(EYE_TOP, EYE_BOTTOM)
    val cosR = cos(rot)
    val sinR = sin(rot)

    repeat(FIT_PASSES) {
        var over = 0f
        for (i in 0 until FIT_SAMPLES) {
            val theta = i.toFloat() / FIT_SAMPLES * 2f * PI.toFloat()
            val lx = cos(theta) * halfWidth * scaleX
            val ly = sin(theta) * halfHeight * max(scaleY, FIT_MIN_HEIGHT)
            val px = x + lx * cosR - ly * sinR
            val py = y + lx * sinR + ly * cosR
            over = max(over, abs(px) - (DragonHead.faceHalfWidth(py) - FACE_MARGIN))
        }
        if (over <= 0f) return@repeat
        x -= sign(x) * over
        if (abs(x) < EYE_MIN_X) {
            x = (if (x == 0f) side.toFloat() else sign(x)) * EYE_MIN_X
            y -= over * FIT_LIFT
        }
    }

    return PlacedEye(
        side = side,
        x = x,
        y = y,
        scaleX = scaleX,
        scaleY = scaleY,
        rot = rot,
        halfWidth = halfWidth,
        halfHeight = halfHeight,
        lid = (expression.lid * (1f - side * expression.asym * LID_ASYM)).coerceIn(0f, LID_MAX),
    )
}

private fun DrawScope.drawEyes(
    expression: MascotExpression,
    yaw: Float,
    pitch: Float,
    blink: Float,
    widen: Float,
    eye: Path,
    lidClip: Path,
    cheekClip: Path,
) {
    listOf(-1, 1).forEach { side ->
        val e = placeEye(side, expression, yaw, pitch, blink, widen)
        val ex = e.halfWidth
        val ey = e.halfHeight
        val closed = expression.arc != 0f

        withTransform({
            translate(e.x, e.y)
            rotate(degrees = e.rot * RAD_TO_DEG, pivot = Offset.Zero)
            scale(e.scaleX, e.scaleY, Offset.Zero)
            // From here +x is the inner side of the eye, whichever eye it is.
            scale(if (side > 0) -1f else 1f, 1f, Offset.Zero)
        }) {
            if (closed) {
                // A closed eye: n when smiling, u when serene.
                val smiling = expression.arc > 0f
                drawArc(
                    color = Color.Black,
                    startAngle = if (smiling) ARC_UP_START else ARC_DOWN_START,
                    sweepAngle = ARC_SWEEP,
                    useCenter = false,
                    topLeft =
                        Offset(
                            -ex * ARC_RADIUS,
                            (if (smiling) ey * ARC_OFFSET else -ey * ARC_OFFSET) - ex * ARC_RADIUS,
                        ),
                    size = Size(ex * ARC_RADIUS * 2f, ex * ARC_RADIUS * 2f),
                    style = Stroke(width = ey * ARC_STROKE, cap = StrokeCap.Round),
                    blendMode = BlendMode.Clear,
                )
                return@withTransform
            }

            val paintEye: DrawScope.() -> Unit = {
                eye.rewind()
                // A drop: round and full on the outside, sharp toward the inner side.
                eye.moveTo(ex, 0f)
                eye.cubicTo(ex * .35f, -ey * 1.55f, -ex * .80f, -ey * 1.45f, -ex * .92f, -ey * .10f)
                eye.cubicTo(-ex * 1.02f, ey * .78f, -ex * .50f, ey * 1.45f, ex * .02f, ey * 1.02f)
                eye.cubicTo(ex * .42f, ey * .70f, ex * .78f, ey * .25f, ex, 0f)
                eye.close()
                drawPath(eye, Color.Black, blendMode = BlendMode.Clear)
            }
            val cheeked: DrawScope.() -> Unit =
                if (expression.cheek > CLIP_THRESHOLD) {
                    {
                        // The lower lid rises in the middle: the eye smiles.
                        val lowEnd = ey * 1.2f
                        val low = ey * 1.15f - expression.cheek * ey * 2.3f
                        val reach = ex * 1.4f
                        cheekClip.rewind()
                        cheekClip.moveTo(-reach, -ey * 3f)
                        cheekClip.lineTo(reach, -ey * 3f)
                        cheekClip.lineTo(reach, lowEnd)
                        cheekClip.quadraticTo(0f, 2f * low - lowEnd, -reach, lowEnd)
                        cheekClip.close()
                        clipPath(cheekClip) { paintEye() }
                    }
                } else {
                    paintEye
                }

            if (e.lid > CLIP_THRESHOLD) {
                // The lid is a straight cut through the hole; lidTilt raises the inner corner.
                val cut = -ey * 1.15f + e.lid * ey * 2.3f
                val slope = tan(expression.lidTilt)
                val reach = ex * 3f
                lidClip.rewind()
                lidClip.moveTo(-reach, cut + slope * reach)
                lidClip.lineTo(reach, cut - slope * reach)
                lidClip.lineTo(reach, ey * 3f)
                lidClip.lineTo(-reach, ey * 3f)
                lidClip.close()
                clipPath(lidClip) { cheeked() }
            } else {
                cheeked()
            }
        }
    }
}

/** A hollow triangle pointing down, on the same plane as the eyes but nearer, so it travels further. */
private fun DrawScope.drawNose(
    nose: Path,
    expression: MascotExpression,
    yaw: Float,
    pitch: Float,
    beat: Float,
) {
    val twitch = if (expression.jit > 0f) sin(beat * NOSE_TWITCH_SPEED) * NOSE_TWITCH * expression.jit else 0f
    val halfWidth = NOSE_HALF_WIDTH * expression.noseS * (1f - NOSE_TURN_NARROWING * abs(yaw))
    val height = NOSE_HEIGHT * expression.noseS
    val y = NOSE_LEVEL + expression.noseDy + pitch * FACE_SHIFT_Y * NOSE_TRAVEL_Y + twitch
    // The base of the nose has to fit inside the face.
    val reach = max(0f, DragonHead.faceHalfWidth(y + height) - halfWidth - NOSE_MARGIN)
    val x = (yaw * FACE_SHIFT_X * NOSE_TRAVEL_X).coerceIn(-reach, reach)

    nose.rewind()
    nose.moveTo(x - halfWidth, y)
    nose.lineTo(x + halfWidth, y)
    nose.lineTo(x, y + height)
    nose.close()
    drawPath(nose, Color.Black, blendMode = BlendMode.Clear)
    drawPath(
        nose,
        Color.Black,
        style = Stroke(width = NOSE_ROUNDING, join = StrokeJoin.Round),
        blendMode = BlendMode.Clear,
    )
}

/**
 * Looks around on its own: every few beats it turns to a new side, as if a pointer were being swept
 * from one edge to the other. Sides alternate (left, right, back to the middle) so it never stares
 * at one corner, and the tone's bias and `glance` decide how far it really goes.
 *
 * A pure function of [beat], so there is no state to keep: each slot hashes to its own target and
 * the move interpolates from the previous slot's. [travel] is how long the move takes and [delay] how
 * late it starts; [gliding] eases in and out (a head turning) instead of darting off and settling
 * (eyes).
 */
private fun MascotExpression.idleLook(
    beat: Float,
    travel: Float,
    delay: Float,
    gliding: Boolean,
): Offset {
    val slot = floor(beat / GLANCE_EVERY).toInt()
    val progress = ((beat - slot * GLANCE_EVERY - delay) / travel).coerceIn(0f, 1f)
    val eased = if (gliding) smoothstep(progress) else 1f - (1f - progress).pow(3)
    val from = glanceTarget(slot - 1)
    val to = glanceTarget(slot)
    return Offset(
        (gx + (from.x + (to.x - from.x) * eased) * glance).coerceIn(-1f, 1f),
        (gy + (from.y + (to.y - from.y) * eased) * glance).coerceIn(-1f, 1f),
    )
}

// Every fourth look comes back to the middle; the rest go all the way to one side, alternating.
private fun glanceTarget(slot: Int): Offset {
    if (slot % 4 == 0) return Offset(0f, (hash01(slot, 2f) * 2f - 1f) * GLANCE_Y * .4f)
    val side = if (slot % 2 == 0) -1f else 1f
    return Offset(
        side * (GLANCE_X_MIN + hash01(slot, 1f) * (1f - GLANCE_X_MIN)),
        (hash01(slot, 2f) * 2f - 1f) * GLANCE_Y,
    )
}

/**
 * One blink per slot, at an offset within it that the slot picks for itself — so the gap between
 * blinks wanders between about 2.6 and 5.8 beats instead of ticking every 4.6.
 */
private fun blinkAt(beat: Float): Float {
    val slot = floor(beat / BLINK_SLOT).toInt()
    val since = beat - (slot * BLINK_SLOT + hash01(slot, 3f) * BLINK_JITTER)
    if (since < 0f || since > BLINK_LENGTH) return 1f
    return max(BLINK_MIN, abs(cos(since / BLINK_LENGTH * PI.toFloat())))
}

/** Cheap deterministic noise in `0f..1f`; the stock shader trick, with no allocation. */
private fun hash01(
    n: Int,
    salt: Float,
): Float {
    val v = sin(n * 12.9898f + salt * 78.233f) * 43758.547f
    return v - floor(v)
}

private fun smoothstep(x: Float): Float {
    val t = x.coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private const val RAD_TO_DEG = (180.0 / PI).toFloat()

// Head units: half of the head's width is 1, eye level is 0, y grows down.
private const val EYE_HALF_WIDTH = .262f
private const val EYE_HALF_HEIGHT = .1285f
private const val EYE_GAP = .4235f
private const val EYE_LEVEL = .02f
private const val EYE_TOP = -.16f
private const val EYE_BOTTOM = .30f

// How far the gaze (-1..1) moves the face on the head, and the head on the canvas. Small on purpose.
private const val FACE_SHIFT_X = .20f
private const val FACE_SHIFT_Y = .10f
private const val HEAD_SHIFT_X = .05f
private const val HEAD_SHIFT_Y = .03f
private const val HEAD_ROLL = .05f
private const val FAR_EYE_NARROWING = .28f
private const val FAR_EYE_MIN = .6f
private const val FAR_EYE_MAX = 1.2f

// Keeping the eye inside the head.
private const val FACE_MARGIN = .07f
private const val EYE_MIN_X = .16f
private const val FIT_PASSES = 3
private const val FIT_SAMPLES = 12
private const val FIT_MIN_HEIGHT = .5f
private const val FIT_LIFT = .8f

private const val LID_ASYM = 1.2f
private const val LID_MAX = .72f
private const val CLIP_THRESHOLD = .01f

private const val ARC_RADIUS = .85f
private const val ARC_OFFSET = .95f
private const val ARC_STROKE = 1.15f
private const val ARC_UP_START = 208.8f
private const val ARC_DOWN_START = 28.8f
private const val ARC_SWEEP = 122.4f

private const val NOSE_HALF_WIDTH = .085f
private const val NOSE_HEIGHT = .15f
private const val NOSE_LEVEL = .27f
private const val NOSE_MARGIN = .10f
private const val NOSE_ROUNDING = .014f
private const val NOSE_TURN_NARROWING = .18f
private const val NOSE_TRAVEL_X = 1.5f
private const val NOSE_TRAVEL_Y = 1.2f
private const val NOSE_TWITCH_SPEED = 31f
private const val NOSE_TWITCH = .007f

// The dragon breathes, bobs and shakes around its centre, so it cannot be drawn edge to edge —
// without this headroom the motion pushes it past the canvas and the silhouette clips flat.
private const val FIT = .86f

private const val BREATH_SPEED = 1.5f
private const val BREATH_AMOUNT = 0.016f
private const val BOB_SPEED = 0.9f
private const val BOB = 0.030f
private const val SHAKE_X_SPEED = 24f
private const val SHAKE_Y_SPEED = 19f
private const val SHAKE_X = 0.012f
private const val SHAKE_Y = 0.008f

// Damping 0.5 rings once and lands ~16% past target: inside the 15-25% band an elastic body wants.
private const val ENTRANCE_DAMPING = 0.5f
private const val ENTRANCE_FROM = 0.55f
private const val ENTRANCE_DROP = 0.35f
private const val LANDING_SQUASH = 0.25f
private const val FADE_IN_BY = 0.4f

// Eyes start opening once the body is halfway there, and are fully open before it settles.
private const val EYES_AFTER = 0.5f
private const val EYES_SPAN = 0.4f

// Lower damping than the entrance: a poke should wobble, and the negative swing is the rebound.
private const val POKE_DAMPING = 0.35f
private const val POKE_X = 0.12f
private const val POKE_Y = 0.20f
private const val POKE_DROP = 0.08f
private const val POKE_EYES = 0.3f

private const val GLANCE_EVERY = 3.4f
private const val GLANCE_X_MIN = 0.75f
private const val GLANCE_Y = 0.55f
private const val EYES_TRAVEL = 0.30f
private const val HEAD_TRAVEL = 1.0f
private const val HEAD_LAG = 0.15f

private const val BLINK_SLOT = 4.2f
private const val BLINK_JITTER = 1.6f
private const val BLINK_LENGTH = 0.14f
private const val BLINK_MIN = 0.04f
