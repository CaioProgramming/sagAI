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
     * One comic page instead of a sequence of screens: every page is a frame laid out on a single
     * board, and a camera flies from frame to frame. It plays through on its own, then settles on
     * the whole page — from there a tap flies to a frame, a second tap pulls back out, and swipes
     * walk the reading order. Free pan/zoom was tried first and made the page easy to lose;
     * snapping the camera to a target keeps every gesture landing somewhere legible. Used by
     * Heroes.
     */
    data object ComicBoard : ReviewNavigationStyle()

    /**
     * The saga spread out as photos on a table, panning steadily past: the saga icon, chapter
     * stills and character portraits laid along one long horizontal strip, threaded together by a
     * red string — the travelling line across a map an Indiana Jones intro opens on, rather than
     * another simulated conversation.
     *
     * Deliberately *not* [ComicBoard]'s camera. It started as a reuse of it, and the two genres
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
