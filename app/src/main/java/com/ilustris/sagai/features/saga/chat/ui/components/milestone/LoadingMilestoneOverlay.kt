package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlin.time.Duration.Companion.seconds

@Composable
fun LoadingMilestoneOverlay(
    genre: Genre,
    sparkModifier: Modifier,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
        ) {
            Image(
                painterResource(genre.background),
                null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surfaceContainer),
                modifier =
                    sparkModifier
                        .size(
                            80.dp,
                        ).reactiveShimmer(
                            true,
                            genre.shimmerColors(),
                            duration = 10.seconds,
                            repeatMode = RepeatMode.Restart,
                            targetValue = 1000f,
                        ),
            )

            AnimatedContent(message) {
                if (it != null) {
                    Text(
                        text = it,
                        textAlign = TextAlign.Center,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            ),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }
            }
        }
    }
}
