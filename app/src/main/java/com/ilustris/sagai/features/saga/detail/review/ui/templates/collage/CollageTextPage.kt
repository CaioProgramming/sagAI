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
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/** The title rips across first; the body follows once the reader has had a beat with it. */
private const val TITLE_TEAR_DELAY_MS = 250L
private const val BODY_TEAR_DELAY_MS = 1500L

/**
 * Replacement for the default review's plain hook/text pages. Each line of copy gets its own
 * edge-to-edge [TornPaperStrip] stacked in a [Column]: the title strip rips across the screen
 * first, then [BODY_TEAR_DELAY_MS] later the body strip rips in below it — the page reading as
 * something physically torn open rather than text fading in over a background.
 *
 * Deliberately two separate strips rather than one growing card: a single container that merely
 * got taller read as a resizing box, while two independent rips (each with its own tear geometry
 * and its own travel across the screen) is what sells "breaking the layout".
 */
class CollageTextPage(
    override val content: SagaContent,
    override val pageType: ReviewPageType,
    private val text: ReviewText,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val titleReveal = rememberTearReveal(canAnimate, TITLE_TEAR_DELAY_MS)
        val bodyReveal = rememberTearReveal(canAnimate, BODY_TEAR_DELAY_MS)

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    // Slightly wider than the screen so the strips' small rotation never exposes a
                    // gap at either edge — they have to read as running clean off the page.
                    .fillMaxWidth(1.14f)
                    // The body strip is added to the layout only once its turn comes, so without
                    // this the column's height jumps in one frame instead of opening up.
                    .animateContentSize(tween(700, easing = FastOutSlowInEasing)),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                text.title?.let {
                    AssemblingPiece(
                        rotation = -1.6f,
                        delayMs = TITLE_TEAR_DELAY_MS,
                        canAnimate = canAnimate,
                        seed = 61,
                        entranceOffset = Offset(0f, 26f),
                        scaleFrom = 1f,
                    ) {
                        TornPaperStrip(
                            seed = 61,
                            modifier = Modifier.fillMaxWidth(),
                            revealProgress = titleReveal,
                            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 38.dp),
                        ) {
                            Text(
                                text = it,
                                color = PAPER_INK,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                text.subtitle?.let {
                    AssemblingPiece(
                        rotation = 1.3f,
                        delayMs = BODY_TEAR_DELAY_MS,
                        canAnimate = canAnimate,
                        seed = 62,
                        entranceOffset = Offset(0f, 26f),
                        scaleFrom = 1f,
                    ) {
                        TornPaperStrip(
                            seed = 62,
                            modifier = Modifier.fillMaxWidth(),
                            revealProgress = bodyReveal,
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 36.dp),
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
