package com.ilustris.sagai.features.stories.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.home.data.model.SagaSummary
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill

@Composable
fun StoriesRow(
    sagas: List<SagaSummary>,
    loadingStoryId: Int?,
    onStoryClicked: (SagaSummary) -> Unit,
    isAtTop: Boolean,
    visualConfigs: Map<Genre, GenreVisualConfig>,
) {
    val eligibleSagas = sagas.filter { it.data.isEnded.not() && it.chaptersCount > 1 }
    if (eligibleSagas.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(eligibleSagas) { saga ->
                    StoryItem(
                        saga = saga,
                        isLoading = loadingStoryId == saga.data.id,
                        onStoryClicked = onStoryClicked,
                        expanded = isAtTop,
                        visualConfig = visualConfigs[saga.data.genre],
                    )
                }
            }
        }
    }
}

@Composable
fun StoryItem(
    saga: SagaSummary,
    isLoading: Boolean,
    onStoryClicked: (SagaSummary) -> Unit,
    expanded: Boolean,
    visualConfig: GenreVisualConfig?,
) {
    CompositionLocalProvider(
        LocalGenreVisualConfig provides visualConfig,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .animateContentSize()
                    .padding(4.dp),
        ) {
            val iconSize by animateDpAsState(
                targetValue = if (expanded) 80.dp else 64.dp,
                animationSpec = tween(600, easing = EaseIn),
                label = "icon_size_animation",
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .size(iconSize)
                        .clip(CircleShape),
            ) {
                this@Column.AnimatedVisibility(
                    visible = isLoading,
                    modifier =
                        Modifier
                            .fillMaxSize(),
                ) {
                    StoryLoadingIndicator(
                        brush = saga.data.genre.gradient(),
                        modifier =
                            Modifier
                                .clipToBounds()
                                .fillMaxSize()
                                .gradientFill(saga.data.genre.gradient(true, targetValue = 100f)),
                    )
                }

                val padding by animateDpAsState(
                    if (isLoading) 8.dp else 0.dp,
                )

                AvatarTimelineIcon(
                    saga.data.icon,
                    false,
                    saga.data.genre,
                    saga.data.title
                        .first()
                        .uppercase(),
                    borderWidth = 1.dp,
                    modifier =
                        Modifier
                            .padding(padding)
                            .dropShadow(CircleShape) {
                                radius =
                                    if (isLoading) {
                                        20f
                                    } else {
                                        10f
                                    }
                                color =
                                    saga.data.genre.color
                                        .darker()
                                spread = 10f
                            }
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable(enabled = !isLoading) { onStoryClicked(saga) }
                            .effectForGenre(saga.data.genre),
                )
            }
        }
    }
}
