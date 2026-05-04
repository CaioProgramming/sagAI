@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)

package com.ilustris.sagai.features.act.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.BookPage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlin.math.abs

sealed class PageItem {
    data class ChapterStart(
        val title: String,
    ) : PageItem()

    data class Content(
        val chapterTitle: String,
        val page: BookPage,
    ) : PageItem()
}

@Composable
fun BookReader(
    saga: SagaContent,
    act: ActContent,
    isLast: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onSelectNextVolume: () -> Unit,
) {
    val book = act.book
    val pageItems =
        remember(book) {
            buildList {
                book?.chapters?.forEach { chapter ->
                    add(PageItem.ChapterStart(chapter.title))
                    chapter.pages.forEach { page ->
                        add(PageItem.Content(chapter.title, page))
                    }
                }
            }
        }
    val pagerState = rememberPagerState { pageItems.size + 1 }
    val genre = remember { saga.data.genre }

    with(sharedTransitionScope) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            val readingProgress by animateFloatAsState(
                if (book != null && pageItems.isNotEmpty()) (pagerState.currentPage.toFloat() / pageItems.size.toFloat()) else 0f,
                label = "readingProgress",
            )

            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                pageSpacing = 8.dp,
            ) { pageIdx ->
                val pageOffset =
                    ((pagerState.currentPage - pageIdx) + pagerState.currentPageOffsetFraction).coerceIn(
                        -1f,
                        1f,
                    )

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // High-fidelity 3D Page turn effect
                                cameraDistance = 15 * density
                                rotationY = pageOffset * -35f

                                val scale = 1f - (abs(pageOffset) * 0.08f)
                                scaleX = scale
                                scaleY = scale
                                alpha = (1f - abs(pageOffset)).coerceAtLeast(0.5f)

                                // Pivot points for a more realistic turn
                                transformOrigin =
                                    if (pageOffset > 0) {
                                        TransformOrigin(
                                            0f,
                                            0.5f,
                                        )
                                    } else {
                                        TransformOrigin(1f, 0.5f)
                                    }
                            },
                ) {
                    if (pageIdx < pageItems.size) {
                        when (val item = pageItems[pageIdx]) {
                            is PageItem.ChapterStart -> {
                                ChapterStartPage(
                                    title = item.title,
                                    genre = genre,
                                )
                            }

                            is PageItem.Content -> {
                                ReaderPage(
                                    chapterTitle = item.chapterTitle,
                                    page = item.page,
                                    genre = genre,
                                    titleModifier =
                                        Modifier.sharedElement(
                                            rememberSharedContentState(key = "book-${act.data.id}"),
                                            animatedContentScope,
                                        ),
                                )
                            }
                        }
                    } else {
                        EndPaper(
                            currentAct = act,
                            saga = saga,
                        )
                    }
                }
            }

            Image(
                painterResource(genre.icon),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .align(Alignment.CenterHorizontally)
                        .gradientFill(progressiveBrush(genre.resolveColor(), readingProgress)),
            )

            AnimatedContent(pagerState.currentPage, transitionSpec = {
                slideInVertically { it / 2 } + fadeIn(tween(800)) togetherWith
                    fadeOut()
            }, label = "pageCounter", modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (it < pageItems.size) {
                        Text(
                            text = "${it + 1}",
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = genre.headerFont(),
                                    textAlign = TextAlign.Center,
                                ),
                        )
                    }

                    if (it >= pageItems.size && !isLast) {
                        Button(
                            onClick = {
                                onSelectNextVolume()
                            },
                            shape = RoundedCornerShape(genre.cornerSize()),
                            colors =
                                ButtonDefaults.textButtonColors().copy(
                                    contentColor = genre.resolveColor(),
                                ),
                        ) {
                            Text(
                                "Read next volume",
                                fontFamily = genre.bodyFont(),
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        shadow = Shadow(genre.color, blurRadius = 5f),
                                    ),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.size(32.dp))
        }
    }
}

@Composable
fun ChapterStartPage(
    title: String,
    genre: Genre,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painterResource(genre.icon),
            contentDescription = null,
            modifier =
                Modifier
                    .size(50.dp)
                    .reactiveShimmer(true),
            tint = genre.resolveColor(),
        )

        AutoResizeText(
            text = title.uppercase(),
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = genre.headerFont(),
                    color = genre.resolveColor(),
                    textAlign = TextAlign.Center,
                ),
            maxLines = 2,
        )
    }
}

@Composable
fun ReaderPage(
    chapterTitle: String,
    page: BookPage,
    genre: Genre,
    titleModifier: Modifier,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = page.content,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = genre.bodyFont(),
                    fontWeight = FontWeight.Normal,
                ),
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
fun EndPaper(
    currentAct: ActContent,
    saga: SagaContent,
) {
    val genre = remember { saga.data.genre }
    val genreColor = saga.data.genre.resolveColor()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Icon(
            painterResource(saga.data.genre.icon),
            contentDescription = null,
            modifier =
                Modifier
                    .size(80.dp)
                    .genreVfx(genre),
            tint = genreColor,
        )

        currentAct.book?.authorNote?.let { note ->
            var showSagaSign by remember {
                mutableStateOf(false)
            }
            val signAlpha by animateFloatAsState(
                if (showSagaSign) 1f else 0f,
                label = "signAlpha",
            )
            SimpleTypewriterText(
                text = note,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic,
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center,
                    ),
                modifier = Modifier.padding(16.dp),
                onAnimationFinished = {
                    showSagaSign = true
                },
            )

            SagaTitle(
                textStyle = MaterialTheme.typography.labelLarge,
                modifier =
                    Modifier
                        .alpha(signAlpha)
                        .gradientFill(genre.gradient(true)),
            )
        }
    }
}
