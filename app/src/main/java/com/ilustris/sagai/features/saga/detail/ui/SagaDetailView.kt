package com.ilustris.sagai.features.saga.detail.ui

import ai.atick.material.MaterialColor
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.ui.ChapterCardView
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.components.CharacterSection
import com.ilustris.sagai.features.characters.ui.components.VerticalLabel
import com.ilustris.sagai.features.home.data.model.IllustrationVisuals
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel
import com.ilustris.sagai.features.timeline.ui.TimeLineCard
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.filters.SelectiveColorParams
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import effectForGenre

enum class DetailAction {
    CHARACTERS,
    DELETE,
    TIMELINE,
    CHAPTERS,
    WIKI,
    BACK,
}

@Composable
fun SagaDetailView(
    navHostController: NavHostController,
    sagaId: String,
    paddingValues: PaddingValues,
    viewModel: SagaDetailViewModel = hiltViewModel(),
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var sagaToDelete by remember { mutableStateOf<SagaData?>(null) }

    SagaDetailContentView(state, paddingValues) { action, saga, value ->
        when (action) {
            DetailAction.CHARACTERS -> {
                if (value == null) {
                    navHostController.navigateToRoute(
                        Routes.CHARACTER_GALLERY,
                        mapOf("sagaId" to saga.data.id.toString()),
                    )
                } else {
                    navHostController.navigateToRoute(
                        Routes.CHARACTER_DETAIL,
                        mapOf(
                            "sagaId" to saga.data.id.toString(),
                            "characterId" to value.toString(),
                        ),
                    )
                }
            }

            DetailAction.DELETE -> {
                sagaToDelete = saga.data
                showDeleteConfirmation = true
            }

            DetailAction.TIMELINE ->
                navHostController.navigateToRoute(
                    Routes.TIMELINE,
                    mapOf("sagaId" to saga.data.id.toString()),
                )

            DetailAction.CHAPTERS ->
                navHostController.navigateToRoute(
                    Routes.SAGA_CHAPTERS,
                    mapOf("sagaId" to saga.data.id.toString()),
                )

            DetailAction.WIKI -> TODO()
            DetailAction.BACK -> navHostController.popBackStack()
        }
    }

    if (showDeleteConfirmation && sagaToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja excluir esta saga? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        sagaToDelete?.let { viewModel.deleteSaga(it) }
                        showDeleteConfirmation = false
                        navHostController.navigateToRoute(Routes.HOME)
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                ) { Text("Excluir") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = false },
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                ) { Text("Cancelar") }
            },
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchSagaDetails(sagaId)
    }
}

