package com.ilustris.sagai.features.home.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaSummary
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.morphingGradient

@Composable
fun TrophyPinItem(
    saga: SagaSummary,
    avatarSize: Dp,
    focusFactor: Float = 1f,
    levitateEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    SagAITheme(genre = saga.data.genre) {
        TrophyPinVisual(
            saga = saga,
            avatarSize = avatarSize,
            focusFactor = focusFactor,
            levitateEnabled = levitateEnabled,
            modifier = modifier,
        )
    }
}

@Composable
internal fun TrophyPinVisual(
    saga: SagaSummary,
    avatarSize: Dp,
    focusFactor: Float,
    levitateEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val genre = saga.data.genre
    val isFocused = focusFactor > 0.75f

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 0.dp,
        label = "trophyBorderWidth",
    )

    val shadowAlpha by animateFloatAsState(
        if (isFocused) 1f else 0f,
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(avatarSize),
    ) {
        Crossfade(targetState = isFocused, label = "trophyHighlightBrush") { focused ->
            val highlightBrush =
                if (focused) {
                    Brush.verticalGradient(morphingGradient())
                } else {
                    Brush.verticalGradient(MaterialTheme.colorScheme.primary.darkerPalette())
                }

            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .levitate(isPlaying = levitateEnabled, yOffset = 5f)
                        .size(avatarSize)
                        .dropShadow(CircleShape) {
                            brush = highlightBrush
                            radius = 25f
                            spread = .5f
                            alpha = shadowAlpha
                        }.border(borderWidth, highlightBrush, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.darker(.25f), CircleShape),
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
                    borderWidth = 0.dp,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .effectForGenre(
                                genre,
                                useFallBack = true,
                                enableSelectiveHighlight = true,
                            ),
                )
            }
        }
    }
}
