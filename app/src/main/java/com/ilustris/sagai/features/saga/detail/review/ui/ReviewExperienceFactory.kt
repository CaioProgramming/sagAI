package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.home.data.model.SagaContent

object ReviewExperienceFactory {
    fun createExperience(
        content: SagaContent,): ReviewExperience = DefaultReviewExperience(content)
}
