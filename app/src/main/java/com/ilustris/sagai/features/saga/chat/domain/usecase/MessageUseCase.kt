package com.ilustris.sagai.features.saga.chat.domain.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageGen
import com.ilustris.sagai.features.timeline.data.model.Timeline
import kotlinx.coroutines.flow.Flow

interface MessageUseCase {
    suspend fun getMessages(sagaId: Int): Flow<List<MessageContent>>

    suspend fun getMessageDetail(id: Int): MessageContent

    suspend fun saveMessage(message: Message): RequestResult<Exception, Message>

    suspend fun deleteMessage(messageId: Long)

    suspend fun getLastMessage(sagaId: Int): Message?

    suspend fun generateIntroMessage(
        saga: Saga,
        character: Character?,
    ): RequestResult<Exception, Message>

    suspend fun generateEndingMessage(content: SagaContent): RequestResult<Exception, Message>

    suspend fun generateMessage(
        saga: SagaContent,
        chapter: Chapter?,
        lastEvents: List<Timeline>,
        message: Pair<String, String>,
        lastMessages: List<Pair<String, String>>,
        directive: String,
    ): RequestResult<Exception, MessageGen>

    suspend fun updateMessage(message: Message): RequestResult<Exception, Unit>

    fun setDebugMode(enabled: Boolean)

    fun isInDebugMode(): Boolean
}
