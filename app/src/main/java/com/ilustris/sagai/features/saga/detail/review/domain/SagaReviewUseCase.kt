package com.ilustris.sagai.features.saga.detail.review.domain

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.features.saga.detail.data.usecase.ReviewState
import com.ilustris.sagai.features.saga.detail.review.domain.model.ReviewSteps
import kotlinx.coroutines.flow.Flow

interface SagaReviewUseCase {
    suspend fun createReview(content: SagaContent): Flow<ReviewState>

    suspend fun generateStep(
        content: SagaContent,
        step: ReviewSteps,
        existingReview: Review? = content.data.review,
    ): Flow<ReviewState>
}
