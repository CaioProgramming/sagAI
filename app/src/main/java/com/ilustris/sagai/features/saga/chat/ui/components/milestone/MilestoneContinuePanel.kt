package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.themeBrushColors
import com.ilustris.sagai.ui.theme.themePainter

/**
 * Chat-anchored panel shown when a narrative milestone (new event, chapter or act finished)
 * needs the player's confirmation to continue — replaces [ChatInputView] in the same slot
 * instead of duplicating the reveal in a floating island, since the milestone's own card
 * already renders inline in the message list.
 */
@Composable
fun MilestoneContinuePanel(
    genre: Genre,
    title: String,
    description: String?,
    characters: List<Character>,
    wikis: List<Wiki>,
    emotionalTone: EmotionalTone,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderBrush = Brush.horizontalGradient(themeBrushColors())
    val shape = sagaShape()

    Column(
        modifier =
            modifier
                .padding(16.dp)
                .fillMaxWidth()
                .border(1.dp, borderBrush, shape)
                .background(MaterialTheme.colorScheme.background, shape)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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

        if (characters.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.milestone_characters_created),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier =
                        Modifier
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

        if (wikis.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.milestone_wikis_created),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier =
                        Modifier
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedEmotionalShape(
                emotionalTone = emotionalTone,
                morphProgress = 1f,
                rotationAngle = 0f,
                backgroundBrush =
                    Brush.linearGradient(
                        listOf(emotionalTone.color, emotionalTone.color.copy(alpha = 0.7f)),
                    ),
                outlineBrush =
                    Brush.linearGradient(
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
