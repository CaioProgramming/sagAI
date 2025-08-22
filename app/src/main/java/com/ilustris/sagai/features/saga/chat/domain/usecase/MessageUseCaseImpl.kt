package com.ilustris.sagai.features.saga.chat.domain.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCurrentChapter
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.model.getDirective
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

        override suspend fun getMessages(sagaId: Int) = messageRepository.getMessages(sagaId)

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

        override suspend fun generateIntroMessage(saga: SagaContent): RequestResult<Exception, String> {
            if (isDebugModeEnabled) {
                Log.d(
                    "MessageUseCaseImpl",
                    "[DEBUG MODE] Generating fake intro message for saga: ${saga.data.title}",
                )
                return RequestResult.Success("Welcome to the debug chronicles of '${saga.data.title}'! Adventure awaits... or does it?")
            }
            val genText =
                gemmaClient.generate<String>(
                    generateSagaIntroductionPrompt(saga),
                    true,
                )

            return try {
                genText!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
        }

        override suspend fun generateMessage(
            saga: SagaContent,
            message: MessageContent,
        ): RequestResult<Exception, MessageGen> =
            try {
                if (isDebugModeEnabled) {
                    Log.d(
                        "MessageUseCaseImpl",
                        "[DEBUG MODE] Generating fake reply for message: ${message.joinMessage().second}",
                    )
                    val fakeReply =
                        Message(
                            text = "[Debug AI]: I see you said '${message.joinMessage().second}'. That's... interesting.",
                            senderType = SenderType.CHARACTER, // Or could be NARRATOR or a specific character if needed
                            sagaId = saga.data.id,
                            timelineId = saga.getCurrentTimeLine()!!.timeline.id,
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
                            message = message.joinMessage(showType = true).formatToString(showSender = true),
                            lastMessages =
                                saga
                                    .flatMessages()
                                    .takeLast(10)
                                    .map { it.joinMessage(true).formatToString() },
                            directive = saga.getDirective(),
                        ),
                        true,
                    )

                genText!!.asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun updateMessage(message: Message): RequestResult<Exception, Unit> =
            try {
                messageRepository.updateMessage(message).asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
    }

private fun generateSagaIntroductionPrompt(saga: SagaContent): String =
    SagaPrompts.introductionGeneration(
        saga,
    )
