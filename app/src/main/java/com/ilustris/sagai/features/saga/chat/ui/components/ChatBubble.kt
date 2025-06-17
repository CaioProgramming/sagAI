package com.ilustris.sagai.features.saga.chat.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.botBubbleGradient
import com.ilustris.sagai.ui.theme.bubbleTextColors
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.dashedBorder
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.userBubbleGradient
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
            SenderType.USER -> genre.userBubbleGradient()
            SenderType.CHARACTER -> genre.botBubbleGradient()
            else ->
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.Transparent,
                    ),
                )
        }

    val textColor =
        when (sender) {
            SenderType.NARRATOR, SenderType.NEW_CHAPTER,
            SenderType.THOUGHT, SenderType.ACTION,
            -> MaterialTheme.colorScheme.onBackground

            else -> genre.bubbleTextColors(sender)
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

            SenderType.CHARACTER ->
                RoundedCornerShape(
                    topStart = cornerSize,
                    topEnd = cornerSize,
                    bottomStart = 0.dp,
                    bottomEnd = cornerSize,
                )

            else -> RoundedCornerShape(0.dp)
        }

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
            else -> 1.dp
        }

    val textAlignment =
        when (sender) {
            SenderType.NARRATOR, SenderType.NEW_CHAPTER, SenderType.ACTION -> TextAlign.Center

            else -> TextAlign.Start
        }

    val isAnimated =
        remember {
            isUser.not() && alreadyAnimatedMessages.none { it == message.id }
        }

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
            Column(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .padding(8.dp)
                        .animateContentSize(),
            ) {
                val iconAlignment = if (isUser) Alignment.End else Alignment.Start
                val padding =
                    PaddingValues(
                        start = if (isUser) 0.dp else 24.dp,
                        end = if (isUser) 24.dp else 0.dp,
                        top = 4.dp,
                        bottom = 4.dp,
                    )

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
                TypewriterText(
                    text = message.text,
                    isAnimated = isAnimated,
                    characters = characters,
                    duration = duration,
                    easing = if (isUser) EaseIn else LinearOutSlowInEasing,
                    onTextClick = {
                        openCharacters()
                    },
                    modifier =
                        Modifier
                            .padding(padding)
                            .align(iconAlignment)
                            .fillMaxWidth(.65f)
                            .graphicsLayer(bubbleAlpha)
                            .senderBorder()
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
                val avatarSize = if (messageContent.character == null) 12.dp else 32.dp
                Box(
                    Modifier
                        .padding(8.dp)
                        .clip(CircleShape)
                        .align(iconAlignment)
                        .size(avatarSize),
                ) {
                    messageContent.character?.let {
                        CharacterAvatar(
                            it,
                            borderSize = 1.dp,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        openCharacters()
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
                        emptyList(),
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

@Composable
fun ChapterContentView(
    genre: Genre,
    content: Chapter,
    characters: List<Character>,
    textColor: Color,
    fontStyle: FontStyle,
    isBubbleVisible: MutableState<Boolean>,
    isAnimated: Boolean,
    openCharacters: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .background(fadeGradientBottom())
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .fillMaxWidth(.20f)
                    .height(1.dp)
                    .background(genre.color),
            )

            Text(
                text = content.title,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .weight(1f),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontFamily = genre.bodyFont(),
                        fontStyle = FontStyle.Italic,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    ),
            )

            Box(
                Modifier
                    .fillMaxWidth(.20f)
                    .height(1.dp)
                    .background(genre.color),
            )
        }

        var imageSize by remember {
            mutableStateOf(
                300.dp,
            )
        }

        val sizeAnimation by animateDpAsState(
            targetValue = imageSize,
            label = "Image Size Animation",
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(sizeAnimation),
        ) {
            AsyncImage(
                model = content.coverImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = {
                    imageSize = 0.dp
                },
                modifier =
                    Modifier.fillMaxSize(),
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(fadedGradientTopAndBottom()),
            )
        }

        Text(
            text = content.title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(fadeGradientTop())
                    .padding(16.dp),
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 5.sp,
                    fontFamily = genre.headerFont(),
                    fontStyle = fontStyle,
                    brush = gradientAnimation(genre.gradient()),
                    textAlign = TextAlign.Center,
                ),
        )

        TypewriterText(
            text = content.overview,
            modifier = Modifier.padding(16.dp),
            duration = 8.seconds,
            isAnimated = isAnimated,
            characters = characters,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontFamily = genre.bodyFont(),
                    fontStyle = fontStyle,
                    color = textColor,
                    textAlign = TextAlign.Center,
                ),
            onTextUpdate = {
                isBubbleVisible.value = it.isNotEmpty() || isAnimated.not()
            },
            onTextClick = openCharacters,
        )
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
