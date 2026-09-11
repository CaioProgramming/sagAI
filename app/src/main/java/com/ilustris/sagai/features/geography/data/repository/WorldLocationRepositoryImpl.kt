package com.ilustris.sagai.features.geography.data.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.geography.data.model.WorldLocation
import com.ilustris.sagai.features.geography.data.model.WorldLocationVisit
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorldLocationRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : WorldLocationRepository {
        private val worldLocationDao by lazy { database.worldLocationDao() }

        override fun getBySaga(sagaId: Int): Flow<List<WorldLocation>> = worldLocationDao.getBySaga(sagaId)

        override suspend fun findByName(
            sagaId: Int,
            name: String,
        ): WorldLocation? = worldLocationDao.findByName(sagaId, name)

        override suspend fun getById(id: Int): WorldLocation? = worldLocationDao.getById(id)

        override suspend fun getRootForSaga(sagaId: Int): WorldLocation? = worldLocationDao.getRootForSaga(sagaId)

        override suspend fun insert(location: WorldLocation): WorldLocation {
            val id = worldLocationDao.insert(location.copy(id = 0)).toInt()
            return location.copy(id = id)
        }

        override suspend fun update(location: WorldLocation): WorldLocation {
            worldLocationDao.update(location)
            return location
        }

        override suspend fun recordVisit(visit: WorldLocationVisit) {
            worldLocationDao.insertVisit(visit.copy(id = 0))
        }

        override fun getVisitsForSaga(sagaId: Int): Flow<List<WorldLocationVisit>> = worldLocationDao.getVisitsForSaga(sagaId)
    }
