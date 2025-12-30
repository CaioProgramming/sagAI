package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.ui.SingleRelationShipCard
import com.ilustris.sagai.features.characters.ui.components.CharacterStats
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.domain.model.filterCharacterMessages
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.ui.TimelineCharacterAttachment
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import effectForGenre
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CharacterDetailsView(
    sagaId: String? = null,
    characterId: String? = null,
    navHostController: NavHostController,
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()
    val character by viewModel.character.collectAsStateWithLifecycle()

    LaunchedEffect(saga) {
        if (saga == null) {
            viewModel.loadSagaAndCharacter(sagaId, characterId)
        }
    }

    LaunchedEffect(character) {
        character?.data?.image?.let {
            if (it.isNotEmpty()) {
                viewModel.segmentCharacterImage(it)
            }
        }
    }

    AnimatedContent(saga) {
        if (it != null) {
            CharacterDetailsContent(
                it,
                character,
            )
        } else {
            SparkIcon(
                brush = gradientAnimation(holographicGradient),
                duration = 1.seconds,
                modifier = Modifier.size(50.dp),
            )
        }
    }
}

@OptIn(
    ExperimentalAnimationApi::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class,
)
@Composable
fun CharacterDetailsContent(
    sagaContent: SagaContent,
    characterContent: CharacterContent?,
    openEvent: (Timeline?) -> Unit = {},
) {
    val viewModel: CharacterDetailsViewModel = hiltViewModel()
    val genre = sagaContent.data.genre

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    var shareCharacter by remember { mutableStateOf(false) }
    var currentCharacter by remember { mutableStateOf<CharacterContent?>(null) }
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()

    val blurEffect by animateDpAsState(if (isGenerating) 15.dp else 0.dp)

    LaunchedEffect(characterContent) {
        viewModel.init(characterContent, sagaContent)
        currentCharacter = characterContent
    }

    AnimatedContent(
        targetState = currentCharacter,
        transitionSpec = {
            fadeIn(tween(3600)) togetherWith fadeOut(tween(200))
        },
        modifier = Modifier.blur(blurEffect),
    ) { character ->
        if (character != null) {
            CharacterDetailsLoaded(
                sagaContent = sagaContent,
                characterContent = character,
                openEvent = openEvent,
                viewModel = viewModel,
                onShareCharacter = { shareCharacter = true },
            )
        }
    }

    StarryLoader(
        isGenerating,
        loadingMessage = loadingMessage,
        textStyle =
            MaterialTheme.typography.labelLarge.copy(
                genre.color,
                fontFamily = genre.bodyFont(),
            ),
        brushColors = genre.colorPalette(),
    )

    // State for ShareSheet - moved to parent scope

    // Reset share state when character changes to prevent stale state
    LaunchedEffect(characterContent?.data?.id) {
        shareCharacter = false
    }

    // ShareSheet - only show if we have a character
    if (shareCharacter && characterContent != null) {
        ShareSheet(sagaContent, shareCharacter, ShareType.CHARACTER, characterContent, onDismiss = {
            shareCharacter = false
        })
    }
}

