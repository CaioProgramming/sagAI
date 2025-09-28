package com.ilustris.sagai.features.act.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow

interface ActUseCase {
    fun getActsBySagaId(sagaId: Int): Flow<List<Act>>

    suspend fun saveAct(act: Act): Act

    suspend fun updateAct(act: Act): Act

    suspend fun deleteAct(act: Act)

    suspend fun deleteActsForSaga(sagaId: Int)

    suspend fun generateAct(saga: SagaContent): RequestResult<Act>

    suspend fun generateActIntroduction(
        saga: SagaContent,
        act: Act,
    ): RequestResult<Act>

    fun getActContent(actId: Int): Flow<ActContent?>
}
