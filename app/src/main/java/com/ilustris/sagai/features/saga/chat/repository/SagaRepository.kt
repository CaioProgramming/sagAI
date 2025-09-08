package com.ilustris.sagai.features.saga.chat.repository

import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import kotlinx.coroutines.flow.Flow

interface SagaRepository {
    fun getChats(): Flow<List<SagaContent>>

    fun getSagaById(id: Int): Flow<SagaContent?>

    suspend fun saveChat(saga: Saga): Saga

    suspend fun updateChat(saga: Saga): Saga

    suspend fun deleteChat(saga: Saga)

    suspend fun deleteChatById(id: String)

    suspend fun deleteAllChats()
}
