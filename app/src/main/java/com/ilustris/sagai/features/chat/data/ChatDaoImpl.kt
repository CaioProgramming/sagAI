package com.ilustris.sagai.features.chat.data

import com.ilustris.sagai.core.database.DatabaseBuilder
import com.ilustris.sagai.features.home.data.model.ChatData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatDaoImpl
    @Inject
    constructor(
        private val databaseBuilder: DatabaseBuilder,
    ) : ChatDao {
        override fun getAllChats(): Flow<List<ChatData>> = databaseBuilder.database.chatDao().getAllChats()

        override fun getChat(chatId: String): Flow<ChatData> = databaseBuilder.database.chatDao().getChat(chatId)

        override suspend fun saveChatData(chatData: ChatData) = databaseBuilder.database.chatDao().saveChatData(chatData)

        override suspend fun deleteChatData(chatData: ChatData) = databaseBuilder.database.chatDao().deleteChatData(chatData)

        override suspend fun deleteChatData(chatId: String) = databaseBuilder.database.chatDao().deleteChatData(chatId)

        override suspend fun deleteAllChats() = databaseBuilder.database.chatDao().deleteAllChats()
    }
