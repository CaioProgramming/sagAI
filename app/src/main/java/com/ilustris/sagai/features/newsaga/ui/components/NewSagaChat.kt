@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.newsaga.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.Sender
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.ui.components.BubbleStyle
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NewSagaChat(
    currentForm: SagaForm? = null,
    messages: List<ChatMessage>,
    callback: CallBackAction? = null,
    inputHint: String = emptyString(),
    inputSuggestions: List<String> = emptyList(),
    isLoading: Boolean = false,
    onSendMessage: (String) -> Unit,
    saveSaga: () -> Unit = {},
    updateGenre: (Genre) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    AnimatedContent(currentForm?.saga?.genre, transitionSpec = {
        fadeIn() togetherWith fadeOut()
    }) {
        val defaultGenre = remember { Genre.entries.random() }
        val genre = it ?: defaultGenre

        ConstraintLayout(
            Modifier
                .navigationBarsPadding()
                .fillMaxSize(),
        ) {
            val (chatList, inputView) = createRefs()

            LazyColumn(
                Modifier.constrainAs(chatList) {
                    top.linkTo(parent.top)
                    bottom.linkTo(inputView.top)
                    width = Dimension.matchParent
                    height = Dimension.fillToConstraints
                },
                state = listState,
            ) {
                if (messages.size == 1) {
                    item {
                        SimpleTypewriterText(
                            messages.first().text,
                            modifier = Modifier.padding(16.dp),
                            style =
                                MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = MaterialTheme.colorScheme.onBackground,
                                ),
                        )
                    }
                } else {
                    items(messages) { message ->
                        ChatMessageBubble(
                            message,
                            genre,
                            sagaForm = currentForm,
                            isLast = messages.last() == message,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                if (callback == CallBackAction.AWAITING_CONFIRMATION && isLoading.not()) {
                    item {
                        Button(
                            shape = it.shape(),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                ),
                            onClick = { saveSaga() },
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .border(1.dp, genre.gradient(), it.shape())
                                    .background(genre.gradient(), it.shape())
                                    .fillMaxWidth()
                                    .reactiveShimmer(true),
                        ) {
                            Text(
                                stringResource(R.string.save_saga),
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = genre.bodyFont(),
                                        color = genre.iconColor,
                                    ),
                            )
                        }
                    }
                }
            }

            val isBusy = isLoading
            remember {
                if (isBusy) {
                    Brush.verticalGradient(genre.colorPalette())
                } else {
                    Color.Transparent.solidGradient()
                }
            }

            remember { it.shape() }

            Column(
                Modifier.constrainAs(inputView) {
                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                },
            ) {
                AnimatedVisibility(callback != CallBackAction.AWAITING_CONFIRMATION) {
                    LazyRow(
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items(inputSuggestions) { suggestion ->
                            Button(
                                onClick = {
                                    onSendMessage(suggestion)
                                },
                                shape = it.shape(),
                                modifier =
                                    Modifier
                                        .border(1.dp, genre.color.gradientFade(), it.shape())
                                        .fillParentMaxWidth(.6f),
                                colors =
                                    ButtonDefaults.outlinedButtonColors().copy(
                                        contentColor = genre.color,
                                        containerColor = Color.Transparent,
                                    ),
                            ) {
                                Text(
                                    suggestion,
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = genre.bodyFont(),
                                            brush = genre.gradient(),
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    genre: Genre,
    isLast: Boolean,
    sagaForm: SagaForm?,
    modifier: Modifier = Modifier,
) {
    val textColor = remember { genre.iconColor }
    val isUSer = remember { message.sender == Sender.USER }
    remember { if (isUSer) genre.iconColor else genre.color }

    val bubbleStyle =
        remember(message.sender == Sender.USER, genre) {
            if (isUSer) {
                BubbleStyle.userBubble(genre)
            } else {
                BubbleStyle.characterBubble(genre, false)
            }
        }

    val bubbleShape = remember { genre.bubble(bubbleStyle.tailAlignment) }

    Column(modifier.padding(16.dp)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            horizontalArrangement = if (message.sender == Sender.USER) Arrangement.End else Arrangement.Start,
        ) {
            if (message.sender == Sender.AI) {
                Image(
                    painterResource(R.drawable.ic_spark),
                    null,
                    modifier =
                        Modifier
                            .padding(top = 24.dp)
                            .gradientFill(bubbleStyle.backgroundColor.gradientFade())
                            .size(24.dp)
                            .align(Alignment.Bottom),
                )
            }

            val horizontalPadding =
                if (isUSer) {
                    PaddingValues(
                        start = 50.dp,
                    )
                } else {
                    PaddingValues(
                        end = 50.dp,
                    )
                }
            SimpleTypewriterText(
                text = message.text,
                isAnimated = isLast,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = textColor,
                        textAlign = if (isUSer) TextAlign.End else TextAlign.Start,
                    ),
                modifier =
                    Modifier
                        .padding(horizontalPadding)
                        .background(bubbleStyle.backgroundColor, bubbleShape)
                        .padding(16.dp),
            )
        }

        if (message.callback == CallBackAction.AWAITING_CONFIRMATION && message.sender == Sender.AI) {
            sagaForm?.let { SagaFormSummaryCards(it, genre) }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NewSagaChatPreview() {
    SagAIScaffold {
        NewSagaChat(
            callback = CallBackAction.AWAITING_CONFIRMATION,
            currentForm =
                SagaForm(
                    saga =
                        SagaDraft(
                            "The one",
                            "A deep journey to find the one of a kind",
                        ),
                    character =
                        CharacterInfo(
                            "Luke",
                            "A humble warrior trying to find its place",
                        ),
                ),
            messages =
                listOf(
                    ChatMessage(text = "Hello there!", sender = Sender.USER),
                    ChatMessage(
                        text = "Hi! How can I help you?",
                        sender = Sender.AI,
                        callback = CallBackAction.AWAITING_CONFIRMATION,
                    ),
                ),
            onSendMessage = {},
        )
    }
}
