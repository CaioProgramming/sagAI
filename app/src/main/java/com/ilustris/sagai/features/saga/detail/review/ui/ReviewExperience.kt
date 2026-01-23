package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.share.domain.model.ShareType

interface ReviewExperience {
    val pages: List<ReviewPage>
}

sealed class ReviewAction {
    data class Share(
        val shareType: ShareType,
    ) : ReviewAction()

    data object Continue : ReviewAction()

    data object Finish : ReviewAction()

    data object Restart : ReviewAction()
}
