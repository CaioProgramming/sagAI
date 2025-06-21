package com.ilustris.sagai.features.timeline.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.presentation.TimelineViewModel
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun TimelineView(
    sagaId: String,
    navHostController: NavHostController,
    viewModel: TimelineViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()

    TimelineContentView(saga) {
        navHostController.popBackStack()
    }

    LaunchedEffect(saga) {
        if (saga == null) {
            viewModel.getSaga(sagaId)
        }
    }
}

@Composable
fun TimelineContentView(
    sagaContent: SagaContent?,
    onBack: () -> Unit = {},
) {
    AnimatedContent(sagaContent) {
        when (it) {
            null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SparkLoader(
                        gradientAnimation(holographicGradient),
                        2.dp,
                        modifier = Modifier.padding(12.dp).size(32.dp),
                    )
                    Text("Looking for timeline data...")

                    Button(
                        onClick = { onBack() },
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text("Voltar", textAlign = TextAlign.Center)
                    }
                }
            }

            else -> TimeLineContent(it, onBack)
        }
    }
}

@Composable
fun TimeLineContent(
    content: SagaContent,
    onBack: () -> Unit = {},
) {
    Column {
        val events = content.timelines
        SagaTopBar(
            title = "Linha do tempo",
            subtitle = "${content.timelines.size} eventos",
            genre = content.data.genre,
            modifier = Modifier.padding(top = 50.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
            onBackClick = { onBack() },
        )

        val color = content.data.genre.color
        val cornerSize = content.data.genre.cornerSize()
        val pagerState = rememberPagerState(initialPage = events.lastIndex) { events.size }

        Row {
            Column(
                modifier = Modifier.fillMaxWidth(.1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val fraction = 1f / events.size
                events.forEachIndexed { index, event ->
                    val isCurrentPage = index == pagerState.currentPage
                    val tint by animateColorAsState(
                        if (isCurrentPage) color else MaterialTheme.colorScheme.onBackground.copy(alpha = .4f),
                    )
                    val iconSize by animateDpAsState(
                        if (isCurrentPage) 24.dp else 12.dp,
                        tween(
                            easing = EaseInElastic,
                            durationMillis = 1.seconds.toInt(DurationUnit.MILLISECONDS),
                        ),
                    )
                    val fractionSize by animateFloatAsState(
                        if (isCurrentPage) .4f else fraction,
                        tween(
                            easing = EaseIn,
                            durationMillis = 2.seconds.toInt(DurationUnit.MILLISECONDS),
                            delayMillis = 1.seconds.toInt(DurationUnit.MILLISECONDS),
                        ),
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            contentDescription = null,
                            modifier =
                                Modifier.clip(CircleShape).padding(2.dp).size(iconSize).clickable {
                                    pagerState.requestScrollToPage(index)
                                },
                            colorFilter = ColorFilter.tint(tint),
                        )

                        Box(
                            modifier =
                                Modifier
                                    .width(2.dp)
                                    .fillMaxHeight(fractionSize)
                                    .background(
                                        tint,
                                        shape = RoundedCornerShape(cornerSize),
                                    ),
                        )
                    }
                }
            }

            VerticalPager(pagerState, modifier = Modifier.weight(1f).fillMaxHeight()) {
                val event = events[it]

                TimeLineCard(
                    event,
                    content.data.genre,
                    pagerState.currentPage == it,
                    modifier = Modifier.wrapContentHeight(),
                )
            }
        }
    }
}

@Composable
fun TimeLineCard(
    event: Timeline,
    genre: Genre,
    isCurrentPage: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = genre.color
    val cornerSize = genre.cornerSize()
    val cardShape = RoundedCornerShape(cornerSize)

    val backgroundColor by animateColorAsState(
        if (isCurrentPage) color else MaterialTheme.colorScheme.background,
        tween(
            easing = EaseIn,
            durationMillis = 2.seconds.toInt(DurationUnit.MILLISECONDS),
            delayMillis = 1.seconds.toInt(DurationUnit.MILLISECONDS),
        ),
    )

    val textColor by animateColorAsState(
        if (isCurrentPage) genre.iconColor else MaterialTheme.colorScheme.onBackground,
        tween(
            easing = EaseIn,
            durationMillis = 1.seconds.toInt(DurationUnit.MILLISECONDS),
            delayMillis = 2.seconds.toInt(DurationUnit.MILLISECONDS),
        ),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier =
            modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .border(1.dp, genre.color.gradientFade(), cardShape)
                .background(
                    backgroundColor,
                    cardShape,
                ).padding(16.dp),
    ) {
        Text(
            event.createdAt.formatDate(),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    color = textColor,
                ),
        )

        Text(
            event.title,
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontFamily = genre.headerFont(),
                    color = textColor,
                ),
        )

        Text(
            event.content,
            modifier = Modifier.padding(vertical = 8.dp),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = genre.bodyFont(),
                    color = textColor,
                ),
        )
    }
}
