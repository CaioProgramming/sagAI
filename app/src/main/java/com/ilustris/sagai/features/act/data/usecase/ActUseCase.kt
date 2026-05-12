package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.act.data.model.Act
import kotlinx.coroutines.flow.Flow

interface ActUseCase {
    fun getActsBySagaId(sagaId: Int): Flow<List<Act>>

    suspend fun saveAct(act: Act): Act

    suspend fun updateAct(act: Act): Act

    suspend fun deleteAct(act: Act)

    suspend fun deleteActsForSaga(sagaId: Int)

    suspend fun generateAct(
        saga: com.ilustris.sagai.features.home.data.model.SagaMetadata,
        actContent: com.ilustris.sagai.features.home.data.model.ActMetadata,
    ): RequestResult<Act>

    fun generateActStream(
        saga: com.ilustris.sagai.features.home.data.model.SagaMetadata,
        actContent: com.ilustris.sagai.features.home.data.model.ActMetadata,
    ): Flow<com.ilustris.sagai.core.ai.StreamingState<com.ilustris.sagai.core.ai.model.GeneratedContent<Act>>>

    suspend fun generateActIntroduction(
        saga: com.ilustris.sagai.features.home.data.model.SagaMetadata,
        act: Act,
    ): RequestResult<com.ilustris.sagai.core.ai.model.GeneratedContent<Act>>

    fun generateActIntroductionStream(
        saga: com.ilustris.sagai.features.home.data.model.SagaMetadata,
        act: Act,
    ): Flow<com.ilustris.sagai.core.ai.StreamingState<com.ilustris.sagai.core.ai.model.GeneratedContent<Act>>>

    fun synthesizeActEvolutionStream(
        saga: com.ilustris.sagai.features.home.data.model.SagaMetadata,
        actContent: com.ilustris.sagai.features.home.data.model.ActMetadata,
    ): Flow<com.ilustris.sagai.core.ai.StreamingState<com.ilustris.sagai.core.ai.model.GeneratedContent<Act>>>
}
