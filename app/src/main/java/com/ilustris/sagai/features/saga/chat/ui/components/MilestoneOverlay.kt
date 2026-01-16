package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.playMilestoneSound
import com.ilustris.sagai.core.utils.vibrate
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterYearbookItem
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findChapter
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.milestone.presentation.MilestoneViewModel
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.newsaga.data.model.vibrationPattern
import com.ilustris.sagai.features.playthrough.CounterText
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MilestoneOverlay(
    milestone: SagaMilestone,
    saga: SagaContent,
    isLoading: Boolean = false,
    onDismiss: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val viewModel: MilestoneViewModel = hiltViewModel()

    val genre = saga.data.genre
    val context = LocalContext.current

    // Collect AI-generated congrats message
    val congratsMessage by viewModel.congratsMessage.collectAsState()
    val canPlayAnimation = milestone.isIntrusive

    var showIcon by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(canPlayAnimation.not()) }
    var showSubtitle by remember { mutableStateOf(canPlayAnimation.not()) }
    var showButton by remember { mutableStateOf(canPlayAnimation.not()) }
    var showOverlay by remember { mutableStateOf(canPlayAnimation.not()) }
    var pulsePhase by remember { mutableStateOf(0) }

    val iconSize by animateDpAsState(
        targetValue =
            when (pulsePhase) {
                0, 1 -> 100.dp

                // Large during pulse and message load
                else -> 50.dp // Reduced size after message appears
            },
        animationSpec =
            tween(300, easing = FastOutSlowInEasing),
        label = "icon_size",
    )

    LaunchedEffect(milestone) {
        showOverlay = true
        if (canPlayAnimation.not()) {
            showIcon = true
            pulsePhase = 2
            showTitle = true
            showSubtitle = true
            showButton = true
            return@LaunchedEffect
        }

        delay(300)
        showIcon = true

        // Start pulsing animation while waiting for congrats message
        viewModel.generateCongratsMessage(milestone, saga)
        pulsePhase = 0 // Start pulsing

        // Wait 3 seconds for congrats message to load
        delay(3000)
        pulsePhase = 1 // Message loaded, prepare to show content

        // Trigger celebration effects and content reveal
        context.playMilestoneSound(R.raw.milestone_sound)
        context.vibrate(genre.vibrationPattern())
        showTitle = true
        delay(300)
        showSubtitle = true
        delay(700)
        showButton = true

        pulsePhase = 2 // Animation complete
    }

    // Clean up when milestone is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clear()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "milestone_animations")

    // Pulse animation for the icon while waiting for congrats message
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulse_scale",
    )

    val levitation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "levitation",
    )

    // Animated glow/shine effect for subtitle shadow
    val glowBlurRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 30f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "glow_pulse",
    )

    val milestoneSharedKey =
        when (milestone) {
            is SagaMilestone.ActFinished -> "act-${milestone.act.id}"
            is SagaMilestone.ChapterFinished -> "chapter-${milestone.chapter.id}"
            is SagaMilestone.CurrentObjective -> "timeline-objective"
            is SagaMilestone.NewCharacter -> "character-${milestone.character.id}"
            is SagaMilestone.NewEvent -> "timeline-${milestone.timeline.id}"
        }

    Box(Modifier.fillMaxSize()) {
        with(sharedTransitionScope) {
            AnimatedVisibility(
                visible = showOverlay,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                val boxModifier =
                    if (canPlayAnimation) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.wrapContentSize()
                    }

                Box(
                    modifier =
                        Modifier
                            .animateContentSize(),
                    contentAlignment = if (canPlayAnimation) Alignment.Center else Alignment.TopCenter,
                ) {
                    Column {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier =
                                boxModifier
                                    .background(MaterialTheme.colorScheme.background)
                                    .statusBarsPadding()
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .animateContentSize(tween(300, easing = EaseIn))
                                    .padding(16.dp),
                        ) {
                            AnimatedVisibility(
                                visible = showIcon,
                                enter = fadeIn(tween(400)),
                                exit = fadeOut(tween(700)),
                            ) {
                                val fillBrush = genre.gradient(true)

                                Image(
                                    painter = painterResource(R.drawable.ic_spark),
                                    contentDescription = null,
                                    colorFilter =
                                        ColorFilter.tint(
                                            if (canPlayAnimation) MaterialTheme.colorScheme.background else genre.color,
                                        ),
                                    modifier =
                                        Modifier
                                            .gradientFill(fillBrush)
                                            .size(iconSize)
                                            .graphicsLayer {
                                                translationY =
                                                    if (pulsePhase >= 2) levitation else 0f
                                                scaleX = if (pulsePhase == 0) pulseScale else 1f
                                                scaleY = if (pulsePhase == 0) pulseScale else 1f
                                            }.sharedElement(
                                                rememberSharedContentState(
                                                    key = "saga_${saga.data.id}_spark",
                                                ),
                                                animatedVisibilityScope = this,
                                            ),
                                )
                            }

                            AnimatedVisibility(
                                visible = showTitle,
                                enter = slideInVertically(animationSpec = tween(500)) { it / 2 } + fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Text(
                                    text = stringResource(milestone.title).uppercase(),
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = genre.bodyFont(),
                                            letterSpacing = 3.sp,
                                            color =
                                                MaterialTheme.colorScheme.onBackground.copy(
                                                    alpha = .5f,
                                                ),
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    modifier =
                                        Modifier.sharedBounds(
                                            rememberSharedContentState(milestoneSharedKey),
                                            this,
                                        ),
                                )
                            }

                            AnimatedVisibility(
                                visible = showSubtitle,
                                enter =
                                    scaleIn(
                                        initialScale = 0.8f,
                                        animationSpec = tween(700, easing = EaseInOutQuad),
                                    ) + fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    val textStyle =
                                        if (milestone.isIntrusive) {
                                            MaterialTheme.typography.headlineMedium
                                        } else {
                                            MaterialTheme.typography.labelLarge
                                        }
                                    if (milestone is SagaMilestone.NewCharacter) {
                                        val character = saga.findCharacter(milestone.character.id)
                                        character?.let {
                                            if (it.data.image.isNotBlank()) {
                                                CharacterAvatar(
                                                    milestone.character,
                                                    genre = genre,
                                                    modifier = Modifier.size(120.dp),
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = milestone.subtitle,
                                        style =
                                            textStyle.copy(
                                                fontFamily = if (milestone.isIntrusive) genre.headerFont() else genre.bodyFont(),
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                shadow =
                                                    Shadow(
                                                        color = genre.color,
                                                        offset = Offset(0f, 0f),
                                                        blurRadius = glowBlurRadius,
                                                    ),
                                            ),
                                        modifier =
                                            Modifier
                                                .reactiveShimmer(
                                                    milestone.isIntrusive,
                                                    genre.shimmerColors(),
                                                    repeatMode = RepeatMode.Restart,
                                                    targetValue = 1000f,
                                                ).padding(8.dp),
                                    )

                                    if (milestone is SagaMilestone.NewEvent) {
                                        val brush =
                                            Brush.horizontalGradient(
                                                genre.color.darkerPalette(
                                                    factor = .25f,
                                                ),
                                            )
                                        val event = saga.findTimeline(milestone.timeline.id)
                                        event?.let {
                                            val stats = event.statsSummary()

                                            LazyRow(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier =
                                                    Modifier
                                                        .animateContentSize()
                                                        .fillMaxWidth(),
                                            ) {
                                                items(stats) {
                                                    MilestoneBadge(
                                                        it.second,
                                                        stringResource(it.first),
                                                        brush,
                                                        glowBlurRadius,
                                                        genre,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (milestone is SagaMilestone.ChapterFinished) {
                                        val chapter = saga.findChapter(milestone.chapter.id)
                                        chapter?.let {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                val characters =
                                                    it.fetchCharacters(saga).filterNotNull()
                                                Text("Personagens mais importantes")

                                                LazyRow(
                                                    horizontalArrangement =
                                                        Arrangement.spacedBy(
                                                            8.dp,
                                                        ),
                                                ) {
                                                    items(characters) { character ->
                                                        CharacterYearbookItem(
                                                            character.data,
                                                            genre,
                                                            imageModifier =
                                                                Modifier
                                                                    .size(50.dp)
                                                                    .reactiveShimmer(true),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    AnimatedContent(
                                        targetState = congratsMessage,
                                        label = "congrats_message",
                                    ) { message ->
                                        message?.let {
                                            Text(
                                                text =
                                                message,
                                                style =
                                                    MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = genre.bodyFont(),
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        textAlign = TextAlign.Center,
                                                        fontWeight = FontWeight.Medium,
                                                    ),
                                            )
                                        }
                                    }
                                }
                            }

                            // Continue button
                            AnimatedVisibility(
                                visible = showButton,
                                enter =
                                    slideInVertically { it / 2 } + fadeIn() +
                                        scaleIn(
                                            initialScale = 0.8f,
                                        ),
                                exit = fadeOut(),
                            ) {
                                val buttonColors =
                                    if (canPlayAnimation) {
                                        ButtonDefaults.buttonColors(
                                            containerColor = genre.color,
                                            contentColor = genre.iconColor,
                                            disabledContainerColor = genre.color.copy(alpha = 0.5f),
                                            disabledContentColor = genre.iconColor.copy(alpha = 0.5f),
                                        )
                                    } else {
                                        ButtonDefaults.textButtonColors(
                                            contentColor = genre.color,
                                            disabledContentColor = genre.color.copy(alpha = .5f),
                                        )
                                    }
                                Button(
                                    onClick = onDismiss,
                                    enabled = !isLoading,
                                    colors = buttonColors,
                                    shape =
                                        genre.bubble(
                                            BubbleTailAlignment.BottomLeft,
                                            0.dp,
                                            0.dp,
                                            true,
                                        ),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = stringResource(R.string.continue_button),
                                            style =
                                                MaterialTheme.typography.labelLarge.copy(
                                                    fontFamily = genre.bodyFont(),
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(fadeGradientTop()),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneBadge(
    count: Int,
    labelText: String,
    brush: Brush,
    glowBlurRadius: Float,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    val shape = genre.bubble(BubbleTailAlignment.BottomRight, 0.dp, 0.dp, true)
    ConstraintLayout(
        modifier =
            modifier
                .padding(8.dp)
                .clip(shape)
                .dropShadow(shape, {
                    color = genre.color
                    radius = glowBlurRadius
                })
                .background(brush, shape),
    ) {
        val (label, counter) = createRefs()
        Text(
            labelText,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = genre.bodyFont(),
                    color = genre.iconColor,
                    fontWeight = FontWeight.SemiBold,
                ),
            modifier =
                Modifier
                    .padding(horizontal = 8.dp, 4.dp)
                    .constrainAs(label) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
        )

        CounterText(
            count,
            textStyle =
                MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = genre.headerFont(),
                    brush = brush,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            modifier =
                Modifier
                    .padding(2.dp)
                    .constrainAs(counter) {
                        top.linkTo(label.bottom)
                        start.linkTo(label.start)
                        end.linkTo(label.end)
                        width = Dimension.fillToConstraints
                    }.background(
                        MaterialTheme.colorScheme.background,
                        shape,
                    ).padding(4.dp)
                    .reactiveShimmer(
                        true,
                        repeatMode = RepeatMode.Restart,
                    ),
        )
    }
}
