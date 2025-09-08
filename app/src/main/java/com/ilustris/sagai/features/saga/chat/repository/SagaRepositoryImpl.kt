package com.ilustris.sagai.features.saga.chat.repository

import android.icu.util.Calendar
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.datasource.SagaDao
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

        override fun getChats(): Flow<List<SagaContent>> = sagaDao.getSagaContent()

        override fun getSagaById(id: Int) = sagaDao.getSagaContent(id)

        override suspend fun saveChat(saga: Saga) =
            saga.copy(id = sagaDao.saveSagaData(saga.copy(createdAt = Calendar.getInstance().timeInMillis)).toInt())

        override suspend fun updateChat(saga: Saga): Saga {
            sagaDao.updateSaga(saga)
            return saga
        }

        override suspend fun deleteChat(saga: Saga) = sagaDao.deleteSagaData(saga)

        override suspend fun deleteChatById(id: String) = sagaDao.deleteSagaData(id)

        override suspend fun deleteAllChats() = sagaDao.deleteAllSagas()
    }
