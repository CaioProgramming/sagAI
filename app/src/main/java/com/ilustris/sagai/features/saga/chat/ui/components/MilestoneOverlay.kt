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
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.ilustris.sagai.features.newsaga.data.model.vibrationPattern
import com.ilustris.sagai.features.playthrough.CounterText
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadeColors
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    var showIcon by remember { mutableStateOf(canPlayAnimation.not()) }
    var showTitle by remember { mutableStateOf(canPlayAnimation.not()) }
    var showSubtitle by remember { mutableStateOf(canPlayAnimation.not()) }
    var showButton by remember { mutableStateOf(canPlayAnimation.not()) }
    var showOverlay by remember { mutableStateOf(canPlayAnimation.not()) }
    var fillProgress by remember { mutableFloatStateOf(if (canPlayAnimation) 0f else 1f) }
    val iconSize by animateDpAsState(
        targetValue = if (fillProgress >= 1f) 50.dp else 100.dp,
        animationSpec =
            tween(300, easing = FastOutSlowInEasing),
        label = "icon_size",
    )

    // Vibrate on progress milestones
    LaunchedEffect(fillProgress) {
        if (fillProgress > 0f && fillProgress < 1f && canPlayAnimation) {
            val vibrationMilestones = listOf(0.25f, 0.5f, 0.75f)
            vibrationMilestones.forEach { milestone ->
                if (fillProgress >= milestone && fillProgress < milestone + 0.05f) {
                    context.vibrate(longArrayOf(0, 50))
                }
            }
        } else if (fillProgress >= 1f) {
            // Final celebration vibration
            if (canPlayAnimation) {
                context.playMilestoneSound(R.raw.milestone_sound)
                context.vibrate(genre.vibrationPattern())
                showTitle = true
                delay(300)
                showSubtitle = true
                delay(700)
                showButton = true
            } else {
                showTitle = true
                showSubtitle = true
                showButton = true
            }
        }
    }

    LaunchedEffect(milestone) {
        showOverlay = true
        if (canPlayAnimation.not()) {
            showIcon = true
            fillProgress = 1f
            showTitle = true
            showSubtitle = true
            showButton = true
            return@LaunchedEffect
        }
        viewModel.generateCongratsMessage(milestone, saga)

        delay(300)
        showIcon = true

        launch {
            for (i in 0..100) {
                fillProgress = i / 100f
                delay(50)
            }
        }
    }

    // Clean up when milestone is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clear()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "milestone_animations")
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
                if (canPlayAnimation) {
                    MaterialTheme.colorScheme.background.solidGradient()
                } else {
                    fadeGradientTop()
                }

                Box(
                    modifier =
                        boxModifier
                            .background(MaterialTheme.colorScheme.background)
                            .animateContentSize(),
                    contentAlignment = if (canPlayAnimation) Alignment.Center else Alignment.TopCenter,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .animateContentSize(tween(300, easing = EaseIn))
                                .padding(16.dp),
                    ) {
                        AnimatedVisibility(
                            visible = showIcon,
                            enter = fadeIn(tween(400)) + scaleIn(),
                            exit = fadeOut(tween(700)) + scaleOut(),
                        ) {
                            val fillBrush =
                                if (canPlayAnimation) {
                                    progressiveBrush(genre.color, fillProgress)
                                } else {
                                    genre.gradient(true)
                                }

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
                                                if (fillProgress >= 1f) levitation else 0f
                                        }.sharedElement(
                                            rememberSharedContentState(
                                                key = "saga_${saga.data.id}_spark",
                                            ),
                                            animatedVisibilityScope = animatedVisibilityScope,
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
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                                        fontWeight = FontWeight.Bold,
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
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier =
                                    Modifier.graphicsLayer {
                                        clip = false
                                    },
                                // Prevent shadow clipping
                            ) {
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

                                val textStyle =
                                    if (milestone.isIntrusive) {
                                        MaterialTheme.typography.headlineMedium
                                    } else {
                                        MaterialTheme.typography.labelLarge
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
                                                listOf(Color.Transparent).plus(genre.color.fadeColors()),
                                                repeatMode = RepeatMode.Restart,
                                            ).padding(8.dp),
                                )

                                if (milestone is SagaMilestone.NewEvent) {
                                    val event = saga.findTimeline(milestone.timeline.id)
                                    event?.let {
                                        Row {
                                            if (it.numberOfRelationshipUpdates() > 0) {
                                                CounterText(
                                                    it.numberOfRelationshipUpdates(),
                                                    stringResource(R.string.saga_detail_relationships_section_title),
                                                    textStyle =
                                                        MaterialTheme.typography.titleMedium.copy(
                                                            fontFamily = genre.bodyFont(),
                                                        ),
                                                    labelStyle =
                                                        MaterialTheme.typography.labelMedium.copy(
                                                            fontFamily = genre.bodyFont(),
                                                            color =
                                                                MaterialTheme.colorScheme.onBackground.copy(
                                                                    alpha = 0.7f,
                                                                ),
                                                        ),
                                                )

                                                CounterText(
                                                    it.updatedWikis.size,
                                                    stringResource(R.string.wiki_updated),
                                                    textStyle =
                                                        MaterialTheme.typography.titleMedium.copy(
                                                            fontFamily = genre.bodyFont(),
                                                        ),
                                                    labelStyle =
                                                        MaterialTheme.typography.labelMedium.copy(
                                                            fontFamily = genre.bodyFont(),
                                                            color =
                                                                MaterialTheme.colorScheme.onBackground.copy(
                                                                    alpha = 0.7f,
                                                                ),
                                                        ),
                                                )
                                            }
                                        }
                                    }
                                }

                                if (milestone is SagaMilestone.ChapterFinished) {
                                    val chapter = saga.findChapter(milestone.chapter.id)
                                    chapter?.let {
                                        Column {
                                            val characters =
                                                it.fetchCharacters(saga).filterNotNull()
                                            Text("Personagens mais importantes")

                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                                message
                                                    ?: stringResource(R.string.milestone_encouragement),
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
                            enter = slideInVertically { it / 2 } + fadeIn() + scaleIn(initialScale = 0.8f),
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
                                shape = genre.shape(),
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
                }
            }
        }
    }
}
