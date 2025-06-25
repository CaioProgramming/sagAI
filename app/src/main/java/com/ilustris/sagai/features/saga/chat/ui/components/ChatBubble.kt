package com.ilustris.sagai.features.saga.chat.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.chapter.ui.ChapterContentView
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.ui.theme.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.CurvedChatBubbleShape
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.dashedBorder
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChatBubble(
    messageContent: MessageContent,
    genre: Genre,
    characters: List<Character>,
    alreadyAnimatedMessages: MutableSet<Int> = remember { mutableSetOf() },
    canAnimate: Boolean = true,
    openCharacters: () -> Unit = {},
) {
    val message = messageContent.message
    val sender = message.senderType

    val isUser =
        when (sender) {
            SenderType.USER, SenderType.THOUGHT, SenderType.ACTION -> true
            else -> false
        }
    val bubbleColor =
        when (sender) {
            SenderType.USER -> genre.color
            SenderType.CHARACTER ->
                genre.color.copy(
                    alpha = .4f,
                )
            else -> Color.Transparent
        }

    val textColor =
        when (sender) {
            SenderType.NARRATOR, SenderType.NEW_CHAPTER,
            SenderType.THOUGHT, SenderType.ACTION, SenderType.NEW_CHARACTER,
            -> MaterialTheme.colorScheme.onBackground

            else -> genre.iconColor
        }

    val cornerSize = genre.cornerSize()
    val tailAlignment = if (isUser) BubbleTailAlignment.BottomRight else BubbleTailAlignment.BottomLeft
    val bubbleShape =
        CurvedChatBubbleShape(
            cornerRadius = cornerSize,
            tailWidth = 5.dp,
            tailHeight = 8.dp,
            tailAlignment = tailAlignment,
        )
    val borderBrush =
        when (sender) {
            SenderType.NARRATOR, SenderType.NEW_CHAPTER ->
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.Transparent,
                    ),
                )

            else -> MaterialTheme.colorScheme.onBackground.gradientFade()
        }

    val borderSize =
        when (sender) {
            SenderType.NARRATOR, SenderType.NEW_CHAPTER, SenderType.ACTION, SenderType.THOUGHT -> 0.dp
            else -> 0.dp
        }

    val textAlignment =
        when (sender) {
            SenderType.NARRATOR, SenderType.NEW_CHAPTER, SenderType.ACTION -> TextAlign.Center

            else -> TextAlign.Start
        }

    val isAnimated = isUser.not() && canAnimate

    val isBubbleVisible =
        remember {
            mutableStateOf(false)
        }

    val bubbleAlpha by animateFloatAsState(
        targetValue = if (isBubbleVisible.value) 1f else 0f,
        label = "Bubble Alpha Animation",
    )

    val duration =
        when (sender) {
            SenderType.USER -> 0.seconds
            SenderType.CHARACTER -> 5.seconds
            else -> 10.seconds
        }
    val fontStyle =
        when (sender) {
            SenderType.NARRATOR, SenderType.ACTION -> FontStyle.Italic
            else -> FontStyle.Normal
        }

    when (sender) {
        SenderType.USER, SenderType.CHARACTER, SenderType.THOUGHT -> {
            Box(Modifier.fillMaxWidth()) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(.7f)
                            .align(if (isUser) Alignment.BottomEnd else Alignment.BottomStart)
                            .padding(8.dp)
                            .animateContentSize(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    @Composable
                    fun Modifier.senderBorder() =
                        if (sender == SenderType.THOUGHT) {
                            this.dashedBorder(
                                1.dp,
                                textColor.copy(alpha = .3f),
                                cornerSize,
                            )
                        } else {
                            this.border(borderSize, borderBrush, bubbleShape)
                        }
                    val avatarSize = if (messageContent.character == null) 12.dp else 32.dp

                    if (isUser) {
                        TypewriterText(
                            text = message.text,
                            isAnimated = isAnimated,
                            characters = characters,
                            duration = duration,
                            easing = EaseIn,
                            onTextClick = {
                                openCharacters()
                            },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically)
                                    .senderBorder()
                                    .background(
                                        color = bubbleColor,
                                        bubbleShape,
                                    ).padding(16.dp),
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
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .size(avatarSize),
                        ) {
                            messageContent.character?.let {
                                CharacterAvatar(
                                    it,
                                    genre = genre,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                openCharacters()
                                            },
                                )
                            }
                        }
                    } else {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .size(avatarSize),
                        ) {
                            messageContent.character?.let {
                                CharacterAvatar(
                                    it,
                                    genre = genre,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                openCharacters()
                                            },
                                )
                            }
                        }
                        TypewriterText(
                            text = message.text,
                            isAnimated = isAnimated,
                            characters = characters,
                            duration = duration,
                            easing = LinearOutSlowInEasing,
                            onTextClick = {
                                openCharacters()
                            },
                            modifier =
                                Modifier
                                    .weight(.5f, false)
                                    .graphicsLayer(bubbleAlpha)
                                    .background(
                                        bubbleColor,
                                        bubbleShape,
                                    ).padding(16.dp),
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
        }

        SenderType.ACTION -> {
            Box(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.align(
                            Alignment.CenterEnd,
                        ),
                ) {
                    Icon(
                        painterResource(
                            R.drawable.action_icon,
                        ),
                        null,
                        modifier =
                            Modifier
                                .size(24.dp)
                                .graphicsLayer(alpha = .6f),
                    )
                    TypewriterText(
                        text = message.text,
                        isAnimated = isAnimated,
                        duration = duration,
                        characters = characters,
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(16.dp),
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontStyle = fontStyle,
                                textAlign = textAlignment,
                                fontFamily = genre.bodyFont(),
                                color = textColor.copy(alpha = .7f),
                            ),
                        onTextClick = openCharacters,
                    )

                    messageContent.character?.let {
                        CharacterAvatar(
                            it,
                            borderSize = 1.dp,
                            genre = genre,
                            modifier =
                                Modifier.clip(CircleShape).size(32.dp).clickable {
                                    openCharacters()
                                },
                        )
                    }
                }
            }
        }

        SenderType.NARRATOR -> {
            TypewriterText(
                text = message.text,
                isAnimated = isAnimated,
                duration = duration,
                characters = characters,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontStyle = fontStyle,
                        textAlign = textAlignment,
                        fontFamily = genre.bodyFont(),
                        color = textColor,
                    ),
                onTextClick = openCharacters,
            )
        }

        SenderType.NEW_CHAPTER -> {
            AnimatedVisibility(messageContent.chapter != null) {
                messageContent.chapter?.let {
                    ChapterContentView(
                        genre,
                        it,
                        characters,
                        textColor,
                        fontStyle,
                        isBubbleVisible,
                        isAnimated,
                        openCharacters = openCharacters,
                    )
                }
            }
        }

        SenderType.NEW_CHARACTER -> {
            NewCharacterView(messageContent, genre)
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true,
)
@Composable
fun ChatBubblePreview() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.verticalScroll(
                rememberScrollState(),
            ),
    ) {
        Genre.entries.forEach { genre ->
            Text(
                "This is a ${genre.name} chat",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = genre.headerFont(),
                    ),
                modifier = Modifier.padding(16.dp),
            )
            SenderType.entries.forEach {
                ChatBubble(
                    messageContent =
                        MessageContent(
                            Message(
                                id = 0,
                                text = "This is a test message! To demonstrate the ${it.name}.",
                                senderType = it,
                                timestamp = System.currentTimeMillis(),
                                sagaId = 0,
                            ),
                        ),
                    genre = genre,
                    characters = emptyList(),
                )
            }

            Box(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(genre.color),
            )
        }
    }
}
