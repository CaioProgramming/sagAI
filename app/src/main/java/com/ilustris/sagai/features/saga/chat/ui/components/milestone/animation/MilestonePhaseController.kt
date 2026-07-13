package com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
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

/**
 * Three-step reveal: spark icon → scrollable content → continue action.
 *
 * Replaces the older Hero / Headline / Body / Stats / Dashboard chain.
 */
enum class MilestonePhase {
    Spark,
    Reveal,
    Ready,
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
                    MilestonePhase.Spark -> MilestonePhase.Reveal
                    MilestonePhase.Reveal -> MilestonePhase.Ready
                    MilestonePhase.Ready -> MilestonePhase.Ready
                }
    }

    fun isAtLeast(phase: MilestonePhase): Boolean = currentPhase.ordinal >= phase.ordinal
}

@Composable
fun rememberMilestonePhaseController(initialPhase: MilestonePhase = MilestonePhase.Spark): MilestonePhaseController =
    remember {
        MilestonePhaseController(initialPhase)
    }

fun MilestonePhaseController.advanceAfter(
    scope: CoroutineScope,
    hold: Duration,
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
    val fadeEnter: EnterTransition = fadeIn(tween(700, easing = EaseOutCubic))
    val fadeExit: ExitTransition = fadeOut(tween(450))
    val labelEnter: EnterTransition =
        fadeIn(tween(600, easing = EaseOutCubic)) + slideInVertically { it / 4 }
    val revealEnter: EnterTransition =
        fadeIn(tween(800, easing = EaseOutCubic)) +
            slideInVertically(
                animationSpec = tween(900, easing = EaseOutCubic),
            ) { it / 5 }
    val sparkEnter: EnterTransition =
        scaleIn(
            initialScale = 0.72f,
            animationSpec = tween(900, easing = EaseOutBack),
        ) + fadeIn(tween(650, easing = EaseOutCubic))
    val heroEnter: EnterTransition = sparkEnter
    val cardEnter: (delayMillis: Int) -> EnterTransition = { delayMillis ->
        fadeIn(tween(650, delayMillis = delayMillis, easing = EaseOutCubic)) +
            slideInVertically(
                animationSpec = tween(750, delayMillis = delayMillis, easing = EaseOutCubic),
            ) { it / 5 } +
            scaleIn(
                initialScale = 0.94f,
                animationSpec = tween(750, delayMillis = delayMillis, easing = EaseOutCubic),
            )
    }
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
