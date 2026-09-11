package com.ilustris.sagai.features.geography.data.repository

import com.ilustris.sagai.features.geography.data.model.WorldLocation
import com.ilustris.sagai.features.geography.data.model.WorldLocationVisit
import kotlinx.coroutines.flow.Flow

interface WorldLocationRepository {
    fun getBySaga(sagaId: Int): Flow<List<WorldLocation>>

    suspend fun findByName(
        sagaId: Int,
        name: String,
    ): WorldLocation?

    suspend fun getById(id: Int): WorldLocation?

    suspend fun getRootForSaga(sagaId: Int): WorldLocation?

    suspend fun insert(location: WorldLocation): WorldLocation

    suspend fun update(location: WorldLocation): WorldLocation

    suspend fun recordVisit(visit: WorldLocationVisit)

    fun getVisitsForSaga(sagaId: Int): Flow<List<WorldLocationVisit>>
}
