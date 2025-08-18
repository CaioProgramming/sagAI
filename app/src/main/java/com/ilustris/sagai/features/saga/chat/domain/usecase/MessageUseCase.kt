package com.ilustris.sagai.features.saga.chat.domain.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.MessageGen
import kotlinx.coroutines.flow.Flow

interface MessageUseCase {
    suspend fun getMessages(sagaId: Int): Flow<List<MessageContent>>

    suspend fun saveMessage(message: Message): RequestResult<Exception, Message>

    suspend fun deleteMessage(messageId: Long)

    suspend fun getLastMessage(sagaId: Int): Message?

    suspend fun generateIntroMessage(saga: SagaContent): RequestResult<Exception, String>


    suspend fun generateMessage(
        saga: SagaContent,
        message: MessageContent,
    ): RequestResult<Exception, MessageGen>

    suspend fun updateMessage(message: Message): RequestResult<Exception, Unit>

    fun setDebugMode(enabled: Boolean)

    fun isInDebugMode(): Boolean
}
