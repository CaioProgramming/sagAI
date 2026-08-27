package com.ilustris.sagai.features.playthrough

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun AnimatedPlaytimeCounter(
    playtimeMs: Long,
    label: String,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    labelStyle: TextStyle = MaterialTheme.typography.labelMedium,
    animationDuration: Duration = 5.seconds,
    isAnimated: Boolean = true,
    onAnimationFinished: () -> Unit = {},
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    modifier: Modifier = Modifier,
) {
    val breakdown = remember(playtimeMs) { playtimeMs.toPlaytimeBreakdown() }

    var targetPrimary by remember { mutableIntStateOf(0) }
    var targetSecondary by remember { mutableIntStateOf(0) }

    val animatedPrimary by animateIntAsState(
        targetValue = targetPrimary,
        animationSpec = tween(durationMillis = if (isAnimated) animationDuration.toInt(DurationUnit.MILLISECONDS) else 0),
        label = "primary_unit_animation",
        finishedListener = {
            onAnimationFinished()
        },
    )

    val animatedSecondary by animateIntAsState(
        targetValue = targetSecondary,
        animationSpec = tween(durationMillis = if (isAnimated) 1000 else 0),
        label = "secondary_unit_animation",
    )

    LaunchedEffect(playtimeMs) {
        targetPrimary = breakdown.primary.value
        targetSecondary = breakdown.secondary.value
    }

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier =
            modifier
                .padding(16.dp),
    ) {
        Text(
            text =
                PlaytimeBreakdown(
                    PlaytimeUnit(animatedPrimary, breakdown.primary.unit),
                    PlaytimeUnit(animatedSecondary, breakdown.secondary.unit),
                ).format(),
            style =
                textStyle.copy(
                    fontWeight = FontWeight.Normal,
                    textAlign = if (horizontalAlignment == Alignment.CenterHorizontally) TextAlign.Center else TextAlign.Start,
                ),
            modifier =
                Modifier
                    .padding(2.dp),
        )

        Text(
            text = label,
            style = labelStyle,
            modifier =
                Modifier
                    .padding(2.dp)
                    .alpha(0.7f),
        )
    }
}

@Composable
fun CounterText(
    count: Int,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    animationDuration: Duration = 5.seconds,
    animationEasing: Easing = EaseIn,
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {},
) {
    var counter by remember {
        mutableIntStateOf(0)
    }

    val countAnimation by animateIntAsState(
        targetValue = counter,
        animationSpec =
            tween(
                durationMillis = animationDuration.toInt(DurationUnit.MILLISECONDS),
                easing = animationEasing,
            ),
        finishedListener = {
            onAnimationFinished()
        },
        label = "counter_animation",
    )

    Text(
        text = countAnimation.toString(),
        style = textStyle,
        modifier = modifier,
    )

    LaunchedEffect(count) {
        counter = count
    }
}

enum class LabelOrientation {
    VERTICAL,
    HORIZONTAL,
}
