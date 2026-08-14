package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.components.VibeShapeDrawing
import com.ilustris.sagai.ui.theme.gradientFade
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * "The Vibe" page — same emotional-tone shape drawing as [com.ilustris.sagai.features.saga.detail.review.ui.ReviewExpressivenessPage],
 * inked instead of shimmered: on parchment the pen-drawn shape itself is the "flourish", so no
 * holographic shimmer is layered on top the way the Default template does.
 */
class BookExpressivenessPage(
    override val content: SagaContent,
    private val stage: ReviewStage,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.EXPRESSIVENESS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent =
            content.data.genre
                .compiledColorPalette()
                .firstOrNull() ?: MaterialTheme.colorScheme.primary
        val ink = LocalContentColor.current
        var showText by remember { mutableStateOf(false) }
        var showShareLink by remember { mutableStateOf(false) }

        LaunchedEffect(showText) {
            if (showText) {
                delay(2.seconds)
                showShareLink = true
            }
        }

        val coroutineScope = rememberCoroutineScope()
        val emotionalTone =
            remember {
                content
                    .flatEvents()
                    .map { it.emotionalRanking() }
                    .first()
                    .first()
            }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp)
                    .animateContentSize(tween(1200, easing = LinearOutSlowInEasing)),
        ) {
            emotionalTone.first?.let {
                VibeShapeDrawing(
                    emotionalTone = it,
                    strokeWidth = 3.dp,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    color = ink,
                    onFinishDraw = {
                        coroutineScope.launch {
                            delay(1500)
                            showText = true
                        }
                    },
                )
            }

            AnimatedVisibility(showText, modifier = Modifier.padding(16.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    stage.content?.title?.let { title ->
                        SimpleTypewriterText(
                            text = title,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    emotionalTone.first?.let {
                        SimpleTypewriterText(
                            text = it.getTitle(),
                            style =
                                MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    brush = it.color.gradientFade(),
                                    textAlign = TextAlign.Center,
                                ),
                        )
                    }

                    stage.content?.subtitle?.let {
                        SimpleTypewriterText(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge.copy(color = ink),
                            textAlign = TextAlign.Center,
                            duration = (it.length * 16).coerceIn(800, 4000).milliseconds,
                            isAnimated = canAnimate,
                        )
                    }
                }
            }

            AnimatedVisibility(showShareLink, modifier = Modifier.padding(top = 16.dp)) {
                BookShareLink(ShareType.EMOTIONS, accent, onAction)
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        BookBackground(modifier)
    }
}
