package com.ilustris.sagai.features.chat.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension.Companion.fillToConstraints
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.MessageContent
import com.ilustris.sagai.features.chat.data.model.SenderType
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.botBubbleGradient
import com.ilustris.sagai.ui.theme.cornerSize
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
    alreadyAnimatedMessages: MutableSet<Int> = remember { mutableSetOf() },
    canAnimate: Boolean = true,
) {
    val message = messageContent.message
    val isUser = message.senderType == SenderType.USER
    val sender = message.senderType
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
            SenderType.NARRATOR, SenderType.NEW_CHAPTER -> MaterialTheme.colorScheme.onBackground
            else -> genre.iconColor
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
            SenderType.NARRATOR, SenderType.NEW_CHAPTER -> 0.dp
            else -> 1.dp
        }

    val textAlignment =
        when (sender) {
            SenderType.NARRATOR, SenderType.NEW_CHAPTER -> TextAlign.Center

            else -> TextAlign.Start
        }

    val isAnimated =
        remember {
            canAnimate.not() &&
                alreadyAnimatedMessages.contains(message.id).not() &&
                isUser.not()
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
            SenderType.NARRATOR -> FontStyle.Italic
            else -> FontStyle.Normal
        }

    when (sender) {
        SenderType.USER, SenderType.CHARACTER -> {
            Column(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .padding(4.dp)
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
                TypewriterText(
                    text = message.text,
                    isAnimated = isAnimated,
                    duration = duration,
                    modifier =
                        Modifier
                            .padding(padding)
                            .fillMaxWidth()
                            .graphicsLayer(bubbleAlpha)
                            .border(borderSize, borderBrush, bubbleShape)
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
                val avatarSize = if (messageContent.character == null) 12.dp else 24.dp
                Box(
                    Modifier
                        .padding(8.dp)
                        .align(iconAlignment)
                        .size(avatarSize)
                        .background(MaterialTheme.colorScheme.surfaceContainer.gradientFade(), CircleShape),
                ) {
                    messageContent.character?.let {
                        CharacterAvatar(
                            it,
                            modifier =
                                Modifier
                                    .fillMaxSize(),
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
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontStyle = fontStyle,
                        textAlign = textAlignment,
                        fontFamily = genre.bodyFont(),
                        color = textColor,
                    ),
            )
        }

        SenderType.NEW_CHAPTER -> {
            AnimatedVisibility(messageContent.chapter != null) {
                messageContent.chapter?.let {
                    ChapterContentView(
                        genre,
                        it,
                        textColor,
                        fontStyle,
                        isBubbleVisible,
                        isAnimated,
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
private fun ChapterContentView(
    genre: Genre,
    content: Chapter,
    textColor: Color,
    fontStyle: FontStyle,
    isBubbleVisible: MutableState<Boolean>,
    isAnimated: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ConstraintLayout(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
        ) {
            val (firstDivider, title, secondDivider) = createRefs()
            Box(
                Modifier
                    .constrainAs(firstDivider) {
                        start.linkTo(parent.start)
                        end.linkTo(title.start)
                        top.linkTo(title.top)
                        bottom.linkTo(title.bottom)
                        width = fillToConstraints
                    }.height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            genre.gradient(),
                        ),
                    ),
            )
            Text(
                text = "Chapter ${content.title}",
                modifier =
                    Modifier
                        .padding(12.dp)
                        .constrainAs(title) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontFamily = genre.bodyFont(),
                        fontStyle = FontStyle.Italic,
                        color = textColor,
                        textAlign = TextAlign.Start,
                    ),
            )
            Box(
                Modifier
                    .constrainAs(secondDivider) {
                        start.linkTo(title.end)
                        end.linkTo(parent.end)
                        top.linkTo(title.top)
                        bottom.linkTo(title.bottom)
                        width = fillToConstraints
                    }.height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            genre.gradient(),
                        ),
                    ),
            )
        }

        Box {
            AsyncImage(
                model = content.coverImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(fadedGradientTopAndBottom()),
            )
        }
        Text(
            text = content.title,
            modifier = Modifier.padding(16.dp),
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
        )
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
                messageContent =
                    MessageContent(
                        Message(
                            id = 0,
                            text = "This is a test message! To demonstrate the narrator.",
                            senderType = SenderType.NARRATOR,
                            timestamp = System.currentTimeMillis(),
                            sagaId = 0,
                        ),
                    ),
                genre = genre,
                canAnimate = false,
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
                    messageContent =
                        MessageContent(
                            Message(
                                id = it,
                                text = messageText,
                                senderType = if (it % 2 == 0) SenderType.USER else SenderType.CHARACTER,
                                timestamp = System.currentTimeMillis(),
                                sagaId = 0,
                            ),
                        ),
                    genre = genre,
                )
            }

            ChatBubble(
                messageContent =
                    MessageContent(
                        Message(
                            id = 0,
                            text = "This is a new chapter message with an image.",
                            senderType = SenderType.NEW_CHAPTER,
                            timestamp = System.currentTimeMillis(),
                            sagaId = 0,
                            chapterId = 1,
                        ),
                        chapter =
                            Chapter(
                                id = 1,
                                sagaId = 0,
                                title = "Chapter 1",
                                overview = "This is the overview of Chapter 1.",
                                messageReference = 0,
                                coverImage =
                                    "https://example.com/cover_image.jpg",
                            ),
                    ),
                genre = genre,
                canAnimate = false,
            )
        }
    }
}
