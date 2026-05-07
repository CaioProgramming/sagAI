package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.model.ImagePalette
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterDetailData
import com.ilustris.sagai.features.characters.relations.ui.SingleRelationShipCard
import com.ilustris.sagai.features.characters.ui.components.CharacterStats
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.ui.TimelineCharacterAttachment
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.shimmerize
import kotlin.time.Duration.Companion.seconds

@Composable
fun CharacterDetailsView(
    characterId: Int? = null,
    onBack: () -> Unit = {},
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
) {
    val detailData by viewModel.characterDetailData.collectAsStateWithLifecycle()

    LaunchedEffect(characterId) {
        viewModel.loadCharacterDetails(characterId)
    }

    AnimatedContent(detailData, transitionSpec = {
        slideInVertically { -it } togetherWith fadeOut()
    }) {
        if (it != null) {
            CharacterDetailsContent(it)
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                SparkIcon(
                    brush = gradientAnimation(holographicGradient),
                    duration = 1.seconds,
                    modifier = Modifier.size(50.dp),
                )
            }
        }
    }
}

@OptIn(
    ExperimentalAnimationApi::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class,
)
@Composable
fun CharacterDetailsContent(
    detailData: CharacterDetailData,
    openEvent: (Timeline?) -> Unit = {},
) {
    val viewModel: CharacterDetailsViewModel = hiltViewModel()
    val genre = detailData.saga.genre
    val resolvedColor = genre.resolveColor()

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val imagePalette by viewModel.imagePalette.collectAsStateWithLifecycle()
    var shareCharacter by remember { mutableStateOf(false) }

    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val imageReasoning by viewModel.imageReasoning.collectAsStateWithLifecycle()

    val blurEffect by animateDpAsState(if (isGenerating) 15.dp else 0.dp)

    Box(modifier = Modifier.blur(blurEffect)) {
        CharacterDetailsLoaded(
            detailData = detailData,
            openEvent = openEvent,
            viewModel = viewModel,
            onShareCharacter = { shareCharacter = true },
            imagePalette = imagePalette,
        )
    }

    StarryLoader(
        isLoading = isGenerating,
        loadingMessage = loadingMessage,
        subtitle = imageReasoning,
        textStyle =
            MaterialTheme.typography.labelLarge.copy(
                resolvedColor,
                fontFamily = genre.bodyFont(),
            ),
        brushColors = genre.colorPalette(),
    )

    LaunchedEffect(detailData.character.id) {
        shareCharacter = false
    }

    if (shareCharacter) {
        val characterContent =
            remember(detailData.character) { CharacterContent(data = detailData.character) }
        val sagaContent = remember(detailData.saga) { SagaContent(data = detailData.saga) }
        ShareSheet(sagaContent, shareCharacter, ShareType.CHARACTER, characterContent, onDismiss = {
            shareCharacter = false
        })
    }

    val showPremiumSheet by viewModel.showPremiumSheet.collectAsStateWithLifecycle()
    if (showPremiumSheet) {
        OnboardingDialog(
            type = OnboardingType.PREMIUM_GUIDE,
            force = true,
            onDismiss = { viewModel.togglePremiumSheet() },
        )
    }
}

