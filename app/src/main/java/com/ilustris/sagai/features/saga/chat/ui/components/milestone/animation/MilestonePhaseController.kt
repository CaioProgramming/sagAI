package com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

enum class MilestonePhase {
    Hero,
    Headline,
    Body,
    Stats,
    Action,
}

@Stable
class MilestonePhaseController(
    initialPhase: MilestonePhase,
) {
    var currentPhase by mutableStateOf(initialPhase)
        private set

    fun advance(to: MilestonePhase? = null) {
        currentPhase =
            to
                ?: when (currentPhase) {
                    MilestonePhase.Hero -> MilestonePhase.Headline
                    MilestonePhase.Headline -> MilestonePhase.Body
                    MilestonePhase.Body -> MilestonePhase.Stats
                    MilestonePhase.Stats -> MilestonePhase.Action
                    MilestonePhase.Action -> MilestonePhase.Action
                }
    }

    fun isAtLeast(phase: MilestonePhase): Boolean = currentPhase.ordinal >= phase.ordinal

    fun isVisible(phase: MilestonePhase): Boolean = currentPhase == phase
}

@Composable
fun rememberMilestonePhaseController(initialPhase: MilestonePhase = MilestonePhase.Hero): MilestonePhaseController =
    remember {
        MilestonePhaseController(initialPhase)
    }

fun MilestonePhaseController.advanceAfter(
    scope: CoroutineScope,
    hold: Duration = 900.milliseconds,
    to: MilestonePhase? = null,
) {
    scope.launch {
        delay(hold)
        advance(to)
    }
}

fun milestoneTypewriterDuration(text: String): Duration {
    val calculated = (text.length * 50L).milliseconds
    return minOf(5.seconds, maxOf(2.seconds, calculated))
}

object MilestoneTransitions {
    val fadeEnter: EnterTransition = fadeIn(tween(450))
    val fadeExit: ExitTransition = fadeOut(tween(300))
    val labelEnter: EnterTransition = fadeIn(tween(400)) + slideInVertically { it / 4 }
    val heroEnter: EnterTransition =
        scaleIn(
            initialScale = 0.6f,
            animationSpec = tween(550, easing = EaseInBounce),
        ) + fadeIn(tween(400))
    val slamEnter: EnterTransition =
        scaleIn(
            initialScale = 2f,
            animationSpec = tween(450, easing = EaseInBounce),
        ) + fadeIn(tween(350))
}

@Composable
fun MilestonePhaseVisibility(
    visible: Boolean,
    enter: EnterTransition = MilestoneTransitions.fadeEnter,
    exit: ExitTransition = MilestoneTransitions.fadeExit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = enter,
        exit = exit,
        modifier = modifier,
        content = { content() },
    )
}
