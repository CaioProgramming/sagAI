package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.vibrate
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.newsaga.data.model.vibrationPattern
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.lighter
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MilestoneOverlay(
    milestone: SagaMilestone,
    saga: SagaContent,
    onDismiss: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val genre = saga.data.genre
    val colors = genre.shimmerColors()

    var showIcon by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showOverlay by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(milestone) {
        showOverlay = true
        context.vibrate(genre.vibrationPattern())
        delay(300)
        showIcon = true
        delay(400)
        showTitle = true
        delay(600)
        showSubtitle = true
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

    val shakeIntensity =
        when (genre) {
            Genre.HORROR -> 5f
            Genre.PUNK_ROCK -> 8f
            Genre.CYBERPUNK -> 4f
            else -> 0f
        }

    val shake by infiniteTransition.animateFloat(
        initialValue = -shakeIntensity,
        targetValue = shakeIntensity,
        animationSpec =
            infiniteRepeatable(
                animation = tween(if (genre == Genre.PUNK_ROCK) 50 else 100, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "shake",
    )

    AnimatedVisibility(
        visible = showOverlay,
        enter =
            slideInVertically(
                animationSpec =
                    tween(
                        600,
                        easing = EaseInOutQuad,
                    ),
            ) { it } + fadeIn(),
        exit = fadeOut() + scaleOut(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp),
            ) {
                AnimatedVisibility(
                    visible = showIcon,
                    enter =
                        fadeIn() +
                            scaleIn(
                                initialScale = 0.5f,
                                animationSpec = tween(600, easing = EaseInOutQuad),
                            ),
                    exit = fadeOut(),
                ) {
                    with(sharedTransitionScope) {
                        Image(
                            painter = painterResource(R.drawable.ic_spark),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(genre.color),
                            modifier =
                                Modifier
                                    .size(64.dp)
                                    .reactiveShimmer(
                                        true,
                                        colors,
                                        repeatMode = RepeatMode.Restart,
                                        targetValue = 100f,
                                    )
                                    .sharedElement(
                                        rememberSharedContentState(key = "current_objective_${saga.data.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                    ),
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showTitle,
                    enter = slideInVertically(animationSpec = tween(500)) { it / 2 } + fadeIn(),
                    exit = fadeOut(),
                ) {
                    Text(
                        text = stringResource(milestone.title).uppercase(),
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontFamily = genre.bodyFont(),
                                letterSpacing = 6.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold,
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
                        modifier =
                            Modifier.graphicsLayer {
                                translationY = levitation
                                if (shakeIntensity > 0) {
                                    translationX = shake
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (milestone is SagaMilestone.NewCharacter) {
                            if (milestone.character.image.isNotEmpty()) {
                                CharacterAvatar(
                                    milestone.character,
                                    genre = genre,
                                    modifier = Modifier.size(120.dp),
                                )
                            }
                        }

                        Text(
                            text = milestone.subtitle,
                            style =
                                MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = genre.headerFont(),
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    brush =
                                        Brush.verticalGradient(
                                            listOf(
                                                genre.color.lighter(),
                                                Color.White,
                                            ),
                                        ),
                                    shadow = Shadow(genre.color, blurRadius = 15f),
                                ),
                            modifier =
                                Modifier
                                    .reactiveShimmer(true, colors, repeatMode = RepeatMode.Reverse)
                                    .padding(vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun NewChapterPreview() {
    val genre = Genre.CYBERPUNK
    val sagaContent = SagaContent(Saga(genre = genre))
    SharedTransitionLayout {
        AnimatedContent(targetState = true, label = "preview") { _ ->
            MilestoneOverlay(
                milestone =
                    SagaMilestone.ChapterFinished(
                        Chapter(
                            title = "The Dragon's Awakening",
                            actId = 0,
                        ),
                    ),
                saga = sagaContent,
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this,
            )
        }
    }
}
