package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.components.CharacterSection
import com.ilustris.sagai.features.characters.ui.components.CharacterStats
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.zoomAnimation
import effectForGenre
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

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
                    messageCount,
                ) {
                    navHostController.popBackStack()
                }
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
    character: Character,
    messageCount: Int,
    onBack: () -> Unit = {},
) {
    val genre = sagaContent.data.genre
    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            var fraction by remember {
                mutableStateOf(2.dp)
            }

            val imageSize by animateDpAsState(
                fraction,
                tween(easing = EaseIn, durationMillis = 1.seconds.toInt(DurationUnit.MILLISECONDS)),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(imageSize)
                    .clipToBounds(),
            ) {
                AsyncImage(
                    character.image,
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    onSuccess = {
                        fraction = 300.dp
                    },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .zoomAnimation()
                            .clipToBounds()
                            .effectForGenre(genre),
                )

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            fadeGradientBottom(),
                        ),
                )

                IconButton(
                    onClick = {
                        onBack()
                    },
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(32.dp)
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = .3f),
                                CircleShape,
                            ).padding(2.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }

        stickyHeader {
            val characterColor = Color(character.hexColor.toColorInt())
            SagaTopBar(
                character.name,
                subtitle = character.details.occupation,
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .gradientFill(
                            gradientAnimation(
                                characterColor.darkerPalette(),
                                targetValue = 500f,
                                gradientType = GradientType.VERTICAL,
                            ),
                        ).padding(top = 50.dp, start = 16.dp),
                genre = genre,
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
                title = "Status",
                content = character.status,
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
    }
}

@Preview
@Composable
fun CharacterDetailsDialogPreview() {
    val character =
        Character(
            name = "Character Name",
            backstory = "Character backstory",
            image = "https://www.example.com/image.jpg",
            details = Details(occupation = "Occupation", race = "Human"),
        )
    val genre = Genre.FANTASY
    SagAIScaffold {
        CharacterDetailsContent(
            SagaContent(
                data =
                    SagaData(
                        title = "Saga Title",
                        description = "Saga Description",
                        genre = genre,
                    ),
                mainCharacter = character,
                messages = emptyList(),
                chapters = emptyList(),
                characters = emptyList(),
            ),
            character,
            0,
        )
    }
}
