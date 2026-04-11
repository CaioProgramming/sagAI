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
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.random.Random

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
    val stars = remember { mutableStateListOf<Star>() }

    // Used to trigger recomposition for the animation
    val infiniteTransition = rememberInfiniteTransition(label = "starry_sky_transition")
    val animationTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f, // Doesn't really matter, just need it to cycle
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = twinkleDurationMillis * 2, easing = LinearEasing), // Long cycle
                repeatMode = RepeatMode.Restart,
            ),
        label = "star_trigger",
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Initialize stars if the list is empty and we have a size
            if (stars.isEmpty() && size.width > 0 && size.height > 0) {
                for (i in 0 until starCount) {
                    stars.add(
                        Star(
                            x = Random.nextFloat() * size.width,
                            y = Random.nextFloat() * size.height,
                            alpha = 0f, // Start invisible
                        ),
                    )
                }
            }

            // Update and draw stars
            val currentTime = System.currentTimeMillis()
            stars.forEachIndexed { index, star ->
                // Simple twinkling effect: fade in then out
                val elapsed = currentTime - star.initialDelay
                val progressInCycle = (elapsed % twinkleDurationMillis).toFloat() / twinkleDurationMillis

                star.alpha =
                    if (progressInCycle < 0.5f) {
                        progressInCycle * 2f // Fade in
                    } else {
                        (1f - progressInCycle) * 2f // Fade out
                    }
                star.alpha = star.alpha.coerceIn(0f, 1f)

                // Breathing effect: scale oscillates smoothly
                star.currentScale =
                    1f + (kotlin.math.sin(currentTime * star.breathingRate) * 0.3f).toFloat()

                if (star.alpha <= 0.01f && Random.nextFloat() > 0.7f) { // Chance to reposition when invisible and delay next appearance
                    star.x = Random.nextFloat() * size.width
                    star.y = Random.nextFloat() * size.height
                    star.initialDelay =
                        currentTime + Random.nextLong(0, (twinkleDurationMillis * 0.5).toLong())
                }

                drawStar(star, starColor)
            }
            // Trigger recomposition to keep animation running
            // This is just to use animationTrigger, which forces recomposition of the Canvas
            if (animationTrigger > -1f) {
                // Using the trigger to ensure the canvas recomposes
            }
        }
    }
}

private fun DrawScope.drawStar(
    star: Star,
    color: Color,
) {
    // Size is primarily based on random baseSize + sine breath cycle,
    // with a small amount of shrinking tied to the alpha fade.
    val dynamicSize = (star.baseSize * star.currentScale * (0.5f + star.alpha * 0.5f)).dp.toPx()
    val center = Offset(star.x, star.y)

    if (dynamicSize > 0f) {
        // Draw subtle smooth glow
        drawIntoCanvas { canvas ->
            val glowPaint =
                Paint().apply {
                    this.color = color
                    this.alpha = (star.alpha * 0.6f).coerceIn(0f, 1f)
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        style = android.graphics.Paint.Style.FILL
                        maskFilter = BlurMaskFilter(dynamicSize * 1.5f, BlurMaskFilter.Blur.NORMAL)
                    }
                }
            canvas.drawCircle(center, dynamicSize * 1.2f, glowPaint)
        }
    }

    val sharpPath =
        Path().apply {
            val innerRadius = dynamicSize * 0.2f
            moveTo(center.x, center.y - dynamicSize)
            lineTo(center.x + innerRadius, center.y - innerRadius)
            lineTo(center.x + dynamicSize, center.y)
            lineTo(center.x + innerRadius, center.y + innerRadius)
            lineTo(center.x, center.y + dynamicSize)
            lineTo(center.x - innerRadius, center.y + innerRadius)
            lineTo(center.x - dynamicSize, center.y)
            lineTo(center.x - innerRadius, center.y - innerRadius)
            close()
        }

    drawPath(sharpPath, color.copy(alpha = star.alpha))
}

@Preview(showBackground = true, backgroundColor = 0xFF0000FF)
@Composable
private fun StarryTextPlaceholderPreview() {
    Box(modifier = Modifier.size(width = 200.dp, height = 50.dp)) {
        StarryTextPlaceholder(starCount = 50)
    }
}
