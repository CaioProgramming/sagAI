package com.ilustris.sagai.features.home.data.usecase

import android.net.Uri
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaEnding
import kotlinx.coroutines.flow.Flow

interface SagaHistoryUseCase {
    suspend fun getSagaById(sagaId: Int?): Flow<SagaContent?>

    suspend fun getSagaMetadata(sagaId: Int): Flow<com.ilustris.sagai.features.home.data.model.SagaMetadata?>

    suspend fun updateSaga(saga: Saga): Saga

    suspend fun createFakeSaga(): RequestResult<Saga>

    suspend fun generateEndMessage(saga: SagaContent): RequestResult<String>

    fun generateEndMessageStream(saga: SagaContent): Flow<StreamingState<GeneratedContent<String>>>

    fun generateSagaEndingStream(saga: SagaContent): Flow<StreamingState<GeneratedContent<SagaEnding>>>

    suspend fun backupSaga(saga: SagaContent): RequestResult<RequestResult<Uri>>
}
