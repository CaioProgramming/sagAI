package com.ilustris.sagai.features.saga.chat.repository

import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun getMessages(sagaId: Int): Flow<List<MessageContent>>

    suspend fun saveMessage(message: Message): Message?

    suspend fun deleteMessage(messageId: Long)

    suspend fun deleteMessages(sagaId: String)

    suspend fun updateMessage(message: Message)

    suspend fun getLastMessage(sagaId: Int): Message?
}
