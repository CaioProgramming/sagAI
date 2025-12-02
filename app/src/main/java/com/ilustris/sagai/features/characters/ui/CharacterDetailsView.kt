package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import effectForGenre
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
            character?.let { char ->
                CharacterDetailsContent(
                    it,
                    char,
                )
            }
        } else {
            SparkIcon(
                brush = gradientAnimation(holographicGradient),
                duration = 1.seconds,
                modifier = Modifier.size(50.dp),
            )
        }
    }
}

@Composable
fun CharacterDetailsContent(
    sagaContent: SagaContent,
    characterContent: CharacterContent,
    openEvent: (Timeline?) -> Unit = {},
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
) {
    val genre = sagaContent.data.genre

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val timelineEvents = remember { sagaContent.flatEvents().map { it.data } }
    var shareCharacter by remember { mutableStateOf(false) }
    val characterEvents = remember { characterContent.sortEventsByTimeline(timelineEvents) }
    val characterRelations = remember { characterContent.sortRelationsByTimeline(timelineEvents) }

    val originalBitmap = viewModel.originalBitmap.collectAsStateWithLifecycle().value
    val segmentedBitmap = viewModel.segmentedBitmap.collectAsStateWithLifecycle().value

    LaunchedEffect(characterContent) {
        characterContent.data.image.let {
            if (it.isNotEmpty()) {
                viewModel.segmentCharacterImage(it)
            }
        }
    }

    val sheetBlur by animateDpAsState(
        if (isGenerating) 25.dp else 0.dp
    )

    AnimatedContent(
        characterContent.data,
        transitionSpec = {
            fadeIn(tween(700)) togetherWith fadeOut(tween(200))
        },
    ) { character ->
        val characterColor = character.hexColor.hexToColor() ?: genre.color
        val messageCount = sagaContent.flatMessages().filterCharacterMessages(character).size

        Box(modifier = Modifier.blur(sheetBlur)) {
            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                listState,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (character.image.isNotEmpty()) {
                    item {
                        val deepEffectAvailable = originalBitmap != null && segmentedBitmap != null
                        AnimatedContent(
                            deepEffectAvailable,
                            transitionSpec = { fadeIn(tween(700)) togetherWith fadeOut(tween(200)) }) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(500.dp)
                            ) {

                                if (deepEffectAvailable) {
                                    DepthLayout(
                                        originalImage = originalBitmap,
                                        segmentedImage = segmentedBitmap,
                                        modifier = Modifier.fillMaxSize(),
                                        imageModifier = Modifier.effectForGenre(genre)
                                    ) {
                                        Text(
                                            text = "${character.name} ${(character.lastName ?: emptyString())}".trim(),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .reactiveShimmer(true)
                                                .padding(12.dp)
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
                                                    shadow = Shadow(genre.color, blurRadius = 15f),
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
                                                        shareCharacter = true
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
                                                    text = "aka: ${
                                                        character.nicknames.joinToString(
                                                            ", "
                                                        )
                                                    }",
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
                                                }
                                                .fillMaxSize()
                                                .clipToBounds()
                                                .effectForGenre(
                                                    genre,
                                                    useFallBack = character.emojified
                                                ),
                                    )

                                    Box(
                                        Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(fadeGradientBottom()),
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
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
                                                    shadow = Shadow(genre.color, blurRadius = 15f),
                                                ),
                                        )
                                        character.nicknames?.let {
                                            if (it.isNotEmpty()) {
                                                Text(
                                                    text = "aka: ${
                                                        character.nicknames.joinToString(
                                                            ", "
                                                        )
                                                    }",
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

                    }
                } else {
                    item {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            Modifier
                                .clickable {
                                    viewModel.regenerate(
                                        sagaContent,
                                        character,
                                    )
                                }
                                .padding(16.dp)
                                .size(100.dp)
                                .gradientFill(characterColor.gradientFade()),
                        )
                    }
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            character.nicknames?.let {
                                if (it.isNotEmpty()) {
                                    Text(
                                        text = "aka: ${character.nicknames.joinToString(", ")}",
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

                    item {
                        Text(
                            character.profile.occupation,
                            style =
                                MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = characterColor,
                                    textAlign = TextAlign.Center,
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                        )
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

                        Text(
                            character.backstory,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                ),
                        )
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
                                sagaContent.findCharacter(
                                    relationContent.getCharacterExcluding(
                                        character
                                    ).id
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

    StarryLoader(
        isGenerating,
        null,
        useAsDialog = false,
        textStyle =
            MaterialTheme.typography.headlineMedium.copy(
                genre.color,
                fontFamily = genre.bodyFont(),
            ),
        brushColors = genre.colorPalette(),
    )

    if (shareCharacter) {
        ShareSheet(sagaContent, shareCharacter, ShareType.CHARACTER, characterContent, onDismiss = {
            shareCharacter = false
        })
    }
}
