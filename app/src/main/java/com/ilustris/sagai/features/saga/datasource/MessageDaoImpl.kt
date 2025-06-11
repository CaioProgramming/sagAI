package com.ilustris.sagai.features.saga.datasource

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import javax.inject.Inject

class MessageDaoImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : MessageDao {
        private val messageDao by lazy {
            database.messageDao()
        }

        override fun getMessages(sagaId: Int) = messageDao.getMessages(sagaId)

        override suspend fun getMessageDetail(id: Int) = messageDao.getMessageDetail(id)

        override suspend fun saveMessage(message: Message) = messageDao.saveMessage(message)

        override suspend fun deleteMessage(messageId: Long) = messageDao.deleteMessage(messageId)

        override suspend fun deleteMessages(sagaId: String) = messageDao.deleteMessages(sagaId)

        override suspend fun updateMessage(message: Message) = messageDao.updateMessage(message)

        override suspend fun getLastMessage(sagaId: Int): Message? = messageDao.getLastMessage(sagaId)
    }
