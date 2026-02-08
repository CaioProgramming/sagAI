package com.ilustris.sagai.ui.animations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import kotlin.random.Random

@Composable
fun Modifier.glitch(
    isPlaying: Boolean = true,
    glitchFrequency: Float = 0.05f,
): Modifier =
    composed {
        if (!isPlaying) return@composed this
        val infiniteTransition = rememberInfiniteTransition(label = "glitch")
        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(150, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "glitchTicker",
        )

        val isGlitching = remember(ticker) { Random.nextFloat() < glitchFrequency }
        val offsetX = remember(ticker) { if (isGlitching) (Random.nextFloat() - 0.5f) * 20f else 0f }
        val offsetY = remember(ticker) { if (isGlitching) (Random.nextFloat() - 0.5f) * 5f else 0f }

        this
            .graphicsLayer {
                translationX = offsetX
                translationY = offsetY
                alpha = if (isGlitching && Random.nextFloat() < 0.3f) 0.5f else 1f
            }
    }

/**
 * A specialized Glitch effect for drawText operations
 */
fun drawGlitchText(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    textLayoutResult: TextLayoutResult,
    color: Color,
    glitchFrequency: Float = 0.2f,
    offset: androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset.Zero,
) {
    with(drawScope) {
        val shouldGlitch = Random.nextFloat() < glitchFrequency

        if (shouldGlitch) {
            val glitchX = (Random.nextFloat() - 0.5f) * 15f
            val glitchY = (Random.nextFloat() - 0.5f) * 5f

            // Draw red channel
            drawText(
                textLayoutResult = textLayoutResult,
                color = Color.Red.copy(alpha = 0.5f),
                topLeft =
                    offset +
                        androidx.compose.ui.geometry
                            .Offset(glitchX, glitchY),
                blendMode = BlendMode.Screen,
            )

            // Draw blue channel
            drawText(
                textLayoutResult = textLayoutResult,
                color = Color.Cyan.copy(alpha = 0.5f),
                topLeft =
                    offset +
                        androidx.compose.ui.geometry
                            .Offset(-glitchX, -glitchY),
                blendMode = BlendMode.Screen,
            )
        }

        // Draw main text
        drawText(
            textLayoutResult = textLayoutResult,
            color = color,
            topLeft = offset,
        )
    }
}
