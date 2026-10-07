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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.glassLight
import com.ilustris.sagai.ui.theme.sagaShape

/**
 * Dark glass lit by [tint], the way iOS lights a glass control: a translucent base with the
 * [glassLight] on top, so the color is never a fill, only light caught by the edges.
 */
@Composable
internal fun Modifier.glassSurface(
    shape: Shape,
    tint: Color,
    strength: () -> Float = { 1f },
): Modifier {
    val base = MaterialTheme.colorScheme.background
    return this
        .clip(shape)
        .background(base.copy(alpha = 0.5f))
        .glassLight(shape, tint, strength)
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
