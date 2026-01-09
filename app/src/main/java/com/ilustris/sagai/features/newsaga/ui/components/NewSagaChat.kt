@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.newsaga.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.manager.FormState
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Sender
import com.ilustris.sagai.features.saga.chat.ui.components.BubbleStyle
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.shape
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NewSagaChat(
    state: FormState.NewSagaForm,
    continueToCharacter: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val genre = state.draft.genre
    val messages = state.messages

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }

    AnimatedContent(genre, transitionSpec = {
        fadeIn() togetherWith fadeOut()
    }, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
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
                        isLast = messages.last() == message,
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            if (state.isReady) {
                item {
                    Button(
                        onClick = continueToCharacter,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = genre.color,
                                contentColor = genre.iconColor,
                            ),
                        shape = genre.shape(),
                    ) {
                        Text(
                            "Criar personagem",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = genre.bodyFont()),
                            modifier = Modifier.padding(8.dp),
                        )
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
    }
}
