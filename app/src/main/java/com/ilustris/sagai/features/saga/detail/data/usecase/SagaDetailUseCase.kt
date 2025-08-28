package com.ilustris.sagai.features.saga.detail.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import kotlinx.coroutines.flow.Flow

interface SagaDetailUseCase {
    suspend fun regenerateSagaIcon(saga: SagaContent): RequestResult<Exception, Saga>

    suspend fun fetchSaga(sagaId: Int): Flow<SagaContent?>

    suspend fun deleteSaga(saga: Saga)

    suspend fun createReview(content: SagaContent): RequestResult<Exception, Saga>

    suspend fun resetReview(content: SagaContent)

    suspend fun createEmotionalReview(content: SagaContent): RequestResult<Exception, Saga>

    suspend fun createTimelineReview(
        content: SagaContent,
        timelineContent: TimelineContent,
    ): RequestResult<Exception, Unit>
}
