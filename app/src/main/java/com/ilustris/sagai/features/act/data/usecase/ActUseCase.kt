package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import kotlinx.coroutines.flow.Flow

interface ActUseCase {
    fun getActsBySagaId(sagaId: Int): Flow<List<Act>>

    suspend fun saveAct(act: Act): Act

    suspend fun updateAct(act: Act): Act

    suspend fun deleteAct(act: Act)

    suspend fun deleteActsForSaga(sagaId: Int)

    suspend fun generateAct(
        saga: SagaMetadata,
        actContent: com.ilustris.sagai.features.home.data.model.ActMetadata,
    ): RequestResult<Act>

    fun generateActStream(
        saga: SagaMetadata,
        actContent: com.ilustris.sagai.features.home.data.model.ActMetadata,
    ): Flow<StreamingState<GeneratedContent<Act>>>

    suspend fun generateActIntroduction(
        saga: SagaMetadata,
        act: Act,
    ): RequestResult<GeneratedContent<Act>>

    fun generateActIntroductionStream(
        saga: SagaMetadata,
        act: Act,
    ): Flow<StreamingState<GeneratedContent<Act>>>

    fun synthesizeActEvolutionStream(
        saga: SagaContent,
        actContent: ActContent,
    ): Flow<StreamingState<GeneratedContent<Act>>>
}
