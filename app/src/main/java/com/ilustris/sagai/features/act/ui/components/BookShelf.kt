@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)

package com.ilustris.sagai.features.act.ui.components

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.features.home.data.model.findAct
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.grayScale
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun BookShelf(
    saga: SagaContent,
    acts: List<ActContent>,
    selectedBook: ActContent?,
    isLoading: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBookSelected: (ActContent) -> Unit,
) {
    val pagerState = rememberPagerState { acts.size }
    saga.data.genre.resolveColor()
    saga.data.genre.colorPalette()

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
                BookCard(
                    act = act.data,
                    saga = saga,
                    modifier =
                        Modifier
                            .width(280.dp)
                            .fillMaxHeight(.6f),
                    isSelected = isSelected,
                    isLoading = isLoading,
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

@Composable
fun BookCard(
    act: Act,
    saga: SagaContent,
    isLoading: Boolean,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    titleModifier: Modifier,
) {
    val genre =
        remember {
            saga.data.genre
        }
    val genreColor = saga.data.genre.resolveColor()
    val isReady = act.book != null
    val shape =
        RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp)
    val brush = genre.gradient()
    val saturation by animateFloatAsState(if (isReady) 1f else 0f)
    Box(
        modifier =
            modifier
                .grayScale(saturation)
                .levitate(isSelected)
                .dropShadow(shape) {
                    this.brush = brush
                    radius = 10f
                    spread = 5f
                }.border(1.dp, genre.color.gradientFade(), shape)
                .clip(shape),
    ) {
        // The Book Cover
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            genreColor.darkerPalette(factor = .2f, count = 6),
                        ),
                    ).reactiveShimmer(isLoading && isSelected),
        ) {
            // Spine Detail
            Row {
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(32.dp)
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = .4f),
                            ),
                )

                // Cover Content
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Genre Icon with VFX
                    Icon(
                        painter = painterResource(saga.data.genre.icon),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .genreVfx(genre)
                                .size(64.dp),
                        tint = genre.resolveIconColor(),
                    )

                    AutoResizeText(
                        text = saga.actNumber(act).toRoman(),
                        style =
                            MaterialTheme.typography.titleSmall.copy(
                                fontFamily = genre.headerFont(),
                                color = genre.iconColor,
                            ),
                    )

                    AutoResizeText(
                        text = act.title,
                        style =
                            MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = genre.headerFont(),
                                fontWeight = FontWeight.Normal,
                                color = genre.iconColor,
                                textAlign = TextAlign.Center,
                                shadow =
                                    Shadow(
                                        genre.color,
                                        blurRadius = 10f,
                                    ),
                            ),
                        modifier = titleModifier,
                    )
                }
            }
        }
    }
}
