package com.ilustris.sagai.features.saga.detail.ui

import ai.atick.material.MaterialColor
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.ilustris.sagai.features.playthrough.toPlaytimeFormat
import com.ilustris.sagai.features.saga.detail.data.model.SagaDetailResume
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.review.domain.ReviewGenerationState
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.components.MorphingThemeIcon
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.sagaHighlight
import com.ilustris.sagai.ui.theme.sagaShader
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.themeIcon

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SagaDetailInitialContent(
    saga: Saga,
    section: DetailSectionView.InitialSection,
    resume: SagaDetailResume,
    reviewGenerationState: ReviewGenerationState,
    gridState: LazyGridState = rememberLazyGridState(),
    onAction: (DetailAction) -> Unit = {},
    showTitleOnly: Boolean = false,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val columnCount = 2
    val genre = remember { saga.genre }
    var iconError by remember(saga.id) { mutableStateOf(false) }

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
                                if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                    with(sharedTransitionScope) {
                                        Modifier.sharedBounds(
                                            rememberSharedContentState(key = "saga_${saga.id}_title"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                        )
                                    }
                                } else {
                                    Modifier
                                },
                            ).fillMaxWidth()
                            .align(Alignment.Center)
                            .reactiveShimmer(true)
                            .padding(8.dp),
                )
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                ) {
                    item(span = { GridItemSpan(columnCount) }) {
                        sagaHeaderComponent(
                            saga,
                            modifier =
                                Modifier.clickable(enabled = saga.icon.isBlank()) {
                                    onAction(DetailAction.RegenerateIcon)
                                },
                        )
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
                                reviewGenerationState = reviewGenerationState,
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
                            section.miniSection(
                                it,
                                resume,
                                onAction,
                                sharedTransitionScope,
                                animatedVisibilityScope,
                            )
                        }
                    }

                    if (saga.isEnded) {
                        item(span = { GridItemSpan(columnCount) }) {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
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
                                    stringResource(R.string.saga_detail_manage_story),
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

                AnimatedVisibility(
                    gridState.canScrollBackward,
                    modifier = Modifier.align(Alignment.TopCenter),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    SagaTopBar(
                        title = saga.title,
                        genre = genre,
                        onBackClick = {
                            onAction(DetailAction.Back)
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun sagaHeaderComponent(
    saga: Saga,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .animateContentSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (saga.icon.isBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MorphingThemeIcon(
                    modifier = Modifier.size(64.dp),
                    brush = sagaBrush(),
                    glowIntensity = 0.5f,
                )
                saga.genre.stylisedText(
                    saga.title,
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 36.dp)
                            .fillMaxWidth(),
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
                contentScale = ContentScale.Crop,
                placeholder = themeIcon(),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.45f)
                        .sagaShader()
                        .sagaHighlight(),
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(fadeGradientBottom()),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (saga.isEnded) {
                    Row(
                        Modifier
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.shapes.large,
                            ).padding(4.dp)
                            .alpha(.6f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MorphingThemeIcon(
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            glowIntensity = 0.35f,
                        )

                        Text(
                            stringResource(R.string.chat_card_saga_ended),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                ),
                        )
                    }
                }

                saga.genre.stylisedText(
                    saga.title,
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 36.dp)
                            .fillMaxWidth()
                            .padding(8.dp),
                )
            }
        }
    }
}
