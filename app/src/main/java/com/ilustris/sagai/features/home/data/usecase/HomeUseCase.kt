package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.services.BillingState
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface HomeUseCase {
    val billingState: MutableStateFlow<BillingState?>

    fun getSagas(): Flow<List<SagaContent>>

    suspend fun requestDynamicCall(): RequestResult<DynamicSagaPrompt>

    suspend fun createFakeSaga(): RequestResult<Saga>

    suspend fun checkDebugBuild(): Boolean

    suspend fun checkBackups(): RequestResult<List<SagaContent>>

    fun backupEnabled(): Flow<Boolean>

    suspend fun recoverSaga(sagaContent: SagaContent): RequestResult<SagaContent>
}
