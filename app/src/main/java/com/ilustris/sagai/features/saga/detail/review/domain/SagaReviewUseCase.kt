package com.ilustris.sagai.features.saga.detail.review.domain

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.usecase.ReviewState
import kotlinx.coroutines.flow.Flow

interface SagaReviewUseCase {
    suspend fun createReview(content: SagaContent): Flow<ReviewState>
}
