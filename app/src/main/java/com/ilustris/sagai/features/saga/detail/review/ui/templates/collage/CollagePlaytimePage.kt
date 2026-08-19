package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.playthrough.AnimatedPlaytimeCounter
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

private const val TIME_TEAR_DELAY_MS = 250L
private const val LABEL_DELAY_MS = 1400L
private const val BODY_TEAR_DELAY_MS = 2100L

/**
 * Punk Rock's playtime stage, built on the same sheet language as [CollageEmotionPage] — minus the
 * adaptive colour, since playtime has no tone to key off. The clocked time is the headline on a
 * white sheet, the stage's own label lands as a small accent-coloured scrap pinned over that sheet,
 * and the body copy rips in on a strip below.
 */
class CollagePlaytimePage(
    override val content: SagaContent,
    private val playstyle: ReviewText,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.PLAYSTYLE

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
        val timeReveal = rememberTearReveal(canAnimate, TIME_TEAR_DELAY_MS)
        val bodyReveal = rememberTearReveal(canAnimate, BODY_TEAR_DELAY_MS)

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .fillMaxWidth(1.14f)
                    .animateContentSize(tween(700, easing = FastOutSlowInEasing)),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Box {
                    AssemblingPiece(
                        rotation = -1.4f,
                        delayMs = TIME_TEAR_DELAY_MS,
                        canAnimate = canAnimate,
                        seed = 91,
                        entranceOffset = Offset(0f, 26f),
                        scaleFrom = 1f,
                    ) {
                        TornPaperStrip(
                            seed = 91,
                            modifier = Modifier.fillMaxWidth(),
                            revealProgress = timeReveal,
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 40.dp),
                        ) {
                            AnimatedPlaytimeCounter(
                                playtimeMs = content.data.playTimeMs,
                                // The stage label is its own scrap below, so the counter carries none.
                                label = "",
                                isAnimated = canAnimate,
                                textStyle =
                                    MaterialTheme.typography.displayMedium.copy(
                                        color = PAPER_INK,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                    ),
                                labelStyle = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    playstyle.title?.let {
                        AssemblingPiece(
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(x = 24.dp, y = 18.dp),
                            rotation = 4f,
                            delayMs = LABEL_DELAY_MS,
                            canAnimate = canAnimate,
                            seed = 92,
                        ) {
                            TornPaperScrap(
                                seed = 92,
                                paperColor = accent,
                                modifier = Modifier.widthIn(max = 220.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                            ) {
                                Text(
                                    text = it,
                                    color = accent.readableTextColor(),
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }
                    }
                }

                playstyle.subtitle?.let {
                    AssemblingPiece(
                        rotation = 1.2f,
                        delayMs = BODY_TEAR_DELAY_MS,
                        canAnimate = canAnimate,
                        seed = 93,
                        entranceOffset = Offset(0f, 26f),
                        scaleFrom = 1f,
                    ) {
                        TornPaperStrip(
                            seed = 93,
                            modifier = Modifier.fillMaxWidth(),
                            revealProgress = bodyReveal,
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 34.dp),
                        ) {
                            Text(
                                text = it,
                                color = PAPER_INK,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }
}
