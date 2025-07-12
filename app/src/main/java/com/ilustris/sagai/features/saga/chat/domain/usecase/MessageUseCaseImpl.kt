package com.ilustris.sagai.features.saga.chat.domain.usecase

import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageGen
import com.ilustris.sagai.features.saga.chat.repository.MessageRepository
import com.ilustris.sagai.features.timeline.data.model.Timeline
import javax.inject.Inject

class MessageUseCaseImpl
    @Inject
    constructor(
        private val messageRepository: MessageRepository,
        private val textGenClient: TextGenClient,
    ) : MessageUseCase {
        override suspend fun getMessages(sagaId: Int) = messageRepository.getMessages(sagaId)

        override suspend fun getMessageDetail(id: Int): MessageContent = messageRepository.getMessageDetail(id)

        override suspend fun saveMessage(message: Message) =
            try {
                messageRepository
                    .saveMessage(message)!!
                    .asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun deleteMessage(messageId: Long) {
            messageRepository.deleteMessage(messageId)
        }

        override suspend fun getLastMessage(sagaId: Int): Message? = messageRepository.getLastMessage(sagaId)

        override suspend fun generateIntroMessage(
            saga: SagaData,
            character: Character?,
        ): RequestResult<Exception, Message> {
            val genText =
                textGenClient.generate<Message>(
                    generateSagaIntroductionPrompt(saga, character),
                    true,
                )

            return try {
                genText!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
        }

        override suspend fun generateEndingMessage(content: SagaContent): RequestResult<Exception, Message> =
            try {
                val genText =
                    textGenClient.generate<Message>(
                        SagaPrompts.endingGeneration(content),
                    )

                genText!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateMessage(
            saga: SagaContent,
            chapter: Chapter?,
            lastEvents: List<Timeline>,
            message: Pair<String, String>,
            lastMessages: List<Pair<String, String>>,
            directive: String,
        ): RequestResult<Exception, MessageGen> {
            val genText =
                textGenClient.generate<MessageGen>(
                    generateReplyMessage(
                        saga,
                        chapter,
                        lastEvents,
                        message,
                        lastMessages,
                        directive,
                    ),
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

        override suspend fun updateMessage(message: Message): RequestResult<Exception, Unit> =
            try {
                messageRepository.updateMessage(message).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
    }

private fun generateSagaIntroductionPrompt(
    saga: SagaData,
    character: Character?,
): String =
    SagaPrompts.introductionGeneration(
        saga,
        character,
    )

private fun generateReplyMessage(
    sagaContent: SagaContent,
    chapter: Chapter?,
    lastEvents: List<Timeline>,
    message: Pair<String, String>,
    lastMessages: List<Pair<String, String>>,
    directive: String,
) = ChatPrompts.replyMessagePrompt(
    saga = sagaContent,
    message.formatToString(),
    chapter,
    lastEvents,
    lastMessages.map { it.formatToString() },
    directive,
)
