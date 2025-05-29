package com.ilustris.sagai.features.chat.repository

import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow

interface SagaRepository {
    fun getChats(): Flow<List<SagaData>>

    fun getChatById(id: String): Flow<SagaData?>

    suspend fun saveChat(sagaData: SagaData): Long

    suspend fun deleteChat(sagaData: SagaData)

    suspend fun deleteChatById(id: String)

    suspend fun deleteAllChats()
}
