package com.ilustris.sagai.features.saga.chat.repository

import android.icu.util.Calendar
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.datasource.MessageDao
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

        override suspend fun saveMessage(message: Message) =
            try {
                message.copy(
                    id =
                        messageDao
                            .saveMessage(
                                message.copy(
                                    id = 0,
                                    timestamp = Calendar.getInstance().timeInMillis,
                                    text = message.text.trimEnd(),
                                ),
                            )!!
                            .toInt(),
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

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
