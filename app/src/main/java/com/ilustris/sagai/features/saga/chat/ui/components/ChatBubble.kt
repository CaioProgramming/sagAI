package com.ilustris.sagai.features.saga.chat.ui.components

import ai.atick.material.MaterialColor
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ilustris.sagai.core.utils.formatHours
import com.ilustris.sagai.features.act.ui.ActComponent
import com.ilustris.sagai.features.chapter.ui.ChapterContentView
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.isUser
import com.ilustris.sagai.ui.theme.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.CurvedChatBubbleShape
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.dashedBorder
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.saturate
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChatBubble(
    messageContent: MessageContent,
    content: SagaContent,
    hazeState: HazeState = rememberHazeState(),
    alreadyAnimatedMessages: MutableSet<Int> = remember { mutableSetOf() },
    canAnimate: Boolean = true,
    openCharacters: () -> Unit = {},
) {
    val message = messageContent.message
    val sender = message.senderType
    val mainCharacter = content.mainCharacter
    val characters = content.characters
    val wiki = content.wikis
    val genre = content.data.genre
    val isUser = messageContent.isUser(mainCharacter)
    val cornerSize = genre.cornerSize()
    val isAnimated = isUser.not() && canAnimate
    val bubbleStyle =
        remember {
            if (isUser) {
                BubbleStyle.userBubble(genre)
            } else {
                BubbleStyle.characterBubble(
                    genre,
                    isAnimated,
                )
            }
        }
    val duration = bubbleStyle.animationDuration
    val bubbleShape =
        CurvedChatBubbleShape(
            cornerRadius = cornerSize,
            tailWidth = 5.dp,
            tailHeight = 8.dp,
            tailAlignment = bubbleStyle.tailAlignment,
        )

    when (sender) {
        SenderType.USER,
        SenderType.CHARACTER,
        -> {
            ConstraintLayout(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
            ) {
                val avatarSize = if (messageContent.character == null) 12.dp else 32.dp
                val (messageText, characterAvatar, messageTime) = createRefs()
                TypewriterText(
                    text = message.text,
                    isAnimated = isAnimated,
                    genre = genre,
                    mainCharacter = mainCharacter,
                    characters = characters,
                    wiki = wiki,
                    duration = duration,
                    easing = EaseIn,
                    onTextClick = {
                        openCharacters()
                    },
                    modifier =
                        Modifier
                            .background(bubbleStyle.backgroundColor, bubbleShape)
                            .constrainAs(messageText) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                if (isUser) {
                                    start.linkTo(parent.start, margin = 50.dp)
                                    end.linkTo(characterAvatar.start)
                                } else {
                                    start.linkTo(characterAvatar.end)
                                    end.linkTo(parent.end, margin = 50.dp)
                                }
                                width = Dimension.fillToConstraints
                            }.fillMaxWidth()
                            .padding(2.dp)
                            .clip(bubbleShape)
                            .padding(16.dp),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontFamily = genre.bodyFont(),
                            color = bubbleStyle.textColor,
                            textAlign = TextAlign.Start,
                        ),
                    onTextUpdate = {
                    },
                    onAnimationFinished = {
                        if (alreadyAnimatedMessages.contains(message.id).not()) {
                            alreadyAnimatedMessages.add(message.id)
                        }
                    },
                )
                Box(
                    Modifier
                        .padding(4.dp)
                        .constrainAs(characterAvatar) {
                            bottom.linkTo(messageText.bottom)
                            if (isUser) {
                                end.linkTo(parent.end)
                            } else {
                                start.linkTo(parent.start)
                            }
                        }.background(genre.color.copy(alpha = if (messageContent.character == null) .4f else 0f), CircleShape)
                        .clip(CircleShape)
                        .size(avatarSize),
                ) {
                    messageContent.character?.let {
                        CharacterAvatar(
                            it,
                            genre = genre,
                            softFocusRadius = 0.03f,
                            grainRadius = 0.01f,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        openCharacters()
                                    },
                        )
                    }
                }
                Text(
                    message.timestamp.formatHours(),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .4f),
                        ),
                    modifier =
                        Modifier.constrainAs(messageTime) {
                            top.linkTo(messageText.bottom)
                            if (isUser) {
                                end.linkTo(messageText.end, margin = 16.dp)
                            } else {
                                start.linkTo(messageText.start, margin = 16.dp)
                            }
                        },
                )
            }
        }

        SenderType.THOUGHT -> {
            Box(
                Modifier
                    .fillMaxWidth(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                            .padding(16.dp),
                ) {
                    messageContent.character?.let {
                        CharacterAvatar(
                            it,
                            borderSize = 1.dp,
                            softFocusRadius = 0.03f,
                            grainRadius = 0.01f,
                            genre = genre,
                            modifier =
                                Modifier
                                    .clip(CircleShape)
                                    .size(32.dp)
                                    .clickable {
                                        openCharacters()
                                    },
                        )
                    }
                    TypewriterText(
                        text = message.text,
                        isAnimated = isAnimated,
                        duration = duration,
                        genre = genre,
                        mainCharacter = mainCharacter,
                        characters = characters,
                        wiki = wiki,
                        modifier =
                            Modifier
                                .dashedBorder(
                                    1.dp,
                                    MaterialTheme.colorScheme.onBackground,
                                    genre.cornerSize(),
                                ).padding(2.dp)
                                .clip(RoundedCornerShape(genre.cornerSize()))
                                .hazeEffect(
                                    hazeState,
                                    HazeStyle(
                                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                                        tint = null,
                                        noiseFactor = 0f,
                                    ),
                                ).padding(16.dp),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                fontFamily = genre.bodyFont(),
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                        onTextClick = openCharacters,
                    )
                }
            }
        }

        SenderType.ACTION -> {
            Box(
                Modifier
                    .hazeEffect(
                        hazeState,
                        HazeStyle(
                            backgroundColor = Color.Transparent,
                            tint = null,
                            noiseFactor = 0f,
                        ),
                    ).fillMaxWidth(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier.align(
                            Alignment.CenterEnd,
                        ),
                ) {
                    messageContent.character?.let {
                        CharacterAvatar(
                            it,
                            borderSize = 1.dp,
                            softFocusRadius = 0.03f,
                            grainRadius = 0.01f,
                            genre = genre,
                            modifier =
                                Modifier
                                    .clip(CircleShape)
                                    .size(32.dp)
                                    .clickable {
                                        openCharacters()
                                    },
                        )
                    }
                    TypewriterText(
                        text = "(${message.text})",
                        isAnimated = isAnimated,
                        duration = duration,
                        genre = genre,
                        mainCharacter = mainCharacter,
                        characters = characters,
                        wiki = wiki,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                fontFamily = genre.bodyFont(),
                                color = MaterialColor.Amber400,
                                shadow = Shadow(Color.Black),
                            ),
                        onTextClick = openCharacters,
                    )
                }
            }
        }

        SenderType.NARRATOR -> {
            TypewriterText(
                text = message.text,
                isAnimated = isAnimated,
                duration = duration,
                genre = genre,
                mainCharacter = mainCharacter,
                characters = characters,
                wiki = wiki,
                modifier =
                    Modifier
                        .hazeEffect(
                            hazeState,
                            HazeStyle(
                                backgroundColor = Color.Transparent,
                                tint = null,
                                noiseFactor = 0f,
                            ),
                        ).padding(16.dp)
                        .fillMaxWidth(),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground,
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
                        mainCharacter,
                        characters,
                        wiki,
                        MaterialTheme.colorScheme.onBackground,
                        FontStyle.Normal,
                        isAnimated,
                        openCharacters = openCharacters,
                    )
                }
            }
        }

        SenderType.NEW_CHARACTER -> {
            NewCharacterView(messageContent, genre, characters, wiki) {
                openCharacters()
            }
        }

        SenderType.NEW_ACT ->
            messageContent.act?.let {
                ActComponent(
                    it,
                    content.acts.size,
                    content,
                )
            }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
