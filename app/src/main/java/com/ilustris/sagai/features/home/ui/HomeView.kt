@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.SenderType
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.components.SagaLoader
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.defaultHeaderImage
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.lighter
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds

@Suppress("ktlint:standard:function-naming")
@Composable
fun HomeView(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val sagas by viewModel.sagas.collectAsStateWithLifecycle(emptyList())

    ChatList(
        sagas = sagas,
        onCreateNewChat = {
            navController.navigateToRoute(Routes.NEW_SAGA)
        },
        onSelectSaga = { sagaId ->
            navController.navigateToRoute(
                Routes.CHAT,
                Routes.CHAT.arguments.associate {
                    it to sagaId.id.toString()
                },
            )
        },
    )
}

@Composable
private fun ChatList(
    sagas: List<SagaContent>,
    onCreateNewChat: () -> Unit = {},
    onSelectSaga: (SagaData) -> Unit = {},
) {
    Box {
        val styleGradient =
            gradientAnimation(genresGradient(), targetValue = 1000f, duration = 5.seconds)

        LazyColumn(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
        ) {
            if (sagas.isEmpty()) {
                item {
                    NewChatCard(
                        animatedBrush = styleGradient,
                        onButtonClick = {
                            onCreateNewChat()
                        },
                        modifier =
                            Modifier.fillParentMaxSize(),
                    )
                }
            }

            items(sagas) {
                ChatCard(it) {
                    onSelectSaga(it.saga)
                }
            }
        }
        AnimatedVisibility(sagas.isNotEmpty(), modifier = Modifier.align(Alignment.BottomCenter)) {
            SparkIcon(
                description = "Criar nova saga",
                brush = styleGradient,
                blurRadius = 5.dp,
                modifier =
                    Modifier.size(100.dp).clip(CircleShape).clickable {
                        onCreateNewChat()
                    },
            )
        }
    }
}

@Composable
fun ChatCard(
    saga: SagaContent,
    onClick: () -> Unit = {},
) {
    val sagaData = saga.saga
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    onClick()
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        val date =
            Calendar.getInstance().apply {
                timeInMillis = sagaData.createdAt
            }
        val time = "${date.get(Calendar.HOUR_OF_DAY)}:${date.get(Calendar.MINUTE)}"

        AsyncImage(
            sagaData.icon ?: sagaData.genre.defaultHeaderImage(),
            contentDescription = sagaData.title,
            modifier =
                Modifier
                    .size(50.dp)
                    .border(2.dp, Brush.verticalGradient(sagaData.genre.gradient()), CircleShape)
                    .padding(2.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                    .clip(CircleShape),
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name and Last Message
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sagaData.title, // Replace with actual contact name
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            val lastMessageText =
                if (saga.messages.isNotEmpty()) {
                    saga.messages.last().text
                } else {
                    "Sua saga começa agora!"
                }
            Text(
                text = lastMessageText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.lighter(.3f),
                maxLines = 3,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Last Message Time
            Text(
                text = time, // Replace with actual last message time
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = saga.messages.size.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = sagaData.genre.color,
                modifier =
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            CircleShape,
                        ).padding(8.dp)
                        .align(Alignment.End)
                        .alpha(.4f),
            )
        }
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
            SagaLoader(
                brush = animatedBrush,
                animationDuration = 1.seconds,
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
                        Modifier.padding(8.dp).fillMaxWidth(0.85f).gradientFill(
                            animatedBrush,
                        ),
                )

                Icon(
                    Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = stringResource(R.string.new_saga_title),
                    modifier =
                        Modifier.padding(8.dp).size(24.dp).gradientFill(
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
                            SagaData(
                                title = "Chat ${it + 1}",
                                description = "The journey of our lifes",
                                genre = Genre.FANTASY,
                                icon = "",
                                createdAt = Calendar.getInstance().timeInMillis,
                                mainCharacterId = null,
                            ),
                            mainCharacter = null,
                            messages =
                                List(4) {
                                    Message(
                                        id = it,
                                        text = "Message ${it + 1} in chat ${it + 1}",
                                        timestamp = Calendar.getInstance().timeInMillis,
                                        sagaId = 0,
                                        senderType = (if (it % 2 == 0) SenderType.USER else SenderType.BOT),
                                    )
                                },
                        )
                    }
                ChatList(
                    previewChats,
                )
            }
        }
    }
}
