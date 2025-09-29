package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.services.BillingState
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface HomeUseCase {
    fun getSagas(): Flow<List<SagaContent>>

    suspend fun fetchDynamicNewSagaTexts(): RequestResult<DynamicSagaPrompt>

    suspend fun createFakeSaga(): RequestResult<Saga>

    val billingState: MutableStateFlow<BillingState?>
}
