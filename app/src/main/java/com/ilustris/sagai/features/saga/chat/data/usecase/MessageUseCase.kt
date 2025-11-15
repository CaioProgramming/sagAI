package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.domain.model.MessageGen
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
    ): RequestResult<MessageGen>

    suspend fun updateMessage(message: Message): RequestResult<Message>

    fun setDebugMode(enabled: Boolean)

    fun isInDebugMode(): Boolean

    suspend fun checkMessageTypo(
        genre: Genre,
        message: String,
        lastMessage: String?,
    ): RequestResult<TypoFix?>

    suspend fun getSceneContext(saga: SagaContent): RequestResult<SceneSummary?>
}
