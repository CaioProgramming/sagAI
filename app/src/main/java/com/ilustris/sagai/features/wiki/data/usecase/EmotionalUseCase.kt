package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaEnding
import com.ilustris.sagai.features.timeline.data.model.Timeline
import kotlinx.coroutines.flow.Flow

interface EmotionalUseCase {
    suspend fun generateEmotionalConclusion(sagaContent: SagaContent): RequestResult<SagaEnding>

    suspend fun getEmotionalCard(saga: Saga): RequestResult<String>

    suspend fun getEmotionalMascot(
        sagaContent: Saga,
        timelineContent: Timeline?,
    ): RequestResult<String>

    fun streamEmotionalConclusion(sagaContent: SagaContent): Flow<StreamingState<SagaEnding>>
}
