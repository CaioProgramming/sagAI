package com.ilustris.sagai.features.saga.chat.data.usecase

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.model.getDirective
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.Reaction
import com.ilustris.sagai.features.saga.chat.data.model.ReactionGen
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.domain.model.MessageGen
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.saga.chat.repository.MessageRepository
import com.ilustris.sagai.features.saga.chat.repository.ReactionRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class MessageUseCaseImpl
    @Inject
    constructor(
        private val messageRepository: MessageRepository,
        private val reactionRepository: ReactionRepository,
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
                    ChatPrompts.checkForTypo(genre, message, lastMessage),
                    requireTranslation = true,
                )!!
            }

        override suspend fun getSceneContext(saga: SagaContent): RequestResult<SceneSummary?> =
            executeRequest {
                gemmaClient.generate<SceneSummary>(
                    ChatPrompts.sceneSummarizationPrompt(
                        saga = saga,
                        recentMessages =
                            saga
                                .flatMessages()
                                .map { it.message }
                                .takeLast(UpdateRules.LORE_UPDATE_LIMIT),
                    ),
                )
            }

        override suspend fun getMessages(sagaId: Int) = messageRepository.getMessages(sagaId)

        override suspend fun saveMessage(
            saga: SagaContent,
            message: Message,
            isFromUser: Boolean,
            sceneSummary: SceneSummary?,
        ) = executeRequest {
            val tone =
                if (isFromUser) {
                    val prompt = EmotionalPrompt.emotionalToneExtraction(message.text)
                    val raw =
                        gemmaClient
                            .generate<String>(prompt, requireTranslation = false)
                            ?.trim()
                            ?.uppercase()
                    EmotionalTone.getTone(raw)
                } else {
                    message.emotionalTone
                }

            val newMessage =
                messageRepository.saveMessage(
                    message.copy(
                        emotionalTone = tone,
                    ),
                )

            newMessage
        }

        override suspend fun deleteMessage(messageId: Long) {
            messageRepository.deleteMessage(messageId)
        }

        override suspend fun getLastMessage(sagaId: Int): Message? = messageRepository.getLastMessage(sagaId)

        override suspend fun generateMessage(
            saga: SagaContent,
            message: MessageContent,
            sceneSummary: SceneSummary?,
        ): RequestResult<Message> =
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
                    return@executeRequest fakeReply
                }

                val charactersInScene =
                    sceneSummary?.charactersPresent?.mapNotNull { characterName ->
                        saga.getCharacters().find {
                            it.name.equals(characterName, ignoreCase = true)
                        }
                    } ?: emptyList()

                val genText =
                    gemmaClient.generate<Message>(
                        ChatPrompts.replyMessagePrompt(
                            saga = saga,
                            message =
                                message.message,
                            lastMessages =
                                saga
                                    .flatMessages()
                                    .takeLast(UpdateRules.LORE_UPDATE_LIMIT)
                                    .map { it.message },
                            directive = saga.getDirective(),
                            sceneSummary =
                                sceneSummary?.copy(
                                    charactersPresent = charactersInScene.map { it.name },
                                ),
                        ),
                        useCore = true,
                    )

                genText!!
            }

        override suspend fun generateReaction(
            saga: SagaContent,
            message: Message,
            sceneSummary: SceneSummary?,
        ) = executeRequest {
            if (sceneSummary == null) error("Can't define reactions without context.")
            if (sceneSummary.charactersPresent.isEmpty()) error("generateReaction: No characters related to react")

            if (message.senderType == SenderType.THOUGHT) error("generateReaction: Thought message cannot be reacted")

            val charactersIds =
                sceneSummary.charactersPresent
                    .filter {
                        it.lowercase() !=
                            saga.mainCharacter
                                ?.data
                                ?.name
                                ?.lowercase()
                    }.mapNotNull {
                        saga.characters
                            .find { character ->
                                character.data.name.equals(it, ignoreCase = true)
                            }?.data
                            ?.id
                    }

            val relationships =
                saga.mainCharacter!!.relationships.filter {
                    it.characterOne.id in charactersIds || it.characterTwo.id in charactersIds
                }

            val prompt =
                ChatPrompts.generateReactionPrompt(
                    saga = saga.data,
                    summary = sceneSummary,
                    mainCharacter = saga.mainCharacter,
                    relationships = relationships,
                    messageToReact = message,
                )

            val reaction = gemmaClient.generate<ReactionGen>(prompt)!!
            Log.d(javaClass.simpleName, "generateReaction: ${reaction.reactions.size} reactions generated.")
            reaction.reactions.forEach { reaction ->
                saga.characters
                    .find { it.data.name.equals(reaction.character, ignoreCase = true) }
                    ?.let {
                        if (it.data.id != message.characterId) {
                            reactionRepository.saveReaction(
                                Reaction(
                                    messageId = message.id,
                                    characterId = it.data.id,
                                    emoji = reaction.reaction,
                                    thought = reaction.thought,
                                ),
                            )
                            Log.d(javaClass.simpleName, "Saving reaction from ${it.data.name} at message ${message.id}")
                        } else {
                            Log.w(
                                javaClass.simpleName,
                                "generateReaction: Character can't react to itself.",
                            )
                        }
                    }
            }
        }

        override suspend fun updateMessage(message: Message): RequestResult<Message> =
            executeRequest {
                messageRepository.updateMessage(message)
            }
    }
