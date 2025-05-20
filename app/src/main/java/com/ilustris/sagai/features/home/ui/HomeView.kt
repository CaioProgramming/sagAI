@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.ChatData
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.themeBrushColors
import java.util.Calendar

@Composable
fun HomeView(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val chats by viewModel.chats.collectAsStateWithLifecycle(emptyList())

    ChatList(chats) {}
}

@Composable
private fun ChatList(
    chats: List<ChatData>,
    onCreateNewChat: () -> Unit = {},
) {
    LazyColumn(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        item {
            NewChatCard()
        }
        items(chats.size) { index ->
            ChatCard(chats[index])
        }
    }
}

@Composable
fun ChatCard(chatData: ChatData) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        val color = Color(chatData.color.toColorInt())
        val colorBrush =
            Brush.linearGradient(
                colors =
                    listOf(
                        color,
                        color.copy(alpha = .5f),
                        color.copy(alpha = .2f),
                    ),
            )
        val date =
            Calendar.getInstance().apply {
                timeInMillis = chatData.createdAt
            }
        val time = "${date.get(Calendar.HOUR_OF_DAY)}:${date.get(Calendar.MINUTE)}"
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = .3f), RoundedCornerShape(50))
                    .border(
                        BorderStroke(1.dp, brush = colorBrush),
                        RoundedCornerShape(50),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            // Placeholder for avatar image
            Text(
                text = chatData.name.first().toString(),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        brush = colorBrush,
                    ),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name and Last Message
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chatData.name, // Replace with actual contact name
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Last Message Time
        Text(
            text = time, // Replace with actual last message time
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NewChatCard() {
    val appBrush =
        Brush.linearGradient(
            colors = themeBrushColors(),
        )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .background(
                    brush = appBrush,
                    RoundedCornerShape(25.dp),
                ).padding(16.dp)
                .fillMaxSize(),
    ) {
        Text(
            "A jornada começa aqui",
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                ),
        )

        Text(
            "Crie sua nova aventura e descubra o que o futuro reserva para você.",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                ),
        )
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
                ChatList(
                    List(10) {
                        ChatData(
                            name = "Chat ${it + 1}",
                            description = "The journey of our lifes",
                            color = "#5992cb",
                            icon = "",
                            createdAt = Calendar.getInstance().timeInMillis,
                        )
                    },
                )
            }
        }
    }
}
