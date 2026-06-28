package com.ilustris.sagai.features.saga.chat.data.usecase

import MessageStatus
import com.ilustris.sagai.core.ai.AudioGenClient
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.AudioConfig
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.AudioPrompts
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.model.getDirectiveKey
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
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        private val characterUseCase: CharacterUseCase,
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
        private val audioGenClient: AudioGenClient,
        private val fileHelper: FileHelper,
        private val genreConfigService: GenreConfigService,
        private val promptService: PromptService,
        private val remoteConfigService: RemoteConfigService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
        private val timelineUseCase: TimelineUseCase,
    ) : MessageUseCase {
        private var isDebugModeEnabled: Boolean = false

        override fun setDebugMode(enabled: Boolean) {
            isDebugModeEnabled = enabled
            Timber.i("Debug mode set to: $enabled")
        }

        override fun isInDebugMode(): Boolean = isDebugModeEnabled

        private suspend fun fetchNarrativeRules() = remoteConfigService.getJson<NarrativeRules>("narrative_rules")!!

        override suspend fun checkMessageTypo(
            saga: SagaMetadata,
            message: String,
        ): RequestResult<TypoFix?> =
            executeRequest {
                val sagaContent = sagaRepository.getSagaById(saga.data.id).first() as SagaContent

                val narrativeRules = fetchNarrativeRules()
                val prompt =
                    ChatPrompts.checkForTypo(
                        promptService = promptService,
                        saga = sagaContent,
                        conversationDirective = emptyString(),
                        updateLimit = narrativeRules.loreUpdateLimit,
                        message = message,
                    )
                gemmaClient.generate<TypoFix>(
                    promptSplit =
                        prompt.mergeInstructions(
                            genreConfigService.conversationInstructions(saga.data.genre),
                        ),
                    userInteraction = true,
                    requireTranslation = true,
                    requirement = ModelRequirement.LOW,
                )!!
            }

        override suspend fun getSceneContext(saga: SagaMetadata): RequestResult<SceneSummary?> =
            executeRequest {
                val sagaContent = sagaRepository.getSagaById(saga.data.id).first() as SagaContent
                val prompt =
                    ChatPrompts.sceneSummarizationPrompt(
                        promptService = promptService,
                        saga = sagaContent,
                        remoteConfigService.getNarrativeRules(),
                    )
                gemmaClient.generate<SceneSummary>(
                    promptSplit = prompt,
                    requirement = ModelRequirement.LOW,
                )
            }

        override suspend fun getMessages(sagaId: Int) = messageRepository.getMessages(sagaId)

        override fun getMessagesPagingSource(sagaId: Int) = messageRepository.getMessagesPagingSource(sagaId)

        override fun getMessagesCount(sagaId: Int) = messageRepository.getMessagesCount(sagaId)

        override suspend fun saveMessage(
            saga: SagaMetadata,
            message: Message,
            isFromUser: Boolean,
        ) = executeRequest {
            messageRepository.saveMessage(
                message.copy(
                    status = MessageStatus.OK,
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }

        override suspend fun analyzeMessageTone(
            saga: SagaMetadata,
            message: Message,
            isFromUser: Boolean,
        ) = executeRequest {
            val prompt =
                EmotionalPrompt.emotionalToneExtraction(
                    promptService,
                    message.text,
                )
            val raw =
                gemmaClient
                    .generate<String>(
                        promptSplit = prompt,
                        requireTranslation = false,
                        requirement = ModelRequirement.MINIMAL,
                    )?.trim()
                    ?.uppercase()
            EmotionalTone.getTone(raw)
        }

        override suspend fun deleteMessage(messageId: Long) {
            messageRepository.deleteMessage(messageId)
        }

        override suspend fun getLastMessage(sagaId: Int): Message? = messageRepository.getLastMessage(sagaId)

        override suspend fun generateMessage(
            saga: SagaMetadata,
            message: MessageContent,
        ): Flow<StreamingState<AIReply?>> =
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
                    val narrativeRules = fetchNarrativeRules()

                    val sagaContent =
                        sagaRepository.getSagaById(saga.data.id).first() ?: error("Saga not found")
                    val sceneSummary =
                        sagaContent.getCurrentTimeLine()?.data?.sceneSummary
                            ?: getSceneContext(saga).getSuccess()
                    val characterArcsById = loadCharacterArcsForScene(sagaContent, sceneSummary)
                    val prompt =
                        ChatPrompts.replyMessagePrompt(
                            promptService = promptService,
                            saga = sagaContent,
                            message = message.message,
                            sceneSummary = sceneSummary,
                            conversationDirective = emptyString(),
                            updateLimit = narrativeRules.loreUpdateLimit,
                            characterArcsById = characterArcsById,
                        )
                    val conversationInstructions =
                        genreConfigService
                            .conversationInstructions(saga.data.genre)
                    val actContext =
                        promptService.buildSplitBlueprint(
                            saga.getDirectiveKey(),
                            emptyMap(),
                        )
                    val generateStream =
                        gemmaClient.generateStreaming<AIReply>(
                            promptSplit =
                                prompt.mergeInstructions(
                                    conversationInstructions,
                                    actContext.renderInstructions(),
                                ),
                            userInteraction = true,
                            filterOutputFields = ChatPrompts.messageExclusions,
                            requirement = ModelRequirement.HIGH,
                            useCore = true,
                        )
                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            generateStream,
                            "Generating a deep narrative reply",
                            genre = saga.data.genre,
                        ).collect { state ->
                            if (state is StreamingState.Success) {
                                val reply = state.data!!
                                reply.newCharacter?.let { discovery ->
                                    val speaker = reply.message.speakerName
                                    if (speaker != null &&
                                        !speaker.equals(
                                            discovery.name,
                                            ignoreCase = true,
                                        )
                                    ) {
                                        Timber.w(
                                            "AIReply newCharacter.name (${discovery.name}) " +
                                                "does not match message.speakerName ($speaker)",
                                        )
                                    }
                                }
                                val deferSaveForNewCharacter =
                                    reply.newCharacter != null &&
                                        reply.message.senderType != SenderType.NARRATOR
                                reply.sceneSummary?.let { summary ->
                                    saga.getCurrentTimeLine()?.let { timeline ->
                                        timelineUseCase.updateTimeline(
                                            timeline.data.copy(
                                                sceneSummary = summary,
                                            ),
                                        )
                                    }
                                }
                                val savedMessage =
                                    if (deferSaveForNewCharacter) {
                                        reply.message.copy(
                                            sagaId = saga.data.id,
                                            timelineId = saga.getCurrentTimeLine()!!.data.id,
                                            status = MessageStatus.OK,
                                            speakerName =
                                                reply.message.speakerName
                                                    ?: reply.newCharacter?.name,
                                        )
                                    } else {
                                        persistAiReplyMessage(saga, reply, character = null)
                                    }
                                withContext(Dispatchers.IO) {
                                    if (!deferSaveForNewCharacter) {
                                        handleAIReplyReactions(saga, savedMessage, reply.reactions)
                                    }
                                    reply.userTone?.let { tone ->
                                        updateMessage(message.message.copy(emotionalTone = tone))
                                    }
                                    reply.userReactions?.let { reactions ->
                                        handleAIReplyReactions(saga, message.message, reactions)
                                    }
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

    override suspend fun saveGeneratedReply(
            saga: SagaMetadata,
            reply: AIReply,
            userMessage: Message,
            character: Character?,
        ): RequestResult<Message> =
            executeRequest {
                val savedMessage = persistAiReplyMessage(saga, reply, character)
                handleAIReplyReactions(saga, savedMessage, reply.reactions)
                savedMessage
            }

        private suspend fun persistAiReplyMessage(
            saga: SagaMetadata,
            reply: AIReply,
            character: Character?,
        ): Message {
            val speakerName =
                character?.fullName()
                    ?: reply.message.speakerName
                    ?: reply.newCharacter?.name
            return messageRepository.saveMessage(
                Message(
                    id = 0,
                    sagaId = saga.data.id,
                    text = reply.message.text,
                    senderType = reply.message.senderType,
                    timelineId = saga.getCurrentTimeLine()!!.data.id,
                    status = MessageStatus.OK,
                    speakerName = speakerName,
                characterId = character?.id,
            ),
        )
    }

        private suspend fun handleAIReplyReactions(
            saga: SagaMetadata,
            message: Message,
            reactions: List<AIReaction>?,
        ) {
            val sagaContent = sagaRepository.getSagaById(saga.data.id).first() ?: return
            reactions?.forEach { aiReaction ->
                val character = sagaContent.findCharacter(aiReaction.character)
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
            saga: SagaMetadata,
            message: Message,
            sceneSummary: SceneSummary?,
        ) = executeRequest {
            val sagaContent = sagaRepository.getSagaById(saga.data.id).first() as SagaContent
            if (sceneSummary == null) error("Can't define reactions without context.")
            if (sceneSummary.charactersPresent.isEmpty()) error("generateReaction: No characters related to react")

            val charactersInScene =
                sceneSummary.charactersPresent.mapNotNull { characterName ->
                    sagaContent.findCharacter(characterName)
                }

            if (charactersInScene.isEmpty()) {
                error("generateReaction: No characters found in scene to react.")
            }

            sagaContent.mainCharacter!!.relationships.filter {
                it.characterOne.id in charactersInScene.map { character -> character.data.id } ||
                    it.characterTwo.id in charactersInScene.map { character -> character.data.id }
            }

            val prompt =
                ChatPrompts.generateReactionPrompt(
                    promptService = promptService,
                    summary = sceneSummary,
                    saga = sagaContent,
                    messageToReact = message,
                    conversationDirective = emptyString(),
                )

            val reaction =
                gemmaClient.generate<ReactionGen>(
                    promptSplit =
                        prompt.mergeInstructions(
                            genreConfigService.conversationInstructions(saga.data.genre),
                        ),
                    requirement = ModelRequirement.LOW,
                )!!
            Timber.d("generateReaction: ${reaction.reactions.size} reactions generated.")
            reaction.reactions.distinctBy { it.character }.forEach { reaction ->
                val reactingCharacter = sagaContent.findCharacter(reaction.character)
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
            saga: SagaMetadata,
            savedMessage: Message,
            characterReference: Character?,
        ): RequestResult<Unit> =
            executeRequest {
                val sagaContent = sagaRepository.getSagaById(saga.data.id).first() as SagaContent
                val isNarrator = savedMessage.senderType == SenderType.NARRATOR
                val speaker = characterReference?.let { "Character: ${it.name}" } ?: "Narrator"
                Timber.i("🎙️ Starting audio generation for $speaker")

                val voice =
                    Voice.findByName(
                        if (isNarrator) {
                            saga.data.narratorVoice
                        } else {
                            characterReference?.voice
                        },
                    )

                val audioConfig =
                    gemmaClient.generate<AudioConfig>(
                        promptSplit =
                            AudioPrompts.audioConfigPrompt(
                                promptService,
                                sagaContent,
                                message = savedMessage,
                                character = characterReference?.let { CharacterContent(it) },
                            ),
                        requireTranslation = false,
                        requirement = ModelRequirement.MEDIUM,
                    )!!

                val finalConfig =
                    audioConfig.copy(
                        voice = voice ?: audioConfig.voice,
                    )
                if (isNarrator) {
                    sagaRepository.updateSaga(
                        saga.data.copy(
                            narratorVoice = finalConfig.voice.id,
                        ),
                    )
                } else {
                    if (characterReference != null) {
                        characterRepository.updateCharacter(
                            characterReference.copy(
                                voice = finalConfig.voice.id,
                            ),
                        )
                        Timber.i("✅ Character voice updated to: ${finalConfig.voice.name} for ${characterReference.name}")
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
            saga: SagaMetadata,
            message: Message,
            characterReference: Character?,
            generateAudio: Boolean,
            isFromUser: Boolean,
        ) {
            val sagaContent =
                sagaRepository.getSagaById(saga.data.id).first() ?: error("Saga not found")
            val sceneSummary =
                sagaContent.getCurrentTimeLine()?.data?.sceneSummary
                    ?: getSceneContext(saga).getSuccess()

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

        private suspend fun loadCharacterArcsForScene(
            saga: SagaContent,
            sceneSummary: SceneSummary?,
        ): Map<Int, List<CharacterArc>> {
            val characterIds =
                sceneSummary
                    ?.charactersPresent
                    ?.mapNotNull { saga.findCharacter(it)?.data?.id }
                    .orEmpty()
            if (characterIds.isEmpty()) return emptyMap()

            return characterIds
                .associateWith { characterId ->
                    characterUseCase.getCharacterArcs(characterId).first()
                }.filterValues { it.isNotEmpty() }
        }
    }
