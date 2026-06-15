package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.UniverseEcho
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaIntent
import com.ilustris.sagai.ui.components.CosmicBook
import com.ilustris.sagai.ui.components.NewSagaBookFocus
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.themeShimmer
import kotlinx.coroutines.delay

val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope?> {
        null
    }

val LocalGenderPlaceholders =
    staticCompositionLocalOf<com.ilustris.sagai.features.newsaga.data.model.GenderPlaceholderMap> {
        emptyMap()
    }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LibraryPager(
    books: List<Pair<SagaBook, GenreVisualConfig>>,
    lockedSaga: SagaDraft?,
    lockedCharacter: CharacterInfo?,
    isGenerating: Boolean,
    isLoadingMore: Boolean,
    hasMoreGenres: Boolean,
    onLoadMore: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onIntent: (NewSagaIntent) -> Unit,
) {
    val pagerState =
        rememberPagerState { books.size + if (hasMoreGenres) 1 else 0 }
    LaunchedEffect(pagerState.currentPage, hasMoreGenres, isLoadingMore) {
        if (hasMoreGenres && !isLoadingMore && pagerState.currentPage == books.size) {
            onLoadMore()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = lockedSaga == null,
        ) { pageIdx ->
            if (pageIdx < books.size) {
                val bookEntry = books[pageIdx]
                val isOpened = bookEntry.first.draft.id == lockedSaga?.id
                val isPageLoading = isGenerating && isOpened

                SagAITheme(genre = bookEntry.first.draft.genre) {
                    val bookVisual = LocalGenreVisualConfig.current ?: bookEntry.second

                    if (isPageLoading) {
                        val bookKey = "new-saga-book-${bookEntry.first.draft.id}"
                        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                NewSagaBookFocus(
                                    book = bookEntry.first,
                                    visualConfig = bookVisual,
                                    reasoning = null,
                                    isOpened = true,
                                    isLoading = true,
                                    lockedCharacter = lockedCharacter,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    sharedContentKey = bookKey,
                                    showReasoning = false,
                                    onToggle = { onIntent(NewSagaIntent.UnlockSaga) },
                                    onIntent = onIntent,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        } else {
                            CosmicBook(
                                book = bookEntry.first,
                                visualConfig = bookVisual,
                                isOpened = true,
                                lockedCharacter = lockedCharacter,
                                isLoading = true,
                                reasoning = null,
                                onToggle = { onIntent(NewSagaIntent.UnlockSaga) },
                                onIntent = onIntent,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CosmicBook(
                                book = bookEntry.first,
                                visualConfig = bookVisual,
                                isOpened = isOpened,
                                lockedCharacter = lockedCharacter,
                                isLoading = false,
                                reasoning = null,
                                onToggle = {
                                    if (isOpened) {
                                        onIntent(NewSagaIntent.UnlockSaga)
                                    } else {
                                        onIntent(NewSagaIntent.SelectSaga(bookEntry.first.draft))
                                    }
                                },
                                onIntent = onIntent,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val icons =
                        remember {
                            Genre.entries.map { it.icon }
                        }

                    var actualIcon by remember {
                        mutableStateOf(icons.first())
                    }

                    LaunchedEffect(actualIcon) {
                        delay(1000)
                        actualIcon = icons.random()
                    }

                    AnimatedContent(actualIcon, transitionSpec = {
                        scaleIn() togetherWith scaleOut()
                    }) {
                        Icon(
                            painterResource(it),
                            null,
                            modifier =
                                Modifier
                                    .size(
                                        24.dp,
                                    ).reactiveShimmer(
                                        repeatMode = RepeatMode.Restart,
                                        shimmerColors = themeShimmer(),
                                        isPlaying = true,
                                    ),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(books.size + if (hasMoreGenres) 1 else 0) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }

                AnimatedContent(pagerState.currentPage == iteration, transitionSpec = {
                    scaleIn() togetherWith scaleOut()
                }) {
                    if (it) {
                        if (iteration < books.size) {
                            val pageGenre = books[iteration]
                            Icon(
                                painterResource(pageGenre.first.draft.genre.icon),
                                null,
                                modifier = Modifier.size(12.dp),
                                tint = color,
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(2.dp).size(12.dp),
                                trackColor = Color.Transparent,
                                gapSize = 0.dp,
                                color = color,
                            )
                        }
                    } else {
                        Box(
                            modifier =
                                Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(8.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UniverseEchoesSection(
    echoes: List<Pair<UniverseEcho, GenreVisualConfig>>,
    onEchoSelected: (String) -> Unit,
) {
    val maxSuggestionWidth =
        (LocalConfiguration.current.screenWidthDp * 0.6f).dp

    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp, max = 180.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = echoes,
            key = { (echo, _) -> echo.input },
        ) { (echo, config) ->
            EchoBubbleCard(
                echo = echo,
                visualConfig = config,
                maxWidth = maxSuggestionWidth,
                modifier = Modifier.animateItem(),
                onClick = { onEchoSelected(echo.input) },
            )
        }
    }
}

@Composable
private fun EchoBubbleCard(
    echo: UniverseEcho,
    visualConfig: GenreVisualConfig,
    maxWidth: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SagAITheme(genre = echo.genre) {
        val genre = echo.genre
        val activeVisual = LocalGenreVisualConfig.current ?: visualConfig
        val color = genre.resolveColor(activeVisual)
        val shape = RoundedCornerShape(25.dp)
        val genreBrush = Brush.linearGradient(genre.colorPalette(activeVisual))

        val textMaxWidth = maxWidth - 56.dp

        Row(
            modifier =
                modifier
                    .widthIn(max = maxWidth)
                    .wrapContentWidth(Alignment.Start)
                    .padding(2.dp)
                    .dropShadow(shape) {
                        brush = genreBrush
                        radius = 15f
                        spread = 3f
                    }.clip(shape)
                    .background(MaterialTheme.colorScheme.background, shape)
                    .clickable(onClick = onClick)
                    .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painterResource(genre.icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = echo.input,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.widthIn(max = textMaxWidth),
                softWrap = true,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
