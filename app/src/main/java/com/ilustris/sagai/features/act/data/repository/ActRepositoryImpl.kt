package com.ilustris.sagai.features.act.data.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.source.ActDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ActRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : ActRepository {
        private val actDao: ActDao by lazy {
            database.actDao()
        }

        override fun getActsBySagaId(sagaId: Int): Flow<List<Act>> = actDao.getActsForSaga(sagaId)

        override suspend fun saveAct(act: Act) = act.copy(id = actDao.insert(act).toInt())

        override suspend fun updateAct(act: Act): Act {
            actDao.update(act)
            return act
        }

        override suspend fun deleteAct(act: Act) = actDao.delete(act)

        override suspend fun deleteActsForSaga(sagaId: Int) = actDao.deleteActsForSaga(sagaId)

        override fun getActContent(actId: Int): Flow<ActContent?> = actDao.getActContent(actId)
    }
