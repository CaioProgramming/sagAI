package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.sagaShape

/**
 * The creation flow's action button: dark glass with the theme's color reflected from its bottom
 * edge, the same look as the suggestion chips but louder, so it still reads as the thing to press.
 * The rim is light at the top and the theme's color at the bottom, and a soft glow sits under it.
 * Disabled, the reflection and the glow fade out and the label dims: a flat pane, nothing to press.
 */
@Composable
internal fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onBackground = MaterialTheme.colorScheme.onBackground
    val shape = sagaShape()
    val reflection by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = tween(300),
        label = "glass_button_reflection",
    )

    Box(
        modifier =
            modifier
                .heightIn(min = 52.dp)
                .dropShadow(shape) {
                    color = primary
                    radius = 24f
                    spread = 0f
                    alpha = 0.22f * reflection
                }.border(
                    1.dp,
                    Brush.verticalGradient(
                        0f to onBackground.copy(alpha = 0.28f),
                        0.5f to onBackground.copy(alpha = 0.08f),
                        1f to primary.copy(alpha = 0.7f * reflection),
                    ),
                    shape,
                ).clip(shape)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
                .background(
                    Brush.verticalGradient(
                        0.3f to Color.Transparent,
                        1f to primary.copy(alpha = 0.55f * reflection),
                    ),
                ).clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = onBackground.copy(alpha = if (enabled) 1f else 0.4f),
        )
    }
}
