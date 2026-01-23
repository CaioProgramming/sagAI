package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.pulse
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shake
import com.ilustris.sagai.ui.theme.shape
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class ReviewIntroAnimationPage(
    private val hook: ReviewText,
    override val content: SagaContent,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val delay = 2000

        var showIcon by remember {
            mutableStateOf(false)
        }

        var showContent by remember {
            mutableStateOf(false)
        }

        var showSaga by remember {
            mutableStateOf(false)
        }

        val genre = content.data.genre

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .animateContentSize(
                        tween(2000, easing = FastOutSlowInEasing),
                    ).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(
                    showIcon,
                    enter = scaleIn(tween(1500)),
                    exit = scaleOut(),
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp),
                ) {
                    val iconSize by animateDpAsState(
                        if (showSaga) 50.dp else 150.dp,
                        tween(delay, easing = EaseInCubic),
                    )
                    Image(
                        painterResource(R.drawable.ic_spark),
                        null,
                        Modifier
                            .size(iconSize)
                            .pulse(canAnimate)
                            .levitate(canAnimate),
                        colorFilter = ColorFilter.tint(genre.color),
                    )
                }

                val font = genre.bodyFont()

                AnimatedVisibility(
                    showContent,
                    enter = fadeIn(tween(delay, easing = EaseInCubic)),
                    exit = fadeOut(tween(delay, easing = EaseInCubic)),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
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
                }

                AnimatedVisibility(
                    showContent,
                    enter = fadeIn(tween(delay, delayMillis = 500)),
                    exit = fadeOut(tween(delay, delayMillis = 500)),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
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
                }

                AnimatedVisibility(
                    showSaga,
                    enter = fadeIn(tween(delay)) + scaleIn(tween(delay)),
                    exit = fadeOut(tween(delay)),
                    modifier = Modifier.padding(8.dp),
                ) {
                    StrokedText(
                        content.data.title,
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
                                .padding(8.dp)
                                .shake(xOffset = 5f),
                    )
                }
            }

            AnimatedVisibility(
                showSaga,
                enter = scaleIn(tween(delay)),
                exit = fadeOut(tween(delay)),
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
                            }.levitate()
                            .reactiveShimmer(true),
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
            delay(1.seconds)
            showIcon = true
        }

        LaunchedEffect(showIcon) {
            if (showIcon) {
                delay(3.seconds)
                showContent = true
            }
        }

        LaunchedEffect(showContent) {
            if (showContent) {
                delay(3.seconds)
                showSaga = true
            }
        }
    }
}
