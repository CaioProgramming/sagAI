package com.ilustris.sagai.features.chat.data.usecase

import com.ilustris.sagai.features.chat.data.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageUseCase {
   suspend fun getMessages(sagaId: Int): Flow<List<Message>>

   suspend fun saveMessage(message: Message): Long

    suspend fun deleteMessage(messageId: Long)

}