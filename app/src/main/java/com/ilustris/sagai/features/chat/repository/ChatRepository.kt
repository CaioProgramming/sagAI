package com.ilustris.sagai.features.chat.repository

import com.ilustris.sagai.features.home.data.model.ChatData
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<ChatData>>

    fun getChatById(id: String): Flow<ChatData?>

    suspend fun saveChat(chatData: ChatData): Long

    suspend fun deleteChat(chatData: ChatData)

    suspend fun deleteChatById(id: String)

    suspend fun deleteAllChats()
}