@OptIn(
    ExperimentalAnimationApi::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class,
)
@Composable
private fun CharacterDetailsLoaded(
    detailData: CharacterDetailData,
    viewModel: CharacterDetailsViewModel,
    onShareCharacter: () -> Unit = {},
    imagePalette: ImagePalette? = null,
    openEvent: (Timeline?) -> Unit = {},
) {
    val saga = detailData.saga
    val character = detailData.character
    val genre = saga.genre
    val resolvedColor = genre.resolveColor()
    genre.resolveIconColor()

    val adaptiveColor by animateColorAsState(
        targetValue = imagePalette?.dominant ?: MaterialTheme.colorScheme.background,
        animationSpec = tween(1000),
    )
    val adaptiveTextColor by animateColorAsState(
        targetValue = imagePalette?.onDominant ?: MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(1000),
    )

    val listState = rememberLazyListState()
    val characterEvents = detailData.events
    val characterRelations = detailData.relationships
    val messageCount by viewModel.messageCount.collectAsStateWithLifecycle()

    // Lite wrappers to satisfy legacy components without reloading the full graph
    val liteSagaContent =
        remember(saga) {
            SagaContent(data = saga)
        }
    remember(character) { CharacterContent(data = character) }

    AnimatedContent(
        character,
        transitionSpec = {
            fadeIn(tween(700)) togetherWith fadeOut(tween(200))
        },
    ) { characterData ->
        var imageError by remember { mutableStateOf(false) }
        val characterColor = characterData.hexColor.hexToColor() ?: resolvedColor

        Box(
            modifier =
                Modifier.background(
                    adaptiveColor,
                ),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize(),
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    if (characterData.image.isNotBlank() && !imageError) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxHeight(.6f),
                        ) {
                            DepthLayout(
                                imagePath = characterData.image,
                                onLoadError = { imageError = true },
                                modifier =
                                    Modifier
                                        .fillParentMaxHeight(.6f)
                                        .fillMaxSize()
                                        .clickable(enabled = characterData.emojified || characterData.image.isEmpty()) {
                                            viewModel.regenerate(
                                                saga,
                                                characterData,
                                            )
                                        },
                                imageModifier =
                                    Modifier
                                        .clipToBounds()
                                        .fillMaxSize()
                                        .effectForGenre(
                                            genre,
                                        ).graphicsLayer(
                                            translationY = 120f,
                                        ),
                            ) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopCenter)
                                        .fillMaxSize()
                                        .background(fadeGradientTop(adaptiveColor)),
                                ) {
                                    genre.stylisedText(
                                        text = "${characterData.name} ${(characterData.lastName ?: emptyString())}".trim(),
                                        modifier =
                                            Modifier
                                                .align(Alignment.TopCenter)
                                                .statusBarsPadding()
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                                .gradientFill(Brush.verticalGradient(characterColor.darkerPalette()))
                                                .reactiveShimmer(true, characterColor.shimmerize()),
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier =
                                    Modifier
                                        .background(fadeGradientBottom(adaptiveColor))
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter),
                            ) {
                                Image(
                                    painterResource(R.drawable.ic_spark),
                                    stringResource(id = R.string.share_character_cd),
                                    modifier =
                                        Modifier
                                            .padding(top = 16.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                onShareCharacter()
                                            },
                                    colorFilter = ColorFilter.tint(characterColor),
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                characterData.profile.occupation,
                                style =
                                    MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = genre.bodyFont(),
                                        color = adaptiveTextColor,
                                        textAlign = TextAlign.Center,
                                    ),
                            )

                            characterData.nicknames?.let {
                                if (it.isNotEmpty()) {
                                    Text(
                                        text =
                                            stringResource(
                                                id = R.string.character_details_aka,
                                                it.joinToString(", "),
                                            ),
                                        style =
                                            MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = genre.bodyFont(),
                                                color = adaptiveTextColor,
                                                textAlign = TextAlign.Center,
                                            ),
                                    )
                                }
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painterResource(genre.icon),
                                null,
                                Modifier
                                    .statusBarsPadding()
                                    .clickable {
                                        viewModel.regenerate(
                                            saga,
                                            characterData,
                                        )
                                    }.padding(16.dp)
                                    .size(100.dp)
                                    .gradientFill(characterColor.gradientFade()),
                            )

                            genre.stylisedText(
                                text = "${characterData.name} ${(characterData.lastName ?: emptyString())}".trim(),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .gradientFill(Brush.verticalGradient(characterColor.darkerPalette()))
                                        .reactiveShimmer(true),
                            )

                            Text(
                                characterData.profile.occupation,
                                style =
                                    MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = genre.bodyFont(),
                                        color = characterColor,
                                        textAlign = TextAlign.Center,
                                    ),
                            )

                            characterData.nicknames?.let {
                                if (it.isNotEmpty()) {
                                    Text(
                                        text =
                                            stringResource(
                                                id = R.string.character_details_aka,
                                                it.joinToString(", "),
                                            ),
                                        style =
                                            MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = genre.bodyFont(),
                                                color = characterColor.copy(alpha = 0.8f),
                                                textAlign = TextAlign.Center,
                                            ),
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    CharacterStats(
                        character = characterData,
                        genre = genre,
                        contentColor = adaptiveTextColor,
                    )
                }

                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            messageCount.toString(),
                            style =
                                MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = genre.bodyFont(),
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    color = adaptiveTextColor,
                                ),
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                        )

                        Text(
                            stringResource(id = R.string.messages_label),
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = genre.bodyFont(),
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Center,
                                    color = adaptiveTextColor,
                                ),
                        )
                    }
                }

                item {
                    val characterResume by viewModel.characterResume.collectAsStateWithLifecycle()
                    val isSummarizing by viewModel.isSummarizing.collectAsStateWithLifecycle()

                    Column(
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.character_form_title_backstory),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = adaptiveTextColor,
                                ),
                        )

                        AnimatedContent(
                            targetState = characterResume ?: characterData.backstory,
                            transitionSpec = {
                                fadeIn(tween(1000)) togetherWith fadeOut(tween(100))
                            },
                        ) { text ->
                            val textColor by animateColorAsState(
                                if (isSummarizing.not()) adaptiveTextColor else adaptiveColor,
                            )
                            Text(
                                text,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = genre.bodyFont(),
                                        color = textColor,
                                    ),
                                modifier =
                                    Modifier
                                        .reactiveShimmer(
                                            isSummarizing,
                                            targetValue = 1000f,
                                            repeatMode = RepeatMode.Restart,
                                        ).padding(vertical = 16.dp),
                            )
                        }
                    }
                }

                item {
                    Column(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.personality_title),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = adaptiveTextColor,
                                ),
                        )

                        Text(
                            characterData.profile.personality,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = adaptiveTextColor,
                                ),
                        )
                    }
                }

                if (characterRelations.isNotEmpty()) {
                    item {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(.1f),
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                        )
                    }

                    item {
                        Text(
                            stringResource(R.string.saga_detail_relationships_section_title),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = adaptiveTextColor,
                                ),
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                        )
                    }

                    item {
                        LazyRow {
                            items(
                                characterRelations,
                            ) { relationContent ->
                                val relatedCharacter =
                                    relationContent.getCharacterExcluding(characterData)
                                SingleRelationShipCard(
                                    saga = liteSagaContent,
                                    character = CharacterContent(data = relatedCharacter),
                                    content = relationContent,
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .requiredWidthIn(max = 300.dp),
                                )
                            }
                        }
                    }
                }

                if (characterEvents.isNotEmpty()) {
                    item {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(.1f),
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                        )
                    }
                    item {
                        Text(
                            stringResource(R.string.saga_detail_timeline_section_title),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = adaptiveTextColor,
                                ),
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                        )
                    }

                    items(items = characterEvents) { characterEvent ->
                        TimelineCharacterAttachment(
                            eventDetails = characterEvent,
                            sagaContent = liteSagaContent,
                            showIndicator = true,
                            showSpark = false, // Simplified for now
                            isLast = characterEvent == characterEvents.last(),
                            onSelectReference = { timeline ->
                                openEvent(timeline)
                            },
                            modifier =
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .clip(genre.shape()),
                        )
                    }
                }
            }

            val alpha by animateFloatAsState(
                if (listState.canScrollBackward.not()) 0f else 1f,
                animationSpec = tween(1500),
            )
            Text(
                "${characterData.name} ${characterData.lastName ?: emptyString()}",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = genre.headerFont(),
                        textAlign = TextAlign.Center,
                        color = adaptiveTextColor,
                    ),
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .alpha(alpha)
                        .background(adaptiveColor)
                        .padding(16.dp)
                        .reactiveShimmer(true)
                        .fillMaxWidth(),
            )
        }
    }
}
