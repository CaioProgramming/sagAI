package com.ilustris.sagai.features.saga.chat.ui.components

import MessageStatus
import ai.atick.material.MaterialColor
import android.content.res.Configuration
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.formatHours
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.isUser
import com.ilustris.sagai.features.saga.chat.ui.animations.emotionalEntrance
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.dashedBorder
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    messageContent: MessageContent,
    content: SagaContent,
    canAnimate: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    openCharacters: (CharacterContent?) -> Unit = {},
    onRetry: (MessageContent) -> Unit = {},
    onReactionsClick: (MessageContent) -> Unit = {},
    requestNewCharacter: () -> Unit = {},
    messageEffectsEnabled: Boolean = true,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onLongPress: () -> Unit = {},
) {
    val message = messageContent.message
    val sender = message.senderType
    val mainCharacter = content.mainCharacter
    val characters = content.characters
    val wiki = content.wikis
    val genre = content.data.genre
    val isUser = messageContent.isUser(mainCharacter?.data)
    val cornerSize = genre.cornerSize()
    val isAnimated = canAnimate && messageEffectsEnabled.not()
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
        remember(genre, cornerSize, bubbleStyle.tailAlignment) {
            genre.bubble(bubbleStyle.tailAlignment)
        }
    val narratorShape =
        remember(genre, cornerSize) {
            genre.bubble(
                BubbleTailAlignment.BottomRight,
                isNarrator = true,
                tailHeight = 0.dp,
                tailWidth = 0.dp,
            )
        }
    var tooltipData by remember { mutableStateOf<Any?>(null) }

    val reactionToolTipState =
        androidx.compose.material3.rememberTooltipState(
            isPersistent = true,
        )
    val tooltipPositionProvider =
        androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above,
            spacingBetweenTooltipAndAnchor = 0.dp,
        )

    LaunchedEffect(tooltipData) {
        if (tooltipData != null) {
            reactionToolTipState.show()
        } else {
            reactionToolTipState.dismiss()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "border_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "rotation",
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "scaleAnimation",
    )

    val paddingAnimation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "paddingAnimation",
    )

    val borderColorAnimation by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "borderColorAnimation",
    )

    when (sender) {
        SenderType.USER,
        SenderType.CHARACTER,
        SenderType.THOUGHT,
        SenderType.ACTION,
        -> {
            val layoutDirection = if (isUser) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                Box(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .animateContentSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        val avatarSize = if (messageContent.character == null) 24.dp else 50.dp

                        Box(
                            Modifier
                                .clip(CircleShape)
                                .size(avatarSize),
                        ) {
                            messageContent.character?.let { character ->
                                AnimatedContent(
                                    character,
                                    transitionSpec = {
                                        fadeIn() + scaleIn() togetherWith scaleOut()
                                    },
                                ) {
                                    CharacterAvatar(
                                        it,
                                        isLoading = isLoading,
                                        genre = genre,
                                        borderSize = 2.dp,
                                        pixelation = 0f,
                                        grainRadius = 0f,
                                        modifier =
                                            Modifier
                                                .padding(8.dp)
                                                .fillMaxSize()
                                                .clickable {
                                                    openCharacters(characters.find { c -> c.data.id == character.id })
                                                },
                                    )
                                }

                                val relationWithMainCharacter =
                                    mainCharacter
                                        ?.findRelationship(character.id)
                                        ?.sortedByEvents(content.flatEvents().map { it.data })
                                        ?.firstOrNull()

                                if (isUser.not()) {
                                    relationWithMainCharacter?.let {
                                        Text(
                                            it.emoji,
                                            style =
                                                MaterialTheme.typography.labelSmall.copy(
                                                    shadow =
                                                        Shadow(
                                                            color =
                                                                character.hexColor.hexToColor()
                                                                    ?: genre.color,
                                                            offset = Offset(2f, 2f),
                                                            blurRadius = 0f,
                                                        ),
                                                ),
                                            modifier =
                                                Modifier
                                                    .animateContentSize()
                                                    .align(Alignment.BottomCenter)
                                                    .padding(2.dp),
                                        )
                                    }
                                }
                            } ?: run {
                                Image(
                                    painterResource(R.drawable.ic_spark),
                                    null,
                                    Modifier
                                        .clickable {
                                            requestNewCharacter()
                                        }
                                        .size(24.dp)
                                        .gradientFill(genre.gradient()),
                                )
                            }
                        }

                        Column(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .padding(end = 50.dp),
                        ) {
                            val bubbleModifier =
                                if (message.status == MessageStatus.LOADING) {
                                    Modifier
                                        .emotionalEntrance(
                                            message.emotionalTone,
                                            isAnimated && messageEffectsEnabled,
                                        )
                                        .wrapContentSize()
                                        .drawWithContent {
                                            drawContent()
                                            val outline =
                                                bubbleShape.createOutline(
                                                    size,
                                                    layoutDirection,
                                                    this,
                                                )
                                            val brush =
                                                object : ShaderBrush() {
                                                    override fun createShader(size: Size): Shader {
                                                        val shader =
                                                            (
                                                                sweepGradient(
                                                                    genre.colorPalette(),
                                                                    ) as ShaderBrush
                                                                    ).createShader(size)
                                                        val matrix = Matrix()
                                                        matrix.setRotate(
                                                            rotation,
                                                            size.width / 2,
                                                            size.height / 2,
                                                        )
                                                        shader.setLocalMatrix(matrix)
                                                        return shader
                                                    }
                                                }
                                            drawOutline(
                                                outline = outline,
                                                brush = brush,
                                                style = Stroke(width = 1.dp.toPx()),
                                            )
                                        }.background(
                                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .3f),
                                            bubbleShape,
                                        )
                                } else {
                                    when (sender) {
                                        SenderType.USER -> {
                                            Modifier
                                                .combinedClickable(
                                                    interactionSource = interactionSource,
                                                    indication = ripple(),
                                                    onClick = {
                                                        if (isSelectionMode) {
                                                            onToggleSelection()
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (!isSelectionMode) {
                                                            onLongPress()
                                                        }
                                                    },
                                                )
                                                .emotionalEntrance(
                                                    message.emotionalTone,
                                                    isAnimated && messageEffectsEnabled,
                                                ).wrapContentSize()
                                                .background(
                                                    bubbleStyle.backgroundColor,
                                                    bubbleShape,
                                                )
                                        }

                                        SenderType.CHARACTER -> {
                                            if (isUser.not()) {
                                                Modifier
                                                    .combinedClickable(
                                                        interactionSource = interactionSource,
                                                        indication = ripple(),
                                                        onClick = {
                                                            if (isSelectionMode) {
                                                                onToggleSelection()
                                                            }
                                                        },
                                                        onLongClick = {
                                                            if (!isSelectionMode) {
                                                                onLongPress()
                                                            }
                                                        },
                                                    )
                                                    .emotionalEntrance(
                                                        message.emotionalTone,
                                                        isAnimated && messageEffectsEnabled,
                                                    ).wrapContentSize()
                                                    .background(
                                                        bubbleStyle.backgroundColor,
                                                        bubbleShape,
                                                    )
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceContainer.copy(
                                                            alpha = .5f,
                                                        ),
                                                        bubbleShape,
                                                    )
                                            } else {
                                                Modifier
                                                    .combinedClickable(
                                                        interactionSource = interactionSource,
                                                        indication = ripple(),
                                                        onClick = {
                                                            if (isSelectionMode) {
                                                                onToggleSelection()
                                                            }
                                                        },
                                                        onLongClick = {
                                                            if (!isSelectionMode) {
                                                                onLongPress()
                                                            }
                                                        },
                                                    )
                                                    .emotionalEntrance(
                                                        message.emotionalTone,
                                                        isAnimated && messageEffectsEnabled,
                                                    )
                                                    .wrapContentSize()
                                                    .background(
                                                        bubbleStyle.backgroundColor,
                                                        bubbleShape,
                                                    )
                                            }
                                        }

                                        SenderType.THOUGHT -> {
                                            Modifier
                                                .combinedClickable(
                                                    interactionSource = interactionSource,
                                                    indication = ripple(),
                                                    onClick = {
                                                        if (isSelectionMode) {
                                                            onToggleSelection()
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (!isSelectionMode) {
                                                            onLongPress()
                                                        }
                                                    },
                                                )
                                                .emotionalEntrance(
                                                    message.emotionalTone,
                                                    isAnimated && messageEffectsEnabled,
                                                )
                                                .wrapContentSize()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceContainer,
                                                    bubbleShape,
                                                )
                                                .dashedBorder(
                                                    strokeWidth = 1.dp,
                                                    color =
                                                        MaterialTheme.colorScheme.onBackground.copy(
                                                            alpha = 0.5f,
                                                        ),
                                                    shape = bubbleShape,
                                                    dashLength = 10.dp,
                                                    gapLength = 5.dp,
                                                )
                                        }

                                        SenderType.ACTION -> {
                                            Modifier
                                                .combinedClickable(
                                                    interactionSource = interactionSource,
                                                    indication = ripple(),
                                                    onClick = {
                                                        if (isSelectionMode) {
                                                            onToggleSelection()
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (!isSelectionMode) {
                                                            onLongPress()
                                                        }
                                                    },
                                                )
                                                .emotionalEntrance(
                                                    message.emotionalTone,
                                                    isAnimated && messageEffectsEnabled,
                                                )
                                                .wrapContentSize()
                                                .background(Color.Black, bubbleShape)
                                        }

                                        else -> {
                                            Modifier.combinedClickable(
                                                interactionSource = interactionSource,
                                                indication = ripple(),
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        onToggleSelection()
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!isSelectionMode) {
                                                        onLongPress()
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                TooltipBox(
                                    positionProvider = tooltipPositionProvider,
                                    state = reactionToolTipState,
                                    onDismissRequest = {
                                        tooltipData = null
                                    },
                                    tooltip = {
                                        tooltipData?.let {
                                            AnnotationTooltip(
                                                data = it,
                                                genre = genre,
                                                shape = narratorShape,
                                            )
                                        }
                                    },
                                    modifier =
                                        bubbleModifier
                                            .scale(scaleAnimation)
                                            .border(
                                                2.dp,
                                                borderColorAnimation,
                                                bubbleShape,
                                            )
                                            .padding(paddingAnimation)
                                            .clip(bubbleShape)
                                            .padding(vertical = 4.dp)
                                            .animateContentSize(),
                                ) {
                                    Box {
                                        var starAlpha by remember { mutableFloatStateOf(1f) }
                                        val alphaAnimation by animateFloatAsState(
                                            targetValue = starAlpha,
                                            animationSpec =
                                                tween(
                                                    1000,
                                                    easing = FastOutSlowInEasing,
                                                ),
                                            label = "starAlpha",
                                        )
                                        val blurAnimation by animateFloatAsState(
                                            targetValue = if (starAlpha == 1f) 0f else 1f,
                                            label = "blurAnimation",
                                        )
                                        val textAlpha =
                                            if (sender == SenderType.THOUGHT) blurAnimation else 1f
                                        val textColor =
                                            when (sender) {
                                                SenderType.ACTION -> MaterialColor.Amber400
                                                SenderType.THOUGHT -> MaterialTheme.colorScheme.onBackground
                                                else -> bubbleStyle.textColor
                                            }
                                        val textAlign =
                                            if (sender == SenderType.ACTION ||
                                                sender == SenderType.THOUGHT
                                            ) {
                                                TextAlign.Center
                                            } else {
                                                TextAlign.Start
                                            }
                                        val fontStyle =
                                            if (sender == SenderType.ACTION ||
                                                sender == SenderType.THOUGHT
                                            ) {
                                                FontStyle.Italic
                                            } else {
                                                FontStyle.Normal
                                            }
                                        val text =
                                            if (sender == SenderType.ACTION) "(${message.text})" else message.text

                                        TypewriterText(
                                            text = text,
                                            isAnimated = isAnimated,
                                            genre = genre,
                                            mainCharacter = mainCharacter?.data,
                                            characters = characters.map { it.data },
                                            wiki = wiki,
                                            duration = duration,
                                            easing = EaseIn,
                                            onAnnotationClick = { data ->
                                                tooltipData = data
                                            },
                                            modifier =
                                                Modifier
                                                    .padding(16.dp)
                                                    .alpha(textAlpha)
                                                    .reactiveShimmer(
                                                        isLoading || message.status == MessageStatus.LOADING,
                                                        genre.shimmerColors(),
                                                    ),
                                            style =
                                                MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Normal,
                                                    fontFamily = genre.bodyFont(),
                                                    fontStyle = fontStyle,
                                                    color = textColor,
                                                    textAlign = textAlign,
                                                ),
                                        )

                                        if (sender == SenderType.THOUGHT) {
                                            StarryTextPlaceholder(
                                                modifier =
                                                    Modifier
                                                        .matchParentSize()
                                                        .alpha(alphaAnimation)
                                                        .clip(bubbleShape)
                                                        .clickable {
                                                            starAlpha = 0f
                                                        }
                                                        .background(
                                                            MaterialTheme.colorScheme.surfaceContainer.copy(
                                                                alpha = .4f,
                                                            ),
                                                            bubbleShape,
                                                        ),
                                                starColor = genre.color,
                                            )
                                        }
                                    }
                                }
                            }

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                AnimatedContent(message.status) {
                                    if (it == MessageStatus.ERROR) {
                                        Button(
                                            onClick = {
                                                onRetry(messageContent)
                                            },
                                            colors =
                                                ButtonDefaults.textButtonColors().copy(
                                                    contentColor = MaterialTheme.colorScheme.error,
                                                    containerColor = Color.Transparent,
                                                ),
                                            modifier =
                                                Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .fillMaxWidth(),
                                        ) {
                                            Icon(
                                                painterResource(R.drawable.baseline_refresh_24),
                                                null,
                                                Modifier.size(12.dp),
                                            )

                                            Text(
                                                stringResource(R.string.try_again),
                                                style =
                                                    MaterialTheme.typography.labelMedium.copy(
                                                        fontFamily = genre.bodyFont(),
                                                        fontWeight = FontWeight.Normal,
                                                    ),
                                                modifier = Modifier.padding(start = 4.dp),
                                            )
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier =
                                                Modifier
                                                    .padding(horizontal = 20.dp)
                                                    .offset(y = (-10).dp),
                                        ) {
                                            Text(
                                                message.timestamp.formatHours(),
                                                style =
                                                    MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                        fontFamily = genre.bodyFont(),
                                                        fontWeight = FontWeight.Light,
                                                        textAlign = TextAlign.Start,
                                                    ),
                                                modifier =
                                                    Modifier
                                                        .padding(horizontal = 4.dp)
                                                        .alpha(0.5f)
                                                        .weight(1f),
                                            )

                                            AnimatedVisibility(
                                                visible = messageContent.reactions.isNotEmpty(),
                                            ) {
                                                ReactionsView(
                                                    reactions = messageContent.reactions,
                                                    genre = genre,
                                                ) {
                                                    onReactionsClick(messageContent)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isSelectionMode,
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut(),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            val color by animateColorAsState(
                                if (isSelected) {
                                    genre.color
                                } else {
                                    MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = .3f,
                                    )
                                },
                            )
                            Icon(
                                painterResource(R.drawable.ic_spark),
                                contentDescription = "Selected",
                                tint = color,
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }

        SenderType.NARRATOR -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier =
                        modifier
                            .emotionalEntrance(
                                message.emotionalTone,
                                isAnimated && messageEffectsEnabled,
                            )
                            .padding(16.dp)
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = narratorShape,
                                spotColor = genre.color,
                            )
                            .border(1.dp, genre.color.gradientFade(), narratorShape)
                            .background(
                                MaterialTheme.colorScheme.background,
                                shape = narratorShape,
                            )
                            .padding(16.dp),
                ) {
                    TypewriterText(
                        text = message.text,
                        isAnimated = false,
                        duration = duration,
                        genre = genre,
                        mainCharacter = mainCharacter?.data,
                        characters = content.getCharacters(),
                        wiki = wiki,
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .reactiveShimmer(isLoading, genre.shimmerColors())
                                .fillMaxWidth(),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                fontFamily = genre.bodyFont(),
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                        onAnnotationClick = { data ->
                            tooltipData = data
                        },
                    )
                }

                AnimatedVisibility(
                    message.status == MessageStatus.ERROR,
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally),
                ) {
                    IconButton(
                        onClick = {
                            onRetry(messageContent)
                        },
                        modifier =
                            Modifier
                                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .size(24.dp),
                        colors =
                            IconButtonDefaults
                                .iconButtonColors()
                                .copy(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                    ) {
                        Icon(
                            painterResource(R.drawable.baseline_refresh_24),
                            "Tentar novamente",
                            modifier =
                                Modifier
                                    .padding(4.dp)
                                    .fillMaxSize(),
                        )
                    }
                }
                AnimatedVisibility(
                    visible = messageContent.reactions.isNotEmpty(),
                    modifier =
                        Modifier.padding(vertical = 8.dp),
                ) {
                    ReactionsView(
                        reactions = messageContent.reactions,
                        genre = genre,
                    ) {
                        onReactionsClick(messageContent)
                    }
                }
            }
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
            val character =
                Character(
                    name = "John",
                    details = Details(),
                    profile = CharacterProfile(),
                )
            Genre.entries.forEach { genre ->
                item {
                    ChatBubble(
                        canAnimate = false,
                        messageContent =
                            MessageContent(
                                Message(
                                    id = 0,
                                    text = "This is a message from ${genre.name}",
                                    senderType = SenderType.CHARACTER,
                                    timestamp = System.currentTimeMillis(),
                                    sagaId = 0,
                                    timelineId = 0,
                                ),
                                character = character,
                                reactions = emptyList(),
                            ),
                        content =
                            SagaContent(
                                data =
                                    Saga(
                                        title = "Test",
                                        description = "Test",
                                        genre = genre,
                                    ),
                                // mainCharacter = CharacterContent(data = character),
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
                animationDuration = 2.seconds,
                horizontalArrangement = Arrangement.End,
                false,
            )

        fun characterBubble(
            genre: Genre,
            canAnimate: Boolean,
        ) = BubbleStyle(
            backgroundColor = genre.color.copy(alpha = .4f),
            textColor = genre.iconColor,
            tailAlignment = BubbleTailAlignment.BottomLeft,
            animationDuration = 3.seconds,
            horizontalArrangement = Arrangement.Start,
            canAnimate,
        )
    }
}
