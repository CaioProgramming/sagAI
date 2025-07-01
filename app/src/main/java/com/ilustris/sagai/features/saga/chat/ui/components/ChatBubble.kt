package com.ilustris.sagai.features.saga.chat.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.CurvedChatBubbleShape
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.dashedBorder
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.saturate
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChatBubble(
    messageContent: MessageContent,
    mainCharacter: Character?,
    genre: Genre,
    characters: List<Character>,
    wiki: List<Wiki>,
    hazeState: HazeState = rememberHazeState(),
    alreadyAnimatedMessages: MutableSet<Int> = remember { mutableSetOf() },
    canAnimate: Boolean = true,
    openCharacters: () -> Unit = {},
) {
    val message = messageContent.message
    val sender = message.senderType

    val isUser =
        if (message.characterId == mainCharacter?.id || message.speakerName == mainCharacter?.name) {
            true
        } else {
            when (sender) {
                SenderType.USER -> true
                else -> false
            }
        }

    val bubbleColor =

        when (sender) {
            SenderType.USER -> genre.color
            SenderType.CHARACTER -> {
                if (isUser) {
                    genre.color
                } else {
                    genre.color.saturate(0.3f)
                }
            }

            else -> Color.Transparent
        }

    val textColor =
        if (sender == SenderType.THOUGHT) MaterialTheme.colorScheme.onBackground else genre.iconColor

    val cornerSize = genre.cornerSize()
    val tailAlignment =
        if (isUser) BubbleTailAlignment.BottomRight else BubbleTailAlignment.BottomLeft
    val bubbleShape =
        CurvedChatBubbleShape(
            cornerRadius = cornerSize,
            tailWidth = 5.dp,
            tailHeight = 8.dp,
            tailAlignment = tailAlignment,
        )

    val textAlignment =
        when (sender) {
            SenderType.NARRATOR, SenderType.NEW_CHAPTER, SenderType.ACTION -> TextAlign.Center

            else -> TextAlign.Start
        }

    val isAnimated = isUser.not() && canAnimate

    val duration =
        when (sender) {
            SenderType.USER -> 0.seconds
            SenderType.CHARACTER -> 5.seconds
            else -> 5.seconds
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
                                MaterialTheme.colorScheme.onBackground,
                                cornerSize,
                            )
                        } else {
                            this.border(0.dp, Color.Transparent, bubbleShape)
                        }

                    @Composable
                    fun Modifier.bubbleHaze() =
                        this.hazeEffect(
                            hazeState,
                            HazeStyle(
                                backgroundColor = Color.Transparent,
                                tint = HazeTint(bubbleColor),
                                blurRadius = 25.dp,
                                noiseFactor = 0f,
                            ),
                        )

                    val avatarSize = if (messageContent.character == null) 12.dp else 32.dp

                    if (isUser) {
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
                                    .weight(1f)
                                    .align(Alignment.CenterVertically)
                                    .senderBorder()
                                    .padding(2.dp)
                                    .clip(
                                        if (sender != SenderType.THOUGHT) {
                                            bubbleShape
                                        } else {
                                            RoundedCornerShape(
                                                0.dp,
                                            )
                                        },
                                    ).bubbleHaze()
                                    .padding(16.dp),
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = genre.bodyFont(),
                                    fontStyle = fontStyle,
                                    color = textColor,
                                    textAlign = textAlignment,
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
                        TypewriterText(
                            text = message.text,
                            isAnimated = isAnimated,
                            genre = genre,
                            mainCharacter = mainCharacter,
                            characters = characters,
                            wiki = wiki,
                            duration = duration,
                            easing = LinearOutSlowInEasing,
                            onTextClick = {
                                openCharacters()
                            },
                            modifier =
                                Modifier
                                    .weight(.5f, false)
                                    .senderBorder()
                                    .padding(2.dp)
                                    .clip(
                                        if (sender != SenderType.THOUGHT) {
                                            bubbleShape
                                        } else {
                                            RoundedCornerShape(
                                                0.dp,
                                            )
                                        },
                                    ).bubbleHaze()
                                    .padding(16.dp),
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = genre.bodyFont(),
                                    fontStyle = fontStyle,
                                    color = textColor,
                                    textAlign = textAlignment,
                                ),
                            onTextUpdate = {
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
                    .hazeEffect(hazeState)
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
                        tint = genre.iconColor,
                        modifier =
                            Modifier
                                .size(24.dp)
                                .graphicsLayer(alpha = .6f),
                    )
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
                                .weight(1f)
                                .padding(16.dp),
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontStyle = fontStyle,
                                textAlign = textAlignment,
                                fontFamily = genre.bodyFont(),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                            ),
                        onTextClick = openCharacters,
                    )

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
                        fontStyle = fontStyle,
                        textAlign = textAlignment,
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
                        fontStyle,
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
                    wiki = emptyList(),
                    mainCharacter = null,
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
