package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/**
 * The cast, sent as a "shared group link" card — collapsed to an overlapping avatar stack + count,
 * like forwarding a group chat link instead of a full roster page. Tapping expands it inline into
 * the actual list: name + message count per character, same data
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookCharactersPage] shows.
 */
class CrimeContactCardMessagePage(
    override val content: SagaContent,
    private val topCharacters: List<Pair<Character, Int>>,
    private val isMe: Boolean,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.CHARACTERS

    /** No typing to wait for, just its own pop-in. */
    override val estimatedRevealDurationMs: Long = 500L

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary
        var expanded by remember { mutableStateOf(false) }
        val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f)

        CrimeBubbleFrame(
            isMe = isMe,
            genre = genre,
            useSpeechShape = false,
            canAnimate = canAnimate,
            modifier = modifier,
        ) { ink ->
            Column(
                Modifier
                    .width(220.dp)
                    .clickable { expanded = !expanded },
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row {
                        topCharacters.take(3).forEachIndexed { index, (character, _) ->
                            CharacterAvatar(
                                character,
                                genre = genre,
                                borderColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                borderSize = 2.dp,
                                modifier =
                                    Modifier
                                        .size(32.dp)
                                        .offset(x = (-8 * index).dp),
                            )
                        }
                    }

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.review_stage_characters_title),
                            fontWeight = FontWeight.Bold,
                            color = ink,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = stringResource(R.string.saga_detail_section_subtitle_characters, topCharacters.size),
                            fontStyle = FontStyle.Italic,
                            color = accent,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.round_arrow_forward_ios_24),
                        contentDescription = null,
                        tint = ink.copy(alpha = 0.6f),
                        modifier = Modifier.size(10.dp).rotate(rotation),
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalDivider(color = ink.copy(alpha = 0.15f))

                        topCharacters.forEach { (character, messageCount) ->
                            CrimeContactRow(
                                character = character,
                                genre = genre,
                                subtitle = stringResource(R.string.messages_count_label, messageCount),
                                ink = ink,
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(if (expanded) R.string.review_tap_to_collapse else R.string.review_tap_to_expand),
                    color = ink.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CrimeBackground(modifier)
    }
}
