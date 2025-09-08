package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.timeline.data.model.LoreGen
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import kotlinx.coroutines.flow.Flow

interface SagaHistoryUseCase {
    suspend fun getSagaById(sagaId: Int): Flow<SagaContent?>

    suspend fun updateSaga(saga: Saga): Saga

    suspend fun generateLore(
        saga: SagaContent,
        currentTimeline: TimelineContent,
    ): RequestResult<Exception, Timeline>

    suspend fun createFakeSaga(): RequestResult<Exception, Saga>

    suspend fun generateEndMessage(saga: SagaContent): RequestResult<Exception, String>
}
