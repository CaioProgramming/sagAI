@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaIntent
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaUiState

/** Picking one of the proposed stories: swipe, and the card in the middle is the one chosen. */
@Composable
internal fun NewSagaStoriesContent(
    uiState: NewSagaUiState,
    onIntent: (NewSagaIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val books = uiState.libraryBooks
    val pageCount = books.size + if (uiState.hasMoreGenres) 1 else 0
    val pagerState =
        rememberPagerState(initialPage = uiState.focusedIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))) {
            pageCount
        }
    val onStory = pagerState.currentPage < books.size

    LaunchedEffect(pagerState.currentPage) {
        onIntent(NewSagaIntent.FocusStory(pagerState.currentPage))
    }
    // Reaching the last page asks for stories of the genres not offered yet.
    LaunchedEffect(pagerState.currentPage, uiState.hasMoreGenres, uiState.isLoadingMore) {
        if (uiState.hasMoreGenres && !uiState.isLoadingMore && pagerState.currentPage == books.size) {
            onIntent(NewSagaIntent.LoadMore)
        }
    }

    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        StepHeader(
            title = stringResource(R.string.new_saga_stories_title),
            subtitle = stringResource(R.string.new_saga_stories_subtitle),
        )
        GenreCardPager(
            items = books.map { it.first.draft },
            genreOf = { it.genre },
            pagerState = pagerState,
            modifier = Modifier.weight(1f),
            trailingPage = { MoreStoriesCard(isLoading = uiState.isLoadingMore, modifier = Modifier.fillMaxSize()) },
        ) { draft, sheenOffset ->
            StoryCard(draft = draft, sheenOffset = sheenOffset, modifier = Modifier.fillMaxSize())
        }
        StepActions(
            primary = stringResource(R.string.new_saga_pick_story),
            primaryEnabled = onStory,
            onPrimary = { books.getOrNull(pagerState.currentPage)?.let { onIntent(NewSagaIntent.SelectSaga(it.first.draft)) } },
            secondary = stringResource(R.string.new_saga_change_idea),
            onSecondary = { onIntent(NewSagaIntent.ChangeIdea) },
        )
    }
}

/** Picking the protagonist of the chosen story, the same way. */
@Composable
internal fun NewSagaHeroesContent(
    uiState: NewSagaUiState,
    onIntent: (NewSagaIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val book = uiState.libraryBooks.firstOrNull { it.first.draft.id == uiState.lockedSaga?.id }?.first ?: return
    val characters = book.characters
    if (characters.isEmpty()) return
    val pagerState = rememberPagerState { characters.size }
    val current = characters.getOrNull(pagerState.currentPage)

    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        StepHeader(
            title = stringResource(R.string.new_saga_hero_title),
            subtitle = stringResource(R.string.new_saga_hero_subtitle),
        )
        GenreCardPager(
            items = characters,
            genreOf = { book.draft.genre },
            pagerState = pagerState,
            modifier = Modifier.weight(1f),
        ) { character, sheenOffset ->
            HeroCard(character = character, sheenOffset = sheenOffset, modifier = Modifier.fillMaxSize())
        }
        StepActions(
            primary = stringResource(R.string.new_saga_pick_hero, current?.name.orEmpty()),
            primaryEnabled = current != null,
            onPrimary = { current?.let { onIntent(NewSagaIntent.SelectCharacter(it)) } },
            secondary = stringResource(R.string.new_saga_change_story),
            onSecondary = { onIntent(NewSagaIntent.UnlockSaga) },
        )
    }
}

private enum class ReviewCard { Story, Hero }

/**
 * The last look before saving: the story and its protagonist as two stacked cards. Tapping the one
 * behind brings it forward; holding (or the pencil on) the one in front opens its editor.
 */
@Composable
internal fun NewSagaReviewContent(
    uiState: NewSagaUiState,
    onIntent: (NewSagaIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val saga = uiState.lockedSaga ?: return
    val character = uiState.lockedCharacter ?: return
    var front by rememberSaveable { mutableStateOf(ReviewCard.Story) }
    var editing by rememberSaveable { mutableStateOf<ReviewCard?>(null) }

    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        StepHeader(
            title = stringResource(R.string.new_saga_review_title),
            subtitle = stringResource(R.string.new_saga_review_subtitle),
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            ReviewCard.entries.forEach { card ->
                val isFront = front == card
                val peek by animateDpAsState(if (isFront) 0.dp else (-36).dp, tween(600), label = "review_peek")
                val scale by animateFloatAsState(if (isFront) 1f else 0.92f, tween(600), label = "review_scale")
                val dim by animateFloatAsState(if (isFront) 1f else 0.7f, tween(600), label = "review_dim")
                val haptics = LocalHapticFeedback.current
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = 36.dp)
                            .zIndex(if (isFront) 1f else 0f)
                            .offset(y = peek)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                alpha = dim
                            }.combinedClickable(
                                onClick = { if (!isFront) front = card },
                                onLongClick = {
                                    if (isFront) {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        editing = card
                                    }
                                },
                            ),
                ) {
                    when (card) {
                        ReviewCard.Story -> StoryCard(draft = saga, compact = true, modifier = Modifier.fillMaxSize())
                        ReviewCard.Hero -> HeroCard(character = character, modifier = Modifier.fillMaxSize())
                    }
                    if (isFront) {
                        IconButton(
                            onClick = { editing = card },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription =
                                    stringResource(
                                        if (card == ReviewCard.Story) R.string.new_saga_edit_story else R.string.new_saga_edit_hero,
                                    ),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
        uiState.savingError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )
        }
        Text(
            text = stringResource(R.string.new_saga_review_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
    }

    editing?.let { card ->
        ModalBottomSheet(onDismissRequest = { editing = null }) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text =
                        stringResource(
                            if (card == ReviewCard.Story) R.string.new_saga_edit_story else R.string.new_saga_edit_hero,
                        ),
                    style = MaterialTheme.typography.titleMedium,
                )
                val first = if (card == ReviewCard.Story) saga.title else character.name
                val second = if (card == ReviewCard.Story) saga.description else character.description
                OutlinedTextField(
                    value = first,
                    onValueChange = {
                        onIntent(
                            if (card == ReviewCard.Story) {
                                NewSagaIntent.UpdateSaga(saga.id, it, saga.description)
                            } else {
                                NewSagaIntent.UpdateCharacter(character.id, it, character.description)
                            },
                        )
                    },
                    label = {
                        Text(stringResource(if (card == ReviewCard.Story) R.string.new_saga_field_title else R.string.new_saga_field_name))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = second,
                    onValueChange = {
                        onIntent(
                            if (card == ReviewCard.Story) {
                                NewSagaIntent.UpdateSaga(saga.id, saga.title, it)
                            } else {
                                NewSagaIntent.UpdateCharacter(character.id, character.name, it)
                            },
                        )
                    },
                    label = { Text(stringResource(R.string.new_saga_field_description)) },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = { editing = null }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.new_saga_edit_done))
                }
            }
        }
    }
}

@Composable
private fun StepHeader(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

/** The primary action first, the way back below it. */
@Composable
private fun StepActions(
    primary: String,
    primaryEnabled: Boolean,
    onPrimary: () -> Unit,
    secondary: String,
    onSecondary: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlassButton(
            text = primary,
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onSecondary) {
            Text(secondary)
        }
    }
}
