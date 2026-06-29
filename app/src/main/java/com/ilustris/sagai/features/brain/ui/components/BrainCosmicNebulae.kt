package com.ilustris.sagai.features.brain.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.sin
import kotlin.random.Random

internal data class BrainNebulaSpec(
    val offsetX: Float,
    val offsetY: Float,
    val radius: Float,
    val color: Color,
    val baseAlpha: Float,
    val phase: Float,
)

internal fun generateBrainNebulae(
    seed: Int,
    colors: List<Color>,
): List<BrainNebulaSpec> {
    if (colors.isEmpty()) return emptyList()
    val random = Random(seed)
    return List(5) {
        BrainNebulaSpec(
            offsetX = random.nextFloat() * 720f - 360f,
            offsetY = random.nextFloat() * 720f - 360f,
            radius = random.nextFloat() * 110f + 85f,
            color = colors[random.nextInt(colors.size)],
            baseAlpha = random.nextFloat() * 0.022f + 0.028f,
            phase = random.nextFloat() * 6.28f,
        )
    }
}

internal fun DrawScope.drawCosmicNebulae(
    nebulae: List<BrainNebulaSpec>,
    centerX: Float,
    centerY: Float,
    breathePhase: Float,
) {
    nebulae.forEach { nebula ->
        val alphaPulse = 0.86f + 0.14f * sin(nebula.phase + breathePhase * 6.28f)
        val alpha = (nebula.baseAlpha * alphaPulse).coerceIn(0f, 0.09f)
        drawNebulaBlob(
            center = Offset(centerX + nebula.offsetX, centerY + nebula.offsetY),
            radius = nebula.radius,
            color = nebula.color,
            alpha = alpha,
        )
    }
}

private fun DrawScope.drawNebulaBlob(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float,
) {
    if (alpha <= 0f) return

    drawIntoCanvas { canvas ->
        val coreArgb = color.copy(alpha = alpha).toArgb()
        val edgeArgb = color.copy(alpha = 0f).toArgb()
        val paint =
            android.graphics.Paint().apply {
                isAntiAlias = true
                shader =
                    RadialGradient(
                        center.x,
                        center.y,
                        radius,
                        intArrayOf(coreArgb, edgeArgb),
                        floatArrayOf(0f, 1f),
                        Shader.TileMode.CLAMP,
                    )
                maskFilter = BlurMaskFilter(radius * 0.5f, BlurMaskFilter.Blur.NORMAL)
            }
        canvas.nativeCanvas.drawCircle(center.x, center.y, radius, paint)

        val haloPaint =
            android.graphics.Paint().apply {
                isAntiAlias = true
                this.color = coreArgb
                this.alpha = (alpha * 0.45f * 255f).toInt().coerceIn(0, 255)
                maskFilter = BlurMaskFilter(radius * 0.85f, BlurMaskFilter.Blur.NORMAL)
            }
        canvas.nativeCanvas.drawCircle(center.x, center.y, radius * 0.65f, haloPaint)
    }
}
