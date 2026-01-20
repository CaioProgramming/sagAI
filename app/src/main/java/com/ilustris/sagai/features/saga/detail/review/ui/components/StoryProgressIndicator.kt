package com.ilustris.sagai.features.saga.detail.review.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun StoryProgressIndicator(
    modifier: Modifier = Modifier,
    totalSteps: Int,
    currentStep: Int,
    isPaused: Boolean = false,
    stepDuration: Int = 15.seconds.toInt(DurationUnit.MILLISECONDS),
    onStepComplete: () -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (i in 0 until totalSteps) {
            val progress =
                remember(currentStep, i) {
                    Animatable(
                        if (i < currentStep) 1f else 0f,
                    )
                }

            LaunchedEffect(currentStep, isPaused) {
                if (i == currentStep && !isPaused) {
                    // Animate current step
                    val remainingProgress = 1f - progress.value
                    val remainingDuration = (remainingProgress * stepDuration).toInt()

                    if (remainingDuration > 0) {
                        progress.animateTo(
                            targetValue = 1f,
                            animationSpec =
                                tween(
                                    durationMillis = remainingDuration,
                                    easing = LinearEasing,
                                ),
                        )
                        onStepComplete()
                    }
                } else if (i == currentStep && isPaused) {
                    // Stop animation (hold state) - implementation detail:
                    // Animatable doesn't support "pausing" mid-flight easily without
                    // snapshot usage or frame counting, but for simple story UX,
                    // we often just stop the advancement or let it finish and wait.
                    // For true pause, we'd need a more complex state, but usually
                    // users just want to hold the *current* frame.
                    // If we want to pause the *progress bar*, we'd need to track elapsed time.
                    progress.stop()
                } else if (i < currentStep) {
                    progress.snapTo(1f)
                } else {
                    progress.snapTo(0f)
                }
            }

            // If we are resuming from pause, we might want to continue from where we left off
            // But simplistic approach: just restart or continue.
            // For a robust story player, let's keep it simple: Reset if we re-enter?
            // Actually, `progress.stop()` freezes it. `animateTo` would restart or continue depending on current value.
            // If `isPaused` becomes false, the LaunchedEffect re-runs.
            // Since `progress` retains its value, `animateTo(1f)` will continue from current value!

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(progress.value)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.onBackground),
                )
            }
        }
    }
}
