package com.ilustris.sagai.features.act.data.repository

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent // Import ActContent
import kotlinx.coroutines.flow.Flow

interface ActRepository {
    fun getActsBySagaId(sagaId: Int): Flow<List<Act>>
    suspend fun saveAct(act: Act): Act
    suspend fun updateAct(act: Act): Act
    suspend fun deleteAct(act: Act)
    suspend fun deleteActsForSaga(sagaId: Int)

    // New method
    fun getActContent(actId: Int): Flow<ActContent?>
}
