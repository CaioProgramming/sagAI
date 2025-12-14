package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.stories.data.model.StoryDailyBriefing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface HomeUseCase {
    val billingState: MutableStateFlow<BillingService.BillingState?>

    fun getSagas(): Flow<List<SagaContent>>

    suspend fun requestDynamicCall(): RequestResult<DynamicSagaPrompt>

    suspend fun createFakeSaga(): RequestResult<Saga>

    suspend fun checkDebugBuild(): Boolean

    suspend fun recoverSaga(sagaContent: RestorableSaga): RequestResult<SagaContent>

    suspend fun generateStoryBriefing(saga: SagaContent): RequestResult<StoryDailyBriefing>
}
