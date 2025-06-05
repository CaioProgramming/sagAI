package com.ilustris.sagai.features.chat.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.MessageContent
import com.ilustris.sagai.features.chat.data.model.SenderType
import com.ilustris.sagai.features.chat.ui.components.ChatBubble
import com.ilustris.sagai.features.chat.ui.presentation.ChatState
import com.ilustris.sagai.features.chat.ui.presentation.ChatViewModel
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.defaultHeaderImage
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import java.util.Calendar

@Composable
fun ChatView(
    navHostController: NavHostController,
    sagaId: String? = null,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val saga by viewModel.saga.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    LaunchedEffect(saga) {
        if (saga == null) {
            viewModel.initChat(sagaId)
        }
    }

    ChatContent(state.value, saga, messages) {
        viewModel.sendInput(it)
    }
}

@Composable
fun ChatContent(
    state: ChatState = ChatState.Empty,
    saga: SagaData? = null,
    messagesList: List<MessageContent>? = null,
    onSendMessage: (String) -> Unit = {},
) {
    var input by remember {
        mutableStateOf("")
    }
    ConstraintLayout(
        Modifier
            .fillMaxSize(),
    ) {
        val brush =
            saga?.genre?.gradient()?.let { gradientAnimation(it, targetValue = 500f) }
                ?: gradientAnimation()
        val (messages, chatInput) = createRefs()

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
                is ChatState.Loading ->
                    EmptyMessagesView(
                        text = "Carregando mensagens...",
                        brush = gradientAnimation(holographicGradient),
                        modifier =
                            Modifier
                                .fillMaxSize(),
                    )

                is ChatState.Success -> {
                    ChatList(
                        saga = saga,
                        messages = messagesList,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is ChatState.Error ->
                    EmptyMessagesView(
                        text = it.message,
                        brush = brush,
                        modifier =
                            Modifier
                                .fillMaxSize(),
                    )

                is ChatState.Empty -> {
                    EmptyMessagesView(
                        brush = gradientAnimation(genresGradient()),
                        modifier =
                            Modifier
                                .fillMaxSize(),
                    )
                }
            }
        }

        TextField(
            value = input,
            onValueChange = {
                if (it.length <= 200) {
                    input = it
                }
            },
            placeholder = { Text("Continua sua saga...") },
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
                            input = ""
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
                    .padding(16.dp)
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
private fun EmptyMessagesView(
    text: String = "Comece a escrever sua saga!",
    brush: Brush,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.Center) {
        SparkIcon(
            modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally),
            description = "No messages",
            brush = brush,
        )

        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
    }
}

@Composable
fun SagaHeader(saga: SagaData) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(300.dp),
    ) {
        val iconUrl =
            if (saga.icon.isNullOrEmpty()) {
                saga.genre.defaultHeaderImage()
            } else {
                saga.icon
            }
        AsyncImage(
            iconUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(modifier = Modifier.fillMaxSize().background(fadedGradientTopAndBottom()))
    }
}

@Composable
fun ChatList(
    saga: SagaData?,
    messages: List<MessageContent>?,
    modifier: Modifier,
) {
    val animatedMessages = remember { mutableSetOf<Int>() }
    val listState = rememberLazyListState()

    LaunchedEffect(messages) {
        if (messages?.isNotEmpty() == true) {
            listState.scrollToItem(0)
        }
    }
    LazyColumn(modifier, state = listState, reverseLayout = true) {
        saga?.let {
            messages?.let {
                items(messages, key = { it.message.id }) { message ->
                    ChatBubble(
                        message,
                        saga.genre,
                        animatedMessages,
                        canAnimate = message != messages.first(),
                    )
                }
            }

            item {
                var isDescriptionExpanded by remember { mutableStateOf(false) }
                val textColor by animateColorAsState(
                    targetValue =
                        if (isDescriptionExpanded) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        },
                )
                Text(
                    if (isDescriptionExpanded) {
                        saga.description
                    } else {
                        saga.description
                            .take(200)
                            .plus("...")
                    },
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                        ),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .clickable {
                                isDescriptionExpanded = !isDescriptionExpanded
                            },
                )
            }
            stickyHeader {
                Text(
                    saga.title,
                    style =
                        MaterialTheme.typography.displayMedium.copy(
                            fontFamily = saga.genre.headerFont(),
                        ),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp)
                            .gradientFill(
                                gradientAnimation(
                                    saga.genre.gradient(),
                                    targetValue = 500f,
                                ),
                            ),
                )
            }

            item {
                SagaHeader(saga)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun ChatViewPreview() {
    val saga =
        SagaData(
            id = 1,
            title = "Byte Legend",
            description = "This is a sample saga for preview purposes.",
            icon = "",
            genre = Genre.SCI_FI,
            createdAt = Calendar.getInstance().timeInMillis,
        )
    val messages =
        List(17) {
            Message(
                id = it,
                text = "This is a sample message number $it.",
                senderType = if (it % 2 == 0) SenderType.BOT else SenderType.USER,
                timestamp = Calendar.getInstance().timeInMillis - it * 1000L,
                sagaId = saga.id,
            )
        }.plus(
            Message(
                id = 20,
                text = "This is a sample message from the narrator.",
                senderType = SenderType.NARRATOR,
                timestamp = Calendar.getInstance().timeInMillis - 11 * 1000L,
                sagaId = saga.id,
            ),
        ).reversed()
    val successState = ChatState.Success
    SagAIScaffold {
        ChatContent(
            successState,
            saga = saga,
            messagesList = messages.map {
                MessageContent(
                    message = it
                )
            },
        )
    }
}
