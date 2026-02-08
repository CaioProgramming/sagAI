package com.ilustris.sagai.features.playthrough

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
    onAnimationFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val hours = (playtimeMs / 3600000).toInt()
    val minutes = ((playtimeMs % 3600000) / 60000).toInt()

    var targetHours by remember { mutableIntStateOf(0) }
    var targetMinutes by remember { mutableIntStateOf(0) }

    val animatedHours by animateIntAsState(
        targetValue = targetHours,
        animationSpec = tween(durationMillis = animationDuration.toInt(DurationUnit.MILLISECONDS)),
        label = "hours_animation",
        finishedListener = {
            onAnimationFinished()
        },
    )

    val animatedMinutes by animateIntAsState(
        targetValue = targetMinutes,
        animationSpec = tween(durationMillis = 1000),
        label = "minutes_animation",
    )

    LaunchedEffect(playtimeMs) {
        targetHours = hours
        targetMinutes = minutes
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Text(
            text = "${animatedHours}h ${animatedMinutes}m",
            style =
                textStyle.copy(
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                ),
            modifier =
                Modifier
                    .padding(2.dp)
                    .fillMaxWidth(),
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
