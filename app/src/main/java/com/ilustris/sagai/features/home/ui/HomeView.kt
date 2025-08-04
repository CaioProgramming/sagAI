@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.home.ui

import ai.atick.material.MaterialColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.defaultHeaderImage
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import effectForGenre
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds

@Suppress("ktlint:standard:function-naming")
@Composable
fun HomeView(
    navController: NavHostController,
    padding: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val sagas by viewModel.sagas.collectAsStateWithLifecycle(emptyList())
    val showDebugButton by viewModel.showDebugButton.collectAsStateWithLifecycle()
    val startFakeSaga by viewModel.startDebugSaga.collectAsStateWithLifecycle()
    ChatList(
        sagas = if (showDebugButton.not()) sagas.filter { !it.data.isDebug } else sagas,
        padding = padding,
        showDebugButton = showDebugButton,
        onCreateNewChat = {
            navController.navigateToRoute(Routes.NEW_SAGA)
        },
        onSelectSaga = { sagaData ->
            navController.navigateToRoute(
                Routes.CHAT,
                mapOf(
                    "sagaId" to sagaData.id.toString(),
                    "isDebug" to sagaData.isDebug.toString(),
                ),
            )
        },
        createFakeSaga = {
            viewModel.createFakeSaga()
        },
    )
    LaunchedEffect(startFakeSaga) {
        startFakeSaga?.let {
            navController.navigateToRoute(
                Routes.CHAT,
                mapOf(
                    "sagaId" to it.id.toString(),
                    "isDebug" to "true",
                ),
            )
        }
    }
}

@Composable
private fun ChatList(
    sagas: List<SagaContent>,
    padding: PaddingValues = PaddingValues(0.dp),
    showDebugButton: Boolean,
    onCreateNewChat: () -> Unit = {},
    onSelectSaga: (Saga) -> Unit = {},
    createFakeSaga: () -> Unit = {},
) {
    LazyColumn(
        modifier =
            Modifier.padding(padding),
    ) {
        if (showDebugButton) { // Condition updated
            item {
                val debugBrush = Brush.verticalGradient(listOf(Color.DarkGray, Color.Gray))
                Row(
                    modifier =
                        Modifier
                            .clickable {
                                createFakeSaga()
                            }.padding(16.dp)
                            .gradientFill(debugBrush)
                            .clip(RoundedCornerShape(15.dp))
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_bug),
                        contentDescription = "Debug Session",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .size(32.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Start Debug Session",
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                ),
                        )

                        Text(
                            "Test with fake messages.",
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Light,
                                    color = Color.White.copy(alpha = 0.8f),
                                ),
                        )
                    }
                }
            }
        }
        item {
            val brush =
                gradientAnimation(
                    genresGradient(),
                    gradientType = GradientType.LINEAR,
                    targetValue = 500f,
                    duration = 4.seconds,
                )
            Row(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .gradientFill(brush)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable {
                            onCreateNewChat()
                        }.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SparkLoader(
                    brush = Brush.verticalGradient(holographicGradient),
                    strokeSize = 2.dp,
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .padding(4.dp)
                            .size(32.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Criar nova saga",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                brush = Brush.verticalGradient(holographicGradient),
                            ),
                    )

                    Text(
                        "Crie uma nova aventura e descubra o que o futuro reserva para você.",
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Light,
                                brush = Brush.verticalGradient(holographicGradient),
                            ),
                    )
                }
            }
        }

        items(sagas) {
            ChatCard(it, isEnabled = showDebugButton) {
                onSelectSaga(it.data)
            }
        }
    }
}

