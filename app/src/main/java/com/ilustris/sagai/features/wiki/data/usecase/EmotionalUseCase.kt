package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

interface EmotionalUseCase {
    suspend fun generateEmotionalReview(
        sagaContent: SagaContent,
        context: String,
    ): RequestResult<String>

    suspend fun generateEmotionalConclusion(sagaContent: SagaContent): RequestResult<String>

    suspend fun getEmotionalCard(sagaContent: SagaContent): RequestResult<String>

    suspend fun getEmotionalMascot(
        sagaContent: SagaContent,
        timelineContent: TimelineContent,
    ): RequestResult<String>
}
