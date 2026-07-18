package com.ilustris.sagai.ui.components.island

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.AnimatedEmotionalShape
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.themePainter

/**
 * Lean expanded body for dynamic milestone islands — shows visual galleries of created
 * characters and wikis, plus emotional tone indicator. Replaces the old dashboard-item
 * grid with compact visual references of what the player generated.
 */
@Composable
fun MilestoneIslandBody(
    genre: Genre,
    title: String,
    description: String?,
    characters: List<Character>,
    wikis: List<Wiki>,
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

        // Characters gallery
        if (characters.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Personagens Criados",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    characters.forEach { character ->
                        CharacterAvatar(
                            character = character,
                            genre = genre,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
            }
        }

        // Wikis gallery
        if (wikis.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Wikis Criadas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    wikis.forEach { wiki ->
                        WikiBadge(wiki = wiki)
                    }
                }
            }
        }

        // Emotional tone indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
private fun WikiBadge(
    wiki: Wiki,
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
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = wiki.emojiTag ?: "📖",
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = wiki.title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
