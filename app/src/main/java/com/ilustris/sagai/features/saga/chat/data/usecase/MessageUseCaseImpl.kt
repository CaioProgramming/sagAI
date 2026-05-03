package com.ilustris.sagai.features.saga.chat.data.usecase

import MessageStatus
import com.ilustris.sagai.core.ai.AudioGenClient
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.AudioConfig
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.core.ai.prompts.AudioPrompts
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.saga.chat.data.model.AIReaction
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MessageUseCaseImpl
    @Inject
    constructor(
        private val messageRepository: MessageRepository,
        private val reactionRepository: ReactionRepository,
        private val characterRepository: CharacterRepository,
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
        private val audioGenClient: AudioGenClient,
        private val fileHelper: FileHelper,
        private val genreConfigService: com.ilustris.sagai.core.ai.services.GenreConfigService,
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
        private val remoteConfigService: com.ilustris.sagai.core.services.RemoteConfigService,
        private val reasoningSynthesizerService: com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService,
        private val timelineUseCase: com.ilustris.sagai.features.timeline.domain.TimelineUseCase,
    ) : MessageUseCase {
        private var isDebugModeEnabled: Boolean = false

        override fun setDebugMode(enabled: Boolean) {
            isDebugModeEnabled = enabled
            Timber.i("Debug mode set to: $enabled")
        }

        override fun isInDebugMode(): Boolean = isDebugModeEnabled

        private suspend fun fetchNarrativeRules() = remoteConfigService.getJson<NarrativeRules>("narrative_rules")!!

        override suspend fun checkMessageTypo(
            saga: SagaContent,
            message: String,
        ): RequestResult<TypoFix?> =
            executeRequest {
                val conversationDirective =
                    genreConfigService.conversationBlueprint(saga.data.genre)
                val narrativeRules = fetchNarrativeRules()
                gemmaClient.generate<TypoFix>(
                    ChatPrompts.checkForTypo(
                        promptService = promptService,
                        saga = saga,
                        conversationDirective = conversationDirective,
                        updateLimit = narrativeRules.loreUpdateLimit,
                        message = message,
                    ),
                    blueprintKey = ChatPrompts.CHAT_WRITING_PAL_BLUEPRINT,
                    userInteraction = true,
                    requireTranslation = true,
                    requirement = GemmaClient.ModelRequirement.LOW,
                )!!
            }

        override suspend fun getSceneContext(saga: SagaContent): RequestResult<SceneSummary?> =
            executeRequest {
                gemmaClient.generate<SceneSummary>(
                    prompt =
                        ChatPrompts.sceneSummarizationPrompt(
                            promptService = promptService,
                            saga = saga,
                            remoteConfigService.getNarrativeRules(),
                        ),
                    blueprintKey = ChatPrompts.SCENE_SUMMARIZATION_BLUEPRINT,
                    temperatureRandomness = 0.2f,
                    requirement = GemmaClient.ModelRequirement.LOW,
                )
            }

        override suspend fun getMessages(sagaId: Int) = messageRepository.getMessages(sagaId)

        override suspend fun saveMessage(
            saga: SagaContent,
            message: Message,
            isFromUser: Boolean,
            sceneSummary: SceneSummary?,
        ) = executeRequest {
            messageRepository.saveMessage(
                message.copy(
                    status = MessageStatus.OK,
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }

        override suspend fun analyzeMessageTone(
            saga: SagaContent,
            message: Message,
            isFromUser: Boolean,
        ) = executeRequest {
            val prompt =
                EmotionalPrompt.emotionalToneExtraction(
                    promptService,
                    promptService.getPromptDirectives(),
                    message.text,
                )
            val raw =
                gemmaClient
                    .generate<String>(
                        prompt,
                        blueprintKey = EmotionalPrompt.EMOTIONAL_TONE_EXTRACTION_BLUEPRINT,
                        requireTranslation = false,
                        requirement = GemmaClient.ModelRequirement.LOW,
                    )?.trim()
                    ?.uppercase()
            EmotionalTone.getTone(raw)
        }

        override suspend fun deleteMessage(messageId: Long) {
            messageRepository.deleteMessage(messageId)
        }

        override suspend fun getLastMessage(sagaId: Int): Message? = messageRepository.getLastMessage(sagaId)

        override suspend fun generateMessage(
            saga: SagaContent,
            message: MessageContent,
            sceneSummary: SceneSummary?,
        ): Flow<StreamingState<AIReply>> =
            flow {
                try {
                    if (isDebugModeEnabled) {
                        Timber.d("[DEBUG MODE] Generating fake reply for message: ${message.joinMessage().second}")
                        val fakeReply =
                            AIReply(
                                message =
                                    Message(
                                        text = "[Debug AI]: I see you said '${message.joinMessage().second}'.",
                                        senderType = SenderType.CHARACTER,
                                        sagaId = saga.data.id,
                                        timelineId = saga.getCurrentTimeLine()!!.data.id,
                                    ),
                            )
                        emit(StreamingState.Success(fakeReply))
                        return@flow
                    }

                    genreConfigService.getGenreConfig(saga.data.genre, saga.data.variationId)
                    val conversationDirective =
                        genreConfigService.conversationBlueprint(saga.data.genre)
                    val narrativeRules = fetchNarrativeRules()

                    val generateStream =
                        gemmaClient.generateStreaming<AIReply>(
                            prompt =
                                ChatPrompts.replyMessagePrompt(
                                    promptService = promptService,
                                    saga = saga,
                                    message = message.message,
                                    sceneSummary = sceneSummary!!,
                                    conversationDirective = conversationDirective,
                                    updateLimit = narrativeRules.loreUpdateLimit,
                                ),
                            blueprintKey = ChatPrompts.REPLY_GENERATION_BLUEPRINT,
                            userInteraction = true,
                            filterOutputFields = ChatPrompts.messageExclusions,
                            requirement = GemmaClient.ModelRequirement.HIGH,
                            useCore = true,
                        )
                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            generateStream,
                            "Generating a deep narrative reply",
                            conversationStyle = conversationDirective,
                            genre = saga.data.genre.name,
                        ).collect { state ->
                            if (state is StreamingState.Success) {
                                val reply = state.data
                                val savedMessage =
                                    messageRepository.saveMessage(
                                        reply.message.copy(
                                            sagaId = saga.data.id,
                                            timelineId = saga.getCurrentTimeLine()!!.data.id,
                                            characterId = saga.findCharacter(reply.message.speakerName)?.data?.id,
                                            status = MessageStatus.OK,
                                            timestamp = System.currentTimeMillis(),
                                        ),
                                    )
                                reply.sceneSummary?.let { summary ->
                                    saga.getCurrentTimeLine()?.let { timeline ->
                                        timelineUseCase.updateTimeline(
                                            timeline.data.copy(
                                                sceneSummary = summary,
                                            ),
                                        )
                                    }
                                }
                                withContext(Dispatchers.IO) {
                                    handleAIReplyReactions(saga, savedMessage, reply.reactions)
                                }
                                emit(StreamingState.Success(reply.copy(message = savedMessage)))
                            } else {
                                emit(state)
                            }
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(
                        StreamingState.Error(
                            message = e.message ?: "Unknown error",
                            throwable = e,
                        ),
                    )
                }
            }

        private suspend fun handleAIReplyReactions(
            saga: SagaContent,
            message: Message,
            reactions: List<AIReaction>?,
        ) {
            reactions?.forEach { aiReaction ->
                val character = saga.findCharacter(aiReaction.character)
                if (character != null && character.data.id != message.characterId) {
                    reactionRepository.saveReaction(
                        Reaction(
                            messageId = message.id,
                            characterId = character.data.id,
                            emoji = aiReaction.reaction,
                            thought = aiReaction.thought,
                        ),
                    )
                }
            }
        }

        override suspend fun generateReaction(
            saga: SagaContent,
            message: Message,
            sceneSummary: SceneSummary?,
        ) = executeRequest {
            if (sceneSummary == null) error("Can't define reactions without context.")
            if (sceneSummary.charactersPresent.isEmpty()) error("generateReaction: No characters related to react")

            val charactersInScene =
                sceneSummary.charactersPresent.mapNotNull { characterName ->
                    saga.findCharacter(characterName)
                }

            if (charactersInScene.isEmpty()) {
                error("generateReaction: No characters found in scene to react.")
            }

            saga.mainCharacter!!.relationships.filter {
                it.characterOne.id in charactersInScene.map { character -> character.data.id } ||
                    it.characterTwo.id in charactersInScene.map { character -> character.data.id }
            }

            genreConfigService.getGenreConfig(saga.data.genre, saga.data.variationId)

            val conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre)

            val prompt =
                ChatPrompts.generateReactionPrompt(
                    promptService = promptService,
                    summary = sceneSummary,
                    saga = saga,
                    messageToReact = message,
                    conversationDirective = conversationDirective,
                )

            val reaction =
                gemmaClient.generate<ReactionGen>(
                    prompt,
                    blueprintKey = ChatPrompts.CHAT_REACTION_BLUEPRINT,
                    requirement = GemmaClient.ModelRequirement.LOW,
                )!!
            Timber.d("generateReaction: ${reaction.reactions.size} reactions generated.")
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
                        Timber.d("Saving reaction from ${reactingCharacter.data.name} at message ${message.id}")
                    } else {
                        Timber.w("generateReaction: Character can't react to itself.")
                    }
                } else {
                    Timber.w("generateReaction: Character '${reaction.character}' not in scene, skipping reaction.")
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
                Timber.i("🎙️ Starting audio generation for $speaker")

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
                            promptService,
                            saga,
                            message = savedMessage,
                            character = characterReference,
                        ),
                        blueprintKey = AudioPrompts.AUDIO_CONFIG_BLUEPRINT,
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
                        Timber.i("✅ Character voice updated to: ${finalConfig.voice.name} for ${characterReference.data.name}")
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

        override suspend fun generateExtraContent(
            saga: SagaContent,
            message: Message,
            sceneSummary: SceneSummary?,
            characterReference: CharacterContent?,
            generateAudio: Boolean,
            isFromUser: Boolean,
        ) {
            val tone = analyzeMessageTone(saga, message, isFromUser).getSuccess()
            if (tone != null) {
                updateMessage(message.copy(emotionalTone = tone))
            }
            if (isFromUser) {
                generateReaction(saga, message, sceneSummary)
            }
            if (generateAudio) {
                generateAudio(saga, message, characterReference)
            }
        }
    }
