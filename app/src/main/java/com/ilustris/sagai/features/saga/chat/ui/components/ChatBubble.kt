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
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.TooltipState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
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
import kotlinx.coroutines.delay
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** Inner padding around a bubble's text — also what the block splitter measures against. */
private val BUBBLE_TEXT_PADDING = 16.dp

/** Floor for a block's share of the typing budget, so a one-word block isn't a blink. */
private val MIN_BLOCK_DURATION = 400.milliseconds

/**
 * Shared spec for every `animateContentSize()` around a message bubble (the message row, the
 * blocks column, and each block's own shape/size). Previously these used the zero-arg default
 * (an implicit `spring()`), which snapped rather than eased — this is the same tuned duration/
 * easing [com.ilustris.sagai.features.saga.detail.review.ui.templates.crime.CrimeBubbleFrame]
 * uses so a bubble's size keeps pace with its typewriter text instead of jumping ahead of it.
 */
private val BUBBLE_RESIZE_SPEC: FiniteAnimationSpec<IntSize> =
    tween(durationMillis = 350, easing = FastOutSlowInEasing)

/** One visual bubble of a message that got broken into several. */
@Immutable
private data class BubbleBlock(
    val text: String,
    val isAnimated: Boolean,
    val duration: Duration,
    /** True for the final block of the message — carries the bits that show up only once. */
    val isLast: Boolean,
)

