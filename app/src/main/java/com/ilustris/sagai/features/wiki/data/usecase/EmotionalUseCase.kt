package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent

interface EmotionalUseCase {
    suspend fun generateEmotionalReview(
        content: List<String>,
        emotionalRanking: Map<String, Int>,
    ): RequestResult<String>

    suspend fun generateEmotionalProfile(saga: SagaContent): RequestResult<String>
}
