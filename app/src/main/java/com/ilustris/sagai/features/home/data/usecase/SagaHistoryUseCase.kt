package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.timeline.data.model.LoreGen
import kotlinx.coroutines.flow.Flow

interface SagaHistoryUseCase {
    fun getSagas(): Flow<List<SagaContent>>

    suspend fun getSagaById(sagaId: Int): Flow<SagaContent?>

    suspend fun updateSaga(saga: SagaData): SagaData

    suspend fun generateLore(
        saga: SagaContent,
        loreReference: Int,
        lastMessages: List<String>,
    ): RequestResult<Exception, LoreGen>
}
