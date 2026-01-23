package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.levitate
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class ReviewHookPage(
    override val content: SagaContent,
    private val hook: ReviewText,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        var showContent by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(3.seconds)
            showContent = true
        }

        val delay = if (canAnimate) 2500 else 100

        Box(
            modifier =
                modifier
                    .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .animateContentSize(tween(500, easing = FastOutSlowInEasing)),
            ) {
                hook.title?.let {
                    Text(
                        text = it,
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                        modifier = Modifier.levitate(true),
                        textAlign = TextAlign.Center,
                    )
                }

                AnimatedVisibility(
                    showContent,
                    enter = slideInVertically(tween(delay)) { -it } + fadeIn(tween(delay)),
                    exit = fadeOut(),
                ) {
                    hook.subtitle?.let {
                        Text(
                            text = it,
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                ),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
