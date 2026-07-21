package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
import com.ilustris.sagai.core.data.model.ImagePalette
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.characters.ui.components.VerticalLabel
import com.ilustris.sagai.features.emotional.ui.EmotionalProfileCard
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.playthrough.toPlaytimeFormat
import com.ilustris.sagai.features.saga.detail.data.model.SagaDetailResume
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.saga.detail.review.domain.ReviewGenerationState
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.components.views.HeroAction
import com.ilustris.sagai.ui.components.views.HeroMenuAction
import com.ilustris.sagai.ui.components.views.HeroOverflowMenu
import com.ilustris.sagai.ui.components.views.heroBottomCluster
import com.ilustris.sagai.ui.theme.characterDetailsTitleGradient
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.PaletteTheme
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.sagaHighlight
import com.ilustris.sagai.ui.theme.sagaShader
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.solidGradient
import com.ilustris.sagai.ui.theme.themeFilter
import com.ilustris.sagai.ui.theme.themeIcon
import com.ilustris.sagai.ui.theme.themePainter
import com.ilustris.sagai.ui.theme.themeVfx
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SagaDetailInitialContent(
    saga: Saga,
    section: DetailSectionView.InitialSection,
    resume: SagaDetailResume,
    imagePalette: ImagePalette?,
    reviewGenerationState: ReviewGenerationState,
    gridState: LazyGridState = rememberLazyGridState(),
    onAction: (DetailAction) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val columnCount = 2
    val genre = remember { saga.genre }
    var showSagaShare by remember { mutableStateOf(false) }

    PaletteTheme(imagePalette = imagePalette) {
    val adaptiveColor = MaterialTheme.colorScheme.background
    val adaptiveTextColor = MaterialTheme.colorScheme.onBackground

    Box(
        Modifier
            .fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
        ) {
            item(span = { GridItemSpan(columnCount) }) {
                sagaHeaderComponent(
                    saga = saga,
                    section = section,
                    imagePalette = imagePalette,
                    modifier =
                        Modifier.clickable(enabled = saga.icon.isBlank()) {
                            onAction(DetailAction.RegenerateIcon)
                        },
                    onAction = onAction,
                )
            }

            section.starring?.let {
                item(span = { GridItemSpan(columnCount) }) {
                    val shape = sagaShape()
                    var starringError by remember(it.data.id) {
                        mutableStateOf(false)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth()
                                .clip(shape)
                                .background(adaptiveTextColor.copy(alpha = 0.08f))
                                .clickable {
                                    onAction(
                                        DetailAction.OpenSection(
                                            RequestSection.CHARACTERS,
                                        ),
                                    )
                                }.padding(12.dp),
                    ) {
                        Box(Modifier.size(56.dp).clip(shape)) {
                            if (!starringError) {
                                AsyncImage(
                                    model =
                                        ImageRequest
                                            .Builder(LocalContext.current)
                                            .data(it.data.image)
                                            .crossfade(true)
                                            .build(),
                                    contentDescription = it.data.name,
                                    onState = { state ->
                                        if (state is AsyncImagePainter.State.Error) {
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
                                Image(
                                    painterResource(genre.icon),
                                    null,
                                    Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .gradientFill(
                                            MaterialTheme.colorScheme.primary.gradientFade(),
                                        ),
                                )
                            }
                        }

                        Column(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                        ) {
                            Text(
                                stringResource(R.string.starring),
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                        color = adaptiveTextColor.copy(alpha = 0.6f),
                                    ),
                            )
                            Text(
                                it.data.name,
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = adaptiveTextColor,
                                    ),
                            )
                        }

                        Icon(
                            painterResource(R.drawable.round_arrow_forward_ios_24),
                            contentDescription = null,
                            tint = adaptiveTextColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            RequestSection.entries.filterNot { it == RequestSection.BRAIN }.forEach {
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

            item(span = { GridItemSpan(columnCount) }) {
                SagaAboutSection(
                    saga = saga,
                    resume = resume,
                )
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
                            onClick = { onAction(DetailAction.OpenEmotionalReview) },
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
                                        color = adaptiveTextColor.copy(alpha = 0.7f),
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
                        shape = MaterialTheme.shapes.medium,
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
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .statusBarsPadding()
                    .padding(8.dp),
        ) {
            IconButton(
                onClick = { onAction(DetailAction.Back) },
                colors =
                    IconButtonDefaults.iconButtonColors().copy(
                        containerColor = adaptiveColor.copy(alpha = .5f),
                    ),
            ) {
                Icon(
                    painterResource(R.drawable.ic_back_left),
                    contentDescription = stringResource(R.string.back_button_description),
                    tint = adaptiveTextColor,
                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = { showSagaShare = true },
                colors =
                    IconButtonDefaults.iconButtonColors().copy(
                        containerColor = adaptiveColor.copy(alpha = .5f),
                    ),
            ) {
                Icon(
                    painterResource(R.drawable.ic_share),
                    contentDescription = stringResource(R.string.share),
                    tint = adaptiveTextColor,
                )
            }

            HeroOverflowMenu(
                tint = adaptiveTextColor,
                containerColor = adaptiveColor.copy(alpha = .5f),
                actions =
                    listOfNotNull(
                        if (BuildConfig.DEBUG) {
                            HeroMenuAction(
                                label = stringResource(R.string.debug_regenerate_image),
                            ) {
                                onAction(DetailAction.RegenerateIcon)
                            }
                        } else {
                            null
                        },
                        HeroMenuAction(
                            label = stringResource(R.string.saga_detail_delete_saga_button),
                            destructive = true,
                        ) {
                            onAction(DetailAction.Delete)
                        },
                    ),
            )
        }
    }
    }

    ShareSheet(
        saga = saga,
        isVisible = showSagaShare,
        shareType = ShareType.HISTORY,
        onDismiss = { showSagaShare = false },
    )
}

@Composable
fun sagaHeaderComponent(
    saga: Saga,
    modifier: Modifier,
    section: DetailSectionView.InitialSection? = null,
    imagePalette: ImagePalette? = null,
    onAction: (DetailAction) -> Unit = {},
) {
    val genre = saga.genre
    // Already palette-animated by the enclosing PaletteTheme — plain reads stay smooth for free.
    val adaptiveColor = MaterialTheme.colorScheme.background
    val adaptiveTextColor = MaterialTheme.colorScheme.onBackground
    val accentColor by animateColorAsState(
        targetValue = imagePalette?.vibrant ?: imagePalette?.dominant ?: MaterialTheme.colorScheme.primary,
        animationSpec = tween(1000),
    )
    val onAccentColor by animateColorAsState(
        targetValue = imagePalette?.onVibrant ?: imagePalette?.onDominant ?: MaterialTheme.colorScheme.onPrimary,
        animationSpec = tween(1000),
    )
    val titleGradient =
        remember(adaptiveTextColor, genre) {
            characterDetailsTitleGradient(adaptiveTextColor, genre.color)
        }

    Box(
        modifier =
            modifier
                .background(adaptiveColor)
                .fillMaxWidth()
                .fillMaxHeight(.4f),
    ) {
        val statusTag =
            if (saga.isEnded) stringResource(R.string.saga_detail_ended_tag) else null

        if (saga.icon.isBlank()) {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EndedSagaTag(saga.isEnded)
                genre.stylisedText(
                    saga.title,
                    modifier =
                        Modifier
                            .statusBarsPadding()
                            .padding(32.dp)
                            .fillMaxWidth()
                            .clickable(onClick = {
                                onAction(DetailAction.RegenerateIcon)
                            })
                            .gradientFill(
                                adaptiveColor.gradientFade(),
                            ).reactiveShimmer(
                                true,
                                shimmerColors = Color.White.shimmerize(),
                                repeatMode = RepeatMode.Restart,
                                duration = 10.seconds,
                            ),
                )
            }
        } else {
            AsyncImage(
                saga.icon,
                contentDescription = saga.title,
                modifier =
                    Modifier.fillMaxSize().themeFilter(
                        selectiveHighlight = true,
                    ),
                contentScale = ContentScale.Crop,
            )

            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(fadeGradientTop(adaptiveColor)),
            )

            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(fadeGradientBottom(adaptiveColor)),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
            ) {
                EndedSagaTag(saga.isEnded)

                genre.stylisedText(
                    text = saga.title,
                    modifier =
                        Modifier
                            .background(fadeGradientBottom(adaptiveColor))
                            .fillMaxWidth()
                            .padding(8.dp)
                            .gradientFill(adaptiveColor.gradientFade())
                            .reactiveShimmer(
                                true,
                                shimmerColors = Color.White.shimmerize(),
                                duration = 10.seconds,
                                repeatMode = RepeatMode.Restart,
                            ),
                )

                Column(
                    Modifier.background(adaptiveColor).fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (saga.isEnded.not()) {
                        Box(
                            modifier =
                                Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        onAction(DetailAction.OpenSection(RequestSection.BRAIN))
                                    },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                themePainter(),
                                contentDescription = stringResource(R.string.saga_brain_title),
                                tint = onAccentColor,
                                modifier = Modifier.padding(8.dp).fillMaxSize(),
                            )
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = .2f), CircleShape)
                                        .clickable {
                                            onAction(DetailAction.OpenSection(RequestSection.BRAIN))
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_cosmos),
                                    contentDescription = stringResource(R.string.saga_brain_title),
                                    tint = accentColor,
                                    modifier = Modifier.padding(8.dp).fillMaxSize(),
                                )
                            }

                            Box(
                                modifier =
                                    Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            onAction(DetailAction.OpenReview)
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    themePainter(),
                                    contentDescription = stringResource(R.string.saga_detail_recap_button),
                                    tint = accentColor,
                                    modifier = Modifier.padding(8.dp).fillMaxSize().themeVfx(),
                                )
                            }

                            Box(
                                modifier =
                                    Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = .2f), CircleShape)
                                        .clickable {
                                            onAction(DetailAction.OpenEmotionalReview)
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_emotional),
                                    contentDescription = stringResource(R.string.emotional_card_title),
                                    tint = accentColor,
                                    modifier = Modifier.padding(8.dp).fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EndedSagaTag(isEnded: Boolean) {
    if (isEnded) {
        Text(
            stringResource(R.string.saga_detail_ended_tag),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    shadow = Shadow(Color.White, blurRadius = 10f),
                    brush = sagaBrush(),
                ),
        )
    }
}

private const val ABOUT_DESCRIPTION_PREVIEW_LENGTH = 160

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SagaAboutSection(
    saga: Saga,
    resume: SagaDetailResume,
    modifier: Modifier = Modifier,
) {
    var showSheet by remember { mutableStateOf(false) }
    val isTruncated = saga.description.length > ABOUT_DESCRIPTION_PREVIEW_LENGTH
    val preview =
        if (isTruncated) {
            saga.description.take(ABOUT_DESCRIPTION_PREVIEW_LENGTH).trimEnd() + "…"
        } else {
            saga.description
        }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            stringResource(R.string.saga_detail_about_title, saga.title),
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontWeight = FontWeight.Bold,
                ),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            preview,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                ),
        )

        if (isTruncated) {
            Text(
                stringResource(R.string.saga_detail_about_more),
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontWeight = FontWeight.Bold,
                    ),
                modifier =
                    Modifier
                        .padding(top = 4.dp)
                        .clickable { showSheet = true },
            )
        }

        Spacer(Modifier.height(20.dp))

        SagaMetadataRow(
            stringResource(R.string.saga_detail_about_playtime),
            resume.playtime.toPlaytimeFormat(),
        )
        SagaMetadataRow(
            stringResource(R.string.saga_detail_about_created),
            saga.createdAt.formatDate(),
        )
        SagaMetadataRow(
            stringResource(R.string.saga_detail_about_theme),
            stringResource(saga.genre.title),
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    saga.title,
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            fontWeight = FontWeight.Bold,
                        ),
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    saga.description,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            textAlign = TextAlign.Justify,
                        ),
                )

                Spacer(Modifier.height(20.dp))

                SagaMetadataRow(
                    stringResource(R.string.saga_detail_about_playtime),
                    resume.playtime.toPlaytimeFormat(),
                )
                SagaMetadataRow(
                    stringResource(R.string.saga_detail_about_created),
                    saga.createdAt.formatDate(),
                )
                SagaMetadataRow(
                    stringResource(R.string.saga_detail_about_theme),
                    stringResource(saga.genre.title),
                )
            }
        }
    }
}

@Composable
private fun SagaMetadataRow(
    label: String,
    value: String,
) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(
            label.uppercase(),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = LocalContentColor.current.copy(alpha = 0.6f),
                ),
        )
        Text(
            value,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                ),
        )
    }
}
