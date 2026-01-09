package com.ilustris.sagai.ui.animations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.ilustris.moterolib.ui.theme.MaterialColor

/**
 * Text composable with levitation and flicker animation for action tags
 *
 * When [animate] is true:
 * - Levitates up and down (2dp sine wave, 1000ms cycle)
 * - Flickers between 85% and 100% alpha (800ms cycle)
 *
 * When [animate] is false:
 * - Shows static styled text (bold italic, amber color)
 *
 * @param text The text content to display
 * @param style Base text style
 * @param animate Whether to enable animations (only true for last message)
 * @param modifier Optional modifier
 */
@Composable
fun LevitatingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    animate: Boolean = false
) {
    val actionStyle = style.copy(
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        color = MaterialColor.Amber400
    )

    if (animate) {
        // Create infinite animation transition
        val infiniteTransition = rememberInfiniteTransition(label = "levitate")

        // Levitation animation: 0f to -4f (upward movement)
        val translationY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "translationY"
        )

        // Flicker animation: 0.85f to 1.0f alpha
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Text(
            text = text,
            style = actionStyle,
            modifier = modifier.graphicsLayer {
                this.translationY = translationY
                this.alpha = alpha
            }
        )
    } else {
        // Static version for old messages
        Text(
            text = text,
            style = actionStyle,
            modifier = modifier
        )
    }
}

