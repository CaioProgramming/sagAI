package com.ilustris.sagai.features.chat.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.ui.components.ChatBubble
import com.ilustris.sagai.features.chat.ui.presentation.ChatState
import com.ilustris.sagai.features.chat.ui.presentation.ChatViewModel
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import java.util.Calendar

@Composable
fun ChatView(
    navHostController: NavHostController,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val sagaId = navHostController.previousBackStackEntry?.arguments?.getString("sagaId")
    val state = viewModel.state.collectAsStateWithLifecycle()
    val saga = viewModel.saga.collectAsStateWithLifecycle().value
    LaunchedEffect(Unit) {
        sagaId?.let { viewModel.getMessages(it) }
    }
    ChatContent(state.value, saga) {
        viewModel.sendMessage(it)
    }
}

@Composable
fun ChatContent(
    state: ChatState = ChatState.Empty,
    saga: SagaData? = null,
    onSendMessage: (String) -> Unit = {},
) {
    var input by remember {
        mutableStateOf("")
    }
    ConstraintLayout(Modifier
        .padding(16.dp)
        .fillMaxSize()) {
        val brush =
            saga?.genre?.gradient()?.let { gradientAnimation(it) } ?: Color.White.gradientFade()
        val (messages, chatInput) = createRefs()

        val isLoading = state is ChatState.Loading

        AnimatedContent(
            state,
            Modifier.constrainAs(messages) {
                top.linkTo(parent.top)
                bottom.linkTo(chatInput.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            },
        ) {
            when (it) {
                is ChatState.Loading -> {
                    Box(Modifier.fillMaxSize()) {
                        SparkIcon(
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .size(100.dp),
                            description = "Loading",
                            brush,
                        )
                    }
                }

                is ChatState.Success -> {
                    ChatList(
                        saga = it.sagaData,
                        messages = it.messages,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is ChatState.Error -> {
                    Text(
                        "Error: ${it.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier =
                            Modifier.constrainAs(messages) {
                                top.linkTo(parent.top)
                                bottom.linkTo(chatInput.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            },
                    )
                }

                is ChatState.Empty -> {
                    Text(
                        "No messages found.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier =
                            Modifier.constrainAs(messages) {
                                top.linkTo(parent.top)
                                bottom.linkTo(chatInput.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            },
                    )
                }
            }
        }

        val themeGradient = saga?.genre?.gradient() ?: holographicGradient

        TextField(
            value = input,
            onValueChange = {
                if (it.length <= 100) {
                    input = it
                }
            },
            label = { Text("Type a message") },
            shape = RoundedCornerShape(25.dp),
            colors =
                TextFieldDefaults.colors().copy(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            textStyle = MaterialTheme.typography.labelSmall,
            maxLines = 3,
            trailingIcon = {
                AnimatedVisibility(state != ChatState.Loading && input.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onSendMessage(input)
                        },
                        modifier =
                            Modifier
                                .padding(12.dp)
                                .size(32.dp)
                                .background(
                                    saga?.genre?.color ?: MaterialTheme.colorScheme.primary,
                                    CircleShape,
                                ),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Send Message",
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.White,
                        )
                    }
                }
            },
            modifier =
                Modifier
                    .constrainAs(chatInput) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    },
        )
    }
}

@Composable
fun ChatList(
    saga: SagaData,
    messages: List<Message>,
    modifier: Modifier,
) {
    LazyColumn(modifier) {
        item {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)) {
                Image(
                    painterResource(saga.genre.icon),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(fadedGradientTopAndBottom()))

                Column(verticalArrangement = Arrangement.spacedBy (8.dp),modifier = Modifier.padding(16.dp).fillMaxWidth().align(Alignment.BottomCenter)) {
                    Text(
                        saga.title,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .gradientFill(saga.genre.color.gradientFade())
                        ,
                    )

                    Text(
                        saga.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .gradientFill(saga.genre.color.gradientFade())
                    )
                }

            }
        }

        items(messages) {
            ChatBubble(it)
        }
    }
}

@Preview
@Composable
fun ChatViewPreview() {
    val saga = SagaData(
        id = 1,
        title = "FireWings",
        description = "This is a sample saga for preview purposes.",
        icon = "",
        genre = Genre.FANTASY,
        createdAt = Calendar.getInstance().timeInMillis,
    )
    SagAIScaffold {

        ChatContent(
            ChatState.Success(
                saga, emptyList()
            ),
            saga = saga,
        )
    }
}
