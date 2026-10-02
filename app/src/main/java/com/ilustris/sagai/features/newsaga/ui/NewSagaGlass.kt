package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.sagaShape

/**
 * Dark glass lit by [tint], the way iOS lights a glass control: the color is never a fill, only light
 * caught by the edges.
 *  - a thin reflection hugging the bottom edge, with a faint wash rising from it;
 *  - a specular rim that is bright at the top-left corner and takes the tint at the bottom-right.
 * [strength] (0..1) scales only the tinted light, so a disabled control keeps its rim but goes flat.
 * It is read while drawing, so animating it never recomposes.
 */
@Composable
internal fun Modifier.glassSurface(
    shape: Shape,
    tint: Color,
    strength: () -> Float = { 1f },
): Modifier {
    val light = MaterialTheme.colorScheme.onBackground
    val base = MaterialTheme.colorScheme.background
    return this
        .clip(shape)
        .background(base.copy(alpha = 0.5f))
        .drawWithContent {
            val s = strength().coerceIn(0f, 1f)
            val outline = shape.createOutline(size, layoutDirection, this)
            // A faint wash rising from the bottom edge.
            drawOutline(
                outline,
                Brush.verticalGradient(0.6f to Color.Transparent, 1f to tint.copy(alpha = 0.3f * s)),
            )
            drawContent()
            // The reflection itself: a thin band of light just inside the bottom edge. The stroke is
            // centered on the edge and the clip drops its outer half, so only the inner half remains.
            drawOutline(
                outline,
                Brush.verticalGradient(0.55f to Color.Transparent, 1f to tint.copy(alpha = 0.75f * s)),
                style = Stroke(width = 7.dp.toPx()),
            )
            // The specular rim, one dp visible.
            drawOutline(
                outline,
                Brush.linearGradient(
                    colorStops =
                        arrayOf(
                            0f to light.copy(alpha = 0.55f),
                            0.22f to light.copy(alpha = 0.10f),
                            0.6f to light.copy(alpha = 0.04f),
                            1f to tint.copy(alpha = 0.85f * s).compositeOver(light.copy(alpha = 0.12f)),
                        ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
}

/**
 * The creation flow's action button, on [glassSurface], louder than the chips so it still reads as
 * the thing to press. While [isLoading] the label gives way to a spinner and the reflection
 * breathes, so a step that is waiting on the AI shows it right where the player is looking.
 * Disabled, the tinted light fades out and the label dims: a flat pane, nothing to press.
 */
@Composable
internal fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onBackground = MaterialTheme.colorScheme.onBackground
    val shape = sagaShape()
    val lit = enabled || isLoading
    val reflection =
        animateFloatAsState(
            targetValue = if (lit) 1f else 0f,
            animationSpec = tween(300),
            label = "glass_button_reflection",
        )
    val breathing: State<Float>? =
        if (isLoading) {
            rememberInfiniteTransition(label = "glass_button_breathing").animateFloat(
                initialValue = 0.55f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "glass_button_breathing_value",
            )
        } else {
            null
        }
    val strength = { reflection.value * (breathing?.value ?: 1f) }

    Box(
        modifier =
            modifier
                .heightIn(min = 52.dp)
                .semantics { contentDescription = text }
                .dropShadow(shape) {
                    color = primary
                    radius = 24f
                    spread = 0f
                    alpha = 0.22f * strength()
                }.glassSurface(shape, primary, strength)
                .clickable(enabled = enabled && !isLoading, role = Role.Button, onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(targetState = isLoading, label = "glass_button_content") { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = onBackground,
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = onBackground.copy(alpha = if (enabled) 1f else 0.4f),
                )
            }
        }
    }
}
