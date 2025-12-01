package com.ilustris.sagai.features.playthrough

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
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun AnimatedPlaytimeCounter(
    playtimeMs: Long,
    label: String,
    genre: Genre? = null,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    modifier: Modifier = Modifier,
    animationDuration: Duration = 1.seconds
) {
    val hours = (playtimeMs / 3600000).toInt()
    val minutes = ((playtimeMs % 3600000) / 60000).toInt()

    var targetHours by remember { mutableIntStateOf(0) }
    var targetMinutes by remember { mutableIntStateOf(0) }

    val animatedHours by animateIntAsState(
        targetValue = targetHours,
        animationSpec = tween(durationMillis = animationDuration.toInt(DurationUnit.MILLISECONDS)),
        label = "hours_animation",
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
                    fontFamily = genre?.headerFont(),
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
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = genre?.bodyFont(),
                    textAlign = TextAlign.Center,
                ),
            modifier =
                Modifier
                    .padding(2.dp)
                    .alpha(0.7f),
        )
    }
}
