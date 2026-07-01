package com.ilustris.sagai.ui.animations

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.random.Random

private const val MAX_STAR_COUNT = 60

// Data class to hold star properties
private data class Star(
    var x: Float,
    var y: Float,
    var alpha: Float,
    var initialDelay: Long = Random.nextLong(0, 1000), // Stagger appearance
    val baseSize: Float = Random.nextFloat() * 2.5f + 1.5f, // Random base sizes (1.5 to 4.0)
    val breathingRate: Float = Random.nextFloat() * 0.004f + 0.002f, // Different breathing speeds
    var currentScale: Float = 1f,
)

@Composable
fun StarryTextPlaceholder(
    modifier: Modifier = Modifier,
    starColor: Color = Color.White,
    starCount: Int = Genre.entries.size * 5,
    twinkleDurationMillis: Int = 1500,
) {
    val animationsActive = rememberLifecycleAnimationsActive()

    Box(modifier = modifier) {
        if (animationsActive) {
            StarryCanvas(
                starColor = starColor,
                starCount = starCount.coerceIn(1, MAX_STAR_COUNT),
                twinkleDurationMillis = twinkleDurationMillis,
            )
        }
    }
}

@Composable
private fun StarryCanvas(
    starColor: Color,
    starCount: Int,
    twinkleDurationMillis: Int,
) {
    val stars = remember { mutableStateListOf<Star>() }
    val starPath = remember { Path() }
    val glowPaint =
        remember {
            android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
        }
    val glowBlurFilters = remember { mutableMapOf<Int, BlurMaskFilter>() }

    val infiniteTransition = rememberInfiniteTransition(label = "starry_sky_transition")
    val animationTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = twinkleDurationMillis * 2,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Restart,
            ),
        label = "star_trigger",
    )

    @Suppress("UNUSED_VARIABLE")
    val animationFrame = animationTrigger

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (stars.isEmpty() && size.width > 0 && size.height > 0) {
            repeat(starCount) {
                stars.add(
                    Star(
                        x = Random.nextFloat() * size.width,
                        y = Random.nextFloat() * size.height,
                        alpha = 0f,
                    ),
                )
            }
        }

        val currentTime = System.currentTimeMillis()
        stars.forEach { star ->
            val elapsed = currentTime - star.initialDelay
            val progressInCycle =
                (elapsed % twinkleDurationMillis).toFloat() / twinkleDurationMillis

            star.alpha =
                if (progressInCycle < 0.5f) {
                    progressInCycle * 2f
                } else {
                    (1f - progressInCycle) * 2f
                }
            star.alpha = star.alpha.coerceIn(0f, 1f)

            star.currentScale =
                1f + (kotlin.math.sin(currentTime * star.breathingRate) * 0.3f).toFloat()

            if (star.alpha <= 0.01f && Random.nextFloat() > 0.7f) {
                star.x = Random.nextFloat() * size.width
                star.y = Random.nextFloat() * size.height
                star.initialDelay =
                    currentTime + Random.nextLong(0, (twinkleDurationMillis * 0.5).toLong())
            }

            drawStar(star, starColor, starPath, glowPaint, glowBlurFilters)
        }
    }
}

private fun Path.setSharpStar(
    center: Offset,
    size: Float,
) {
    rewind()
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

private fun DrawScope.drawStar(
    star: Star,
    color: Color,
    starPath: Path,
    glowPaint: android.graphics.Paint,
    glowBlurFilters: MutableMap<Int, BlurMaskFilter>,
) {
    val dynamicSize = (star.baseSize * star.currentScale * (0.5f + star.alpha * 0.5f)).dp.toPx()
    if (dynamicSize <= 0f || star.alpha <= 0.01f) return

    val center = Offset(star.x, star.y)
    val alpha = star.alpha

    drawIntoCanvas { canvas ->
        val blurRadius = dynamicSize * 1.5f
        val blurKey = (blurRadius * 4f).toInt()
        val blurFilter =
            glowBlurFilters.getOrPut(blurKey) {
                BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }
        glowPaint.color = color.toArgb()
        glowPaint.alpha = (alpha * 0.6f * 255f).toInt().coerceIn(0, 255)
        glowPaint.maskFilter = blurFilter
        canvas.nativeCanvas.drawCircle(center.x, center.y, dynamicSize * 1.2f, glowPaint)
    }

    starPath.setSharpStar(center, dynamicSize)
    drawPath(starPath, color.copy(alpha = alpha))
}

@Preview(showBackground = true, backgroundColor = 0xFF0000FF)
@Composable
private fun StarryTextPlaceholderPreview() {
    Box(modifier = Modifier.size(width = 200.dp, height = 50.dp)) {
        StarryTextPlaceholder(starCount = 50)
    }
}
