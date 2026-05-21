@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)

package com.ilustris.sagai.features.act.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
import com.ilustris.sagai.ui.animations.chromaticAberration
import com.ilustris.sagai.ui.animations.divineAura
import com.ilustris.sagai.ui.components.CosmicBook
import com.ilustris.sagai.ui.theme.grayScale
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun BookShelf(
    saga: SagaContent,
    acts: List<ActContent>,
    selectedBook: ActContent?,
    isLoading: Boolean,
    reasoning: String? = null,
    generatingActTitle: String? = null,
    visualConfig: GenreVisualConfig? = null,
    sharedTransitionScope: SharedTransitionScope,
    onBookSelected: (ActContent) -> Unit,
) {
    val generatingAct =
        generatingActTitle?.let { title -> acts.find { it.data.title == title } }
    val isGenerating = isLoading && generatingAct != null
    val config = visualConfig ?: GenreVisualConfig()

    AnimatedContent(
        targetState = isGenerating,
        label = "BookShelfMode",
        transitionSpec = {
            fadeIn(tween(400)) togetherWith fadeOut(tween(400))
        },
        modifier = Modifier.fillMaxSize(),
    ) { generating ->
        if (generating && generatingAct != null) {
            with(sharedTransitionScope) {
                GeneratingBookFocus(
                    saga = saga,
                    act = generatingAct,
                    reasoning = reasoning,
                    visualConfig = config,
                    animatedVisibilityScope = this@AnimatedContent,
                )
            }
        } else {
            BookShelfPager(
                saga = saga,
                acts = acts,
                selectedBook = selectedBook,
                visualConfig = config,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = this@AnimatedContent,
                onBookSelected = onBookSelected,
            )
        }
    }
}

@Composable
private fun SharedTransitionScope.GeneratingBookFocus(
    saga: SagaContent,
    act: ActContent,
    reasoning: String?,
    visualConfig: GenreVisualConfig,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val sagaBook =
        remember(act) {
            SagaBook(
                draft =
                    SagaDraft(
                        title = act.data.title,
                        genre = saga.data.genre,
                        description = "",
                    ),
            )
        }
    val genre = saga.data.genre

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CosmicBook(
            book = sagaBook,
            visualConfig = visualConfig,
            isOpened = false,
            isLoading = true,
            reasoning = null,
            onToggle = {},
            onAction = {},
            modifier =
                Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "book-${act.data.id}"),
                        animatedVisibilityScope,
                    ).width(280.dp)
                    .fillMaxHeight(0.55f)
                    .levitate(true)
                    .divineAura()
                    .chromaticAberration(),
            titleModifier = Modifier,
        )

        BookGenerationReasoning(
            reasoning = reasoning,
            genre = genre,
            visualConfig = visualConfig,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}

@Composable
private fun BookGenerationReasoning(
    reasoning: String?,
    genre: com.ilustris.sagai.features.newsaga.data.model.Genre,
    visualConfig: GenreVisualConfig,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = reasoning,
        label = "BookGenerationReasoning",
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(200))
        },
        modifier = modifier.fillMaxWidth(),
    ) { text ->
        if (text != null) {
            Text(
                text = text,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .reactiveShimmer(true, genre.shimmerColors(visualConfig)),
            )
        }
    }
}

@Composable
private fun BookShelfPager(
    saga: SagaContent,
    acts: List<ActContent>,
    selectedBook: ActContent?,
    visualConfig: GenreVisualConfig,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBookSelected: (ActContent) -> Unit,
) {
    val pagerState = rememberPagerState { acts.size }

    LaunchedEffect(selectedBook) {
        saga.findAct(selectedBook?.data?.id)?.let {
            pagerState.animateScrollToPage(acts.indexOf(it))
        }
    }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 24.dp,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        val act = acts[page]
        val isSelected = pagerState.currentPage == page

        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1f else 0.85f,
            animationSpec = tween(500),
            label = "BookScale",
        )

        with(sharedTransitionScope) {
            Box(
                modifier =
                    Modifier
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.alpha = if (isSelected) 1f else 0.6f
                        }
                        .clickable {
                            if (isSelected) onBookSelected(act)
                        },
                contentAlignment = Alignment.Center,
            ) {
                val sagaBook =
                    remember(act) {
                        SagaBook(
                            draft =
                                SagaDraft(
                                    title = act.data.title,
                                    genre = saga.data.genre,
                                    description = "",
                                ),
                        )
                    }

                CosmicBook(
                    book = sagaBook,
                    visualConfig = visualConfig,
                    isOpened = false,
                    isLoading = false,
                    reasoning = null,
                    onToggle = { onBookSelected(act) },
                    onAction = {},
                    modifier =
                        Modifier
                            .sharedBounds(
                                rememberSharedContentState(key = "book-${act.data.id}"),
                                animatedVisibilityScope,
                            ).grayScale(if (act.book == null) 0f else 1f)
                            .width(280.dp)
                            .fillMaxHeight(.75f)
                            .padding(vertical = 16.dp)
                            .levitate(isSelected),
                    titleModifier = Modifier,
                )
            }
        }
    }
}
