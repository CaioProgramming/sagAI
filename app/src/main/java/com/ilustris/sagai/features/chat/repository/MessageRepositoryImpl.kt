package com.ilustris.sagai.features.chat.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.chat.data.MessageDao
import com.ilustris.sagai.features.chat.data.model.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : MessageRepository {
        private val messageDao: MessageDao by lazy {
            database.messageDao()
        }

        override suspend fun getMessages(sagaId: Int): Flow<List<Message>> = messageDao.getMessages(sagaId)

        override suspend fun saveMessage(message: Message): Long = messageDao.saveMessage(message)

        override suspend fun deleteMessage(messageId: Long) {
            messageDao.deleteMessage(messageId)
        }

        override suspend fun deleteMessages(sagaId: String) {
            messageDao.deleteMessages(sagaId)
        }

        override suspend fun updateMessage(message: Message) {
            messageDao.updateMessage(message)
        }
    }
