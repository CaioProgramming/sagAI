package com.ilustris.sagai.features.chat.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.chat.data.MessageDao
import com.ilustris.sagai.features.chat.data.model.Message
import javax.inject.Inject

class MessageRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : MessageRepository {
        private val messageDao: MessageDao by lazy {
            database.messageDao()
        }

        override suspend fun getMessages(sagaId: Int) = messageDao.getMessages(sagaId)

        override suspend fun getMessageDetail(id: Int) = messageDao.getMessageDetail(id)

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

        override suspend fun getLastMessage(sagaId: Int): Message? = messageDao.getLastMessage(sagaId)
    }
