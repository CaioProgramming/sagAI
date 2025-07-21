package com.ilustris.sagai.features.saga.datasource

import com.ilustris.sagai.core.database.DatabaseBuilder
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SagaDaoImpl
    @Inject
    constructor(
        private val databaseBuilder: DatabaseBuilder,
    ) : SagaDao {
        private val sagaDao by lazy {
            databaseBuilder.buildDataBase().sagaDao()
        }

        override fun getAllSagas(): Flow<List<Saga>> = sagaDao.getAllSagas()

        override fun getSaga(sagaId: Int) = sagaDao.getSaga(sagaId)

        override suspend fun saveSagaData(saga: Saga): Long = sagaDao.saveSagaData(saga)

        override suspend fun updateSaga(saga: Saga) = sagaDao.updateSaga(saga)

        override suspend fun deleteSagaData(saga: Saga) = sagaDao.deleteSagaData(saga)

        override suspend fun deleteSagaData(sagaId: String) = sagaDao.deleteSagaData(sagaId)

        override suspend fun deleteAllSagas() = sagaDao.deleteAllSagas()

        override fun getSagaContent(sagaId: Int): Flow<SagaContent> = sagaDao.getSagaContent(sagaId)

        override fun getSagaContent() = sagaDao.getSagaContent()
    }
