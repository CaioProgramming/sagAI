package com.ilustris.sagai.features.chat.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow

interface MessageUseCase {
    suspend fun getMessages(sagaId: Int): Flow<List<Message>>

    suspend fun saveMessage(message: Message): Long

    suspend fun deleteMessage(messageId: Long)

    suspend fun generateIntroMessage(saga: SagaData): RequestResult<Exception, Message>

    suspend fun generateMessage(
        saga: SagaData,
        message: Message,
        lastMessages: List<Message>,
    ): RequestResult<Exception, Message>

    suspend fun generateNarratorBreak(
        data: SagaData,
        messages: List<Message>,
    ): RequestResult<Exception, Message>
}
