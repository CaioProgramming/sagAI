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
     * See [com.ilustris.sagai.ui.animations.AutoScrollLazyColumn]. Planned for Crime/Cowboy
     * (see `docs/feature_planning/wrapped_themed_render/implementation_status.md`); not yet
     * selected by any [com.ilustris.sagai.features.newsaga.data.model.Genre].
     */
    data object ContinuousScroll : ReviewNavigationStyle()
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
