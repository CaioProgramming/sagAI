package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ReviewIntroAnimationPage(
    private val hook: ReviewText,
    private val saga: SagaContent,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        onAction: (ReviewAction) -> Unit,
    ) {
        var showIcon by remember {
            mutableStateOf(false)
        }
        var showLines by remember {
            mutableStateOf(false)
        }

        var showContent by remember {
            mutableStateOf(false)
        }

        var showSaga by remember {
            mutableStateOf(false)
        }

        val lineCount =
            remember {
                Random.nextInt(3, 6)
            }

        val genre = saga.data.genre

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedVisibility(showLines, enter = fadeIn(), exit = scaleOut()) {
                DynamicLinework(
                    MaterialTheme.colorScheme.onBackground,
                    lineCount = lineCount,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 4.dp,
                )
            }

            Column(
                Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .animateContentSize(
                        tween(1000, easing = FastOutSlowInEasing),
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(showIcon, enter = scaleIn(), exit = scaleOut()) {
                    val iconSize by animateDpAsState(
                        if (showSaga) 50.dp else 100.dp,
                    )
                    Image(
                        painterResource(R.drawable.ic_spark),
                        null,
                        Modifier.size(iconSize),
                        colorFilter = ColorFilter.tint(genre.color),
                    )
                }

                val font = genre.bodyFont()
                hook.title?.let {
                    Text(
                        it,
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = font,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                shadow =
                                    Shadow(
                                        color = MaterialTheme.colorScheme.background,
                                        offset = Offset(2f.unaryMinus(), 0f),
                                        blurRadius = 0f,
                                    ),
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                hook.subtitle?.let {
                    Text(
                        it,
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFamily = font,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                AnimatedVisibility(showSaga, enter = fadeIn() + scaleIn(), exit = fadeOut()) {
                    StrokedText(
                        saga.data.title,
                        MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = genre.headerFont(),
                            color = genre.color,
                        ),
                        textAlign = TextAlign.Center,
                        strokeColor = MaterialTheme.colorScheme.onBackground,
                        strokeWidth = 5f,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                    )
                }
            }

            AnimatedVisibility(
                showSaga,
                enter = slideInVertically { -it },
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .clip(genre.shape())
                            .gradientFill(genre.gradient())
                            .clickable {
                                onAction(ReviewAction.Continue)
                            },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.continue_button),
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = genre.bodyFont(),
                            ),
                    )

                    Image(
                        painterResource(R.drawable.ic_arrow_down),
                        null,
                        Modifier.size(24.dp),
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            delay(1500)
            showIcon = true
        }

        LaunchedEffect(showIcon) {
            delay(1.seconds)
            showLines = true
        }

        LaunchedEffect(showLines) {
            delay(2.seconds)
            showContent = true
        }

        LaunchedEffect(showContent) {
            delay(3.seconds)
            showSaga = true
        }
    }
}
