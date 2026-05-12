package com.ilustris.sagai.features.saga.chat.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.datasource.MessageDao
import timber.log.Timber
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

        override fun getMessagesPagingSource(sagaId: Int) = messageDao.getMessagesPagingSource(sagaId)

        override suspend fun saveMessage(message: Message): Message {
            Timber.d("saveMessage: Saving\n${message.toJsonFormat()}")
            return message.copy(
                id =
                    messageDao
                        .saveMessage(
                            message.copy(
                                id = 0,
                                timestamp = System.currentTimeMillis(),
                                text = message.text.trimEnd(),
                            ),
                        )!!
                        .toInt(),
            )
        }

        override suspend fun deleteMessage(messageId: Long) {
            messageDao.deleteMessage(messageId)
        }

        override suspend fun deleteMessages(sagaId: String) {
            messageDao.deleteMessages(sagaId)
        }

        override suspend fun updateMessage(message: Message): Message {
            messageDao.updateMessage(message)
            return message
        }

        override suspend fun getLastMessage(sagaId: Int): Message? = messageDao.getLastMessageWithContent(sagaId)?.message

        override fun getMessagesCount(sagaId: Int) = messageDao.getMessagesCount(sagaId)
    }
