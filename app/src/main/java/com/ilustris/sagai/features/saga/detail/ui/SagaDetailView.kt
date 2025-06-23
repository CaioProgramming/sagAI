package com.ilustris.sagai.features.saga.detail.ui

import ai.atick.material.MaterialColor
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel
import com.ilustris.sagai.features.timeline.ui.TimeLineCard
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.cornerSize
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
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var sagaToDelete by remember { mutableStateOf<SagaData?>(null) }

    SagaDetailContentView(state, paddingValues, selectCharacter = {
        it?.let { id ->
            navHostController.navigateToRoute(
                Routes.CHARACTER_DETAIL,
                mapOf(
                    "sagaId" to sagaId,
                    "characterId" to id.toString(),
                ),
            )
        } ?: run {
            navHostController.navigateToRoute(Routes.CHARACTER_GALLERY, mapOf("sagaId" to sagaId))
        }
    }, onBack = {
        navHostController.popBackStack()
    }, onDelete = { saga ->
        sagaToDelete = saga
        showDeleteConfirmation = true
    }, openTimeLine = {
        Log.d("sagadetail", "SagaDetailView: Opening timeLine")
        navHostController.navigateToRoute(Routes.TIMELINE, mapOf("sagaId" to it.toString()))
    })

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
    selectCharacter: (Int?) -> Unit = {},
    openTimeLine: (Int) -> Unit,
    onBack: () -> Unit = {},
    onDelete: (SagaData) -> Unit = {},
) {
    val saga = ((state as? State.Success)?.data as? SagaContent)
    LazyColumn(
        modifier =
            Modifier
                .padding(paddingValues)
                .animateContentSize(),
    ) {
        if (state is State.Loading) {
            item {
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
            stickyHeader { position ->
                SagaTopBar(
                    it.data.title,
                    "Criado em ${it.data.createdAt.formatDate()}",
                    it.data.genre,
                    onBackClick = onBack,
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                            .padding(top = 50.dp, start = 16.dp),
                )
            }
            item {
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
                                    ).clipToBounds()
                                    .zoomAnimation(),
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
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .size(170.dp),
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
                                            .gradientFill(
                                                gradientAnimation(
                                                    it.data.genre.gradient(),
                                                    targetValue = 1500f,
                                                ),
                                            ),
                                )
                            }
                        }
                    }
                }
            }

            item {
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

            item {
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

            item {
                CharacterSection(
                    "Descrição",
                    it.data.description,
                    it.data.genre,
                )
            }

            item {
                Column {
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
                            selectCharacter(null)
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Ver personagens",
                            )
                        }
                    }

                    LazyRow {
                        items(
                            sortCharactersByMessageCount(
                                it.characters,
                                it.messages,
                            ),
                        ) { chars ->
                            Column(
                                Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CharacterAvatar(
                                    chars,
                                    borderSize = 2.dp,
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .clip(CircleShape)
                                            .size(120.dp)
                                            .padding(8.dp)
                                            .clickable {
                                                selectCharacter(chars.id)
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

            if (it.timelines.isNotEmpty()) {
                item {
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
                                openTimeLine(it.data.id)
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
                                            openTimeLine(it.data.id)
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
                item {
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
                        } else {
                        }
                    }
                }
            }

            if (it.chapters.isNotEmpty()) {
                item {
                    Text(
                        "Capítulos",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontFamily = it.data.genre.headerFont(),
                            ),
                        modifier = Modifier.padding(16.dp),
                    )
                }
                item {
                    LazyRow(modifier = Modifier.padding(horizontal = 16.dp)) {
                        items(it.chapters) { chapter ->
                            ChapterCardView(
                                chapter,
                                it.data.genre,
                                it.characters,
                                Modifier
                                    .padding(8.dp)
                                    .width(250.dp)
                                    .height(350.dp),
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        onDelete(it.data)
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
            openTimeLine = {},
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
            openTimeLine = {},
        )
    }
}
