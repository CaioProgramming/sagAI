package com.ilustris.sagai.features.saga.detail.ui

import ai.atick.material.MaterialColor
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.ui.components.VerticalLabel
import com.ilustris.sagai.features.emotional.ui.EmotionalProfileCard
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.playthrough.toPlaytimeFormat
import com.ilustris.sagai.features.saga.detail.data.model.SagaDetailResume
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaShape
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SagaDetailInitialContent(
    saga: Saga,
    section: DetailSectionView.InitialSection,
    resume: SagaDetailResume,
    gridState: LazyGridState = rememberLazyGridState(),
    onAction: (DetailAction) -> Unit = {},
    showTitleOnly: Boolean = false,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val columnCount = 2
    val genre = remember { saga.genre }
    var iconError by remember(saga.id) { mutableStateOf(false) }

    val innerContent: @Composable (SharedTransitionScope?, AnimatedVisibilityScope?) -> Unit =
        { sharedScope, visibilityScope ->
            AnimatedContent(
                showTitleOnly,
                label = "SagaDetailInitialContentTransition",
                transitionSpec = {
                    fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                },
            ) { showOnlyTitle ->
                if (showOnlyTitle) {
                    Box(Modifier.fillMaxSize()) {
                        genre.stylisedText(
                            saga.title,
                            modifier =
                                Modifier
                                    .then(
                                        if (sharedScope != null && visibilityScope != null) {
                                            with(sharedScope) {
                                                Modifier.sharedBounds(
                                                    rememberSharedContentState(key = "saga_${saga.id}_title"),
                                                    animatedVisibilityScope = visibilityScope,
                                                )
                                            }
                                        } else {
                                            Modifier
                                        },
                                    )
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                                    .reactiveShimmer(true)
                                    .padding(8.dp),
                        )
                    }
                } else {
                    Box(Modifier.fillMaxSize()) {
                        val genreHighlight = genre.selectiveHighlight()

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columnCount),
                            modifier = Modifier.fillMaxSize(),
                            state = gridState,
                        ) {
                            item(span = { GridItemSpan(columnCount) }) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    if (saga.icon.isNotBlank() && !iconError) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .then(
                                                        if (sharedScope != null && visibilityScope != null) {
                                                            with(sharedScope) {
                                                                Modifier.sharedBounds(
                                                                    rememberSharedContentState(key = "saga_${saga.id}_icon"),
                                                                    animatedVisibilityScope = visibilityScope,
                                                                )
                                                            }
                                                        } else {
                                                            Modifier
                                                        },
                                                    )
                                                    .background(MaterialTheme.colorScheme.background)
                                                    .height(400.dp)
                                                    .fillMaxWidth(),
                                        ) {
                                            val currentModifier =
                                                Modifier
                                                    .align(Alignment.TopCenter)
                                                    .padding(16.dp)
                                                    .reactiveShimmer(
                                                        true,
                                                        duration = 5.seconds,
                                                    )
                                            if (section.segmentedImage != null) {
                                                DepthLayout(
                                                    originalImage = section.segmentedImage.first,
                                                    segmentedImage = section.segmentedImage.second,
                                                    modifier = Modifier.fillMaxSize(),
                                                    imageModifier =
                                                        Modifier
                                                            .clipToBounds()
                                                            .effectForGenre(genre)
                                                            .selectiveColorHighlight(
                                                                genreHighlight,
                                                            ),
                                                ) {
                                                    genre.stylisedText(
                                                        saga.title,
                                                        modifier =
                                                            currentModifier
                                                                .then(
                                                                    if (sharedScope != null && visibilityScope != null) {
                                                                        with(sharedScope) {
                                                                            Modifier.sharedBounds(
                                                                                rememberSharedContentState(
                                                                                    key = "saga_${saga.id}_title",
                                                                                ),
                                                                                animatedVisibilityScope = visibilityScope,
                                                                            )
                                                                        }
                                                                    } else {
                                                                        Modifier
                                                                    },
                                                                )
                                                                .fillMaxWidth()
                                                                .padding(8.dp),
                                                    )
                                                }
                                            } else {
                                                AsyncImage(
                                                    model =
                                                        ImageRequest
                                                            .Builder(LocalContext.current)
                                                            .data(saga.icon)
                                                            .crossfade(true)
                                                            .build(),
                                                    contentDescription = saga.title,
                                                    onState = {
                                                        if (it is AsyncImagePainter.State.Error) {
                                                            iconError = true
                                                        }
                                                    },
                                                    modifier =
                                                        Modifier
                                                            .background(MaterialTheme.colorScheme.background)
                                                            .fillMaxSize()
                                                            .effectForGenre(genre)
                                                            .selectiveColorHighlight(
                                                                genreHighlight,
                                                            ),
                                                    contentScale = ContentScale.Crop,
                                                )

                                                genre.stylisedText(
                                                    saga.title,
                                                    modifier =
                                                        currentModifier
                                                            .then(
                                                                if (sharedScope != null && visibilityScope != null) {
                                                                    with(sharedScope) {
                                                                        Modifier.sharedBounds(
                                                                            rememberSharedContentState(
                                                                                key = "saga_${saga.id}_title",
                                                                            ),
                                                                            animatedVisibilityScope = visibilityScope,
                                                                        )
                                                                    }
                                                                } else {
                                                                    Modifier
                                                                },
                                                            )
                                                            .padding(8.dp),
                                                )
                                            }

                                            Box(
                                                Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(.6f)
                                                    .background(fadeGradientBottom()),
                                            )
                                        }
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier =
                                                Modifier
                                                    .then(
                                                        if (sharedScope != null && visibilityScope != null) {
                                                            with(sharedScope) {
                                                                Modifier.sharedBounds(
                                                                    rememberSharedContentState(
                                                                        key = "saga_${saga.id}_icon",
                                                                    ),
                                                                    animatedVisibilityScope = visibilityScope,
                                                                )
                                                            }
                                                        } else {
                                                            Modifier
                                                        },
                                                    )
                                                    .padding(16.dp),
                                        ) {
                                            Image(
                                                painterResource(genre.icon),
                                                null,
                                                Modifier
                                                    .clickable {
                                                        onAction(DetailAction.RegenerateIcon)
                                                    }
                                                    .size(100.dp)
                                                    .gradientFill(
                                                        MaterialTheme.colorScheme.primary.gradientFade(),
                                                    ),
                                            )
                                            genre.stylisedText(
                                                saga.title,
                                                modifier =
                                                    Modifier
                                                        .then(
                                                            if (sharedScope != null && visibilityScope != null) {
                                                                with(sharedScope) {
                                                                    Modifier.sharedBounds(
                                                                        rememberSharedContentState(
                                                                            key = "saga_${saga.id}_title",
                                                                        ),
                                                                        animatedVisibilityScope = visibilityScope,
                                                                    )
                                                                }
                                                            } else {
                                                                Modifier
                                                            },
                                                        )
                                                        .padding(8.dp)
                                                        .fillMaxWidth()
                                                        .reactiveShimmer(
                                                            true,
                                                            duration = 5.seconds,
                                                        ),
                                            )
                                        }
                                    }
                                }
                            }

                            item(span = { GridItemSpan(columnCount) }) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                ) {
                                    item {
                                        VerticalLabel(
                                            section.chaptersCount.toString(),
                                            stringResource(
                                                R.string.saga_detail_section_title_chapters,
                                            ),
                                            genre,
                                        )
                                    }

                                    item {
                                        VerticalLabel(
                                            resume.messagesCount.toString(),
                                            stringResource(
                                                R.string.saga_detail_messages_label,
                                            ),
                                            genre,
                                        )
                                    }

                                    item {
                                        VerticalLabel(
                                            resume.charactersCount.toString(),
                                            stringResource(
                                                R.string.saga_detail_section_title_characters,
                                            ),
                                            genre,
                                        )
                                    }

                                    item {
                                        VerticalLabel(
                                            resume.playtime.toPlaytimeFormat(),
                                            stringResource(R.string.total_playtime_label),
                                            genre,
                                        )
                                    }
                                }
                            }

                            item(span = { GridItemSpan(columnCount) }) {
                                Column {
                                    Text(
                                        saga.description,
                                        modifier =
                                            Modifier
                                                .padding(16.dp),
                                        style =
                                            MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                textAlign = TextAlign.Justify,
                                            ),
                                    )
                                }
                            }

                            if (saga.isEnded) {
                                item(span = { GridItemSpan(columnCount) }) {
                                    RecapHeroCard(
                                        saga = saga,
                                        chaptersCount = resume.chaptersCount,
                                        charactersCount = resume.charactersCount,
                                        messagesCount = resume.messagesCount,
                                        modifier =
                                            Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth()
                                                .height(200.dp),
                                        onClick = {
                                            onAction(DetailAction.OpenReview)
                                        },
                                    )
                                }
                            }

                            section.starring?.let {
                                item(span = { GridItemSpan(columnCount) }) {
                                    Text(
                                        stringResource(R.string.starring),
                                        style =
                                            MaterialTheme.typography.headlineLarge.copy(
                                                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                                textAlign = TextAlign.Start,
                                            ),
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }

                                item(span = { GridItemSpan(columnCount) }) {
                                    val shape = sagaShape()
                                    var starringError by remember(it.data.id) {
                                        mutableStateOf(false)
                                    }

                                    Box(
                                        Modifier
                                            .padding(16.dp)
                                            .clip(shape = shape)
                                            .border(1.dp, genre.color.gradientFade(), shape)
                                            .background(genre.color.gradientFade())
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .clickable {
                                                onAction(
                                                    DetailAction.OpenSection(
                                                        RequestSection.CHARACTERS,
                                                    ),
                                                )
                                            },
                                    ) {
                                        if (!starringError) {
                                            AsyncImage(
                                                model =
                                                    ImageRequest
                                                        .Builder(LocalContext.current)
                                                        .data(it.data.image)
                                                        .crossfade(true)
                                                        .build(),
                                                contentDescription = it.data.name,
                                                onState = {
                                                    if (it is AsyncImagePainter.State.Error) {
                                                        starringError = true
                                                    }
                                                },
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .effectForGenre(genre),
                                                contentScale = ContentScale.Crop,
                                            )
                                        } else {
                                            Box(
                                                Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Image(
                                                    painterResource(genre.icon),
                                                    null,
                                                    Modifier
                                                        .size(100.dp)
                                                        .gradientFill(
                                                            MaterialTheme.colorScheme.primary.gradientFade(),
                                                        ),
                                                )
                                            }
                                        }

                                        Box(
                                            Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .fillMaxHeight(.8f)
                                                .background(
                                                    fadeGradientBottom(genre.color),
                                                ),
                                        )

                                        Column(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                                    .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                it.data.name,
                                                style =
                                                    MaterialTheme.typography.headlineMedium.copy(
                                                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                                        color = genre.iconColor,
                                                    ),
                                            )

                                            Text(
                                                it.data.backstory,
                                                maxLines = 5,
                                                overflow = TextOverflow.Ellipsis,
                                                style =
                                                    MaterialTheme.typography.labelMedium.copy(
                                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                        color = genre.iconColor,
                                                    ),
                                            )
                                        }
                                    }
                                }
                            }

                            RequestSection.entries.forEach {
                                item(span = { GridItemSpan(columnCount) }) {
                                    section.miniSection(it, resume, onAction)
                                }
                            }

                            if (saga.isEnded) {
                                item(span = { GridItemSpan(columnCount) }) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        EmotionalProfileCard(
                                            saga = saga,
                                            modifier = Modifier.fillMaxWidth(),
                                        )

                                        if (saga.endMessage.isNotBlank()) {
                                            Text(
                                                text = saga.endMessage,
                                                style =
                                                    MaterialTheme.typography.labelMedium.copy(
                                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                        fontWeight = FontWeight.Light,
                                                        fontStyle = FontStyle.Italic,
                                                        textAlign = TextAlign.Center,
                                                        color =
                                                            MaterialTheme
                                                                .colorScheme
                                                                .onBackground
                                                                .copy(
                                                                    alpha = 0.7f,
                                                                ),
                                                    ),
                                                modifier =
                                                    Modifier
                                                        .padding(8.dp)
                                                        .fillMaxWidth(),
                                            )
                                        }
                                    }
                                }
                            }

                            if (BuildConfig.DEBUG) {
                                item(span = {
                                    GridItemSpan(columnCount)
                                }) {
                                    Button(
                                        onClick = {
                                            onAction(DetailAction.OpenLoreDebug)
                                        },
                                        shape = sagaShape(),
                                        colors =
                                            ButtonDefaults.buttonColors().copy(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = genre.iconColor,
                                            ),
                                        modifier =
                                            Modifier
                                                .padding(horizontal = 16.dp)
                                                .fillMaxWidth(),
                                    ) {
                                        Text(
                                            "Manage Story",
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }

                            item(span = {
                                GridItemSpan(columnCount)
                            }) {
                                Button(
                                    onClick = {
                                        onAction(DetailAction.Delete)
                                    },
                                    colors =
                                        ButtonDefaults.textButtonColors(
                                            contentColor = MaterialColor.Red400,
                                        ),
                                    modifier =
                                        Modifier
                                            .padding(horizontal = 16.dp)
                                            .fillMaxWidth(),
                                ) {
                                    Text(
                                        stringResource(R.string.saga_detail_delete_saga_button),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    innerContent(sharedTransitionScope, animatedVisibilityScope)
}
