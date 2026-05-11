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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.timeline.presentation.TimelineViewModel
import com.ilustris.sagai.features.timeline.ui.components.TimelineThreadList
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.headerFont
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
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val finalBackgroundColor =
        if (backgroundColor == Color.Transparent) {
            genre.resolveColor()
        } else {
            backgroundColor
        }

    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(finalBackgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text = placeHolderChar,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                ),
            color = genre.iconColor,
            fontFamily = genre.headerFont(),
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
                modifier =
                    Modifier
                        .fillMaxSize()
                        .border(borderWidth, borderColor, CircleShape)
                        .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
