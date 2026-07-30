package com.ilustris.sagai.features.saga.chat.ui.components
import MessageStatus
import ai.atick.material.MaterialColor
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
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
import androidx.compose.material3.TooltipScope
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.formatHours
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.isUser
import com.ilustris.sagai.features.saga.chat.presentation.MessageAction
import com.ilustris.sagai.features.saga.chat.ui.animations.emotionalEntrance
import com.ilustris.sagai.features.saga.chat.ui.components.audio.AudioMessagePlayer
import com.ilustris.sagai.features.saga.chat.ui.components.audio.AudioPlaybackState
import com.ilustris.sagai.features.saga.chat.ui.components.decoration.chatBubbleBackgroundDecoration
import com.ilustris.sagai.features.saga.chat.ui.components.decoration.chatBubbleConstraintBackgroundDecoration
import com.ilustris.sagai.features.saga.chat.ui.components.decoration.chatBubbleConstraintDecorationOverlay
import com.ilustris.sagai.features.saga.chat.ui.components.decoration.chatBubbleDecorationOverlay
import com.ilustris.sagai.features.saga.chat.ui.components.decoration.chatBubbleNameTag
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.rememberRotatingBorderAngle
import com.ilustris.sagai.ui.theme.rotatingGradientBorder
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.toEasing
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    messageContent: MessageContent,
    mainCharacter: CharacterContent?,
    characters: List<Character>,
    wikis: List<Wiki>,
    genre: Genre,
    flatEvents: List<Timeline>,
    canAnimate: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    audioPlaybackState: AudioPlaybackState? = null,
    onAction: (MessageAction) -> Unit = {},
    messageEffectsEnabled: Boolean = true,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val message = messageContent.message
    val avatarCharacter =
        messageContent.character?.let { embedded ->
            characters.find { it.id == embedded.id } ?: embedded
        }
    val sender = message.senderType
    val resolvedColor =
        if (genre == Genre.HEROES) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primary
    val resolvedIconColor =
        if (genre == Genre.HEROES) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
    val isUser = messageContent.isUser(mainCharacter?.data)
    genre.cornerSize()
    val isAnimated = canAnimate && messageEffectsEnabled.not()
    val bubbleStyle =
        remember(isUser, genre, resolvedColor, resolvedIconColor) {
            if (isUser) {
                BubbleStyle.userBubble(genre, resolvedColor, resolvedIconColor)
            } else {
                BubbleStyle.characterBubble(
                    genre,
                    isAnimated,
                    resolvedColor.darker(.4f),
                    resolvedIconColor,
                )
            }
        }
    val duration = bubbleStyle.animationDuration
    val bubbleShape = genre.bubble(bubbleStyle.tailAlignment)
    val avatarShape = genre.avatarShape()
    val decorationOverlay = genre.chatBubbleDecorationOverlay(bubbleShape, isUser)
    val decorationBackground = genre.chatBubbleBackgroundDecoration(bubbleShape, isUser)
    val constraintDecorationOverlay =
        genre.chatBubbleConstraintDecorationOverlay(bubbleShape, isUser)
    val constraintDecorationBackground =
        genre.chatBubbleConstraintBackgroundDecoration(bubbleShape, isUser)
    val nameTagContent =
        avatarCharacter
            ?.name
            ?.takeIf { it.isNotBlank() && !isUser }
            ?.let { genre.chatBubbleNameTag(it) }
    val narratorShape =
        genre.bubble(
            BubbleTailAlignment.BottomRight,
            isNarrator = true,
        )
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

    val bumpScale = remember { Animatable(1f) }
    LaunchedEffect(messageContent) {
        val easing = message.emotionalTone?.toEasing() ?: EaseIn
        bumpScale.animateTo(
            targetValue = 1.05f,
            animationSpec = tween(100, easing = easing),
        )
        bumpScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(100, easing = easing),
        )
    }

    val animationsActive = rememberLifecycleAnimationsActive()
    val rotationValue = rememberRotatingBorderAngle(isAnimating = animationsActive)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "scaleAnimation",
    )

    val finalScale = scaleAnimation * bumpScale.value

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

    with(sharedTransitionScope) {
        when (sender) {
            SenderType.USER,
            SenderType.CHARACTER,
            SenderType.THOUGHT,
            SenderType.ACTION,
            -> {
                val layoutDirection = if (isUser) LayoutDirection.Rtl else LayoutDirection.Ltr
                val hasValidAudio =
                    remember(message.audioPath) {
                        message.audioPath?.let { path ->
                            File(path).exists()
                        } ?: false
                    }
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
                            val avatarSize = if (avatarCharacter == null) 24.dp else 50.dp

                            Box(
                                Modifier
                                    .clip(avatarShape)
                                    .clickable {
                                        avatarCharacter?.let { character ->
                                            onAction(
                                                MessageAction.ClickCharacter(
                                                    characters.find { it.id == character.id }
                                                        ?: character,
                                                ),
                                            )
                                        }
                                    }.size(avatarSize),
                            ) {
                                avatarCharacter?.let { character ->
                                    AnimatedContent(
                                        targetState = character.image to character.id,
                                        transitionSpec = {
                                            fadeIn() + scaleIn() togetherWith scaleOut()
                                        },
                                        label = "ChatBubbleAvatar",
                                    ) {
                                        CharacterAvatar(
                                            character,
                                            isLoading = isLoading,
                                            genre = genre,
                                            borderSize = 1.dp,
                                            shape = avatarShape,
                                            pixelation = 0f,
                                            grainRadius = 0f,
                                            modifier =
                                                Modifier
                                                    .sharedElement(
                                                        rememberSharedContentState(key = "character_${character.id}_icon"),
                                                        animatedVisibilityScope,
                                                    ).padding(8.dp)
                                                    .fillMaxSize(),
                                        )
                                    }

                                    val relationWithMainCharacter =
                                        mainCharacter
                                            ?.findRelationship(character.id)
                                            ?.sortedByEvents(flatEvents)
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
                                                                        ?: resolvedColor,
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
                                                onAction(
                                                    MessageAction.RequestNewCharacter(
                                                        message.speakerName ?: "",
                                                        message,
                                                    ),
                                                )
                                            }.size(24.dp)
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
                                val palette = genre.colorPalette()
                                val bubbleModifier =
                                    if (message.status == MessageStatus.LOADING) {
                                        Modifier
                                            .alpha(.7f)
                                            .emotionalEntrance(
                                                message.emotionalTone,
                                                messageEffectsEnabled,
                                            )
                                            .wrapContentSize()
                                            .rotatingGradientBorder(
                                                shape = bubbleShape,
                                                colors = palette,
                                                rotationDegrees = rotationValue,
                                            )
                                            .background(
                                                MaterialTheme.colorScheme.surfaceContainer.copy(
                                                    alpha = .3f,
                                                ),
                                                bubbleShape,
                                            )
                                    } else {
                                        when (sender) {
                                            SenderType.USER -> {
                                                Modifier
                                                    .clip(bubbleShape)
                                                    .combinedClickable(
                                                        interactionSource = interactionSource,
                                                        indication = ripple(),
                                                        onClick = {
                                                            if (isSelectionMode) {
                                                                onAction(
                                                                    MessageAction.ToggleSelection(
                                                                        message.id,
                                                                    ),
                                                                )
                                                            }
                                                        },
                                                        onLongClick = {
                                                            if (!isSelectionMode) {
                                                                onAction(
                                                                    MessageAction.LongPress(
                                                                        message.id,
                                                                    ),
                                                                )
                                                            }
                                                        },
                                                    )
                                                    .emotionalEntrance(
                                                        message.emotionalTone,
                                                        messageEffectsEnabled,
                                                    )
                                                    .wrapContentSize()
                                                    .background(
                                                        bubbleStyle.backgroundColor,
                                                        bubbleShape,
                                                    )
                                            }

                                            SenderType.CHARACTER -> {
                                                if (isUser.not()) {
                                                    Modifier
                                                        .clip(bubbleShape)
                                                        .combinedClickable(
                                                            interactionSource = interactionSource,
                                                            indication = ripple(),
                                                            onClick = {
                                                                if (isSelectionMode) {
                                                                    onAction(
                                                                        MessageAction.ToggleSelection(
                                                                            message.id,
                                                                        ),
                                                                    )
                                                                }
                                                            },
                                                            onLongClick = {
                                                                if (!isSelectionMode) {
                                                                    onAction(
                                                                        MessageAction
                                                                            .LongPress(
                                                                                message.id,
                                                                            ),
                                                                    )
                                                                }
                                                            },
                                                        )
                                                        .emotionalEntrance(
                                                            message.emotionalTone,
                                                            messageEffectsEnabled,
                                                        )
                                                        .wrapContentSize()
                                                        .background(
                                                            bubbleStyle.backgroundColor,
                                                            bubbleShape,
                                                        )
                                                } else {
                                                    Modifier
                                                        .clip(bubbleShape)
                                                        .combinedClickable(
                                                            interactionSource = interactionSource,
                                                            indication = ripple(),
                                                            onClick = {
                                                                if (isSelectionMode) {
                                                                    onAction(
                                                                        MessageAction.ToggleSelection(
                                                                            message.id,
                                                                        ),
                                                                    )
                                                                }
                                                            },
                                                            onLongClick = {
                                                                if (!isSelectionMode) {
                                                                    onAction(
                                                                        MessageAction
                                                                            .LongPress(
                                                                                message.id,
                                                                            ),
                                                                    )
                                                                }
                                                            },
                                                        )
                                                        .emotionalEntrance(
                                                            message.emotionalTone,
                                                            messageEffectsEnabled,
                                                        )
                                                        .wrapContentSize()
                                                        .background(
                                                            bubbleStyle.backgroundColor,
                                                            bubbleShape,
                                                        )
                                                }
                                            }

                                            else -> {
                                                val baseModifier =
                                                    Modifier
                                                        .clip(bubbleShape)
                                                        .combinedClickable(
                                                            interactionSource = interactionSource,
                                                            indication = ripple(),
                                                            onClick = {
                                                                if (isSelectionMode) {
                                                                    onAction(
                                                                        MessageAction.ToggleSelection(
                                                                            message.id,
                                                                        ),
                                                                    )
                                                                }
                                                            },
                                                            onLongClick = {
                                                                if (!isSelectionMode) {
                                                                    onAction(
                                                                        MessageAction.LongPress(
                                                                            message.id,
                                                                        ),
                                                                    )
                                                                }
                                                            },
                                                        )
                                                if (genre == Genre.HEROES) {
                                                    baseModifier
                                                        .wrapContentSize()
                                                        .background(
                                                            bubbleStyle.backgroundColor,
                                                            bubbleShape,
                                                        )
                                                } else {
                                                    baseModifier
                                                }
                                            }
                                        }
                                    }

                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    AudioGenButton(
                                        message,
                                        genre,
                                        onAction,
                                        messageContent,
                                        hasValidAudio,
                                    )
                                }

                                if (nameTagContent != null) {
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        nameTagContent()
                                    }
                                }

                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    val bubbleContentModifier =
                                        bubbleModifier
                                            .graphicsLayer {
                                                scaleX = finalScale
                                                scaleY = finalScale
                                            }.border(
                                                2.dp,
                                                borderColorAnimation,
                                                bubbleShape,
                                            ).padding(paddingAnimation)
                                            .clip(bubbleShape)
                                            .padding(vertical = 4.dp)
                                            .animateContentSize()
                                    val bubbleTooltipContent: @Composable TooltipScope.() -> Unit =
                                        {
                                            tooltipData?.let {
                                                AnnotationTooltip(
                                                    data = it,
                                                    genre = genre,
                                                    shape = narratorShape,
                                                )
                                            }
                                        }
                                    val bubbleTextContent: @Composable () -> Unit = {
                                        Box {
                                            var textAlpha by remember {
                                                mutableStateOf(
                                                    if (sender == SenderType.THOUGHT) 0f else 1f,
                                                )
                                            }
                                            val textColor =
                                                when {
                                                    sender == SenderType.ACTION -> MaterialColor.Amber400
                                                    sender == SenderType.THOUGHT -> MaterialTheme.colorScheme.onBackground
                                                    else -> bubbleStyle.textColor
                                                }
                                            val textAlign = TextAlign.Start
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

                                            val hasExpressiveTags =
                                                remember(text) {
                                                    text.contains("<action>") ||
                                                        text.contains("<think>") ||
                                                        text.contains("<narrator>")
                                                }

                                            Column(
                                                Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                if (hasValidAudio) {
                                                    AudioMessagePlayer(
                                                        transcription = text,
                                                        audioPlaybackState = audioPlaybackState?.takeIf { it.messageId == message.id },
                                                        genre = genre,
                                                        contentColor = textColor,
                                                        onPlayPauseClick = {
                                                            onAction(
                                                                MessageAction.PlayAudio(
                                                                    messageContent,
                                                                ),
                                                            )
                                                        },
                                                    )
                                                } else if (hasExpressiveTags) {
                                                    ExpressiveText(
                                                        text = text,
                                                        genre = genre,
                                                        style =
                                                            MaterialTheme.typography.labelLarge.copy(
                                                                fontWeight = FontWeight.Normal,
                                                                color = textColor,
                                                            ),
                                                        modifier = Modifier.fillMaxWidth(),
                                                        mainCharacter = mainCharacter?.data,
                                                        characters = characters,
                                                        wiki = wikis,
                                                        shouldAnimate = canAnimate && messageEffectsEnabled,
                                                        onAnnotationClick = { data ->
                                                            tooltipData = data
                                                        },
                                                    )
                                                } else {
                                                    TypewriterText(
                                                        text = text,
                                                        isAnimated = isAnimated,
                                                        genre = genre,
                                                        mainCharacter = mainCharacter?.data,
                                                        characters = characters,
                                                        wiki = wikis,
                                                        duration = duration,
                                                        easing = EaseIn,
                                                        onAnnotationClick = { data ->
                                                            tooltipData = data
                                                        },
                                                        modifier =
                                                            Modifier
                                                                .alpha(textAlpha),
                                                        style =
                                                            MaterialTheme.typography.bodySmall.copy(
                                                                fontWeight = FontWeight.Normal,
                                                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                                fontStyle = fontStyle,
                                                                color = textColor,
                                                                textAlign = textAlign,
                                                            ),
                                                    )
                                                }

                                                if (BuildConfig.DEBUG) {
                                                    ReasoningView(message.reasoning, genre)
                                                }
                                            }
                                        }
                                    }
                                    Box(
                                        modifier =
                                            if (genre == Genre.HEROES) {
                                                Modifier.padding(
                                                    top = 4.dp,
                                                    bottom = 12.dp,
                                                    start = 4.dp,
                                                    end = 12.dp,
                                                )
                                            } else {
                                                Modifier
                                            },
                                    ) {
                                        if (constraintDecorationOverlay != null || constraintDecorationBackground != null) {
                                            // ConstraintLayout-backed decoration slot: unlike the plain Box below, this
                                            // actually expands to include a decoration that hangs past the bubble's
                                            // edge, instead of just drawing over/under whatever space was already there.
                                            // TooltipBox is wrapped in a plain Box before being constrained — TooltipBox
                                            // itself has its own internal tooltip/anchor layout logic that didn't behave
                                            // as a direct ConstraintLayout child (see project notes, 2026-07-29 crash).
                                            ConstraintLayout {
                                                val content = createRef()
                                                constraintDecorationBackground?.invoke(
                                                    this,
                                                    content,
                                                )
                                                Box(
                                                    Modifier.constrainAs(content) {
                                                        top.linkTo(parent.top)
                                                        start.linkTo(parent.start)
                                                        end.linkTo(parent.end)
                                                        bottom.linkTo(parent.bottom)
                                                        width = Dimension.wrapContent
                                                        height = Dimension.wrapContent
                                                    },
                                                ) {
                                                    TooltipBox(
                                                        positionProvider = tooltipPositionProvider,
                                                        state = reactionToolTipState,
                                                        onDismissRequest = { tooltipData = null },
                                                        tooltip = bubbleTooltipContent,
                                                        modifier = bubbleContentModifier,
                                                        content = bubbleTextContent,
                                                    )
                                                }
                                                constraintDecorationOverlay?.invoke(this, content)
                                            }
                                        } else {
                                            Box {
                                                decorationBackground?.invoke(this)
                                                TooltipBox(
                                                    positionProvider = tooltipPositionProvider,
                                                    state = reactionToolTipState,
                                                    onDismissRequest = { tooltipData = null },
                                                    tooltip = bubbleTooltipContent,
                                                    modifier = bubbleContentModifier,
                                                    content = bubbleTextContent,
                                                )
                                                decorationOverlay?.invoke(this)
                                            }
                                        }
                                    }
                                }

                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    AnimatedContent(message.status) {
                                        if (it == MessageStatus.ERROR) {
                                            Button(
                                                onClick = {
                                                    onAction(
                                                        MessageAction.RetryMessage(
                                                            messageContent,
                                                        ),
                                                    )
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
                                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
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
                                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
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
                                                        onAction(
                                                            MessageAction.ClickReactions(
                                                                messageContent,
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
                                        resolvedColor
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
                    val hasValidAudio =
                        remember(message.audioPath) {
                            message.audioPath?.let { path ->
                                File(path).exists()
                            } ?: false
                        }

                    AudioGenButton(message, genre, onAction, messageContent, hasValidAudio)

                    val narratorModifier =
                        modifier
                            .emotionalEntrance(
                                message.emotionalTone,
                                messageEffectsEnabled,
                            ).padding(16.dp)
                            .fillMaxWidth()

                    val narratorBubbleContent: @Composable () -> Unit = {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (hasValidAudio) {
                                // Show Audio Player
                                AudioMessagePlayer(
                                    transcription = message.text,
                                    audioPlaybackState = audioPlaybackState?.takeIf { it.messageId == message.id },
                                    genre = genre,
                                    contentColor = if (genre == Genre.HEROES) Color.Black else resolvedIconColor,
                                    onPlayPauseClick = {
                                        onAction(MessageAction.PlayAudio(messageContent))
                                    },
                                )
                            } else {
                                // Show Text
                                ExpressiveText(
                                    text = message.text,
                                    genre = genre,
                                    style =
                                        MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                            color = if (genre == Genre.HEROES) MaterialTheme.colorScheme.onSurface else resolvedIconColor,
                                        ),
                                    modifier =
                                        Modifier
                                            .padding(4.dp)
                                            .fillMaxWidth(),
                                    mainCharacter = mainCharacter?.data,
                                    characters = characters,
                                    wiki = wikis,
                                    shouldAnimate = canAnimate && messageEffectsEnabled,
                                    onAnnotationClick = { data ->
                                        tooltipData = data
                                    },
                                )
                            }
                        }
                    }

                    if (genre == Genre.HEROES) {
                        Box(narratorModifier) {
                            decorationBackground?.invoke(this)
                            Box(
                                Modifier
                                    .clip(bubbleShape)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceContainerHighest,
                                        bubbleShape,
                                    ),
                            ) {
                                narratorBubbleContent()
                            }
                            decorationOverlay?.invoke(this)
                        }
                    } else {
                        Box(narratorModifier) {
                            narratorBubbleContent()
                        }
                    }

                    AnimatedVisibility(
                        message.status == MessageStatus.ERROR,
                        modifier =
                            Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        IconButton(
                            onClick = {
                                onAction(MessageAction.RetryMessage(messageContent))
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
                                contentDescription = stringResource(R.string.try_again),
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
                            onAction(MessageAction.ClickReactions(messageContent))
                        }
                    }

                    if (BuildConfig.DEBUG) {
                        ReasoningView(message.reasoning, genre)
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioGenButton(
    message: Message,
    genre: Genre,
    onAction: (MessageAction) -> Unit,
    messageContent: MessageContent,
    hasValidAudio: Boolean,
) {
    if (hasValidAudio.not() && message.audible) {
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    4.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .alpha(.4f)
                    .clip(sagaShape())
                    .gradientFill(genre.gradient())
                    .clickable {
                        onAction(
                            MessageAction.RegenerateAudio(
                                messageContent,
                            ),
                        )
                    },
        ) {
            Image(
                painterResource(R.drawable.ic_mic),
                null,
                Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            )
            Text(
                text = stringResource(R.string.regenerate_audio),
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.secondary,
                    ),
            )
        }
    }
}

@Composable
private fun ReasoningView(
    reasoning: String?,
    genre: Genre,
) {
    reasoning?.let {
        var isExpanded by remember { mutableStateOf(false) }
        Row(
            modifier =
                Modifier
                    .clickable { isExpanded = !isExpanded }
                    .animateContentSize(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_spark),
                contentDescription = stringResource(R.string.audit_logs_reasoning_cd),
                modifier =
                    Modifier
                        .size(12.dp)
                        .alpha(0.5f),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Text(
                if (isExpanded) it else stringResource(R.string.see_reasoning),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = .5f),
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontWeight = FontWeight.Light,
                    ),
            )
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
        /**
         * Space Opera's "holographic CRT" bubble overlay (see `SpaceOperaBubbleOverlay` in
         * `ChatBubbleDecoration.kt`) relies on the fill being translucent so the `dropShadow` glow
         * behind the shape shows *through* it, reading as projected glass instead of solid
         * plastic. Every other genre keeps a fully opaque fill.
         */
        private fun Genre.resolveFill(
            default: Color,
            isUser: Boolean,
        ): Color =
            when (this) {
                Genre.SPACE_OPERA -> default.copy(alpha = .55f)
                Genre.HEROES -> if (!isUser) default.copy(alpha = 0.75f) else default
                else -> default
            }

        fun userBubble(
            genre: Genre,
            backgroundColor: Color,
            textColor: Color,
        ) = BubbleStyle(
            backgroundColor = genre.resolveFill(backgroundColor.darker(.15f), true),
            textColor = textColor,
            tailAlignment = BubbleTailAlignment.BottomRight,
            animationDuration = 2.seconds,
            horizontalArrangement = Arrangement.End,
            false,
        )

        fun characterBubble(
            genre: Genre,
            canAnimate: Boolean,
            backgroundColor: Color,
            textColor: Color,
        ) = BubbleStyle(
            backgroundColor = genre.resolveFill(backgroundColor, false),
            textColor = textColor,
            tailAlignment = BubbleTailAlignment.BottomLeft,
            animationDuration = 3.seconds,
            horizontalArrangement = Arrangement.Start,
            canAnimate,
        )
    }
}
