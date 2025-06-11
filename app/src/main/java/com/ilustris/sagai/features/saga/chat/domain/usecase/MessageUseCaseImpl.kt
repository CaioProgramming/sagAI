package com.ilustris.sagai.features.saga.chat.domain.usecase

import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.chatReplyPrompt
import com.ilustris.sagai.core.ai.introductionPrompt
import com.ilustris.sagai.core.ai.narratorBreakPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.repository.MessageRepository
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
            message.copy(
                id =
                    messageRepository
                        .saveMessage(message)
                        .toInt(),
            )

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

        override suspend fun generateMessage(
            saga: SagaData,
            chapter: Chapter?,
            message: Pair<String, String>,
            mainCharacter: Character,
            lastMessages: List<Pair<String, String>>,
            characters: List<Character>,
        ): RequestResult<Exception, Message> {
            val genText =
                textGenClient.generate<Message>(
                    generateReplyMessage(
                        saga,
                        message.formatToString(),
                        chapter,
                        mainCharacter,
                        lastMessages.map { it.formatToString() },
                        characters,
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

        override suspend fun generateNarratorBreak(
            data: SagaData,
            messages: List<String>,
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
): String = saga.introductionPrompt(character)

private fun generateNarratorBreakPrompt(
    saga: SagaData,
    messages: List<String>,
): String = saga.narratorBreakPrompt(messages)

private fun generateReplyMessage(
    saga: SagaData,
    message: String,
    currentChapter: Chapter?,
    mainCharacter: Character,
    lastMessages: List<String>,
    characters: List<Character>,
) = chatReplyPrompt(
    saga,
    currentChapter,
    message,
    mainCharacter,
    lastMessages,
    characters,
)
