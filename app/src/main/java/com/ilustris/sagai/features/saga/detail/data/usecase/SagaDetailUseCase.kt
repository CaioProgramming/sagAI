package com.ilustris.sagai.features.saga.detail.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.saga.detail.data.model.SagaDetailResume
import com.ilustris.sagai.features.stories.data.model.StoryDailyBriefing
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import kotlinx.coroutines.flow.Flow

sealed class ReviewState {
    data class Loading(
        val message: String,
    ) : ReviewState()

    data class Success(
        val saga: Saga,
    ) : ReviewState()

    data class Error(
        val message: String,
    ) : ReviewState()
}

interface SagaDetailUseCase {
    suspend fun regenerateSagaIcon(sagaId: Int): RequestResult<Saga>

    fun regenerateSagaIconStream(sagaId: Int): Flow<StreamingState<Saga>>

    suspend fun fetchSaga(sagaId: Int): Flow<SagaContent?>

    fun getSagaResume(sagaId: Int): Flow<SagaDetailResume>

    suspend fun deleteSaga(saga: Saga)

    suspend fun resetReview(content: SagaContent)

    suspend fun createEmotionalConclusion(sagaId: Int): RequestResult<Saga>

    suspend fun generateTimelineContent(
        saga: SagaMetadata,
        timelineContent: TimelineContent,
    ): RequestResult<Unit>

    fun getBackupEnabled(): Flow<Boolean>

    suspend fun generateStoryBriefing(sagaId: Int): RequestResult<StoryDailyBriefing>
}
