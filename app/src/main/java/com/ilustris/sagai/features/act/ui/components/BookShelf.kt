@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)

package com.ilustris.sagai.features.act.ui.components

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
import com.ilustris.sagai.ui.animations.chromaticAberration
import com.ilustris.sagai.ui.animations.divineAura
import com.ilustris.sagai.ui.components.CosmicBook
import com.ilustris.sagai.ui.theme.grayScale
import com.ilustris.sagai.ui.theme.levitate

@Composable
fun BookShelf(
    saga: SagaContent,
    acts: List<ActContent>,
    selectedBook: ActContent?,
    isLoading: Boolean,
    reasoning: String? = null,
    generatingActTitle: String? = null,
    visualConfig: com.ilustris.sagai.core.ai.model.GenreVisualConfig? = null,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
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
        modifier =
            Modifier.fillMaxSize(),
        userScrollEnabled = !isLoading,
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
                        }.clickable {
                            if (isSelected) onBookSelected(act)
                        },
                contentAlignment = Alignment.Center,
            ) {
                val isGenerating = isLoading && act.data.title == generatingActTitle
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
                    visualConfig =
                        visualConfig ?: com.ilustris.sagai.core.ai.model
                            .GenreVisualConfig(),
                    isOpened = isGenerating,
                    isLoading = isGenerating || (isLoading && isSelected),
                    reasoning = if (isGenerating) reasoning else null,
                    onToggle = { if (!isGenerating) onBookSelected(act) },
                    onAction = {},
                    modifier =
                        Modifier
                            .grayScale(if (act.book == null) 0f else 1f)
                            .width(280.dp)
                            .fillMaxHeight(.75f)
                            .padding(vertical = 16.dp)
                            .levitate(isSelected || isGenerating)
                            .then(
                                if (isGenerating) {
                                    Modifier
                                        .divineAura()
                                        .chromaticAberration()
                                } else {
                                    Modifier
                                },
                            ),
                    titleModifier =
                        Modifier.sharedElement(
                            rememberSharedContentState(key = "book-${act.data.id}"),
                            animatedContentScope,
                        ),
                )
            }
        }
    }
}
