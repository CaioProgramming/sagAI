package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.emotional.data.model.EmotionalProfile
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

interface EmotionalUseCase {
    suspend fun generateEmotionalReview(
        sagaContent: SagaContent,
        context: String,
    ): RequestResult<EmotionalProfile>

    suspend fun generateEmotionalConclusion(sagaContent: SagaContent): RequestResult<EmotionalProfile>

    suspend fun getEmotionalCard(sagaContent: SagaContent): RequestResult<String>

    suspend fun getEmotionalMascot(
        sagaContent: SagaContent,
        timelineContent: TimelineContent,
    ): RequestResult<String>
}
