package com.ilustris.sagai.ui.components.island

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.globalshell.GlobalShellEffect
import com.ilustris.sagai.core.globalshell.NewChapterEffect
import com.ilustris.sagai.core.globalshell.NewCharacterEffect
import com.ilustris.sagai.core.globalshell.NewMessageEffect
import com.ilustris.sagai.core.globalshell.ReviewReadyEffect
import com.ilustris.sagai.features.saga.chat.ui.components.ExpressiveText
import com.ilustris.sagai.ui.theme.SagAITheme

/**
 * Top-island content for a [GlobalShellEffect] notification (new message, chapter, character,
 * review, book). Compact is a small labeled pill; expanded shows the message and an open action,
 * sized naturally to content.
 */
class NotificationIslandContent(
    private val effect: GlobalShellEffect,
    private val onNavigate: (String) -> Unit,
    private val onDismiss: () -> Unit,
) : IslandContent {
    override val compact: CompactIslandData =
        CompactIslandData(
            label =
                when (effect) {
                    is NewMessageEffect -> effect.speakerName
                    else -> effect.message
                },
            iconRes = effect.genre.icon,
            genre = effect.genre,
        )

    @Composable
    override fun Expanded(scope: IslandScope) {
        SagAITheme(effect.genre) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(labelResFor(effect)),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        ),
                )

                when (effect) {
                    is NewMessageEffect ->
                        ExpressiveText(
                            text = effect.rawText,
                            genre = effect.genre,
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start,
                                ),
                            modifier = Modifier,
                            shouldAnimate = false,
                            characters = emptyList(),
                            wiki = emptyList(),
                            mainCharacter = null,
                            onAnnotationClick = { _ -> },
                        )

                    else ->
                        Text(
                            text = effect.message,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                ),
                        )
                }

                Button(
                    onClick = {
                        onNavigate(effect.deepLink)
                        onDismiss()
                    },
                ) {
                    Text(text = stringResource(R.string.notification_open_chat))
                }
            }
        }
    }
}

private fun labelResFor(effect: GlobalShellEffect): Int =
    when (effect) {
        is NewMessageEffect -> R.string.notification_new_message
        is NewChapterEffect -> R.string.notification_new_chapter
        is NewCharacterEffect -> R.string.notification_new_character
        is ReviewReadyEffect -> R.string.notification_review_ready
        else -> R.string.notification_new_message
    }