@Composable
fun SagaDetailContentView(
    state: State,
    paddingValues: PaddingValues,
    detailAction: (DetailAction, SagaContent, Any?) -> Unit = { _, _, _ -> },
) {
    val columnCount = 2
    val saga = ((state as? State.Success)?.data as? SagaContent)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier =
            Modifier
                .padding(paddingValues)
                .animateContentSize(),
    ) {
        if (state is State.Loading) {
            item(span = {
                GridItemSpan(columnCount)
            }) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    SparkIcon(
                        Modifier
                            .align(Alignment.Center)
                            .size(50.dp),
                        brush = gradientAnimation(holographicGradient),
                    )
                }
            }
        }

        saga?.let {
            item(span = {
                GridItemSpan(columnCount)
            }) {
                SagaTopBar(
                    it.data.title,
                    "Criado em ${it.data.createdAt.formatDate()}",
                    it.data.genre,
                    onBackClick = { detailAction(DetailAction.BACK, it, null) },
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                            .padding(top = 50.dp, start = 16.dp),
                )
            }
            item(span = {
                GridItemSpan(columnCount)
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clipToBounds(),
                    ) {
                        AsyncImage(
                            it.data.icon,
                            contentDescription = it.data.title,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .background(
                                        it.data.genre.color
                                            .gradientFade(),
                                    ).effectForGenre(saga.data.genre)
                                    .selectiveColorHighlight(
                                        SelectiveColorParams(
                                            it.data.genre.color,
                                        ),
                                    ).clipToBounds(),
                            contentScale = ContentScale.Crop,
                        )

                        Box(
                            Modifier
                                .background(fadedGradientTopAndBottom())
                                .fillMaxWidth()
                                .height(300.dp),
                        )

                        it.mainCharacter?.let { mainChar ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(top = 150.dp),
                            ) {
                                CharacterAvatar(
                                    mainChar,
                                    borderSize = 3.dp,
                                    genre = it.data.genre,
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .size(170.dp),
                                )
                                Text(
                                    "A jornada de",
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = it.data.genre.bodyFont(),
                                            fontWeight = FontWeight.Light,
                                            textAlign = TextAlign.Center,
                                        ),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    it.mainCharacter.name,
                                    style =
                                        MaterialTheme.typography.displaySmall.copy(
                                            fontFamily = it.data.genre.headerFont(),
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                        ),
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .gradientFill(it.data.genre.gradient(true)),
                                )
                            }
                        }
                    }
                }
            }

            item(span = {
                GridItemSpan(columnCount)
            }) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                ) {
                    VerticalLabel(it.chapters.count().toString(), "Capítulos", it.data.genre)
                    VerticalLabel(it.characters.count().toString(), "Personagens", it.data.genre)
                }
            }

            item(span = {
                GridItemSpan(columnCount)
            }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        it.messages.count().toString(),
                        style =
                            MaterialTheme.typography.displaySmall.copy(
                                fontFamily = it.data.genre.headerFont(),
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
                                fontFamily = it.data.genre.bodyFont(),
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                            ),
                    )
                }
            }

            item(span = {
                GridItemSpan(columnCount)
            }) {
                CharacterSection(
                    "Descrição",
                    it.data.description,
                    it.data.genre,
                )
            }

            item(span = {
                GridItemSpan(columnCount)
            }) {
                Row(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        "Personagens",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontFamily = it.data.genre.headerFont(),
                            ),
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .weight(1f),
                    )

                    IconButton(onClick = {
                        detailAction(DetailAction.CHARACTERS, it, null)
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Ver personagens",
                        )
                    }
                }
            }

            item(span = { GridItemSpan(columnCount) }) {
                LazyRow {
                    items(sortCharactersByMessageCount(it.characters, it.messages)) { char ->
                        Column(
                            Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(it.data.genre.cornerSize()))
                                .clickable {
                                    detailAction(DetailAction.CHARACTERS, it, char.id)
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CharacterAvatar(
                                char,
                                borderSize = 2.dp,
                                genre = it.data.genre,
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .size(120.dp)
                                        .padding(8.dp),
                            )

                            Text(
                                char.name,
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Light,
                                        textAlign = TextAlign.Center,
                                        fontFamily = it.data.genre.bodyFont(),
                                    ),
                            )
                        }
                    }
                }
            }

            if (it.timelines.isNotEmpty()) {
                item(span = {
                    GridItemSpan(columnCount)
                }) {
                    Column {
                        Row(
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                        ) {
                            Text(
                                "Linha do tempo",
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = it.data.genre.headerFont(),
                                    ),
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .weight(1f),
                            )

                            IconButton(onClick = {
                                detailAction(
                                    DetailAction.TIMELINE,
                                    it,
                                    null,
                                )
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = "Ver Linha do tempo",
                                )
                            }
                        }

                        it.timelines.lastOrNull()?.let { event ->
                            TimeLineCard(
                                event,
                                it.data.genre,
                                false,
                                modifier =
                                    Modifier
                                        .padding(16.dp)
                                        .clip(RoundedCornerShape(it.data.genre.cornerSize()))
                                        .clickable {
                                            detailAction(
                                                DetailAction.TIMELINE,
                                                it,
                                                null,
                                            )
                                        }.fillMaxWidth()
                                        .wrapContentHeight(),
                            )
                        } ?: run {
                            Text(
                                "Nenhum evento registrado",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            if (it.wikis.isNotEmpty()) {
                item(span = {
                    GridItemSpan(columnCount)
                }) {
                    Column {
                        Row(
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                        ) {
                            Text(
                                "Wiki",
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = it.data.genre.headerFont(),
                                    ),
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .weight(1f),
                            )

                            IconButton(onClick = {
                                // openTimeLine(it.data.id)
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = "Ver wiki",
                                )
                            }
                        }

                        if (it.wikis.isEmpty()) {
                            Text(
                                "Nenhuma informação salva",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                items(it.wikis) { wiki ->
                    WikiCard(
                        wiki,
                        it.data.genre,
                        modifier =
                            Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .height(270.dp),
                    )
                }
            }

            if (it.chapters.isNotEmpty()) {
                item(span = {
                    GridItemSpan(columnCount)
                }) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(
                            "Capítulos",
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = it.data.genre.headerFont(),
                                ),
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                        )

                        IconButton(onClick = {
                            detailAction(
                                DetailAction.CHAPTERS,
                                it,
                                null,
                            )
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Ver personagens",
                            )
                        }
                    }
                }

                item(span = {
                    GridItemSpan(columnCount)
                }) {
                    LazyRow {
                        items(it.chapters) { chapter ->
                            ChapterCardView(
                                chapter,
                                it.data.genre,
                                it.characters,
                                Modifier
                                    .padding(4.dp)
                                    .width(300.dp)
                                    .height(300.dp),
                            )
                        }
                    }
                }
            }

            item(span = {
                GridItemSpan(columnCount)
            }) {
                Button(
                    onClick = {
                        detailAction(
                            DetailAction.DELETE,
                            it,
                            null,
                        )
                    },
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialColor.Red400,
                        ),
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                ) {
                    Text("Excluir Saga", textAlign = TextAlign.Center)
                }
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
                    data =
                        SagaData(
                            title = "Saga de teste",
                            description = "Descrição da saga de teste",
                            icon = null,
                            createdAt = System.currentTimeMillis(),
                            genre = Genre.SCI_FI,
                            mainCharacterId = null,
                            visuals = IllustrationVisuals(),
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
                            MessageContent(
                                Message(
                                    id = 0,
                                    sagaId = 0,
                                    text = "Mensagem de teste",
                                    senderType = SenderType.entries.random(),
                                    timestamp = System.currentTimeMillis(),
                                ),
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
