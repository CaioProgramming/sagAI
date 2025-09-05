package com.ilustris.sagai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.SagAIScaffold
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
    var emotionExpanded by remember { mutableStateOf(isExpanded) }



    Column(
        modifier = modifier
            .clip(cardShape)
            .border(1.dp, genre.color.copy(alpha = .3f), cardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer, cardShape)
            .clickable { emotionExpanded = !emotionExpanded }
            .animateContentSize()
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(8.dp)

        ) {
            Image(
                painterResource(R.drawable.ic_full_spark),
                contentDescription = "Open emotion review",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(genre.color),
            )

            Text(
                text = stringResource(R.string.emotional_card_title),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = genre.bodyFont(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )
        }

        if (emotionExpanded && !review.isNullOrBlank()) {
            Text(
                text = review,
                modifier = Modifier
                    .padding(16.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Justify
                ),
            )
        }
    }
}

@Preview
@Composable
fun EmotionalCardPreview() {
    SagAIScaffold {
        EmotionalCard(
            review = "This is a sample emotional review. It can be expanded to show more details about the user's feelings regarding a specific topic or event.",
            genre = Genre.FANTASY,
            isExpanded = true,
        )
    }
}


