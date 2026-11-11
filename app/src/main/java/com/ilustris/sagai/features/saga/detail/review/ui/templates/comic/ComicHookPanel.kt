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

/** A stage's opening line is at most this many caption boxes — it is a lead-in, not the stage's prose. */
private const val HOOK_BEATS = 2

/**
 * The line that opens a review stage ("the hook"). The other templates give it a screen; on a
 * comic page it is a caption strip hung over the stage's first page of art, so it costs the
 * reader no turn of its own. Text only — the balloons are the whole panel.
 */
class ComicHookPanel(
    override val content: SagaContent,
    private val hook: ReviewText,
    override val pageType: ReviewPageType,
) : ReviewPage,
    ComicPanelPage {
    override val panelSpan = PanelSpan.BAND

    override val hasFrame = false

    override val balloons: List<ComicBalloonSpec>
        get() = comicNarrationBalloons(hook.title, hook.subtitle, maxBeats = HOOK_BEATS)

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        Box(modifier.fillMaxSize())
    }
}
