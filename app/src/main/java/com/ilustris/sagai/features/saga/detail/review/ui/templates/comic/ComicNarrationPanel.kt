package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.comic.ComicBalloonSpec
import com.ilustris.sagai.ui.genre.comic.comicNarrationBalloons

/**
 * A frame that is pure narration, told as caption boxes scattered *loose* over the frame rather
 * than stacked inside it.
 *
 * The boxes are balloons, so they sit above the page and spill past the borders — alternating side
 * to side and overhanging the frame is what gives a text-only beat any movement at all. Boxed
 * neatly inside the panel the same words read as a slide; hung off it they read as a comic.
 *
 * The frame beneath is left as bare ground on purpose: it is the plate the boxes are pinned to,
 * not a container they have to fit within.
 */
class ComicNarrationPanel(
    override val content: SagaContent,
    private val text: ReviewText,
    override val pageType: ReviewPageType,
) : ReviewPage,
    ComicPanelPage {
    // No border, no ground. The boxes are the whole panel, and framing them inside an empty
    // rectangle made a beat that should feel loose on the page read as a boxed-in slide instead.
    override val hasFrame = false

    override val panelSpan = PanelSpan.BAND

    // The geometry lives in `comicNarrationBalloons` so the Milestone screen's single-beat
    // surface lays these out identically. Only the specs are shared, not a composable: the board
    // has to place balloons itself in its own measure policy, or they lose the ability to break
    // out past the panel border — which is the entire reason they are balloons and not content.
    override val balloons: List<ComicBalloonSpec>
        get() = comicNarrationBalloons(text.title, text.subtitle)

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        Box(modifier.fillMaxSize())
    }
}
