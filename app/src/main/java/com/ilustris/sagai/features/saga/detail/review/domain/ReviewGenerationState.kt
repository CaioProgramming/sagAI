package com.ilustris.sagai.features.saga.detail.review.domain

import com.ilustris.sagai.features.saga.detail.review.domain.model.ReviewSteps

sealed class ReviewGenerationState {
    data object Idle : ReviewGenerationState()

    data class Generating(
        val step: ReviewSteps?,
        val reasoning: String?,
        val completedCount: Int,
        val totalSteps: Int = ReviewSteps.entries.size,
    ) : ReviewGenerationState()

    data object Complete : ReviewGenerationState()

    data class Error(
        val message: String,
        val step: ReviewSteps?,
    ) : ReviewGenerationState()
}
