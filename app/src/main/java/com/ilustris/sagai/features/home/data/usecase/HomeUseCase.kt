package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow

interface HomeUseCase {
    fun getSagas(): Flow<List<SagaContent>>

    suspend fun fetchDynamicNewSagaTexts(): RequestResult<Exception, DynamicSagaPrompt>

    suspend fun createFakeSaga(): RequestResult<Exception, Saga> // Added this line
}
