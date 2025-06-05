package com.ilustris.sagai.features.chat.data

import com.ilustris.sagai.core.database.DatabaseBuilder
import com.ilustris.sagai.features.home.data.model.SagaData
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

        override fun getAllSagas(): Flow<List<SagaData>> = sagaDao.getAllSagas()

        override fun getSaga(sagaId: Int) = sagaDao.getSaga(sagaId)

        override suspend fun saveSagaData(sagaData: SagaData): Long = sagaDao.saveSagaData(sagaData)

        override suspend fun deleteSagaData(sagaData: SagaData) = sagaDao.deleteSagaData(sagaData)

        override suspend fun deleteSagaData(sagaId: String) = sagaDao.deleteSagaData(sagaId)

        override suspend fun deleteAllSagas() = sagaDao.deleteAllSagas()
    }
