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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.genre.collage.AssemblingPiece
import com.ilustris.sagai.ui.genre.collage.PAPER_INK
import com.ilustris.sagai.ui.genre.collage.TornPaperStrip
import com.ilustris.sagai.ui.genre.collage.readableTextColor
import com.ilustris.sagai.ui.genre.collage.rememberTearReveal

private const val TONE_TEAR_DELAY_MS = 250L
private const val BODY_TEAR_DELAY_MS = 1600L

/**
 * Punk Rock's expressiveness stage. Drops the default's morphing `VibeShapeDrawing` entirely in
 * favour of the template's own language: the dominant [EmotionalTone] set huge on a sheet printed
 * in **that tone's own colour**, with the ink picked by contrast
 * ([readableTextColor]) so a pale tone like `CURIOUS` and a heavy one like `ANGRY` both stay
 * legible. The stage copy follows on a plain white strip underneath.
 */
class CollageEmotionPage(
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
        val tone =
            remember {
                content
                    .flatEvents()
                    .flatMap { it.emotionalRanking() }
                    .filter { it.first != null }
                    .maxByOrNull { it.second }
                    ?.first ?: EmotionalTone.NEUTRAL
            }

        val toneReveal = rememberTearReveal(canAnimate, TONE_TEAR_DELAY_MS)
        val bodyReveal = rememberTearReveal(canAnimate, BODY_TEAR_DELAY_MS)
        val toneInk = tone.color.readableTextColor()

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .fillMaxWidth(1.14f)
                    .animateContentSize(tween(700, easing = FastOutSlowInEasing)),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                AssemblingPiece(
                    rotation = -1.6f,
                    delayMs = TONE_TEAR_DELAY_MS,
                    canAnimate = canAnimate,
                    seed = 81,
                    entranceOffset = Offset(0f, 26f),
                    scaleFrom = 1f,
                ) {
                    TornPaperStrip(
                        seed = 81,
                        modifier = Modifier.fillMaxWidth(),
                        paperColor = tone.color,
                        revealProgress = toneReveal,
                        contentPadding = PaddingValues(horizontal = 26.dp, vertical = 34.dp),
                    ) {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            stage.content?.title?.let {
                                Text(
                                    text = it,
                                    color = toneInk.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }

                            AutoResizeText(
                                text = tone.getTitle(),
                                style =
                                    MaterialTheme.typography.displayMedium.copy(
                                        color = toneInk,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                    ),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            )
                        }
                    }
                }

                stage.content?.subtitle?.let {
                    AssemblingPiece(
                        rotation = 1.3f,
                        delayMs = BODY_TEAR_DELAY_MS,
                        canAnimate = canAnimate,
                        seed = 82,
                        entranceOffset = Offset(0f, 26f),
                        scaleFrom = 1f,
                    ) {
                        TornPaperStrip(
                            seed = 82,
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
