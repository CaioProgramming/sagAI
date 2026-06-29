package com.ilustris.sagai.ui.animations

import android.graphics.BlurMaskFilter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

fun DrawScope.draw4PointCosmicStar(
    center: Offset,
    size: Float,
    color: Color,
    glowAlpha: Float = 0.75f,
    rotationDegrees: Float = 0f,
    glowColor: Color? = null,
    glowBlurFactor: Float = 1.8f,
    glowSpreadFactor: Float = 1.35f,
) {
    if (size <= 0f) return

    val halo = glowColor ?: color

    drawIntoCanvas { canvas ->
        val glowPaint =
            android.graphics.Paint().apply {
                isAntiAlias = true
                this.color = halo.toArgb()
                alpha = (halo.alpha * glowAlpha * 255).toInt().coerceIn(0, 255)
                maskFilter = BlurMaskFilter(size * glowBlurFactor, BlurMaskFilter.Blur.NORMAL)
            }
        canvas.nativeCanvas.drawCircle(center.x, center.y, size * glowSpreadFactor, glowPaint)
    }

    rotate(degrees = rotationDegrees, pivot = center) {
        val sharpPath =
            Path().apply {
                val innerRadius = size * 0.2f
                moveTo(center.x, center.y - size)
                lineTo(center.x + innerRadius, center.y - innerRadius)
                lineTo(center.x + size, center.y)
                lineTo(center.x + innerRadius, center.y + innerRadius)
                lineTo(center.x, center.y + size)
                lineTo(center.x - innerRadius, center.y + innerRadius)
                lineTo(center.x - size, center.y)
                lineTo(center.x - innerRadius, center.y - innerRadius)
                close()
            }
        drawPath(sharpPath, color)
    }
}
