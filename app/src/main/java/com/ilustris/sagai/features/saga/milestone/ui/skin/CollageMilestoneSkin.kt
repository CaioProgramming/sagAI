package com.ilustris.sagai.features.saga.milestone.ui.skin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.genre.collage.AssemblingPiece
import com.ilustris.sagai.ui.genre.collage.PunkScribbleOverlay
import com.ilustris.sagai.ui.genre.collage.TornPaperScrap

/**
 * Punk Rock's chrome for the Milestone screen — the same torn-paper-and-doodles identity
 * [com.ilustris.sagai.features.saga.detail.ui.SagaReview]'s Collage template wears for the story
 * review, applied here around one milestone step instead of a review page.
 *
 * [content] gets glued onto the screen the way every Collage page assembles its pieces: wrapped in
 * [AssemblingPiece] for the stop-motion "slap it down" entrance, then [TornPaperScrap] for the
 * ragged paper frame itself — matching [com.ilustris.sagai.features.saga.detail.review.ui.templates.collage.CollagePosterPage]'s
 * own insert-chip pairing of the two. [PunkScribbleOverlay] is drawn as a sibling *after* that whole
 * assembled card, the same "doodle on the finished poster" layering
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.collage.CollagePosterPage.Background]
 * uses for its own scribbles — on top of the card, not tucked behind it.
 *
 * Collage doesn't get a custom step indicator — [stepIndex]/[stepTotal] are accepted only for
 * signature parity with the other skins [MilestoneSkinChrome] dispatches to; the plain dot
 * `StepIndicator` elsewhere in `MilestoneClosureContent` stays as-is for this genre.
 */
@Composable
fun CollageMilestoneSkin(
    genre: Genre,
    stepIndex: Int? = null,
    stepTotal: Int? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val seed = remember(genre, stepIndex) { genre.ordinal * 97 + (stepIndex ?: 0) }

    Box(modifier = modifier.fillMaxSize()) {
        AssemblingPiece(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            rotation = -2f,
            delayMs = 120L,
            canAnimate = true,
            seed = seed,
            scaleFrom = 1f,
        ) {
            TornPaperScrap(
                seed = seed,
                modifier = Modifier.fillMaxSize(),
            ) {
                content()
            }
        }

        PunkScribbleOverlay(modifier = Modifier.fillMaxSize())
    }
}
