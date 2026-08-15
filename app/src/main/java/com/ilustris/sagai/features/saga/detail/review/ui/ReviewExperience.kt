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
     * A simulated live chat: messages reveal one at a time (typing-pause timed), each new one
     * forcing the list to scroll to it — pinned to the latest message, like watching a
     * conversation arrive in real time, rather than [ContinuousScroll]'s hands-free drift through
     * already-laid-out content. The user can still scroll up freely; the next revealed message
     * just pulls the view back down. Used by Crime's iMessage-style template.
     */
    data object ChatScroll : ReviewNavigationStyle()
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