/**
 * Wraps one bubble block in its genre decorations. Extracted so the multi-block loop doesn't have
 * to repeat the ConstraintLayout/Box branching inline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BubbleBlockContainer(
    genre: Genre,
    decorationBackground: (@Composable BoxScope.() -> Unit)?,
    decorationOverlay: (@Composable BoxScope.() -> Unit)?,
    constraintDecorationBackground: (@Composable ConstraintLayoutScope.(ConstrainedLayoutReference) -> Unit)?,
    constraintDecorationOverlay: (@Composable ConstraintLayoutScope.(ConstrainedLayoutReference) -> Unit)?,
    tooltipPositionProvider: PopupPositionProvider,
    tooltipState: TooltipState,
    onTooltipDismiss: () -> Unit,
    tooltip: @Composable TooltipScope.() -> Unit,
    contentModifier: Modifier,
    content: @Composable () -> Unit,
) {
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
                val contentRef = createRef()
                constraintDecorationBackground?.invoke(this, contentRef)
                Box(
                    Modifier.constrainAs(contentRef) {
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
                        state = tooltipState,
                        onDismissRequest = onTooltipDismiss,
                        tooltip = tooltip,
                        modifier = contentModifier,
                        content = content,
                    )
                }
                constraintDecorationOverlay?.invoke(this, contentRef)
            }
        } else {
            Box {
                decorationBackground?.invoke(this)
                TooltipBox(
                    positionProvider = tooltipPositionProvider,
                    state = tooltipState,
                    onDismissRequest = onTooltipDismiss,
                    tooltip = tooltip,
                    modifier = contentModifier,
                    content = content,
                )
                decorationOverlay?.invoke(this)
            }
        }
    }
}

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
    // Lets a caller pace several unread messages one at a time instead of all typing at once —
    // see ChatView.kt's activeRevealId. Default true so callers that don't coordinate pacing
    // (previews, etc.) keep today's "animate as soon as visible" behavior.
    revealTurn: Boolean = true,
    onRevealComplete: () -> Unit = {},
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
        if (genre == Genre.HEROES) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary
    val resolvedIconColor =
        if (genre == Genre.HEROES) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
    val isUser = messageContent.isUser(mainCharacter?.data)
    genre.cornerSize()
    val isAnimated = canAnimate && messageEffectsEnabled
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
        genre.chatBubbleConstraintDecorationOverlay(
            bubbleShape,
            isUser,
            bubbleStyle.backgroundColor,
        )
    val constraintDecorationBackground =
        genre.chatBubbleConstraintBackgroundDecoration(bubbleShape, isUser, bubbleStyle.backgroundColor)
    val characterColor = avatarCharacter?.hexColor?.hexToColor() ?: resolvedColor
    val nameTagContent =
        avatarCharacter
            ?.name
            ?.takeIf { it.isNotBlank() && !isUser }
            ?.let { genre.chatBubbleNameTag(it, characterColor, bubbleColor = bubbleStyle.backgroundColor) }
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
    // Keyed on the id, not on messageContent: the UI mapper rebuilds the whole message list on
    // every emission, so any follow-up write (reaction saved, character link resolved, status
    // change) produced a structurally different MessageContent and restarted this bump — that
    // replay was the stutter.
    LaunchedEffect(message.id) {
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
                                .animateContentSize(BUBBLE_RESIZE_SPEC),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .animateContentSize(BUBBLE_RESIZE_SPEC),
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
                                    CharacterAvatar(
                                        character,
                                        isLoading = isLoading,
                                        genre = genre,
                                        borderSize = 1.dp,
                                        innerPadding = 1.dp,
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
                                // Parameterized by shape because a long message renders as several
                                // stacked bubbles and only the last one carries the tail — the
                                // clip/background/border have to follow whichever shape that block
                                // actually uses.
                                val rippleIndication = ripple()
                                val clickableModifier: (Shape) -> Modifier = { blockShape ->
                                    Modifier
                                        .clip(blockShape)
                                        .combinedClickable(
                                            interactionSource = interactionSource,
                                            indication = rippleIndication,
                                            onClick = {
                                                if (isSelectionMode) {
                                                    onAction(MessageAction.ToggleSelection(message.id))
                                                }
                                            },
                                            onLongClick = {
                                                if (!isSelectionMode) {
                                                    onAction(MessageAction.LongPress(message.id))
                                                }
                                            },
                                        )
                                }
                                // Spoken bubbles (user and character) get the filled background and
                                // the emotional entrance; thoughts and actions stay unfilled unless
                                // the genre is Heroes, which paints every bubble.
                                val isSpokenBubble =
                                    sender == SenderType.USER || sender == SenderType.CHARACTER
                                val bubbleModifierFor: @Composable (Shape) -> Modifier = { blockShape ->
                                    when {
                                        message.status == MessageStatus.LOADING ->
                                            Modifier
                                                .alpha(.7f)
                                                .emotionalEntrance(
                                                    message.emotionalTone,
                                                    messageEffectsEnabled,
                                                ).wrapContentSize()
                                                .rotatingGradientBorder(
                                                    shape = blockShape,
                                                    colors = palette,
                                                    rotationDegrees = rotationValue,
                                                ).background(
                                                    MaterialTheme.colorScheme.surfaceContainer.copy(
                                                        alpha = .3f,
                                                    ),
                                                    blockShape,
                                                )

                                        isSpokenBubble ->
                                            clickableModifier(blockShape)
                                                .emotionalEntrance(
                                                    message.emotionalTone,
                                                    messageEffectsEnabled,
                                                ).wrapContentSize()
                                                .background(bubbleStyle.backgroundColor, blockShape)

                                        genre == Genre.HEROES ->
                                            clickableModifier(blockShape)
                                                .wrapContentSize()
                                                .background(bubbleStyle.backgroundColor, blockShape)

                                        else -> clickableModifier(blockShape)
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
                                    val bubbleContentModifierFor: @Composable (Shape) -> Modifier = { shape ->
                                        bubbleModifierFor(shape)
                                            .graphicsLayer {
                                                scaleX = finalScale
                                                scaleY = finalScale
                                            }.border(
                                                2.dp,
                                                borderColorAnimation,
                                                shape,
                                            ).padding(paddingAnimation)
                                            .clip(shape)
                                            .padding(vertical = 4.dp)
                                            .animateContentSize(BUBBLE_RESIZE_SPEC)
                                    }
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
                                    val bubbleTextContent: @Composable (BubbleBlock) -> Unit = { block ->
                                        Box {
                                            val textAlpha =
                                                if (sender == SenderType.THOUGHT) 0f else 1f
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
                                            val text = block.text

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
                                                        shouldAnimate = block.isAnimated,
                                                        onAnnotationClick = { data ->
                                                            tooltipData = data
                                                        },
                                                    )
                                                } else {
                                                    TypewriterText(
                                                        text = text,
                                                        isAnimated = block.isAnimated,
                                                        genre = genre,
                                                        mainCharacter = mainCharacter?.data,
                                                        characters = characters,
                                                        wiki = wikis,
                                                        duration = block.duration,
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

                                                if (BuildConfig.DEBUG && block.isLast) {
                                                    ReasoningView(message.reasoning, genre)
                                                }
                                            }
                                        }
                                    }
                                    val bodyTextStyle =
                                        MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                        )
                                    val fullText =
                                        if (sender == SenderType.ACTION) "(${message.text})" else message.text

                                    BoxWithConstraints {
                                        val density = LocalDensity.current
                                        // The text sits inside 16dp of padding on both sides, so
                                        // that's the width the splitter has to measure against.
                                        val textWidthPx =
                                            with(density) {
                                                (this@BoxWithConstraints.maxWidth - BUBBLE_TEXT_PADDING * 2)
                                                    .roundToPx()
                                                    .coerceAtLeast(1)
                                            }
                                        val blocks =
                                            rememberMessageBlocks(
                                                text = fullText,
                                                style = bodyTextStyle,
                                                maxWidthPx = textWidthPx,
                                                // An audio message is one playback control — there
                                                // is nothing to break up.
                                                enabled = !hasValidAudio,
                                            )

                                        // Waits for revealTurn too, not just isAnimated: several
                                        // unread messages composing at once (opening the chat with
                                        // a backlog) used to all start typing simultaneously with
                                        // no coordination — this is what actually stalls until the
                                        // caller (ChatView.kt) grants this specific message its
                                        // turn, so only one message types at a time.
                                        var revealedBlocks by remember(message.id, blocks.size, isAnimated, revealTurn) {
                                            mutableIntStateOf(
                                                when {
                                                    !isAnimated -> blocks.size
                                                    !revealTurn -> 0
                                                    else -> 1
                                                },
                                            )
                                        }

                                        // Blocks reveal one at a time, each taking a slice of the
                                        // total typing budget proportional to its length — so a
                                        // split message takes about as long to read out as it did
                                        // as a single bubble, it just arrives in pieces.
                                        val blockDurations =
                                            remember(blocks, duration) {
                                                val totalChars =
                                                    blocks.sumOf { it.length }.coerceAtLeast(1)
                                                blocks.map { block ->
                                                    (duration * (block.length.toDouble() / totalChars))
                                                        .coerceAtLeast(MIN_BLOCK_DURATION)
                                                }
                                            }

                                        LaunchedEffect(message.id, blocks.size, isAnimated, revealTurn) {
                                            if (isAnimated && revealTurn) {
                                                blocks.indices.forEach { index ->
                                                    delay(blockDurations[index])
                                                    if (index < blocks.lastIndex) {
                                                        revealedBlocks = index + 2
                                                    }
                                                }
                                                onRevealComplete()
                                            }
                                            // canAnimate is "not viewed yet". Marking it even when
                                            // effects are off matters: otherwise those messages stay
                                            // unviewed forever and would all animate at once the day
                                            // the reader turns effects back on. Gated on revealTurn
                                            // when actually animated — marking viewed before this
                                            // message ever got its turn would make canAnimate false
                                            // by the time its turn *does* come, skipping the
                                            // animation entirely.
                                            if (canAnimate && (!isAnimated || revealTurn)) {
                                                onAction(MessageAction.MarkViewed(message.id))
                                            }
                                        }

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                            horizontalAlignment =
                                                if (isUser) Alignment.End else Alignment.Start,
                                        ) {
                                            blocks.take(revealedBlocks).forEachIndexed { index, blockText ->
                                                key(index) {
                                                    // The tail always sits on the block that is
                                                    // currently last on screen, so the group never
                                                    // looks broken mid-reveal.
                                                    val isTailBlock = index == revealedBlocks - 1
                                                    val blockShape =
                                                        if (isTailBlock) {
                                                            bubbleShape
                                                        } else {
                                                            genre.bubble(
                                                                bubbleStyle.tailAlignment,
                                                                showTail = false,
                                                            )
                                                        }
                                                    val block =
                                                        BubbleBlock(
                                                            text = blockText,
                                                            isAnimated = isAnimated,
                                                            duration = blockDurations[index],
                                                            isLast = index == blocks.lastIndex,
                                                        )
                                                    BubbleBlockContainer(
                                                        genre = genre,
                                                        // Genre decorations only dress the tail
                                                        // block; repeating them on every block in a
                                                        // group reads as noise and multiplies the
                                                        // draw cost.
                                                        decorationBackground =
                                                            decorationBackground.takeIf { isTailBlock },
                                                        decorationOverlay =
                                                            decorationOverlay.takeIf { isTailBlock },
                                                        constraintDecorationBackground =
                                                            constraintDecorationBackground.takeIf { isTailBlock },
                                                        constraintDecorationOverlay =
                                                            constraintDecorationOverlay.takeIf { isTailBlock },
                                                        tooltipPositionProvider = tooltipPositionProvider,
                                                        tooltipState = reactionToolTipState,
                                                        onTooltipDismiss = { tooltipData = null },
                                                        tooltip = bubbleTooltipContent,
                                                        contentModifier = bubbleContentModifierFor(blockShape),
                                                        content = { bubbleTextContent(block) },
                                                    )
                                                }
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
         * Space Opera used to force a translucent fill here so the (now-removed) `dropShadow`
         * glow behind the shape would show through it. That's gone now (2026-07-30) — the
         * "holographic" identity comes entirely from the two overlapping stars in
         * `SpaceOperaOverlay` instead, so the fill goes back to fully opaque like every other
         * genre.
         */
        private fun Genre.resolveFill(
            default: Color,
            isUser: Boolean,
        ): Color =
            when (this) {
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
