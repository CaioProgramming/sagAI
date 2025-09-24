package com.ilustris.sagai.features.saga.chat.data.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.model.getDirective
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.MessageGen
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.saga.chat.repository.MessageRepository
import javax.inject.Inject

class MessageUseCaseImpl
    @Inject
    constructor(
        private val messageRepository: MessageRepository,
        private val textGenClient: TextGenClient,
        private val gemmaClient: GemmaClient,
    ) : MessageUseCase {
        private var isDebugModeEnabled: Boolean = false

        override fun setDebugMode(enabled: Boolean) {
            isDebugModeEnabled = enabled
            Log.i("MessageUseCaseImpl", "Debug mode set to: $enabled")
        }

        override fun isInDebugMode(): Boolean = isDebugModeEnabled

        override suspend fun checkMessageTypo(
            genre: Genre,
            message: String,
            lastMessage: String?,
        ): RequestResult<TypoFix?> =
            executeRequest {
                gemmaClient.generate<TypoFix>(
                    SagaPrompts.checkForTypo(genre, message, lastMessage),
                    requireTranslation = true,
                )!!
            }

        override suspend fun getMessages(sagaId: Int) = messageRepository.getMessages(sagaId)

        override suspend fun saveMessage(
            message: Message,
            isFromUser: Boolean,
        ) = executeRequest {
            val tone =
                if (isFromUser) {
                    val prompt = SagaPrompts.emotionalToneExtraction(message.text)
                    val raw =
                        gemmaClient
                            .generate<String>(prompt, requireTranslation = false)
                            ?.trim()
                            ?.uppercase()
                    EmotionalTone.getTone(raw)
                } else {
                    message.emotionalTone
                }

            messageRepository.saveMessage(
                message.copy(
                    emotionalTone = tone,
                ),
            )!!
        }

        override suspend fun deleteMessage(messageId: Long) {
            messageRepository.deleteMessage(messageId)
        }

        override suspend fun getLastMessage(sagaId: Int): Message? = messageRepository.getLastMessage(sagaId)

        override suspend fun generateMessage(
            saga: SagaContent,
            message: MessageContent,
        ): RequestResult<MessageGen> =
            executeRequest {
                if (isDebugModeEnabled) {
                    Log.d(
                        "MessageUseCaseImpl",
                        "[DEBUG MODE] Generating fake reply for message: ${message.joinMessage().second}",
                    )
                    val fakeReply =
                        Message(
                            text = "[Debug AI]: I see you said '${message.joinMessage().second}'.",
                            senderType = SenderType.CHARACTER,
                            sagaId = saga.data.id,
                            timelineId = saga.getCurrentTimeLine()!!.data.id,
                        )
                    val fakeMessageGen =
                        MessageGen(
                            message = fakeReply,
                            shouldCreateCharacter = false,
                            newCharacter = null,
                            shouldEndSaga = false,
                        )
                    fakeMessageGen.asSuccess()
                }

                val genText =
                    textGenClient.generate<MessageGen>(
                        ChatPrompts.replyMessagePrompt(
                            saga = saga,
                            message =
                                message
                                    .joinMessage(showType = true)
                                    .formatToString(showSender = true),
                            lastMessages =
                                saga
                                    .flatMessages()
                                    .takeLast(UpdateRules.LORE_UPDATE_LIMIT)
                                    .map { it.joinMessage(true).formatToString() },
                            directive = saga.getDirective(),
                        ),
                        true,
                    )

                genText!!
            }

        override suspend fun updateMessage(message: Message): RequestResult<Message> =
            executeRequest {
                messageRepository.updateMessage(message)
            }
    }
