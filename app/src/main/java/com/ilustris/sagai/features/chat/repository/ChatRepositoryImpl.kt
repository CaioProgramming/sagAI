package com.ilustris.sagai.features.chat.repository

import com.ilustris.sagai.features.chat.data.ChatDao
import com.ilustris.sagai.features.home.data.model.ChatData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepositoryImpl
    @Inject
    constructor(
        private val chatDao: ChatDao,
    ) : ChatRepository {
        override fun getChats(): Flow<List<ChatData>> = chatDao.getAllChats()

        override fun getChatById(id: String): Flow<ChatData?> = chatDao.getChat(id)

        override suspend fun saveChat(chatData: ChatData): Long = chatDao.saveChatData(chatData)

        override suspend fun deleteChat(chatData: ChatData) = chatDao.deleteChatData(chatData)

        override suspend fun deleteChatById(id: String) = chatDao.deleteChatData(id)

        override suspend fun deleteAllChats() = chatDao.deleteAllChats()
    }
