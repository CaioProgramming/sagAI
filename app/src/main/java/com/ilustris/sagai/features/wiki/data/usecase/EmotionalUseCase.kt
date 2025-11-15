package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.data.RequestResult

interface EmotionalUseCase {
    suspend fun generateEmotionalReview(
        content: List<String>,
        emotionalRanking: Map<String, Int>,
    ): RequestResult<String>

    suspend fun generateEmotionalProfile(emotionalSummary: List<String>): RequestResult<String>
}
