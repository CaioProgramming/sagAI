package com.ilustris.sagai.ui.components.island

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.AnimatedEmotionalShape
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.ui.theme.themePainter

/**
 * Lean expanded body for dynamic milestone islands — shows dynamic stats (characters, wikis, arcs)
 * and emotional tone indicator using the animated star shape. Replaces the old dashboard-item
 * grid with compact badge summaries of what the player generated.
 */
@Composable
fun MilestoneIslandBody(
    genre: Genre,
    title: String,
    description: String?,
    stats: List<String>,
    emotionalTone: EmotionalTone,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    onRevealStarted: () -> Unit = {},
) {
    LaunchedEffect(Unit) { onRevealStarted() }

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header: icon + title + description
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                themePainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Dynamic stat badges
        if (stats.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                stats.forEach { stat ->
                    StatBadge(text = stat)
                }
            }
        }

        // Emotional tone indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Animated emotional shape
            AnimatedEmotionalShape(
                emotionalTone = emotionalTone,
                morphProgress = 1f,
                rotationAngle = 0f,
                backgroundBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(emotionalTone.color, emotionalTone.color.copy(alpha = 0.7f)),
                ),
                outlineBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(emotionalTone.color, emotionalTone.color.copy(alpha = 0.7f)),
                ),
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = emotionalTone.getTitle(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Continue button
        Button(
            onClick = onContinue,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.continue_text))
        }
    }
}

@Composable
private fun StatBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                ).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
