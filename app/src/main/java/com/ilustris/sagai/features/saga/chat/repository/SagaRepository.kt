package com.ilustris.sagai.features.saga.chat.repository

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow

interface SagaRepository {
    fun getChats(): Flow<List<SagaContent>>

    fun getSagaById(id: Int): Flow<SagaContent?>

    suspend fun saveChat(sagaData: SagaData): SagaData

    suspend fun updateChat(sagaData: SagaData) : SagaData

    suspend fun deleteChat(sagaData: SagaData)

    suspend fun deleteChatById(id: String)

    suspend fun deleteAllChats()
}
