package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.milestone.ui.EventStatsSection
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhase
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhaseController
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhaseVisibility
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestoneTransitions
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.advanceAfter
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.milestoneTypewriterDuration
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.rememberMilestonePhaseController
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.components.mascot.MascotEmotionFace
import com.ilustris.sagai.ui.theme.darkerPalette
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private fun advanceFromHeadline(
    stats: List<Pair<Int, Int>>,
    phaseController: MilestonePhaseController,
) {
    if (stats.isEmpty()) {
        phaseController.advance(MilestonePhase.Action)
    } else {
        phaseController.advance(MilestonePhase.Stats)
    }
}

@Composable
fun EventMilestoneOverlay(
    milestone: SagaMilestone.NewEvent,
    genre: Genre,
    onDismiss: () -> Unit,
) {
    val phaseController = rememberMilestonePhaseController(MilestonePhase.Hero)
    val coroutineScope = rememberCoroutineScope()
    val event = milestone.sagaContent.findTimeline(milestone.timeline.id)
    val emotionalTone = event?.data?.emotionalTone
    val stats = event?.statsSummary().orEmpty()
    val message = milestone.message?.takeIf { it.isNotBlank() }

    LaunchedEffect(Unit) {
        phaseController.advance(MilestonePhase.Hero)
        phaseController.advanceAfter(
            coroutineScope,
            hold = 1.8.seconds,
            to = MilestonePhase.Headline,
        )
    }

    LaunchedEffect(phaseController.currentPhase, message, stats) {
        if (
            phaseController.currentPhase == MilestonePhase.Headline &&
            message.isNullOrBlank()
        ) {
            delay(1.seconds)
            advanceFromHeadline(stats, phaseController)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
    ) {
        MilestonePhaseVisibility(
            visible = phaseController.currentPhase == MilestonePhase.Hero,
            enter = MilestoneTransitions.heroEnter,
            exit = MilestoneTransitions.fadeExit,
        ) {
            emotionalTone?.let { tone ->
                MascotEmotionFace(
                    milestone.emotionalMascot,
                    tone,
                    modifier = Modifier.size(140.dp),
                )
            }
        }

        MilestonePhaseVisibility(
            visible =
                phaseController.isAtLeast(MilestonePhase.Headline) &&
                    phaseController.currentPhase != MilestonePhase.Hero,
            enter = MilestoneTransitions.fadeEnter,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(milestone.title),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                MilestoneStylisedTitle(
                    genre = genre,
                    text = milestone.subtitle,
                    visible = true,
                )

                message?.let { bodyMessage ->
                    SimpleTypewriterText(
                        text = bodyMessage,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                        duration = milestoneTypewriterDuration(bodyMessage),
                        onAnimationFinished = {
                            advanceFromHeadline(stats, phaseController)
                        },
                    )
                }
            }
        }

        val brush =
            Brush.horizontalGradient(
                MaterialTheme.colorScheme.primary.darkerPalette(factor = 0.25f),
            )

        MilestonePhaseVisibility(
            visible = phaseController.isAtLeast(MilestonePhase.Stats),
            enter = MilestoneTransitions.fadeEnter,
        ) {
            EventStatsSection(
                stats = stats,
                genre = genre,
                brush = brush,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        LaunchedEffect(phaseController.currentPhase) {
            if (phaseController.currentPhase == MilestonePhase.Stats && stats.isNotEmpty()) {
                delay(2.5.seconds)
                phaseController.advance(MilestonePhase.Action)
            }
        }

        MilestoneContinueButton(
            genre = genre,
            visible = phaseController.isAtLeast(MilestonePhase.Action),
            onDismiss = onDismiss,
        )
    }
}
