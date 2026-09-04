package com.ilustris.sagai.ui.theme.components.mascot

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.ilustris.sagai.core.services.model.MascotExpression
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * The blob mascot: one circle and two capsule eyes.
 *
 * Both eyes ride on a sphere rather than sliding on a plane — position comes from
 * `sin(longitude + yaw)` and width from `cos(longitude + yaw)`, so the eye on the far side
 * narrows as the ball turns. That is what makes a flat circle read as a head.
 *
 * Draws nothing when [expression] is null: a tone that is not configured in Remote Config
 * has no mascot, by design.
 *
 * @param look where the mascot is looking, each axis in `-1f..1f`. Null drifts on its own.
 */
@Composable
fun BlobMascot(
    expression: MascotExpression?,
    color: Color,
    eyeColor: Color,
    modifier: Modifier = Modifier,
    look: Offset? = null,
    animate: Boolean = true,
) {
    if (expression == null) return

    val time by produceState(0f, animate) {
        if (!animate) return@produceState
        var origin = 0L
        while (true) {
            withInfiniteAnimationFrameMillis { millis ->
                if (origin == 0L) origin = millis
                value = (millis - origin) / 1000f
            }
        }
    }

    Canvas(modifier) {
        val radius = min(size.width, size.height) / 2f * BODY_INSET
        val beat = time * expression.tempo

        val pulse = sin(beat * BREATH_SPEED) * BREATH_AMOUNT
        val shake = expression.jitter
        val center =
            Offset(
                x = size.width / 2f + sin(beat * SHAKE_X_SPEED) * radius * SHAKE_X * shake,
                y = size.height / 2f +
                    sin(beat * BOB_SPEED) * radius * BOB +
                    cos(beat * SHAKE_Y_SPEED) * radius * SHAKE_Y * shake,
            )

        val gaze = look ?: idleGaze(time)

        withTransform({ scale(1f + pulse, 1f - pulse, center) }) {
            drawCircle(color = color, radius = radius, center = center)
        }
        drawEyes(expression, eyeColor, center, radius, gaze, blinkAt(beat))
    }
}

private fun DrawScope.drawEyes(
    expression: MascotExpression,
    eyeColor: Color,
    center: Offset,
    radius: Float,
    gaze: Offset,
    blink: Float,
) {
    val yaw = gaze.x * LOOK_YAW
    val latitude = BASE_LATITUDE + gaze.y * LOOK_PITCH + expression.dy
    val orbit = radius * ORBIT
    val halfWidth = radius * EYE_HALF_WIDTH
    val halfHeight = radius * EYE_HALF_HEIGHT

    listOf(-1f, 1f).forEach { side ->
        val longitude = side * EYE_GAP + yaw
        val depth = cos(longitude)
        if (depth < HIDDEN_BELOW) return@forEach

        val scaleX = abs(depth).coerceIn(HIDDEN_BELOW, 1f) * expression.w
        val scaleY =
            (cos(latitude) * FORESHORTEN + (1f - FORESHORTEN)) *
                expression.h *
                (1f + side * expression.asym / 2f) *
                blink

        withTransform({
            translate(
                left = center.x + sin(longitude) * cos(latitude) * orbit,
                top = center.y + sin(latitude) * orbit,
            )
            rotate(degrees = Math.toDegrees((-expression.tilt * side).toDouble()).toFloat(), pivot = Offset.Zero)
            scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero)
        }) {
            if (expression.arc && blink > BLINK_ARC_THRESHOLD) {
                val arcRadius = halfHeight * ARC_RADIUS
                drawArc(
                    color = eyeColor,
                    startAngle = ARC_START_DEGREES,
                    sweepAngle = ARC_SWEEP_DEGREES,
                    useCenter = false,
                    topLeft = Offset(-arcRadius, halfHeight * ARC_CENTER_Y - arcRadius),
                    size = Size(arcRadius * 2f, arcRadius * 2f),
                    style = Stroke(width = halfWidth * ARC_STROKE, cap = StrokeCap.Round),
                )
            } else {
                drawRoundRect(
                    color = eyeColor,
                    topLeft = Offset(-halfWidth, -halfHeight),
                    size = Size(halfWidth * 2f, halfHeight * 2f),
                    cornerRadius = CornerRadius(halfWidth, halfWidth),
                )
            }
        }
    }
}

private fun idleGaze(time: Float) =
    Offset(
        x = sin(time * IDLE_X_SPEED) * IDLE_X,
        y = cos(time * IDLE_Y_SPEED) * IDLE_Y,
    )

private fun blinkAt(time: Float): Float {
    val progress = (time % BLINK_CYCLE) / BLINK_CYCLE
    if (progress <= BLINK_START) return 1f
    val phase = (progress - BLINK_START) / (1f - BLINK_START)
    return maxOf(BLINK_MIN, abs(cos(phase * Math.PI.toFloat())))
}

// The ball breathes, bobs and shakes around its centre, so it cannot be drawn edge to edge —
// without this headroom the motion pushes it past the canvas and the silhouette clips flat.
// Worst case is BREATH_AMOUNT + BOB + SHAKE_X ≈ 5.8% of the radius.
private const val BODY_INSET = 0.90f

private const val EYE_HALF_WIDTH = 0.128f
private const val EYE_HALF_HEIGHT = 0.255f
private const val EYE_GAP = 0.40f
private const val ORBIT = 0.52f
private const val BASE_LATITUDE = -0.13f
private const val FORESHORTEN = 0.82f
private const val HIDDEN_BELOW = 0.06f

private const val LOOK_YAW = 0.95f
private const val LOOK_PITCH = 0.42f
private const val IDLE_X_SPEED = 0.34f
private const val IDLE_Y_SPEED = 0.26f
private const val IDLE_X = 0.55f
private const val IDLE_Y = 0.30f

private const val BREATH_SPEED = 1.5f
private const val BREATH_AMOUNT = 0.016f
private const val BOB_SPEED = 0.9f
private const val BOB = 0.030f
private const val SHAKE_X_SPEED = 24f
private const val SHAKE_Y_SPEED = 19f
private const val SHAKE_X = 0.012f
private const val SHAKE_Y = 0.008f

private const val BLINK_CYCLE = 4.6f
private const val BLINK_START = 0.972f
private const val BLINK_MIN = 0.04f
private const val BLINK_ARC_THRESHOLD = 0.6f
private const val ARC_RADIUS = 0.78f
private const val ARC_CENTER_Y = 0.35f
private const val ARC_STROKE = 1.5f
private const val ARC_START_DEGREES = 207f
private const val ARC_SWEEP_DEGREES = 126f
