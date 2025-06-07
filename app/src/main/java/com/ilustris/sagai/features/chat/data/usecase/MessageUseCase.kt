package com.ilustris.sagai.features.chat.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.MessageContent
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow

interface MessageUseCase {
    suspend fun getMessages(sagaId: Int): Flow<List<MessageContent>>

    suspend fun getMessageDetail(id: Int): MessageContent

    suspend fun saveMessage(message: Message): Long

    suspend fun deleteMessage(messageId: Long)

    suspend fun getLastMessage(sagaId: Int): Message?

    suspend fun generateIntroMessage(
        saga: SagaData,
        character: Character?,
    ): RequestResult<Exception, Message>

    suspend fun generateMessage(
        saga: SagaData,
        message: Pair<String, String>,
        mainCharacter: Character,
        lastMessages: List<Pair<String, String>>,
        characters: List<Character>,
    ): RequestResult<Exception, Message>

    suspend fun generateNarratorBreak(
        data: SagaData,
        messages: List<String>,
    ): RequestResult<Exception, Message>
}