@Composable
fun ChatCard(
    saga: SagaContent,
    isEnabled: Boolean = false,
    onClick: () -> Unit = {},
) {
    val sagaData = saga.data
    Column {
        Row(
            modifier =
                Modifier
                    .clickable {
                        if (saga.data.isDebug && isEnabled) {
                            onClick()
                        } else {
                            onClick()
                        }
                    }.fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val imageLoaded =
                remember {
                    mutableStateOf(false)
                }
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape),
            ) {
                if (saga.data.isDebug.not()) {
                    AsyncImage(
                        sagaData.icon ?: sagaData.genre.defaultHeaderImage(),
                        contentDescription = sagaData.title,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .border(
                                    2.dp,
                                    sagaData.genre.color,
                                    CircleShape,
                                ).padding(4.dp)
                                .background(
                                    sagaData.genre.color,
                                    CircleShape,
                                ).clip(CircleShape)
                                .fillMaxSize()
                                .effectForGenre(
                                    sagaData.genre,
                                    focusRadius = 0f,
                                    customGrain = 0.05f,
                                ).selectiveColorHighlight(
                                    sagaData.genre.selectiveHighlight(),
                                ),
                        onSuccess = {
                            imageLoaded.value = true
                        },
                    )
                } else {
                    Image(
                        painterResource(R.drawable.ic_bug),
                        contentDescription = null,
                        colorFilter =
                            ColorFilter.tint(
                                sagaData.genre.iconColor,
                            ),
                        contentScale = ContentScale.Fit,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .border(2.dp, sagaData.genre.gradient(), CircleShape)
                                .padding(4.dp),
                    )

                    LaunchedEffect(Unit) {
                        imageLoaded.value = true
                    }
                }

                this@Row.AnimatedVisibility(
                    imageLoaded.value.not(),
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Text(
                        sagaData.title
                            .first()
                            .uppercaseChar()
                            .toString(),
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = sagaData.genre.headerFont(),
                                color = sagaData.genre.iconColor,
                                textAlign = TextAlign.Center,
                            ),
                    )
                }

                if (saga.data.isEnded) {
                    Image(
                        painterResource(R.drawable.ic_spark),
                        contentDescription = null,
                        colorFilter =
                            ColorFilter.tint(
                                sagaData.genre.color,
                            ),
                        modifier =
                            Modifier.offset(y = 6.dp).size(24.dp).align(
                                Alignment.BottomCenter,
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            val lastMessage = saga.messages.firstOrNull()
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = sagaData.title, // Replace with actual contact name
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = saga.data.genre.headerFont(),
                        modifier = Modifier.weight(1f),
                    )

                    lastMessage?.let {
                        val time =
                            Calendar.getInstance().apply { timeInMillis = it.message.timestamp }
                        val timeText =
                            String.format(
                                "%02d:%02d",
                                time.get(Calendar.HOUR_OF_DAY),
                                time.get(Calendar.MINUTE),
                            )

                        Text(
                            text = timeText,
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = saga.data.genre.bodyFont(),
                                ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(Modifier.alpha(.8f).padding(vertical = 4.dp)) {
                    if (sagaData.isEnded.not()) {
                        lastMessage?.let {
                            if (it.message.senderType == SenderType.USER || it.message.senderType == SenderType.CHARACTER) {
                                Text(
                                    text = (it.character?.name ?: "Desconhecido").plus(": "),
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = saga.data.genre.bodyFont(),
                                        ),
                                    maxLines = 2,
                                )

                                Text(
                                    text = it.message.text.take(200),
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = saga.data.genre.bodyFont(),
                                        ),
                                    maxLines = 2,
                                    modifier =
                                        Modifier
                                            .padding(start = 4.dp)
                                            .weight(1f),
                                )
                            } else {
                                Text(
                                    text = it.message.text,
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = saga.data.genre.bodyFont(),
                                            fontStyle = FontStyle.Italic,
                                        ),
                                    maxLines = 2,
                                )
                            }
                        } ?: run {
                            Text(
                                "Sua saga começa agora!",
                                style =
                                    MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = saga.data.genre.bodyFont(),
                                        fontStyle = FontStyle.Italic,
                                    ),
                            )
                        }
                    } else {
                        Text(
                            "Sua saga chegou ao fim!",
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = saga.data.genre.bodyFont(),
                                    color = sagaData.genre.color,
                                ),
                            modifier =
                                Modifier
                                    .reactiveShimmer(
                                        isPlaying = true,
                                        duration = 5.seconds,
                                    ).weight(1f),
                        )
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .1f)),
        )
    }
}

@Composable
private fun NewChatCard(
    modifier: Modifier = Modifier,
    animatedBrush: Brush,
    onButtonClick: () -> Unit = {},
) {
    Box(modifier.padding(16.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SparkIcon(
                brush = animatedBrush,
                duration = 3.seconds,
                rotationTarget = 180f,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .padding(8.dp)
                        .size(200.dp)
                        .clickable {
                            onButtonClick()
                        },
            )

            Text(
                "A jornada começa aqui",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                style =
                    MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        brush = animatedBrush,
                    ),
            )

            Text(
                "Crie sua nova aventura e descubra o que o futuro reserva para você.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
            )

            Button(
                onClick = {
                    onButtonClick()
                },
                modifier =
                    Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = Color.White,
                    ),
                shape = RoundedCornerShape(15.dp),
            ) {
                Text(
                    "Começar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .fillMaxWidth(0.85f)
                            .gradientFill(
                                animatedBrush,
                            ),
                )

                Icon(
                    Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = stringResource(R.string.new_saga_title),
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .size(24.dp)
                            .gradientFill(
                                animatedBrush,
                            ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    SagAITheme {
        val route = Routes.HOME
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = {
                    route.title?.let {
                        Text(
                            text = stringResource(it),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                        )
                    } ?: run {
                        Box(Modifier.fillMaxWidth()) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                contentDescription = stringResource(R.string.app_name),
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .size(50.dp),
                            )
                        }
                    }
                },
                actions = {},
                navigationIcon = {
                    Box(modifier = Modifier.size(24.dp))
                },
            )
        }) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                val previewChats =
                    List(10) {
                        SagaContent(
                            Saga(
                                title = "Chat ${it + 1}",
                                description = "The journey of our lifes",
                                genre = Genre.FANTASY,
                                icon = "",
                                isEnded = true,
                                createdAt = Calendar.getInstance().timeInMillis,
                                mainCharacterId = null,
                            ),
                            mainCharacter = null,
                            messages =
                                List(4) {
                                    MessageContent(
                                        Message(
                                            id = it,
                                            text = "Message ${it + 1} in chat ${it + 1}",
                                            timestamp = Calendar.getInstance().timeInMillis,
                                            sagaId = 0,
                                            senderType = (if (it % 2 == 0) SenderType.USER else SenderType.CHARACTER),
                                        ),
                                    )
                                },
                        )
                    }
                ChatList(
                    sagas = previewChats,
                    showDebugButton = true, // Example for preview
                )
            }
        }
    }
}
