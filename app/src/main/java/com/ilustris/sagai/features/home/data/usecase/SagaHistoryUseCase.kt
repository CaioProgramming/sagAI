package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt // Added import
import com.ilustris.sagai.features.timeline.data.model.LoreGen
import kotlinx.coroutines.flow.Flow

interface SagaHistoryUseCase {
    fun getSagas(): Flow<List<SagaContent>>

    suspend fun getSagaById(sagaId: Int): Flow<SagaContent?>

    suspend fun updateSaga(saga: Saga): Saga

    suspend fun generateLore(
        saga: SagaContent,
        loreReference: Int,
        lastMessages: List<String>,
    ): RequestResult<Exception, LoreGen>

    suspend fun createFakeSaga(): RequestResult<Exception, Saga>

    suspend fun generateEndMessage(saga: SagaContent): RequestResult<Exception, String>

    suspend fun fetchDynamicNewSagaTexts(): DynamicSagaPrompt? // Added function
}
