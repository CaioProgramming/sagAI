package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import kotlinx.coroutines.flow.Flow

interface MessageUseCase {
    suspend fun getMessages(sagaId: Int): Flow<List<MessageContent>>

    suspend fun saveMessage(
        saga: SagaContent,
        message: Message,
        isFromUser: Boolean,
        sceneSummary: SceneSummary?,
    ): RequestResult<Message>

    suspend fun deleteMessage(messageId: Long)

    suspend fun getLastMessage(sagaId: Int): Message?

    suspend fun generateMessage(
        saga: SagaContent,
        message: MessageContent,
        sceneSummary: SceneSummary?,
    ): RequestResult<Message>

    suspend fun updateMessage(message: Message): RequestResult<Message>

    fun setDebugMode(enabled: Boolean)

    fun isInDebugMode(): Boolean

    suspend fun checkMessageTypo(
        saga: SagaContent,
        message: String,
    ): RequestResult<TypoFix?>

    suspend fun getSceneContext(saga: SagaContent): RequestResult<SceneSummary?>

    suspend fun generateReaction(
        saga: SagaContent,
        message: Message,
        sceneSummary: SceneSummary?,
    ): RequestResult<Unit>

    suspend fun analyzeMessageTone(
        saga: SagaContent,
        message: Message,
        isFromUser: Boolean,
    ): RequestResult<Unit>

    suspend fun generateAudio(
        saga: SagaContent,
        savedMessage: Message,
        characterReference: CharacterContent?,
    ): RequestResult<Unit>
}
