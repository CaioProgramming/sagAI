package com.ilustris.sagai.features.chat.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.SenderType
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.botBubbleGradient
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.userBubbleGradient
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChatBubble(
    message: Message,
    genre: Genre,
    alreadyAnimatedMessages: MutableSet<Int> = remember { mutableSetOf() },
    canAnimate: Boolean = true,
) {
    val isUser = message.senderType == SenderType.USER
    val sender = message.senderType
    val bubbleColor =
        when (sender) {
            SenderType.USER -> genre.userBubbleGradient()
            SenderType.BOT -> genre.botBubbleGradient()
            SenderType.NARRATOR ->
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.Transparent,
                    ),
                )
        }

    val textColor =
        when (sender) {
            SenderType.NARRATOR -> MaterialTheme.colorScheme.onBackground
            else -> genre.iconColor
        }
    val alignment =
        when (sender) {
            SenderType.USER -> Alignment.CenterEnd
            SenderType.BOT -> Alignment.CenterStart
            SenderType.NARRATOR -> Alignment.Center
        }
    val cornerSize = genre.cornerSize()
    val bubbleShape =
        when (sender) {
            SenderType.USER ->
                RoundedCornerShape(
                    topStart = cornerSize,
                    topEnd = cornerSize,
                    bottomStart = cornerSize,
                    bottomEnd = 0.dp,
                )

            SenderType.BOT ->
                RoundedCornerShape(
                    topStart = cornerSize,
                    topEnd = cornerSize,
                    bottomStart = 0.dp,
                    bottomEnd = cornerSize,
                )

            SenderType.NARRATOR -> RoundedCornerShape(0.dp)
        }

    val borderBrush =
        when (sender) {
            SenderType.NARRATOR -> Brush.verticalGradient(
                listOf(
                    Color.Transparent,
                    Color.Transparent,
                ),
            )
            else -> MaterialTheme.colorScheme.onBackground.gradientFade()
        }

    val borderSize =
        when (sender) {
            SenderType.NARRATOR -> 0.dp
            else -> 1.dp
        }

    val textAlignment =
        when (sender) {
            SenderType.NARRATOR -> TextAlign.Center

            else -> TextAlign.Start
        }

    val isAnimated =
        remember {
            canAnimate.not() &&  alreadyAnimatedMessages.contains(message.id).not() &&
                isUser.not()
        }

    val isBubbleVisible =
        remember {
            mutableStateOf(false)
        }
    
    val bubblePadding = when(sender) {
        SenderType.USER -> PaddingValues(
            start = 100.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 8.dp,
        )
        SenderType.BOT -> PaddingValues(
            start = 16.dp,
            end = 100.dp,
            top = 8.dp,
            bottom = 8.dp,
        )
        SenderType.NARRATOR -> PaddingValues(16.dp)
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bubblePadding)
                .animateContentSize(),
        contentAlignment = alignment,
    ) {
        val bubbleAlpha by animateFloatAsState(
            targetValue = if (isBubbleVisible.value) 1f else 0f,
            label = "Bubble Alpha Animation",
        )

        Box(
            modifier =
                Modifier
                    .alpha(bubbleAlpha)
                    .clip(bubbleShape)
                    .border(
                        borderSize,
                        borderBrush,
                        bubbleShape,
                    ).background(bubbleColor)
                    .padding(12.dp),
        ) {
            val duration = when(sender) {
                SenderType.USER -> 0.seconds
                SenderType.BOT -> 5.seconds
                SenderType.NARRATOR -> 10.seconds
            }
            val fontStyle = when(sender) {
                SenderType.NARRATOR -> FontStyle.Italic
                else -> FontStyle.Normal
            }
            TypewriterText(
                text = message.text,
                isAnimated = isAnimated,
                duration = duration,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal,
                        fontFamily = genre.bodyFont(),
                        fontStyle = fontStyle,
                        color = textColor,
                        textAlign = textAlignment,
                    ),
                onTextUpdate = {
                    isBubbleVisible.value = it.isNotEmpty() || isAnimated.not()
                },
                onAnimationFinished = {
                    if (alreadyAnimatedMessages.contains(message.id).not()) {
                        alreadyAnimatedMessages.add(message.id)
                    }
                },
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true,
)
@Composable
fun ChatBubblePreview() {
    Column {
        Genre.entries.forEach { genre ->
            ChatBubble(
                message =
                    Message(
                        id = 0,
                        text = "This is a test message! To demonstrate the narrator.",
                        senderType = SenderType.NARRATOR,
                        timestamp = System.currentTimeMillis(),
                        sagaId = 0,
                    ),
                genre = genre,
            )
            repeat(2) {
                val messageText =
                    if (it % 2 == 0) {
                        "Hello," +
                            "this is a test message! To demonstrate the chat bubble." +
                            "in the genre of ${genre.name}."
                    } else {
                        "This is a response from the other side." +
                            "How are you doing? in the genre of ${genre.name}."
                    }
                ChatBubble(
                    message =
                        Message(
                            id = it,
                            text = messageText,
                            senderType = if (it % 2 == 0) SenderType.USER else SenderType.BOT,
                            timestamp = System.currentTimeMillis(),
                            sagaId = 0,
                        ),
                    genre = genre,
                )
            }
        }
    }
}
