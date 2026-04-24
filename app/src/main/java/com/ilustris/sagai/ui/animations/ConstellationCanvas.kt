package com.ilustris.sagai.ui.animations

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class ConstellationStar(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float,
)

@Composable
fun ConstellationCanvas(
    modifier: Modifier = Modifier,
    starColor: Color = Color.White.copy(alpha = 0.6f),
    lineColor: Color = Color.White.copy(alpha = 0.2f),
    starCount: Int = 60,
    maxLineDistance: Float = 250f,
) {
    val stars = remember { mutableStateListOf<ConstellationStar>() }
    val infiniteTransition = rememberInfiniteTransition(label = "constellation")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "phase",
    )

    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "twinkle",
    )

    val lineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(10000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "lineProgress",
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (stars.isEmpty()) {
                repeat(starCount) {
                    stars.add(
                        ConstellationStar(
                            x = Random.nextFloat() * size.width,
                            y = Random.nextFloat() * size.height,
                            size = Random.nextFloat() * 2f + 1f,
                            alpha = Random.nextFloat() * 0.6f + 0.2f,
                            speed = Random.nextFloat() * 0.03f + 0.005f,
                        ),
                    )
                }
            }

            stars.forEachIndexed { index, star ->
                val currentX = (star.x + phase * size.width * star.speed) % size.width
                val currentY = star.y

                val starAlpha = (star.alpha * twinkle).coerceIn(0f, 1f)

                // Glow effect
                drawCircle(
                    color = starColor.copy(alpha = starAlpha * 0.3f),
                    radius = star.size * 4f,
                    center = Offset(currentX, currentY),
                )

                drawCircle(
                    color = starColor.copy(alpha = starAlpha),
                    radius = star.size,
                    center = Offset(currentX, currentY),
                )

                // Draw lines to nearby stars (only if lineProgress is growing)
                if (lineProgress > 0.1f) {
                    for (j in index + 1 until stars.size) {
                        val otherStar = stars[j]
                        val otherX =
                            (otherStar.x + phase * size.width * otherStar.speed) % size.width
                        val otherY = otherStar.y

                        val distance =
                            kotlin.math.sqrt(
                                (currentX - otherX) * (currentX - otherX) +
                                    (currentY - otherY) * (currentY - otherY),
                            )

                        if (distance < maxLineDistance) {
                            val lineAlpha = (1f - distance / maxLineDistance) * lineProgress * 0.3f
                            if (lineAlpha > 0.05f) {
                                drawLine(
                                    color = lineColor.copy(alpha = lineAlpha),
                                    start = Offset(currentX, currentY),
                                    end = Offset(otherX, otherY),
                                    strokeWidth = 1.5f,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
