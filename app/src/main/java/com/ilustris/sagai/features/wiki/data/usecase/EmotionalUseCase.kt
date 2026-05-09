package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.emotional.data.model.EmotionalProfile
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaEnding
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import kotlinx.coroutines.flow.Flow

interface EmotionalUseCase {
    suspend fun generateEmotionalReview(
        sagaContent: SagaContent,
        context: String,
    ): RequestResult<EmotionalProfile>

    suspend fun generateEmotionalConclusion(sagaContent: SagaContent): RequestResult<SagaEnding>

    suspend fun getEmotionalCard(sagaContent: SagaContent): RequestResult<String>

    suspend fun getEmotionalMascot(
        sagaContent: SagaContent,
        timelineContent: TimelineContent,
    ): RequestResult<String>

    fun streamEmotionalReview(
        sagaContent: SagaContent,
        context: String,
    ): Flow<StreamingState<EmotionalProfile>>

    fun streamEmotionalConclusion(sagaContent: SagaContent): Flow<StreamingState<SagaEnding>>

    fun streamEmotionalCard(sagaContent: SagaContent): Flow<StreamingState<String>>

    fun streamEmotionalMascot(
        sagaContent: SagaContent,
        timelineContent: TimelineContent,
    ): Flow<StreamingState<String>>
}
