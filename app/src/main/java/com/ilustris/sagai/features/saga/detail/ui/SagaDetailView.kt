package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.components.CharacterSection
import com.ilustris.sagai.features.characters.ui.components.VerticalLabel
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.ui.components.ChapterContentView
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.zoomAnimation

@Composable
fun SagaDetailView(
    navHostController: NavHostController,
    sagaId: String,
    paddingValues: PaddingValues,
    viewModel: SagaDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SagaDetailContentView(state, paddingValues, {
        navHostController.navigateToRoute(
            Routes.CHARACTER_GALLERY,
            mapOf("sagaId" to it.toString()),
        )
    }, onBack = {
        navHostController.popBackStack()
    })

    LaunchedEffect(Unit) {
        viewModel.fetchSagaDetails(sagaId)
    }
}

@Composable
fun SagaDetailContentView(
    state: State,
    paddingValues: PaddingValues,
    selectCharacter: (Int) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val saga = ((state as? State.Success)?.data as? SagaContent)
    LazyColumn(
        modifier = Modifier.padding(paddingValues).animateContentSize(),
    ) {
        if (state is State.Loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(8.dp)) {
                    SparkIcon(
                        Modifier.align(Alignment.Center).size(50.dp),
                        brush = gradientAnimation(holographicGradient),
                    )
                }
            }
        }

        saga?.let {
            stickyHeader { position ->
                SagaTopBar(
                    it.saga.title,
                    "Desde ${it.saga.createdAt.formatDate()}",
                    it.saga.genre,
                    onBackClick = onBack,
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(top = 25.dp, start = 16.dp),
                )
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clipToBounds(),
                    ) {
                        AsyncImage(
                            it.saga.icon,
                            contentDescription = it.saga.title,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(
                                        it.saga.genre.color
                                            .gradientFade(),
                                    ).zoomAnimation(),
                            contentScale = ContentScale.Crop,
                        )

                        Box(
                            Modifier
                                .background(fadedGradientTopAndBottom())
                                .fillMaxSize(),
                        )

                        it.mainCharacter?.let { mainChar ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.align(Alignment.Center),
                            ) {
                                CharacterAvatar(
                                    mainChar,
                                    borderColor = it.saga.genre.color,
                                    modifier = Modifier.padding(8.dp).size(200.dp),
                                )
                            }
                        }
                    }
                    Text(
                        it.mainCharacter?.name.orEmpty(),
                        style =
                            MaterialTheme.typography.displaySmall.copy(
                                fontFamily = it.saga.genre.headerFont(),
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier.padding(8.dp).gradientFill(
                                gradientAnimation(
                                    it.saga.genre.gradient(),
                                    targetValue = 1500f,
                                ),
                            ),
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                ) {
                    VerticalLabel(it.chapters.count().toString(), "Capítulos", it.saga.genre)
                    VerticalLabel(it.characters.count().toString(), "Personagens", it.saga.genre)
                }
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        it.messages.count().toString(),
                        style =
                            MaterialTheme.typography.displaySmall.copy(
                                fontFamily = it.saga.genre.headerFont(),
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            ),
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    )

                    Text(
                        "Mensagens",
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontFamily = it.saga.genre.bodyFont(),
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                            ),
                    )
                }
            }

            item {
                CharacterSection(
                    "Historia",
                    it.saga.description,
                    it.saga.genre,
                )
            }

            item {
                Column {
                    Row(Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(
                            "Personagens",
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = it.saga.genre.headerFont(),
                                ),
                            modifier = Modifier.padding(8.dp).weight(1f),
                        )

                        IconButton(onClick = {
                            selectCharacter(it.saga.id)
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Ver personagens",
                            )
                        }
                    }

                    LazyRow {
                        items(it.characters) { chars ->
                            val characterColor = Color(chars.hexColor.toColorInt())
                            Column(
                                Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CharacterAvatar(
                                    chars,
                                    borderColor = characterColor,
                                    borderSize = 2.dp,
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .size(120.dp)
                                            .padding(8.dp)
                                            .clickable {
                                                selectCharacter(it.saga.id)
                                            },
                                )

                                Text(
                                    chars.name,
                                    style =
                                        MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Light,
                                            textAlign = TextAlign.Center,
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Capítulos",
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontFamily = it.saga.genre.headerFont(),
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            items(it.chapters) { chapter ->
                ChapterContentView(
                    it.saga.genre,
                    chapter,
                    MaterialTheme.colorScheme.onBackground,
                    FontStyle.Normal,
                    remember { mutableStateOf(true) },
                    false,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SagaDetailContentViewLoadingPreview() {
    SagAIScaffold {
        SagaDetailContentView(
            state = State.Loading,
            paddingValues = PaddingValues(0.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SagaDetailContentViewPreview() {
    SagAIScaffold {
        val state =
            State.Success(
                SagaContent(
                    saga =
                        SagaData(
                            title = "Saga de teste",
                            description = "Descrição da saga de teste",
                            icon = null,
                            createdAt = System.currentTimeMillis(),
                            genre = Genre.SCI_FI,
                            mainCharacterId = null,
                        ),
                    mainCharacter =
                        Character(
                            name = "Personagem de teste",
                            backstory = "Descrição do personagem de teste",
                            image = emptyString(),
                            hexColor = "#FFFFFF",
                            details = Details(),
                        ),
                    messages =
                        List(10) {
                            Message(
                                id = 0,
                                sagaId = 0,
                                text = "Mensagem de teste",
                                senderType = SenderType.entries.random(),
                                timestamp = System.currentTimeMillis(),
                            )
                        },
                    chapters =
                        List(3) {
                            Chapter(
                                title = "Capítulo de teste",
                                sagaId = 0,
                                overview = "Texto do capítulo de teste",
                                messageReference = 0,
                                coverImage = emptyString(),
                            )
                        },
                    characters =
                        List(5) {
                            Character(
                                name = "Personagem de teste $it",
                                backstory = "Descrição do personagem de teste",
                                image = emptyString(),
                                hexColor = "#FFFFFF",
                                details = Details(),
                            )
                        },
                ),
            )
        SagaDetailContentView(
            state = state,
            paddingValues = PaddingValues(0.dp),
        )
    }
}
