package com.ilustris.sagai.features.chat.data.usecase

import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.repository.MessageRepository
import com.ilustris.sagai.features.chat.repository.MessageRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageUseCaseImpl @Inject constructor(
    private val repositoryImpl: MessageRepository
) : MessageUseCase {
    override suspend fun getMessages(sagaId: Int): Flow<List<Message>> {
        return repositoryImpl.getMessages(sagaId)
    }

    override suspend fun saveMessage(message: Message): Long {
        return repositoryImpl.saveMessage(message)
    }

    override suspend fun deleteMessage(messageId: Long) {
        repositoryImpl.deleteMessage(messageId)
    }
}