package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhase
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhaseVisibility
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestoneTransitions
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.advanceAfter
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.milestoneTypewriterDuration
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.rememberMilestonePhaseController
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun IntroductionOverlay(
    introduction: SagaMilestone.Introduction,
    saga: Saga,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onComplete: () -> Unit,
) {
    val message = introduction.introduction
    val genre = saga.genre
    val phaseController = rememberMilestonePhaseController(MilestonePhase.Hero)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        phaseController.advance(MilestonePhase.Hero)
        if (message.isNotBlank()) {
            phaseController.advanceAfter(
                coroutineScope,
                hold = 1.2.seconds,
                to = MilestonePhase.Body,
            )
        }
    }

    LaunchedEffect(phaseController.currentPhase) {
        if (phaseController.currentPhase == MilestonePhase.Hero && message.isBlank()) {
            delay(1.5.seconds)
            onComplete()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
        ) {
            MilestonePhaseVisibility(
                visible = phaseController.isAtLeast(MilestonePhase.Hero),
                enter = MilestoneTransitions.fadeEnter,
            ) {
                with(sharedTransitionScope) {
                    genre.stylisedText(
                        text = saga.title,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                    )
                }
            }

            if (message.isNotBlank()) {
                MilestonePhaseVisibility(
                    visible = phaseController.isAtLeast(MilestonePhase.Body),
                    enter = MilestoneTransitions.fadeEnter,
                ) {
                    SimpleTypewriterText(
                        text = message,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 16.dp),
                        duration = milestoneTypewriterDuration(message),
                        onAnimationFinished = {
                            coroutineScope.launch {
                                delay(1.5.seconds)
                                onComplete()
                            }
                        },
                    )
                }
            }
        }
    }
}