fun ChatBubblePreview() {
    SagAIScaffold {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Genre.entries.forEach { genre ->
                stickyHeader {
                    Text(
                        "This is a ${genre.name} chat",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = genre.headerFont(),
                                brush = genre.gradient(),
                            ),
                        modifier = Modifier.padding(16.dp),
                    )
                }

                items(SenderType.entries) {
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
                        SagaContent(
                            data =
                                SagaData(
                                    title = "Test",
                                    description = "Test",
                                    genre = genre,
                                ),
                            mainCharacter = null,
                        ),
                    )
                }
            }
        }
    }
}

@Immutable
data class BubbleStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val tailAlignment: BubbleTailAlignment,
    val animationDuration: Duration,
    val horizontalArrangement: Arrangement.Horizontal,
    val animationEnabled: Boolean,
) {
    companion object {
        fun userBubble(genre: Genre) =
            BubbleStyle(
                backgroundColor = genre.color,
                textColor = genre.iconColor,
                tailAlignment = BubbleTailAlignment.BottomRight,
                animationDuration = 0.seconds,
                horizontalArrangement = Arrangement.End,
                false,
            )

        fun characterBubble(
            genre: Genre,
            canAnimate: Boolean,
        ) = BubbleStyle(
            backgroundColor = genre.color.saturate(0.3f),
            textColor = genre.iconColor,
            tailAlignment = BubbleTailAlignment.BottomLeft,
            animationDuration = 5.seconds,
            horizontalArrangement = Arrangement.Start,
            canAnimate,
        )
    }
}
