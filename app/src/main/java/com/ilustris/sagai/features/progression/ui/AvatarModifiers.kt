package com.ilustris.sagai.features.progression.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/** Washes the colors out of what it draws, for a memorial photo. */
fun Modifier.desaturated(saturation: Float = .15f): Modifier =
    drawWithContent {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply { colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturation) }) }
            val bounds = Rect(Offset.Zero, size)
            canvas.saveLayer(bounds, paint)
            drawContent()
            canvas.restore()
        }
    }

/**
 * The ring a legacy lights around an avatar: a soft gold (or silver) band and, about every ten seconds,
 * a short bright arc that runs once around it.
 */
@Composable
fun Modifier.legacyRing(
    top: Color,
    bottom: Color,
    shimmer: Boolean,
): Modifier {
    val transition = rememberInfiniteTransition(label = "legacyRing")
    val angle by transition.animateFloat(
        0f,
        360f,
        infiniteRepeatable(keyframes { durationMillis = 10_000; 0f at 0; 360f at 1_600 using LinearEasing; 360f at 10_000 }),
        label = "ringPass",
    )
    return drawWithContent {
        val stroke = 3.dp.toPx()
        val radius = size.minDimension / 2f - stroke / 2f
        drawCircle(Brush.verticalGradient(listOf(top, bottom)), radius, style = Stroke(stroke))
        if (shimmer && angle < 360f) {
            rotate(angle) {
                drawArc(
                    Color.White.copy(alpha = .95f),
                    startAngle = -100f,
                    sweepAngle = 40f,
                    useCenter = false,
                    topLeft = Offset(stroke / 2f, stroke / 2f),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke),
                )
            }
        }
        drawContent()
    }
}
