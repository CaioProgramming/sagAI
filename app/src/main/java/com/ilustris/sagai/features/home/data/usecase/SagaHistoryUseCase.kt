package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow

interface SagaHistoryUseCase {
    fun getSagas(): Flow<List<SagaData>>

    suspend fun getSagaById(sagaId: Int): RequestResult<Exception, SagaData>
}
