package com.ilustris.sagai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun EmotionalCard(
    review: String?,
    genre: Genre,
    isExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(genre.cornerSize())
    var emotionExpanded by remember {
        mutableStateOf(isExpanded)
    }
    val emotionCardColor by animateColorAsState(
        if (emotionExpanded.not()) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
    )
    val cardTint by animateColorAsState(
        if (emotionExpanded.not()) MaterialTheme.colorScheme.onSurface else genre.color,
        tween(1.seconds.toInt(DurationUnit.MILLISECONDS), easing = FastOutSlowInEasing),
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Top,
        modifier =
            modifier
                .clip(cardShape)
                .clickable {
                    emotionExpanded = !emotionExpanded
                }
                .animateContentSize(),
    ) {
        Image(
            painterResource(R.drawable.ic_full_spark),
            "Open emotion review",
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(genre.color),
        )

        if (emotionExpanded && review != null) {
            Text(
                review,
                modifier = Modifier
                    .weight(1f)
                    .padding(end= 8.dp)
                    .background(emotionCardColor, cardShape)
                    .padding(8.dp)
                ,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = genre.bodyFont(),
                    ),
            )
        }
    }
}
