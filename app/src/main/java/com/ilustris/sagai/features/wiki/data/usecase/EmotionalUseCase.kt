package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent

interface EmotionalUseCase {
    suspend fun generateEmotionalReview(
        sagaContent: SagaContent,
        context: String,
    ): RequestResult<String>

    suspend fun generateEmotionalProfile(
        sagaContent: SagaContent,
        emotionalSummary: String,
    ): RequestResult<String>

    suspend fun generateEmotionalConclusion(sagaContent: SagaContent): RequestResult<String>
}
