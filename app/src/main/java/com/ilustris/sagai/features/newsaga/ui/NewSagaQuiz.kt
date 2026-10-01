package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaIntent
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaUiState
import com.ilustris.sagai.ui.genre.choice.GenreChoiceCard
import com.ilustris.sagai.ui.genre.choice.GenreChoiceCta

/**
 * The three dilemmas asked between the idea and the stories. Same pattern as the chapter's choice
 * cards — one question at a time, two portrait cards side by side dealt face down, one up at a time —
 * but with no genre yet, so the cards wear the app's default surface.
 *
 * The player only sees the question and the two option texts; the hidden insights stay in the quiz
 * model and are folded into the story prompt by the ViewModel.
 */
@Composable
internal fun NewSagaQuizContent(
    uiState: NewSagaUiState,
    onIntent: (NewSagaIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val quiz = uiState.quiz ?: return
    val total = quiz.cards.size
    if (total == 0) return
    val index = uiState.quizIndex.coerceIn(0, total - 1)
    val isLast = index == total - 1
    val muted = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    val progress by animateFloatAsState(
        targetValue = (index + 1) / total.toFloat(),
        animationSpec = tween(500),
        label = "quiz_progress",
    )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = quiz.screenTitle.takeIf { it.isNotBlank() } ?: stringResource(R.string.new_saga_quiz_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = quiz.screenSubtitle.takeIf { it.isNotBlank() } ?: stringResource(R.string.new_saga_quiz_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = muted,
            modifier = Modifier.fillMaxWidth(),
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.new_saga_quiz_progress, index + 1, total),
            style = MaterialTheme.typography.labelMedium,
            color = muted,
        )

        AnimatedContent(
            targetState = index,
            transitionSpec = {
                (fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 12 }) togetherWith fadeOut(tween(250))
            },
            modifier = Modifier.weight(1f),
            label = "new_saga_quiz_card_pair",
        ) { cardIndex ->
            val card = quiz.cards[cardIndex]
            val selected = uiState.quizAnswers.getOrNull(cardIndex)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = card.choiceTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.new_saga_quiz_hint),
                    style = MaterialTheme.typography.labelMedium,
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
                            onClick = { onIntent(NewSagaIntent.QuizSelect(cardIndex, optionIndex)) },
                            seed = cardIndex * card.options.size + optionIndex,
                            genre = null,
                            modifier = Modifier.weight(1f).aspectRatio(QUIZ_CARD_ASPECT),
                        )
                    }
                }
            }
        }

        GenreChoiceCta(
            label =
                stringResource(
                    if (isLast) R.string.new_saga_quiz_see_stories else R.string.new_saga_quiz_next,
                ),
            enabled = uiState.currentQuizAnswer != null,
            onClick = { onIntent(NewSagaIntent.QuizNext) },
            genre = null,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(
            onClick = { onIntent(NewSagaIntent.QuizPrevious) },
            enabled = index > 0,
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .alpha(if (index > 0) 1f else 0f),
        ) {
            Text(stringResource(R.string.new_saga_quiz_back))
        }
    }
}

private const val QUIZ_CARD_ASPECT = 0.68f
