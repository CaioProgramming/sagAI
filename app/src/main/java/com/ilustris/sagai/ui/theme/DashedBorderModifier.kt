package com.ilustris.sagai.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

fun Modifier.dashedBorder(
    strokeWidth: Dp,
    color: Color,
    shape: Shape,
    dashLength: Dp,
    gapLength: Dp
): Modifier = composed {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val dashLengthPx = with(density) { dashLength.toPx() }
    val gapLengthPx = with(density) { gapLength.toPx() }

    this.then(
        Modifier.drawWithCache {
            onDrawBehind {
                val outline = shape.createOutline(size, layoutDirection, this)
                if (outline is Outline.Generic) {
                    drawPath(
                        path = outline.path,
                        color = color,
                        style = Stroke(
                            width = strokeWidthPx,
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(dashLengthPx, gapLengthPx),
                                phase = 0f
                            )
                        )
                    )
                }
            }
        }
    )
}
