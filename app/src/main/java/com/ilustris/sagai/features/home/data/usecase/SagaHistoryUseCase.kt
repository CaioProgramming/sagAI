package com.ilustris.sagai.features.home.data.usecase

import android.net.Uri
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow

interface SagaHistoryUseCase {
    suspend fun getSagaById(sagaId: Int): Flow<SagaContent?>

    suspend fun updateSaga(saga: Saga): Saga

    suspend fun createFakeSaga(): RequestResult<Saga>

    suspend fun generateEndMessage(saga: SagaContent): RequestResult<String>

    fun generateEndMessageStream(
        saga: SagaContent,
    ): Flow<com.ilustris.sagai.core.ai.StreamingState<com.ilustris.sagai.core.ai.model.GeneratedContent<String>>>

    fun generateSagaEndingStream(
        saga: SagaContent,
    ): Flow<com.ilustris.sagai.core.ai.StreamingState<com.ilustris.sagai.core.ai.model.GeneratedContent<com.ilustris.sagai.features.home.data.model.SagaEnding>>>

    suspend fun backupSaga(saga: SagaContent): RequestResult<RequestResult<Uri>>
}
