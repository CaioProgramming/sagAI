package com.ilustris.sagai.features.saga.detail.review.ui

interface ReviewExperience {
    val pages: List<ReviewPage>
}

sealed class ReviewAction {
    /** Shares the page the reader is on, rendered as a card. */
    data object Share : ReviewAction()

    data object Continue : ReviewAction()

    data object Finish : ReviewAction()

    data object Restart : ReviewAction()

    data object Regenerate : ReviewAction()

    data class Navigate(
        val pageType: ReviewPageType,
    ) : ReviewAction()
}
