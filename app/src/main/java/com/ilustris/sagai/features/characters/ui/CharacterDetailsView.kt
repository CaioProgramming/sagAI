package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.ui.SingleRelationShipCard
import com.ilustris.sagai.features.characters.ui.components.CharacterStats
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.domain.model.filterCharacterMessages
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.ui.TimelineCharacterAttachment
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.zoomAnimation
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
    val character = characterContent.data
    val characterColor = character.hexColor.hexToColor() ?: genre.color
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val messageCount = sagaContent.flatMessages().filterCharacterMessages(character).size
    val listState = rememberLazyListState()
    Box {
        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),
            listState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (character.image.isNotEmpty()) {
                item {
                    val size = if (character.image.isNotEmpty()) 350.dp else 100.dp

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(size)
                            .clipToBounds(),
                    ) {
                        AsyncImage(
                            character.image,
                            contentDescription = character.name,
                            contentScale = ContentScale.Crop,
                            modifier =
                                Modifier
                                    .clickable(enabled = character.emojified) {
                                        viewModel.regenerate(
                                            sagaContent,
                                            character,
                                        )
                                    }.fillMaxSize()
                                    .zoomAnimation()
                                    .clipToBounds()
                                    .effectForGenre(genre, useFallBack = character.emojified),
                        )

                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .fillMaxHeight(.7f)
                                .background(fadeGradientBottom()),
                        )

                        Text(
                            character.name,
                            textAlign = TextAlign.Center,
                            style =
                                MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = genre.headerFont(),
                                    brush = Brush.verticalGradient(characterColor.darkerPalette()),
                                ),
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                                    .reactiveShimmer(true)
                                    .fillMaxWidth(),
                        )
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
                            }.padding(16.dp)
                            .size(100.dp)
                            .gradientFill(characterColor.gradientFade()),
                    )
                }
            }

            item {
                Text(
                    character.profile.occupation,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontFamily = genre.bodyFont(),
                            color = characterColor,
                        ),
                    modifier = Modifier.padding(16.dp),
                )
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
                        "Mensagens",
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
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
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
                Column(Modifier.padding(16.dp).fillMaxWidth()) {
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

            if (characterContent.relationships.isNotEmpty()) {
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
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    )
                }

                item {
                    LazyRow {
                        items(
                            characterContent.relationships
                                .filter { it.relationshipEvents.isNotEmpty() }
                                .sortedByDescending { it.relationshipEvents.last().timestamp },
                        ) { relationContent ->
                            val currentId = character.id
                            val relatedCharacter =
                                when (currentId) {
                                    relationContent.characterOne.id -> relationContent.characterTwo
                                    relationContent.characterTwo.id -> relationContent.characterOne
                                    else -> null
                                }
                            relationContent.relationshipEvents.lastOrNull()?.let {
                                if (relatedCharacter != null) {
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
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    )
                }

                items(characterContent.events.sortedBy { it.timeline?.createdAt }) {
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
                            Modifier.padding(horizontal = 16.dp).clip(genre.shape()),
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
    if (isGenerating) {
        Dialog(
            onDismissRequest = { },
            properties =
                DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            StarryTextPlaceholder(
                modifier = Modifier.fillMaxSize().gradientFill(genre.gradient(true)),
            )
        }
    }
}
