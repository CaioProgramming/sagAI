package com.ilustris.sagai.features.saga.detail.ui

import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.wiki.data.model.Wiki

sealed class DetailAction {
    data object Back : DetailAction()

    data object OpenReview : DetailAction()

    data object OpenEmotionalReview : DetailAction()

    data class OpenChronicles(
        val actId: Int? = null,
    ) : DetailAction()

    data object Delete : DetailAction()

    data object RegenerateIcon : DetailAction()

    data class ReviewWiki(
        val wikis: List<Wiki>,
    ) : DetailAction()

    data class ReviewEvent(
        val event: TimelineContent,
    ) : DetailAction()

    data class OpenSection(
        val section: RequestSection,
    ) : DetailAction()
}
