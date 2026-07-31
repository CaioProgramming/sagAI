package com.ilustris.sagai.features.saga.milestone.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.milestone.presentation.MilestoneUiState
import com.ilustris.sagai.features.saga.milestone.presentation.MilestoneViewModel
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingColor
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.themeBrushColors
import com.ilustris.sagai.ui.theme.themeIconVector
import com.ilustris.sagai.ui.theme.themePainter
import com.ilustris.sagai.ui.theme.themeVfx
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Deliberately simple for now (Duolingo-lesson-complete simple: icon, headline, one CTA) — no
 * mascot/character art yet, [R.drawable.ic_spark] is a placeholder until that's designed. The
 * structural piece that matters here is that this screen fully owns the narrative chain: it
 * can't be backed out of, and it drives itself step by step via [MilestoneViewModel].
 */
@Composable
fun MilestoneScreen(
    sagaId: Int,
    onFinished: () -> Unit,
    viewModel: MilestoneViewModel = hiltViewModel(),
) {
    LaunchedEffect(sagaId) { viewModel.start(sagaId) }
    LaunchedEffect(Unit) { viewModel.finished.collect { onFinished() } }

    // A narrative chain is mandatory once started — no escaping back into chat mid-generation.
    BackHandler(enabled = true) { }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val genre by viewModel.genre.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "milestone_step",
        ) { state ->
            when (state) {
                is MilestoneUiState.Loading -> {
                    MilestoneLoadingContent(reasoning = state.reasoning)
                }

                is MilestoneUiState.ClosureStep -> {
                    MilestoneClosureContent(state = state, onContinue = viewModel::onContinue)
                }

                is MilestoneUiState.IntroductionStep -> {
                    MilestoneIntroductionContent(milestone = state.milestone, onContinue = viewModel::onContinue)
                }
            }
        }
    }
}

/** Genre icon + whatever [ReasoningSynthesizerService][com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService]
 * is currently streaming for this step (falls back to a generic line while it warms up). */
@Composable
internal fun MilestoneLoadingContent(reasoning: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = themePainter(),
                contentDescription = null,
                modifier =
                    Modifier.size(50.dp).gradientFill(Brush.verticalGradient(morphingGradient())).themeVfx(true).reactiveShimmer(
                        true,
                        shimmerColors = Color.White.shimmerize(),
                        repeatMode = RepeatMode.Restart,
                        duration = 10.seconds,
                    ),
            )

            Text(
                text = reasoning?.takeIf { it.isNotBlank() } ?: stringResource(R.string.milestone_loading_default),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        brush = Brush.horizontalGradient(themeBrushColors()),
                        shadow =
                            Shadow(
                                Color.White,
                                blurRadius = 15f,
                            ),
                    ),
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(top = 16.dp)
                        .reactiveShimmer(
                            true,
                            shimmerColors = Color.White.shimmerize(),
                            repeatMode = RepeatMode.Restart,
                            duration = 10.seconds,
                        ),
            )
        }
    }
}

@Composable
internal fun MilestoneClosureContent(
    state: MilestoneUiState.ClosureStep,
    onContinue: () -> Unit,
) {
    val milestone = state.milestone
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        if (state.stepTotal > 1) {
            StepIndicator(stepIndex = state.stepIndex, stepTotal = state.stepTotal)
        }

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val themeBrush = sagaBrush()
            Icon(
                painter = themePainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.size(50.dp).gradientFill(Brush.verticalGradient(morphingGradient())).themeVfx(true).reactiveShimmer(
                        true,
                        shimmerColors = Color.White.shimmerize(),
                        repeatMode = RepeatMode.Restart,
                        duration = 10.seconds,
                    ),
            )
            Text(
                text = stringResource(milestone.title).lowercase(),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp).alpha(.5f),
            )
            Text(
                text = milestone.subtitle,
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        letterSpacing = 0.5.sp,
                        shadow = Shadow(MaterialTheme.colorScheme.primary, blurRadius = 15f),
                        fontWeight = FontWeight.SemiBold,
                    ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            milestone.message?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Text(stringResource(R.string.continue_button))
        }
    }
}

@Composable
internal fun MilestoneIntroductionContent(
    milestone: SagaMilestone.Introduction,
    onContinue: () -> Unit,
) {
    // Cinematic, unstepped on purpose — this is the "cold open" beat, not a checklist item.
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        var textComplete by remember {
            mutableStateOf(false)
        }
        var shownChapter by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(textComplete) {
            if (textComplete) {
                shownChapter = true
                delay(1.seconds)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = themePainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(50.dp).themeVfx(true),
            )
            AnimatedVisibility(shownChapter) {
                Text(
                    text = milestone.number,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp).alpha(.6f),
                )
            }

            AnimatedVisibility(shownChapter) {
                Text(
                    text = milestone.titleText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            milestone.introduction.takeIf { it.isNotBlank() }?.let { introduction ->
                SimpleTypewriterText(
                    text = introduction,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                        ),
                    modifier = Modifier.padding(top = 16.dp),
                    onAnimationFinished = {
                        textComplete = true
                    },
                )
            }
        }

        AnimatedVisibility(shownChapter) {
            Button(
                onClick = onContinue,
                shape = MaterialTheme.shapes.medium,
                modifier =
                    Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.continue_button))
            }
        }
    }
}

@Composable
private fun StepIndicator(
    stepIndex: Int,
    stepTotal: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        repeat(stepTotal) { i ->
            val filled = i < stepIndex
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (filled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
            )
        }
    }
}
