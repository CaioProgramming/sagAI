package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ReviewStartPage(
    override val content: SagaContent,
    private val text: ReviewText,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.INTRO

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        var showText by remember {
            mutableStateOf(canAnimate.not())
        }
        val genre = content.data.genre

        LaunchedEffect(Unit) {
            delay(3.seconds)
            showText = true
        }

        Box(modifier, contentAlignment = Alignment.Center) {
            val alignment = if (content.data.icon.isBlank()) Alignment.Center else Alignment.BottomCenter
            AnimatedVisibility(
                showText,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .align(alignment),
                enter = slideInVertically(tween(1500, easing = EaseIn)) { -it },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    text.title?.let {
                        Text(
                            text = it,
                            style =
                                MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                            modifier = Modifier.levitate(canAnimate),
                        )
                    }

                    text.subtitle?.let {
                        Text(
                            text = it,
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium,
                                ),
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        val genre = content.data.genre
        val lineCount = remember { Random.nextInt(3, 10) }

        DepthLayout(
            content.data.icon,
            modifier =
                Modifier
                    .fillMaxSize()
                    .effectForGenre(genre),
        ) {
            DynamicLinework(
                color = genre.resolveColor(),
                lineCount = lineCount,
                strokeWidth = 4.dp,
                enabled = true,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .reactiveShimmer(
                            true,
                            duration = 10.seconds,
                            repeatMode = RepeatMode.Restart,
                        ),
            )
        }
    }
}
