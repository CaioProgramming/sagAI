package com.ilustris.sagai.features.saga.chat.ui.components

import ai.atick.material.MaterialColor
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.isUser
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.CurvedChatBubbleShape
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.dashedBorder
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.saturate
import com.ilustris.sagai.ui.theme.shape
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChatBubble(
    messageContent: MessageContent,
    content: SagaContent,
    alreadyAnimatedMessages: MutableSet<Int> = remember { mutableSetOf() },
    canAnimate: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    openCharacters: (CharacterContent?) -> Unit = {},
    openWiki: () -> Unit = {},
) {
    val message = messageContent.message
    val sender = message.senderType
    val mainCharacter = content.mainCharacter
    val characters = content.characters
    val wiki = content.wikis
    val genre = content.data.genre
    val isUser = messageContent.isUser(mainCharacter?.data)
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
            tailWidth = 4.dp,
            tailHeight = 4.dp,
            tailAlignment = bubbleStyle.tailAlignment,
        )

    when (sender) {
        SenderType.USER,
        SenderType.CHARACTER,
        -> {
            ConstraintLayout(
                modifier =
                    modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .animateContentSize(),
            ) {
                val avatarSize = if (messageContent.character == null) 12.dp else 32.dp
                val (messageText, characterAvatar, messageTime) = createRefs()
                val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                Box(
                    Modifier
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
                        }.fillMaxWidth(),
                ) {
                    TypewriterText(
                        text = message.text,
                        isAnimated = isAnimated,
                        genre = genre,
                        mainCharacter = mainCharacter?.data,
                        characters = characters.map { it.data },
                        wiki = wiki,
                        duration = duration,
                        easing = EaseIn,
                        onTextClick = {
                        },
                        modifier =
                            Modifier
                                .wrapContentSize()
                                .background(bubbleStyle.backgroundColor, bubbleShape)
                                .clip(bubbleShape)
                                .padding(16.dp)
                                .align(alignment)
                                .animateContentSize()
                                .reactiveShimmer(isLoading, genre.shimmerColors()),
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
                }

                Box(
                    Modifier
                        .constrainAs(characterAvatar) {
                            bottom.linkTo(messageText.bottom)
                            if (isUser) {
                                end.linkTo(parent.end)
                            } else {
                                start.linkTo(parent.start)
                            }
                        }.padding(8.dp)
                        .background(
                            genre.color.copy(alpha = if (messageContent.character == null) .4f else 0f),
                            CircleShape,
                        ).clip(CircleShape)
                        .size(avatarSize),
                ) {
                    messageContent.character?.let { character ->
                        AnimatedContent(character, transitionSpec = {
                            fadeIn() + scaleIn() togetherWith scaleOut()
                        }) {
                            CharacterAvatar(
                                it,
                                isLoading = isLoading,
                                genre = genre,
                                borderSize = 2.dp,
                                pixelation = 0f,
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            openCharacters(characters.find { c -> c.data.id == character.id })
                                        },
                            )
                        }
                    }
                }

                Row(
                    modifier =
                        Modifier.constrainAs(messageTime) {
                            top.linkTo(messageText.bottom)
                            if (isUser) {
                                end.linkTo(messageText.end, margin = 16.dp)
                            } else {
                                start.linkTo(messageText.start, margin = 16.dp)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val containsWiki =
                        wiki.any {
                            message.text.contains(it.title, true) ||
                                message.text.contains(it.content, true)
                        }

                    Text(
                        message.timestamp.formatHours(),
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                    )

                    AnimatedVisibility(
                        containsWiki,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.alpha(.4f),
                    ) {
                        IconButton(onClick = {
                            openWiki()
                        }, modifier = Modifier.size(12.dp)) {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null,
                                tint = genre.iconColor,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }

        SenderType.THOUGHT -> {
            Box(
                modifier
                    .fillMaxWidth(),
            ) {
                ConstraintLayout(
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                            .padding(16.dp),
                ) {
                    val (characterAvatar, text, starPlaceHolder) = createRefs()

                    var starAlpha by remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
                    val alphaAnimation by animateFloatAsState(
                        starAlpha,
                        tween(1000, easing = FastOutSlowInEasing),
                    )
                    val blurAnimation by animateFloatAsState(
                        if (starAlpha == 1f) 0f else 1f,
                    )
                    TypewriterText(
                        text = message.text,
                        isAnimated = isAnimated,
                        duration = duration,
                        genre = genre,
                        mainCharacter = mainCharacter?.data,
                        characters = content.getCharacters(),
                        wiki = wiki,
                        modifier =
                            Modifier
                                .constrainAs(text) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }.clip(genre.shape())
                                .dashedBorder(
                                    1.dp,
                                    MaterialTheme.colorScheme.onBackground,
                                    genre.cornerSize(),
                                ).padding(2.dp)
                                .background(
                                    MaterialTheme.colorScheme.background.copy(alpha = .4f),
                                    RoundedCornerShape(genre.cornerSize()),
                                ).padding(16.dp)
                                .alpha(blurAnimation)
                                .reactiveShimmer(isLoading, genre.shimmerColors()),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                fontFamily = genre.bodyFont(),
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                        onTextClick = {},
                    )

                    StarryTextPlaceholder(
                        modifier =
                            Modifier
                                .alpha(alphaAnimation)
                                .clip(genre.shape())
                                .clickable {
                                    starAlpha = 0f
                                }.background(
                                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .4f),
                                    genre.shape(),
                                ).constrainAs(starPlaceHolder) {
                                    top.linkTo(text.top)
                                    bottom.linkTo(text.bottom)
                                    start.linkTo(text.start)
                                    end.linkTo(text.end)
                                    width = Dimension.fillToConstraints
                                    height = Dimension.fillToConstraints
                                },
                        starColor = genre.color,
                    )

                    messageContent.character?.let {
                        CharacterAvatar(
                            it,
                            isLoading = isLoading,
                            borderSize = 2.dp,
                            genre = genre,
                            pixelation = 0f,
                            modifier =
                                Modifier
                                    .constrainAs(characterAvatar) {
                                        top.linkTo(text.top)
                                        start.linkTo(text.start)
                                        end.linkTo(text.end)
                                    }.offset(y = 16.unaryMinus().dp)
                                    .clip(CircleShape)
                                    .size(32.dp)
                                    .clickable {
                                        openCharacters(characters.find { c -> c.data.id == it.id })
                                    },
                        )
                    }
                }
            }
        }

        SenderType.ACTION -> {
            Box(
                modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier.align(
                            Alignment.Center,
                        ),
                ) {
                    messageContent.character?.let {
                        CharacterAvatar(
                            it,
                            isLoading = isLoading,
                            borderSize = 2.dp,
                            genre = genre,
                            pixelation = 0f,
                            modifier =
                                Modifier
                                    .clip(CircleShape)
                                    .size(32.dp)
                                    .clickable {
                                        openCharacters(characters.find { c -> c.data.id == it.id })
                                    },
                        )
                    }
                    TypewriterText(
                        text = "(${message.text})",
                        isAnimated = isAnimated,
                        duration = duration,
                        genre = genre,
                        mainCharacter = mainCharacter?.data,
                        characters = content.getCharacters(),
                        wiki = wiki,
                        modifier =
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .background(
                                    Color.Black,
                                    shape = RoundedCornerShape(genre.cornerSize()),
                                ).padding(16.dp)
                                .reactiveShimmer(isLoading, genre.shimmerColors()),
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                fontFamily = genre.bodyFont(),
                                color = MaterialColor.Amber400,
                            ),
                        onTextClick = { },
                    )
                }
            }
        }

        SenderType.NARRATOR -> {
            TypewriterText(
                text = message.text,
                isAnimated = false,
                duration = duration,
                genre = genre,
                mainCharacter = mainCharacter?.data,
                characters = content.getCharacters(),
                wiki = wiki,
                modifier =
                    modifier
                        .padding(16.dp)
                        .reactiveShimmer(isLoading, genre.shimmerColors())
                        .fillMaxWidth(),
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Justify,
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                        shadow =
                            Shadow(
                                color = genre.color,
                                offset = Offset(4f, 4f),
                                blurRadius = 10f,
                            ),
                    ),
                onTextClick = { },
            )
        }

        else -> Box {}
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
            val character =
                Character(
                    name = "John",
                    details = Details(),
                )
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
                                    timelineId = 0,
                                ),
                                character = character,
                            ),
                        content =
                            SagaContent(
                                data =
                                    Saga(
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