@OptIn(
    ExperimentalAnimationApi::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class,
)
@Composable
private fun CharacterDetailsLoaded(
    sagaContent: SagaContent,
    characterContent: CharacterContent,
    openEvent: (Timeline?) -> Unit = {},
    viewModel: CharacterDetailsViewModel,
    onShareCharacter: () -> Unit = {},
) {
    val genre = sagaContent.data.genre
    val listState = rememberLazyListState()
    val timelineEvents = remember { sagaContent.flatEvents().map { it.data } }
    val characterEvents = remember { characterContent.sortEventsByTimeline(timelineEvents) }
    val characterRelations = remember { characterContent.sortRelationsByTimeline(timelineEvents) }

    val originalBitmap = viewModel.originalBitmap.collectAsStateWithLifecycle().value
    val segmentedBitmap = viewModel.segmentedBitmap.collectAsStateWithLifecycle().value

    val smartZoom = characterContent.data.smartZoom
    val needsZoom = smartZoom?.needsZoom ?: false

    var titleAlpha by remember {
        mutableFloatStateOf(if (needsZoom) 0f else 1f)
    }

    var scale by remember {
        mutableFloatStateOf(smartZoom?.scale ?: 1f)
    }

    var imageTranslationX by remember {
        mutableFloatStateOf(smartZoom?.translationX ?: 0f)
    }

    var imageTranslationY by remember {
        mutableFloatStateOf((smartZoom?.translationY ?: 0f) * 1000f + 200f)
    }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 2500, easing = EaseIn),
    )
    val animatedTranslationX by animateFloatAsState(
        targetValue = imageTranslationX,
        animationSpec = tween(durationMillis = 1000 * 3, easing = FastOutSlowInEasing),
    )
    val animatedTranslationY by animateFloatAsState(
        targetValue = imageTranslationY,
        animationSpec = tween(durationMillis = 1000 * 3, easing = FastOutSlowInEasing),
    )

    val titleAnimation by animateFloatAsState(
        targetValue = titleAlpha,
        animationSpec = tween(durationMillis = 1500),
    )

    LaunchedEffect(characterContent) {
        if (needsZoom.not()) {
            titleAlpha = 1f
            scale = 1f
            imageTranslationX = 0f
            imageTranslationY = 0f
            return@LaunchedEffect
        } else {
            titleAlpha = 0f
            delay(2.seconds)
            scale = 1f
            imageTranslationX = 0f
            imageTranslationY = 0f
            delay(1.seconds)
            titleAlpha = 1f
        }
    }

    LaunchedEffect(characterContent) {
        viewModel.segmentCharacterImage(characterContent.data.image)
    }

    AnimatedContent(
        characterContent.data,
        transitionSpec = {
            fadeIn(tween(700)) togetherWith fadeOut(tween(200))
        },
    ) { character ->
        val characterColor = character.hexColor.hexToColor() ?: genre.color
        val messageCount = sagaContent.flatMessages().filterCharacterMessages(character).size

        Box {
            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                listState,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    if (character.image.isNotBlank()) {
                        val deepEffectAvailable =
                            smartZoom != null && originalBitmap != null && segmentedBitmap != null

                        AnimatedContent(
                            deepEffectAvailable,
                            transitionSpec = {
                                fadeIn(
                                    tween(
                                        1200,
                                        easing = EaseIn,
                                    ),
                                ) togetherWith fadeOut(tween(1500, easing = FastOutSlowInEasing))
                            },
                        )
                        {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(450.dp)
                                    .clipToBounds(),
                            ) {
                                if (it && deepEffectAvailable) {
                                    DepthLayout(
                                        originalImage = originalBitmap,
                                        segmentedImage = segmentedBitmap,
                                        imageModifier =
                                            Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(
                                                    scaleX = animatedScale,
                                                    scaleY = animatedScale,
                                                    translationX = animatedTranslationX,
                                                    translationY = animatedTranslationY,
                                                    transformOrigin = TransformOrigin.Center,
                                                ).effectForGenre(
                                                    genre,
                                                    useFallBack = character.emojified,
                                                ),
                                    ) {
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .background(fadedGradientTopAndBottom()),
                                        )
                                        Text(
                                            text = "${character.name} ${(character.lastName ?: emptyString())}".trim(),
                                            textAlign = TextAlign.Center,
                                            modifier =
                                                Modifier
                                                    .alpha(titleAnimation)
                                                    .fillMaxWidth()
                                                    .reactiveShimmer(true)
                                                    .offset(y = 4f.unaryMinus().dp)
                                                    .align(Alignment.TopCenter),
                                            style =
                                                MaterialTheme.typography.displayMedium.copy(
                                                    fontFamily = genre.headerFont(),
                                                    textAlign = TextAlign.Center,
                                                    brush =
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                genre.color,
                                                                characterColor,
                                                                genre.iconColor,
                                                            ),
                                                        ),
                                                    shadow =
                                                        Shadow(
                                                            genre.color,
                                                            blurRadius = 15f,
                                                        ),
                                                ),
                                        )
                                    }

                                    Column(
                                        modifier =
                                            Modifier
                                                .background(fadeGradientBottom())
                                                .align(Alignment.BottomCenter)
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Image(
                                            painterResource(R.drawable.ic_spark),
                                            stringResource(id = R.string.share_character_cd),
                                            modifier =
                                                Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        onShareCharacter()
                                                    },
                                            colorFilter = ColorFilter.tint(characterColor),
                                        )

                                        Text(
                                            character.profile.occupation,
                                            style =
                                                MaterialTheme.typography.titleSmall.copy(
                                                    fontFamily = genre.bodyFont(),
                                                    color = characterColor,
                                                    textAlign = TextAlign.Center,
                                                ),
                                        )

                                        character.nicknames?.let {
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
                                } else {
                                    AsyncImage(
                                        character.image,
                                        contentDescription = character.name,
                                        contentScale = ContentScale.Crop,
                                        modifier =
                                            Modifier
                                                .clickable(enabled = character.emojified || character.image.isEmpty()) {
                                                    viewModel.regenerate(
                                                        sagaContent,
                                                        character,
                                                    )
                                                }.fillMaxSize()
                                                .graphicsLayer(
                                                    scaleX = animatedScale,
                                                    scaleY = animatedScale,
                                                    translationX = animatedTranslationX,
                                                    translationY = animatedTranslationY,
                                                    transformOrigin = TransformOrigin.Center,
                                                ).effectForGenre(
                                                    genre,
                                                    useFallBack = character.emojified,
                                                ),
                                    )

                                    Box(
                                        Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(fadedGradientTopAndBottom()),
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomCenter),
                                    ) {
                                        Text(
                                            character.profile.occupation,
                                            style =
                                                MaterialTheme.typography.titleSmall.copy(
                                                    fontFamily = genre.bodyFont(),
                                                    color = characterColor,
                                                    textAlign = TextAlign.Center,
                                                ),
                                        )

                                        Text(
                                            text = "${character.name} ${(character.lastName ?: emptyString())}".trim(),
                                            textAlign = TextAlign.Center,
                                            modifier =
                                                Modifier
                                                    .alpha(titleAnimation),
                                            style =
                                                MaterialTheme.typography.displayMedium.copy(
                                                    fontFamily = genre.headerFont(),
                                                    textAlign = TextAlign.Center,
                                                    brush =
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                genre.color,
                                                                characterColor,
                                                                genre.iconColor,
                                                            ),
                                                        ),
                                                    shadow =
                                                        Shadow(
                                                            genre.color,
                                                            blurRadius = 15f,
                                                        ),
                                                ),
                                        )
                                        character.nicknames?.let {
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
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                null,
                                Modifier
                                    .clickable {
                                        viewModel.regenerate(
                                            sagaContent,
                                            character,
                                        )
                                    }.padding(16.dp)
                                    .size(100.dp)
                                    .gradientFill(characterColor.gradientFade()),
                            )

                            Text(
                                text = "${character.name} ${(character.lastName ?: emptyString())}".trim(),
                                textAlign = TextAlign.Center,
                                style =
                                    MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = genre.headerFont(),
                                        brush =
                                            Brush.verticalGradient(
                                                listOf(
                                                    characterColor,
                                                    genre.iconColor,
                                                    genre.color,
                                                ),
                                            ),
                                    ),
                            )
                        }
                    }
                }

                item { CharacterStats(character = character, genre = genre) }

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
                                ),
                        )

                        AnimatedContent(
                            targetState = characterResume ?: character.backstory,
                            transitionSpec = {
                                fadeIn(tween(1000)) togetherWith fadeOut(tween(100))
                            },
                        ) { text ->
                            val textColor by animateColorAsState(
                                if (isSummarizing.not()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
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
                                ),
                        )

                        Text(
                            character.profile.personality,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = genre.bodyFont(),
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
                                sagaContent
                                    .findCharacter(
                                        relationContent
                                            .getCharacterExcluding(
                                                character,
                                            ).id,
                                    )?.let { relatedCharacter ->
                                        SingleRelationShipCard(
                                            saga = sagaContent,
                                            character = relatedCharacter,
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
                }

                if (characterContent.events.isNotEmpty()) {
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
                                ),
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                        )
                    }

                    items(characterEvents) {
                        TimelineCharacterAttachment(
                            it,
                            sagaContent,
                            showIndicator = true,
                            showSpark = character == sagaContent.mainCharacter,
                            isLast = it == characterContent.events.last(),
                            onSelectReference = {
                                openEvent(it)
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
                character.name,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = genre.headerFont(),
                        textAlign = TextAlign.Center,
                        color = characterColor,
                    ),
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .alpha(alpha)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                        .reactiveShimmer(true)
                        .fillMaxWidth(),
            )
        }
    }
}
