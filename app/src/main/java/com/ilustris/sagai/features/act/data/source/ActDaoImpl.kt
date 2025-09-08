package com.ilustris.sagai.features.act.data.source

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent // Import ActContent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.getValue

class ActDaoImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : ActDao {
        private val actDao by lazy {
            database.actDao()
        }

        override suspend fun insert(act: Act): Long = actDao.insert(act)

        override suspend fun insertAll(acts: List<Act>) = actDao.insertAll(acts)

        override suspend fun update(act: Act) = actDao.update(act)

        override suspend fun delete(act: Act) = actDao.delete(act)

        override fun getActsForSaga(sagaId: Int): Flow<List<Act>> = actDao.getActsForSaga(sagaId)

        override suspend fun deleteActsForSaga(sagaId: Int) = actDao.deleteActsForSaga(sagaId)

        override fun getActContentsForSaga(sagaId: Int): Flow<List<ActContent>> = actDao.getActContentsForSaga(sagaId)

        override fun getActContent(actId: Int): Flow<ActContent?> = actDao.getActContent(actId) // Added implementation
    }
