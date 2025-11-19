package com.ilustris.sagai.features.saga.detail.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.wiki.data.model.Wiki
import kotlinx.coroutines.flow.Flow

interface SagaDetailUseCase {
    suspend fun regenerateSagaIcon(saga: SagaContent): RequestResult<Saga>

    suspend fun fetchSaga(sagaId: Int): Flow<SagaContent?>

    suspend fun deleteSaga(saga: Saga)

    suspend fun createReview(content: SagaContent): RequestResult<Saga>

    suspend fun resetReview(content: SagaContent)

    suspend fun createEmotionalConclusion(currentSaga: SagaContent): RequestResult<Saga>

    suspend fun generateTimelineContent(
        saga: SagaContent,
        timelineContent: TimelineContent,
    ): RequestResult<Unit>

    suspend fun reviewWiki(
        currentsaga: SagaContent,
        wikis: List<Wiki>,
    )
}
