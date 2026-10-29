package com.ilustris.sagai.features.saga.milestone.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.milestone.presentation.MilestoneUiState
import com.ilustris.sagai.ui.genre.GenreSurfaceStyle
import com.ilustris.sagai.ui.genre.choice.GenreChoiceCard
import com.ilustris.sagai.ui.genre.choice.GenreChoiceCta
import com.ilustris.sagai.ui.genre.surface.GenreStoryAmbientOverlay
import com.ilustris.sagai.ui.genre.surface.GenreStoryBackground
import com.ilustris.sagai.ui.genre.surfaceStyle

/**
 * Forced-choice dilemmas shown before a chapter closes, one at a time. Both options are dealt face
 * down: turning one over reveals it and selects it, and turning the other lets the first fall back,
 * so exactly one card is ever face up. The card up is the answer — Continue confirms it.
 *
 * The player only ever sees the title and the two option texts. The hidden tags are resolved in
 * the ViewModel from the picked indices, so nothing here can render or leak them. Like the rest of
 * the milestone chain it can't be backed out of — [MilestoneScreen] owns that.
 */
@Composable
fun ChapterChoiceCardsScreen(
    state: MilestoneUiState.ChoiceCardsStep,
    genre: Genre?,
    onSelect: (cardIndex: Int, optionIndex: Int) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    val total = state.cards.size
    val index = currentIndex.coerceIn(0, total - 1)
    val isLast = index == total - 1
    val picked = state.selections.getOrNull(index)
    val style = genre?.surfaceStyle() ?: GenreSurfaceStyle.DEFAULT
    val muted = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)

    Box(modifier.fillMaxSize()) {
        GenreStoryBackground(Modifier.fillMaxSize(), genre)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // The screen's own framing — the story's voice setting up why it's asking, once,
            // above whichever card is currently up. Blank on a resumed hand (not persisted), where
            // a static line stands in.
            Text(
                text = state.milestone.screenTitle.takeIf { it.isNotBlank() } ?: stringResource(R.string.milestone_choices_eyebrow),
                style = MaterialTheme.typography.titleMedium.forStyle(style),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = state.milestone.screenSubtitle.takeIf { it.isNotBlank() } ?: stringResource(R.string.milestone_choices_subtitle_fallback),
                style = MaterialTheme.typography.bodyMedium.forStyle(style),
                color = muted,
                modifier = Modifier.fillMaxWidth(),
            )
            LinearProgressIndicator(
                progress = { (index + 1) / total.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.milestone_choices_progress, index + 1, total),
                style = MaterialTheme.typography.labelMedium.forStyle(style),
                color = muted,
            )

            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 12 }) togetherWith fadeOut()
                },
                modifier = Modifier.weight(1f),
                label = "choice_card_pair",
            ) { cardIndex ->
                val card = state.cards[cardIndex]
                val selected = state.selections.getOrNull(cardIndex)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = card.choiceTitle,
                        style = MaterialTheme.typography.titleSmall.forStyle(style),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(R.string.milestone_choices_hint),
                        style = MaterialTheme.typography.labelMedium.forStyle(style),
                        color = muted,
                        textAlign = TextAlign.Center,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        card.options.forEachIndexed { optionIndex, option ->
                            GenreChoiceCard(
                                text = option.text,
                                revealed = selected == optionIndex,
                                onClick = { onSelect(cardIndex, optionIndex) },
                                seed = cardIndex * card.options.size + optionIndex,
                                genre = genre,
                                modifier = Modifier.weight(1f).aspectRatio(CARD_ASPECT),
                            )
                        }
                    }
                }
            }

            GenreChoiceCta(
                label =
                    stringResource(
                        if (isLast) R.string.milestone_choices_submit else R.string.milestone_choices_next,
                    ),
                enabled = picked != null,
                onClick = { if (isLast) onSubmit() else currentIndex = index + 1 },
                genre = genre,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        GenreStoryAmbientOverlay(Modifier.fillMaxSize(), genre)
    }
}

private const val CARD_ASPECT = 0.68f

/** The screen's own copy wears each genre's voice, the way its beats do. */
private fun TextStyle.forStyle(style: GenreSurfaceStyle): TextStyle =
    when (style) {
        GenreSurfaceStyle.TERMINAL -> copy(fontFamily = FontFamily.Monospace)
        GenreSurfaceStyle.BOOK -> copy(fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic)
        GenreSurfaceStyle.COMIC, GenreSurfaceStyle.COLLAGE -> copy(fontWeight = FontWeight.Black)
        else -> this
    }
