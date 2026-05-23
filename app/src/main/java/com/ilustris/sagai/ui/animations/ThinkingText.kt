package com.ilustris.sagai.ui.animations

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * Text composable with interactive star reveal animation for thought tags
 *
 * When [animate] is true:
 * - Displays twinkling stars overlay hiding the text
 * - User can tap to reveal: stars fade out, text fades in
 * - Uses genre color for both stars and text
 *
 * When [animate] is false:
 * - Shows static revealed text (italic, genre color)
 *
 * @param text The thought content to display
 * @param style Base text style
 * @param genre Genre for color theming
 * @param animate Whether to enable interactive stars (only true for last message)
 * @param onRevealed Callback when thought is revealed (for analytics)
 * @param modifier Optional modifier
 */
@Composable
fun ThinkingText(
    text: String,
    style: TextStyle,
    genre: Genre,
    modifier: Modifier = Modifier,
    animate: Boolean = false,
    onRevealed: () -> Unit = {},
) {
    val thinkStyle =
        style.copy(
            fontStyle = FontStyle.Italic,
            color = style.color.copy(alpha = 0.5f),
        )

    if (animate) {
        var starAlpha by remember { mutableFloatStateOf(.5f) }
        var textAlpha by remember { mutableFloatStateOf(0f) }

        val alphaAnimation by animateFloatAsState(
            targetValue = starAlpha,
            animationSpec =
                tween(
                    1000,
                    easing = FastOutSlowInEasing,
                ),
            label = "starAlpha",
        )

        val textAlphaAnimation by animateFloatAsState(
            targetValue = textAlpha,
            animationSpec =
                tween(
                    1000,
                    easing = FastOutSlowInEasing,
                ),
            label = "textAlpha",
        )

        val bubbleShape = RoundedCornerShape(12.dp)

        Box(modifier = modifier) {
            // Text underneath
            Text(
                text = text,
                style = thinkStyle,
                modifier =
                    Modifier
                        .alpha(textAlphaAnimation),
            )

            if (starAlpha > 0.01f) {
                StarryTextPlaceholder(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .alpha(alphaAnimation)
                            .clip(bubbleShape)
                            .clickable {
                                starAlpha = 0f
                                textAlpha = 1f
                                onRevealed()
                            },
                    starColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = starAlpha),
                    starCount = text.length,
                )
            }
        }
    } else {
        // Static version for old messages (already revealed)
        Text(
            text = text,
            style = thinkStyle,
            modifier = modifier,
        )
    }
}
