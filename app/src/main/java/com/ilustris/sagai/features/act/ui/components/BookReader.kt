@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)

package com.ilustris.sagai.features.act.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.BookPage
import com.ilustris.sagai.features.act.ui.PageItem
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlin.math.abs

@Composable
fun BookReader(
    saga: SagaContent,
    act: ActContent,
    pages: List<PageItem>,
    isLast: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onSelectNextVolume: () -> Unit,
) {
    val pageItems = pages
    val pagerState = rememberPagerState { pageItems.size + 1 }
    val genre = remember { saga.data.genre }

    with(sharedTransitionScope) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            val readingProgress by animateFloatAsState(
                if (act.book != null && pageItems.isNotEmpty()) (pagerState.currentPage.toFloat() / pageItems.size.toFloat()) else 0f,
                label = "readingProgress",
            )

            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .fillMaxSize(),
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
                            is PageItem.BookCover -> {
                                BookCoverPage(
                                    sagaTitle = item.sagaTitle,
                                    actTitle = item.actTitle,
                                    volume = item.volume,
                                    quote = item.quote,
                                    genre = genre,
                                )
                            }

                            is PageItem.ChapterStart -> {
                                ChapterStartPage(
                                    title = item.title,
                                    genre = genre,
                                )
                            }

                            is PageItem.Illustration -> {
                                IllustrationPage(
                                    imagePath = item.imagePath,
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

                            is PageItem.CharacterGrid -> {
                                CharacterGridPage(
                                    characters = item.characters,
                                    genre = genre,
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

            Column(
                Modifier
                    .navigationBarsPadding()
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painterResource(genre.icon),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .gradientFill(progressiveBrush(genre.resolveColor(), readingProgress)),
                )

                AnimatedContent(pagerState.currentPage, transitionSpec = {
                    slideInVertically(
                        tween(
                            500,
                            easing = LinearOutSlowInEasing,
                        ),
                    ) { it / 2 } + fadeIn(tween(800)) togetherWith
                        fadeOut() + slideOutVertically { it }
                }, label = "pageCounter", modifier = Modifier) {
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

                Text(
                    saga.data.title,
                    fontFamily = genre.headerFont(),
                    letterSpacing = 2.sp,
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            color = genre.resolveColor(),
                            textAlign = TextAlign.Center,
                        ),
                )
            }
        }
    }
}

@Composable
fun BookCoverPage(
    sagaTitle: String,
    actTitle: String,
    volume: String,
    quote: String,
    genre: Genre,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(genre.resolveColor()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .padding(top = 100.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "VOL $volume",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = Color.White,
                        letterSpacing = 2.sp,
                    ),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AutoResizeText(
                    text = sagaTitle.uppercase(),
                    style =
                        MaterialTheme.typography.displayMedium.copy(
                            fontFamily = genre.headerFont(),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        ),
                    maxLines = 3,
                )

                Text(
                    text = actTitle,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = genre.bodyFont(),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        ),
                )
            }

            Icon(
                painterResource(genre.icon),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(100.dp)
                        .alpha(0.9f),
                tint = Color.White,
            )

            Text(
                text = "\"$quote\"",
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                    ),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
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
                .padding(32.dp)
                .padding(top = 100.dp, bottom = 120.dp),
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
                .padding(horizontal = 24.dp, vertical = 80.dp)
                .verticalScroll(rememberScrollState()),
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
fun IllustrationPage(
    imagePath: String,
    genre: Genre,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = imagePath,
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxSize()
                    .effectForGenre(genre),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alignment = Alignment.Center,
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
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 120.dp),
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

@Composable
fun CharacterGridPage(
    characters: List<CharacterContent>,
    genre: Genre,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(top = 80.dp, bottom = 100.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AutoResizeText(
            text = genre.characterGridTitle().uppercase(),
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = genre.headerFont(),
                    color = genre.resolveColor(),
                    textAlign = TextAlign.Center,
                ),
            maxLines = 1,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 3,
        ) {
            characters.forEach { character ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(4.dp),
                ) {
                    AsyncImage(
                        model = character.data.image,
                        contentDescription = character.data.name,
                        modifier =
                            Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(genre.cornerSize()))
                                .effectForGenre(genre),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )
                    Text(
                        text = character.data.name,
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            ),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

fun Genre.characterGridTitle() =
    when (this) {
        Genre.FANTASY -> "Our Legends"
        Genre.CYBERPUNK -> "The Edge-Runners"
        Genre.HORROR -> "The Lost Souls"
        Genre.HEROES -> "The Vanguard"
        Genre.CRIME -> "The Suspects"
        Genre.SHINOBI -> "The Shadow-Walkers"
        Genre.SPACE_OPERA -> "The Star-Farers"
        Genre.COWBOY -> "The Outlaws"
        Genre.PUNK_ROCK -> "The Anarchists"
    }
