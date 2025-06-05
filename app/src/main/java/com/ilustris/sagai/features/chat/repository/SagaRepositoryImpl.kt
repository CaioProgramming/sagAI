package com.ilustris.sagai.features.chat.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.chat.data.SagaDao
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SagaRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : SagaRepository {
        private val sagaDao: SagaDao by lazy {
            database.sagaDao()
        }

        override fun getChats(): Flow<List<SagaData>> = sagaDao.getAllSagas()

        override fun getSagaById(id: Int) = sagaDao.getSaga(id)

        override suspend fun saveChat(sagaData: SagaData): Long = sagaDao.saveSagaData(sagaData)

        override suspend fun deleteChat(sagaData: SagaData) = sagaDao.deleteSagaData(sagaData)

        override suspend fun deleteChatById(id: String) = sagaDao.deleteSagaData(id)

        override suspend fun deleteAllChats() = sagaDao.deleteAllSagas()
    }
