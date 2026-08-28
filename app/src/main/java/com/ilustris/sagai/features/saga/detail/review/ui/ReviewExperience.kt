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
     * A detective's corkboard instead of a chat thread: the saga icon, chapter stills and
     * character portraits are pinned across one board, connected by a hand-drawn route line that
     * grows as the camera visits each pin — the travelling red line across a map, the way an
     * Indiana Jones intro opens a journey, rather than another simulated conversation. Shares
     * [ComicBoard]'s camera model (auto-plays pin to pin, a tap flies to one or pulls back to the
     * overview, swipes walk the order) since that already reads as a distinct set piece rather
     * than more chat. Replaces Crime's old simulated-iMessage-thread template.
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
