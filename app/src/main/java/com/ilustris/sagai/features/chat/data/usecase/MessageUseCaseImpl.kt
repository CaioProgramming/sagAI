package com.ilustris.sagai.features.chat.data.usecase

import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.chatReplyPrompt
import com.ilustris.sagai.core.ai.introductionPrompt
import com.ilustris.sagai.core.ai.narratorBreakPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.repository.MessageRepository
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageUseCaseImpl
    @Inject
    constructor(
        private val repositoryImpl: MessageRepository,
        private val textGenClient: TextGenClient,
    ) : MessageUseCase {
        override suspend fun getMessages(sagaId: Int): Flow<List<Message>> = repositoryImpl.getMessages(sagaId)

        override suspend fun saveMessage(message: Message): Long = repositoryImpl.saveMessage(message)

        override suspend fun deleteMessage(messageId: Long) {
            repositoryImpl.deleteMessage(messageId)
        }

        override suspend fun generateIntroMessage(saga: SagaData): RequestResult<Exception, Message> {
            val genText =
                textGenClient.generate<Message>(
                    generateSagaIntroductionPrompt(saga),
                    true,
                )

            return try {
                RequestResult.Success(
                    genText!!,
                )
            } catch (e: Exception) {
                e.printStackTrace()
                RequestResult.Error(e)
            }
        }

        override suspend fun generateMessage(
            saga: SagaData,
            message: Message,
            lastMessages: List<Message>,
        ): RequestResult<Exception, Message> {
            val genText =
                textGenClient.generate<Message>(
                    generateReplyMessage(saga, message, lastMessages),
                    true,
                )

            return try {
                RequestResult.Success(
                    genText!!,
                )
            } catch (e: Exception) {
                e.printStackTrace()
                RequestResult.Error(e)
            }
        }

        override suspend fun generateNarratorBreak(
            data: SagaData,
            messages: List<Message>,
        ) = try {
            RequestResult.Success(
                textGenClient.generate<Message>(
                    generateNarratorBreakPrompt(data, messages),
                    true,
                )!!,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            RequestResult.Error(e)
        }
    }

private fun generateSagaIntroductionPrompt(saga: SagaData): String = saga.introductionPrompt()

private fun generateNarratorBreakPrompt(
    saga: SagaData,
    messages: List<Message>,
): String = saga.narratorBreakPrompt(messages)

private fun generateReplyMessage(
    saga: SagaData,
    message: Message,
    lastMessages: List<Message>,
) = chatReplyPrompt(
    saga,
    message,
    lastMessages,
)
