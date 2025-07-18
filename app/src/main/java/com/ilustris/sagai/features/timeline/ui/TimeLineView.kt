package com.ilustris.sagai.features.timeline.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.presentation.TimelineViewModel
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import java.util.Calendar
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
    val lazyListState = rememberLazyListState()
    LazyColumn(state = lazyListState) {
        items(content.timelines) {
            TimeLineCard(it, content.data.genre, modifier = Modifier.fillMaxWidth())
        }
    }

    LaunchedEffect(content) {
        lazyListState.animateScrollToItem(content.timelines.size - 1)
    }
}

@Composable
fun TimeLineCard(
    event: Timeline,
    genre: Genre,
    isLast: Boolean = false,
    showText: Boolean = true,
    showSpark: Boolean = true,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    contentStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
) {
    val color = genre.color
    val cornerSize = genre.cornerSize()
    val cardShape = RoundedCornerShape(cornerSize)

    val textColor by animateColorAsState(
        MaterialTheme.colorScheme.onBackground,
        tween(
            easing = EaseIn,
            durationMillis = 1.seconds.toInt(DurationUnit.MILLISECONDS),
            delayMillis = 2.seconds.toInt(DurationUnit.MILLISECONDS),
        ),
    )

    ConstraintLayout(modifier.fillMaxWidth()) {
        val (iconView, titleContent, contentView) = createRefs()
        Column(
            modifier =
                Modifier.constrainAs(iconView) {
                    top.linkTo(parent.top)
                    bottom.linkTo(contentView.bottom)
                    start.linkTo(parent.start)
                    height = Dimension.fillToConstraints
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painterResource(R.drawable.ic_spark),
                null,
                modifier = Modifier.size(if(showSpark) 24.dp else 0.dp),
                colorFilter = ColorFilter.tint(genre.color),
            )

            if (isLast.not()) {
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(genre.color))
            }
        }

        Column(
            modifier =
                Modifier.padding(horizontal = 8.dp).constrainAs(titleContent) {
                    top.linkTo(iconView.top)
                    start.linkTo(iconView.end)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
        ) {
            Text(
                event.title,
                style =
                   titleStyle.copy(
                        fontFamily = genre.headerFont(),
                        color = genre.color,
                    ),
            )

            Text(
                event.createdAt.formatDate(),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        color = textColor.copy(alpha = .4f),
                        fontWeight = FontWeight.Light,
                    ),
            )
        }

            Text(
                if (showText) event.content else emptyString(),
                modifier =
                    Modifier
                        .constrainAs(contentView) {
                            top.linkTo(titleContent.bottom)
                            start.linkTo(titleContent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.fillToConstraints
                        }.padding(8.dp),
                style =
                    contentStyle.copy(
                        fontFamily = genre.bodyFont(),
                        color = textColor,
                        textAlign = TextAlign.Start,
                    ),
            )
    }
}

@Preview(showBackground = true)
@Composable
fun TimeLineContentPreview() {
    val content =
        SagaContent(
            data =
                SagaData(
                    title = "My Awesome Saga",
                    description = "A saga about adventure and stuff.",
                    genre = Genre.FANTASY,
                ),
            timelines =
                listOf(
                    Timeline(
                        title = "Event 1",
                        content = "Something happened",
                        createdAt = Calendar.getInstance().timeInMillis,
                    ),
                    Timeline(
                        title = "Event 2",
                        content = "Something else happened",
                        createdAt = Calendar.getInstance().timeInMillis + 100000,
                    ),
                ),
        )
    TimeLineContent(content)
}

@Preview(showBackground = true)
@Composable
fun TimeLineCardPreview() {
    val event =
        Timeline(
            title = "The Great Battle",
            content = "A fierce battle took place, changing the course of history.",
            createdAt = Calendar.getInstance().timeInMillis,
        )
    val genre = Genre.FANTASY
    TimeLineCard(
        event = event,
        genre = genre,
    )
}
