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
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageGen
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.repository.MessageRepository
import com.ilustris.sagai.features.timeline.data.model.Timeline
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

        override fun isInDebugMode(): Boolean {
            return isDebugModeEnabled
        }

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
            if (isDebugModeEnabled) {
                Log.d("MessageUseCaseImpl", "[DEBUG MODE] Generating fake intro message for saga: ${saga.title}")
                val fakeIntroMessage = Message(
                    text = "Welcome to the debug chronicles of '${saga.title}'! Adventure awaits... or does it?",
                    senderType = SenderType.NARRATOR,
                    sagaId = saga.id
                )
                return RequestResult.Success(fakeIntroMessage)
            }
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

        override suspend fun generateEndingMessage(content: SagaContent): RequestResult<Exception, Message> {
            if (isDebugModeEnabled) {
                Log.d("MessageUseCaseImpl", "[DEBUG MODE] Generating fake ending message for saga: ${content.data.title}")
                val fakeEndingMessage = Message(
                    text = "And so, the debug saga of '${content.data.title}' concludes. Or has it just begun?",
                    senderType = SenderType.NARRATOR,
                    sagaId = content.data.id
                )
                return RequestResult.Success(fakeEndingMessage)
            }
            try {
                val genText =
                    textGenClient.generate<Message>(
                        SagaPrompts.endingGeneration(content),
                    )

                return genText!!.asSuccess()
            } catch (e: Exception) {
                return e.asError()
            }
        }

        override suspend fun generateMessage(
            saga: SagaContent,
            chapter: Chapter?,
            lastEvents: List<Timeline>,
            message: Pair<String, String>,
            lastMessages: List<Pair<String, String>>,
            directive: String,
        ): RequestResult<Exception, MessageGen> {
            if (isDebugModeEnabled) {
                Log.d("MessageUseCaseImpl", "[DEBUG MODE] Generating fake reply for message: ${message.second}")
                val fakeReply = Message(
                    text = "[Debug AI]: I see you said '${message.second}'. That's... interesting.",
                    senderType = SenderType.CHARACTER, // Or could be NARRATOR or a specific character if needed
                    sagaId = saga.data.id,
                )
                val fakeMessageGen = MessageGen(
                    message = fakeReply,
                    shouldCreateCharacter = false,
                    newCharacter = null,
                    shouldEndSaga = false
                )
                return RequestResult.Success(fakeMessageGen)
            }

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
                e.asError()
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
