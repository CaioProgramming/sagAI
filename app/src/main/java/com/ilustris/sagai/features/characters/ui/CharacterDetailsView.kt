package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.components.CharacterSection
import com.ilustris.sagai.features.characters.ui.components.CharacterStats
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.model.filterCharacterMessages
import com.ilustris.sagai.features.timeline.ui.TimeLineCard
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.holographicGradient
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
    val messageCount by viewModel.messageCount.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

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
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
) {
    val genre = sagaContent.data.genre
    val character = characterContent.data
    val characterColor = character.hexColor.hexToColor() ?: genre.color
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val messageCount = sagaContent.flatMessages().filterCharacterMessages(character).size
    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),
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
                                .fillMaxSize()
                                .zoomAnimation()
                                .clipToBounds()
                                .effectForGenre(genre),
                    )

                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .background(
                                fadeGradientTop(),
                            ).height(size * .5f)
                            .fillMaxWidth(),
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(.05f)
                            .background(
                                fadeGradientBottom(),
                            ),
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

        stickyHeader {
            Text(
                character.name,
                textAlign = TextAlign.Center,
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = genre.headerFont(),
                        brush = characterColor.gradientFade(),
                    ),
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 24.dp)
                        .fillMaxWidth(),
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
                            fontFamily = genre.headerFont(),
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
            CharacterSection(
                title = "Backstory",
                content = character.backstory,
                genre = genre,
            )
        }

        item {
            CharacterSection(
                title = "Personality",
                content = character.details.personality,
                genre = genre,
            )
        }

        item {
            CharacterSection(
                title = "Appearance",
                content = character.details.appearance,
                genre = genre,
            )
        }

        if (characterContent.events.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.saga_detail_timeline_section_title),
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = genre.headerFont(),
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            items(characterContent.events) {
                TimeLineCard(it, genre)
            }
        }
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SparkLoader(
                    brush = gradientAnimation(genresGradient(), duration = 2.seconds),
                    modifier = Modifier.size(100.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun CharacterDetailsDialogPreview() {
    val character =
        CharacterContent(
            Character(
                name = "Character Name",
                backstory = "Character backstory",
                image = "https://www.example.com/image.jpg",
                details = Details(occupation = "Occupation", race = "Human"),
            ),
        )
    val genre = Genre.FANTASY
    SagAIScaffold {
        CharacterDetailsContent(
            SagaContent(
                data =
                    Saga(
                        title = "Saga Title",
                        description = "Saga Description",
                        genre = genre,
                    ),
                mainCharacter = character,
                acts = emptyList(),
                characters = emptyList(),
            ),
            character,
        )
    }
}
