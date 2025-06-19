package com.ilustris.sagai.features.saga.detail.data.usecase

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow

interface SagaDetailUseCase {
    suspend fun fetchSaga(sagaId: Int): Flow<SagaContent?>

    suspend fun deleteSaga(saga: SagaData)
}
