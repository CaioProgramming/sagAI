package com.ilustris.sagai.features.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.home.data.model.SagaSummary
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.levitate

@Composable
fun TrophyPinItem(
    saga: SagaSummary,
    avatarSize: Dp,
    visualConfig: GenreVisualConfig?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SagAITheme(visualConfig = visualConfig, genre = saga.data.genre) {
        val genre = saga.data.genre
        val genreColor = genre.color

        Box(
            contentAlignment = Alignment.Center,
            modifier =
                modifier
                    .size(avatarSize + 8.dp)
                    .clickable(onClick = onClick),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .levitate(yOffset = 5f)
                        .size(avatarSize)
                        .dropShadow(CircleShape) {
                            radius = 15f
                            color = genreColor
                            spread = 5f
                        }.clip(CircleShape)
                        .background(genreColor.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.gradientFade(),
                            CircleShape,
                        ),
            ) {
                AvatarTimelineIcon(
                    saga.data.icon,
                    showSpark = true,
                    genre = genre,
                    placeHolderChar =
                        saga.data.title
                            .firstOrNull()
                            ?.uppercaseChar()
                            ?.toString()
                            .orEmpty(),
                    visualConfig = visualConfig,
                    borderWidth = 0.dp,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .effectForGenre(genre, useFallBack = true)
                            .selectiveColorHighlight(genre),
                )
            }
        }
    }
}

@Composable
fun TrophyOverflowPinItem(
    overflowCount: Int,
    avatarSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(avatarSize + 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(avatarSize)
                    .dropShadow(CircleShape) {
                        radius = 8f
                        brush =
                            androidx.compose.ui.graphics.Brush
                                .linearGradient(holographicGradient)
                        spread = 4f
                    }.clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(
                        1.dp,
                        androidx.compose.ui.graphics.Brush
                            .linearGradient(holographicGradient),
                        CircleShape,
                    ),
        ) {
            Text(
                text = stringResource(R.string.home_trophy_overflow, overflowCount),
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    ),
            )
        }
    }
}
