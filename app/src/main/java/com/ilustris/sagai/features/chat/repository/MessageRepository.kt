package com.ilustris.sagai.features.chat.repository

import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.MessageContent
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun getMessages(sagaId: Int): Flow<List<MessageContent>>

    suspend fun getMessageDetail(id: Int): MessageContent

    suspend fun saveMessage(message: Message): Long

    suspend fun deleteMessage(messageId: Long)

    suspend fun deleteMessages(sagaId: String)

    suspend fun updateMessage(message: Message)

    suspend fun getLastMessage(sagaId: Int): Message?
}
