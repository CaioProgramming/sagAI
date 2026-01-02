package com.ilustris.sagai.features.saga.chat.data.usecase

import android.util.Log
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.AudioGenClient
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.model.AudioConfig
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.core.ai.prompts.AudioPrompts
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.model.getDirective
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.AIReply
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.Reaction
import com.ilustris.sagai.features.saga.chat.data.model.ReactionGen
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.features.saga.chat.repository.MessageRepository
import com.ilustris.sagai.features.saga.chat.repository.ReactionRepository
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import javax.inject.Inject

class MessageUseCaseImpl
    @Inject
    constructor(
        private val messageRepository: MessageRepository,
        private val reactionRepository: ReactionRepository,
        private val characterRepository: CharacterRepository,
        private val sagaRepository: SagaRepository,
        private val textGenClient: TextGenClient,
        private val gemmaClient: GemmaClient,
        private val audioGenClient: AudioGenClient,
        private val fileHelper: FileHelper,
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
                    requirement = GemmaClient.ModelRequirement.LOW,
                )!!
            }

        override suspend fun getSceneContext(saga: SagaContent): RequestResult<SceneSummary?> =
            executeRequest {
                gemmaClient.generate<SceneSummary>(
                    prompt =
                        ChatPrompts.sceneSummarizationPrompt(
                            saga = saga,
                        ),
                    temperatureRandomness = 0.2f,
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                )
            }

        override suspend fun getMessages(sagaId: Int) = messageRepository.getMessages(sagaId)

        override suspend fun saveMessage(
            saga: SagaContent,
            message: Message,
            isFromUser: Boolean,
            sceneSummary: SceneSummary?,
        ) = executeRequest {
            messageRepository.saveMessage(message)
        }

        override suspend fun analyzeMessageTone(
            saga: SagaContent,
            message: Message,
            isFromUser: Boolean,
        ): RequestResult<Unit> =
            executeRequest {
                val tone =
                    if (isFromUser) {
                        val prompt = EmotionalPrompt.emotionalToneExtraction(message.text)
                        val raw =
                            gemmaClient
                                .generate<String>(
                                    prompt,
                                    requireTranslation = false,
                                    requirement = GemmaClient.ModelRequirement.LOW,
                                )?.trim()
                                ?.uppercase()
                        EmotionalTone.getTone(raw)
                    } else {
                        message.emotionalTone
                    }
                if (tone != message.emotionalTone) {
                    messageRepository.updateMessage(message.copy(emotionalTone = tone))
            }
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

                sceneSummary?.charactersPresent?.mapNotNull { characterName ->
                    saga.findCharacter(characterName)
                } ?: emptyList()

                val genText =
                    gemmaClient.generate<AIReply>(
                        prompt =
                            ChatPrompts.replyMessagePrompt(
                                saga = saga,
                                message = message.message,
                                directive = saga.getDirective(),
                                sceneSummary = sceneSummary,
                            ),
                        requirement = GemmaClient.ModelRequirement.HIGH,
                        filterOutputFields =
                            ChatPrompts.messageExclusions,
                    )

                Log.i(
                    "MessageUseCaseImpl",
                    "AI Reasoning for message generation: ${genText?.reasoning}",
                )
                val reasoning = if (BuildConfig.DEBUG) genText?.reasoning else null
                genText?.message!!.copy(reasoning = reasoning)
            }

        override suspend fun generateReaction(
            saga: SagaContent,
            message: Message,
            sceneSummary: SceneSummary?,
        ) = executeRequest {
            if (sceneSummary == null) error("Can't define reactions without context.")
            if (sceneSummary.charactersPresent.isEmpty()) error("generateReaction: No characters related to react")

            if (message.senderType == SenderType.THOUGHT) error("generateReaction: Thought message cannot be reacted")

            val charactersInScene =
                sceneSummary.charactersPresent.mapNotNull { characterName ->
                    saga.findCharacter(characterName)
                }

            if (charactersInScene.isEmpty()) {
                error("generateReaction: No characters found in scene to react.")
            }

            val relationships =
                saga.mainCharacter!!.relationships.filter {
                    it.characterOne.id in charactersInScene.map { character -> character.data.id } ||
                        it.characterTwo.id in charactersInScene.map { character -> character.data.id }
                }

            val prompt =
                ChatPrompts.generateReactionPrompt(
                    saga = saga,
                    summary = sceneSummary,
                    relationships = relationships,
                    messageToReact = message,
                )

            val reaction =
                gemmaClient.generate<ReactionGen>(
                    prompt,
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                )!!
            Log.d(
                javaClass.simpleName,
                "generateReaction: ${reaction.reactions.size} reactions generated.",
            )
            reaction.reactions.distinctBy { it.character }.forEach { reaction ->
                val reactingCharacter = saga.findCharacter(reaction.character)
                if (reactingCharacter != null) {
                    if (reactingCharacter.data.id != message.characterId) {
                        reactionRepository.saveReaction(
                            Reaction(
                                messageId = message.id,
                                characterId = reactingCharacter.data.id,
                                emoji = reaction.reaction,
                                thought = reaction.thought,
                            ),
                        )
                        Log.d(
                            javaClass.simpleName,
                            "Saving reaction from ${reactingCharacter.data.name} at message ${message.id}",
                        )
                    } else {
                        Log.w(
                            javaClass.simpleName,
                            "generateReaction: Character can't react to itself.",
                        )
                    }
                } else {
                    Log.w(
                        javaClass.simpleName,
                        "generateReaction: Character '${reaction.character}' not in scene, skipping reaction.",
                    )
                }
            }
        }

        override suspend fun generateAudio(
            saga: SagaContent,
            savedMessage: Message,
            characterReference: CharacterContent?,
        ): RequestResult<Unit> =
            executeRequest {
                val isNarrator = savedMessage.senderType == SenderType.NARRATOR
                val speaker = characterReference?.let { "Character: ${it.data.name}" } ?: "Narrator"
                Log.i(javaClass.simpleName, "🎙️ Starting audio generation for $speaker")

                val voice =
                    Voice.findByName(
                        if (isNarrator) {
                            saga.data.narratorVoice
                        } else {
                            characterReference?.data?.voice
                        },
                    )

                val audioConfig =
                    gemmaClient.generate<AudioConfig>(
                        AudioPrompts.audioConfigPrompt(
                            saga,
                            message = savedMessage,
                            character = characterReference,
                        ),
                        requireTranslation = false,
                        requirement = GemmaClient.ModelRequirement.MEDIUM,
                    )!!

                val finalConfig =
                    audioConfig.copy(
                        voice = voice ?: audioConfig.voice,
                    )
                if (isNarrator) {
                    sagaRepository.updateChat(
                        saga.data.copy(
                            narratorVoice = finalConfig.voice.id,
                        ),
                    )
                } else {
                    if (characterReference != null) {
                        characterRepository.updateCharacter(
                            characterReference.data.copy(
                                voice = finalConfig.voice.id,
                            ),
                        )
                        Log.i(
                            "MessageUseCaseImpl",
                            "✅ Character voice updated to: ${finalConfig.voice.name} for ${characterReference.data.name}",
                        )
                    }
                }

                // Generate audio
                val audioResult =
                    audioGenClient
                        .generateAudio(
                            finalConfig,
                        )!!

                val audioFile =
                    fileHelper.saveBinaryFile(
                        audioResult,
                        path = "sagas/${saga.data.id}/audios",
                        fileName = "message_${savedMessage.id}_audio",
                        extension = "wav",
                    )!!

                updateMessage(
                    savedMessage.copy(
                        audioPath = audioFile.absolutePath,
                        audible = true,
                    ),
                )
            }

        override suspend fun updateMessage(message: Message): RequestResult<Message> =
            executeRequest {
                messageRepository.updateMessage(message)
            }
    }
