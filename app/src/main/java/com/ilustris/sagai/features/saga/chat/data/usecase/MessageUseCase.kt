package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.saga.chat.data.model.AIReply
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import kotlinx.coroutines.flow.Flow

interface MessageUseCase {
    suspend fun getMessages(sagaId: Int): Flow<List<MessageContent>>

    fun getMessagesPagingSource(sagaId: Int): androidx.paging.PagingSource<Int, MessageContent>

    fun getMessagesCount(sagaId: Int): Flow<Int>

    suspend fun saveMessage(
        saga: SagaMetadata,
        message: Message,
        isFromUser: Boolean,
        sceneSummary: SceneSummary?,
    ): RequestResult<Message>

    suspend fun deleteMessage(messageId: Long)

    suspend fun getLastMessage(sagaId: Int): Message?

    suspend fun generateMessage(
        saga: SagaMetadata,
        message: MessageContent,
        sceneSummary: SceneSummary?,
    ): Flow<StreamingState<AIReply>>

    suspend fun updateMessage(message: Message): RequestResult<Message>

    fun setDebugMode(enabled: Boolean)

    fun isInDebugMode(): Boolean

    suspend fun checkMessageTypo(
        saga: SagaMetadata,
        message: String,
    ): RequestResult<TypoFix?>

    suspend fun getSceneContext(saga: SagaMetadata): RequestResult<SceneSummary?>

    suspend fun generateReaction(
        saga: SagaMetadata,
        message: Message,
        sceneSummary: SceneSummary?,
    ): RequestResult<Unit>

    suspend fun analyzeMessageTone(
        saga: SagaMetadata,
        message: Message,
        isFromUser: Boolean,
    ): RequestResult<EmotionalTone?>

    suspend fun generateAudio(
        saga: SagaMetadata,
        savedMessage: Message,
        characterReference: com.ilustris.sagai.features.characters.data.model.Character?,
    ): RequestResult<Unit>

    suspend fun generateExtraContent(
        saga: SagaMetadata,
        message: Message,
        sceneSummary: SceneSummary?,
        characterReference: com.ilustris.sagai.features.characters.data.model.Character?,
        generateAudio: Boolean,
        isFromUser: Boolean,
    )
}
