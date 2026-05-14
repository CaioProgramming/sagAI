@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.timeline.ui
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.timeline.presentation.TimelineViewModel
import com.ilustris.sagai.features.timeline.ui.components.TimelineThreadList
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun TimeLineContent(
    sagaId: Int,
    title: String,
    subtitle: String,
    onBackClick: () -> Unit = {},
    titleModifier: Modifier = Modifier,
) {
    val timelineViewModel = hiltViewModel<TimelineViewModel>()
    val timelineView by timelineViewModel.timelineView.collectAsStateWithLifecycle()
    val genre = timelineView?.saga?.genre ?: Genre.FANTASY

    LaunchedEffect(sagaId) {
        timelineViewModel.loadSaga(sagaId)
    }

    AnimatedContent(timelineView) {
        it?.let { viewContent ->
            Column(modifier = Modifier.fillMaxSize()) {
                SharedTransitionLayout {
                    SagaTopBar(
                        title,
                        subtitle,
                        genre,
                        onBackClick = { onBackClick() },
                        actionContent = { Box(Modifier.size(24.dp)) },
                        modifier =
                            titleModifier
                                .background(MaterialTheme.colorScheme.background)
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp),
                    )
                }

                TimelineThreadList(
                    timelineContent = viewContent,
                    modifier = Modifier.weight(1f),
                    onAction = { },
                )
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(genre.icon),
                    null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .4f),
                    modifier =
                        Modifier
                            .size(64.dp)
                            .reactiveShimmer(true),
                )
            }
        }
    }
}

@Composable
fun AvatarTimelineIcon(
    icon: String?,
    showSpark: Boolean = false,
    genre: Genre,
    placeHolderChar: String = "",
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    modifier: Modifier = Modifier,
    visualConfig: GenreVisualConfig? = null,
) {
    Box(
        modifier =
            modifier
                .background(
                    Brush.verticalGradient(backgroundColor.darkerPalette(factor = .25f)),
                    CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        AutoResizeText(
            text = placeHolderChar,
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary,
                ),
            modifier = Modifier.alpha(.3f),
        )

        if (!icon.isNullOrBlank()) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(icon)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(borderWidth, borderColor, CircleShape)
                        .effectForGenre(genre, visualConfig)
                        .selectiveColorHighlight(genre.selectiveHighlight(visualConfig)),
            )
        }
        if (showSpark) {
            Icon(
                painterResource(R.drawable.ic_spark),
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .size(12.dp)
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            this.translationY = -25f
                        },
            )
        }
    }
}
