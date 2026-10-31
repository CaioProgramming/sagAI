package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.share.domain.model.ShareType

interface ReviewExperience {
    val pages: List<ReviewPage>

    /** How this experience is navigated/rendered. Defaults to today's vertical story pager. */
    val navigationStyle: ReviewNavigationStyle get() = ReviewNavigationStyle.VerticalSwipe
}

sealed class ReviewNavigationStyle {
    /** Instagram/Spotify-Wrapped style vertical swipe — the current Default behavior. */
    data object VerticalSwipe : ReviewNavigationStyle()

    /** Vertical swipe with terminal-styled chrome (CRT background, glitch overlay) — used by the Terminal template. */
    data object TerminalSwipe : ReviewNavigationStyle()

    /** Horizontal page-turn — used by the Book template. */
    data object HorizontalPageFlip : ReviewNavigationStyle()

    /**
     * Hands-free continuous vertical scroll, like reading a newspaper — no swipe required.
     * See [com.ilustris.sagai.ui.animations.AutoScrollLazyColumn]. Used by Fantasy and Shinobi's
     * shared Book template. Planned for Cowboy too (see
     * `docs/feature_planning/wrapped_themed_render/implementation_status.md`).
     */
    data object ContinuousScroll : ReviewNavigationStyle()

    /**
     * The review as the pages of a printed comic: panels are cut into pages (see `paginate`), the
     * reader turns them by hand or an edge tap, and each page plays its own entrance as it
     * settles. Opens on a cover, closes on a back cover, and turns itself after a pause. Used by
     * Heroes.
     */
    data object ComicPages : ReviewNavigationStyle()

    /**
     * The saga spread out as photos on a table, panning steadily past: the saga icon, chapter
     * stills and character portraits laid along one long horizontal strip, threaded together by a
     * red string — the travelling line across a map an Indiana Jones intro opens on, rather than
     * another simulated conversation.
     *
     * Deliberately *not* a camera that flies between frames. It started as one, and the two genres
     * ended up moving identically; a constant, un-zooming drift is what makes this read as leafing
     * through an album instead of reading a comic page. Replaces Crime's old simulated-iMessage
     * thread template. Used by Crime.
     */
    data object Corkboard : ReviewNavigationStyle()
}

sealed class ReviewAction {
    data class Share(
        val shareType: ShareType,
    ) : ReviewAction()

    data object Continue : ReviewAction()

    data object Finish : ReviewAction()

    data object Restart : ReviewAction()

    data object Regenerate : ReviewAction()

    data class Navigate(
        val pageType: ReviewPageType,
    ) : ReviewAction()
}
