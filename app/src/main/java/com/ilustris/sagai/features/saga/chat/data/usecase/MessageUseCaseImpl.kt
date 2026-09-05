package com.ilustris.sagai.features.saga.chat.data.usecase

import MessageStatus
import androidx.room.withTransaction
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
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.file.AVATAR_ICON_TARGET_PX
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.ImageHelper
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.core.globalshell.NewMessageEffect
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
import com.ilustris.sagai.features.home.data.model.findCharacterStrict
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.home.data.model.getDirectiveKey
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.data.model.AIReaction
import com.ilustris.sagai.features.saga.chat.data.model.AIReply
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.Reaction
import com.ilustris.sagai.features.saga.chat.data.model.ReplyFallout
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
        private val imageHelper: ImageHelper,
        private val genreConfigService: GenreConfigService,
        private val promptService: PromptService,
        private val remoteConfigService: RemoteConfigService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
        private val timelineUseCase: TimelineUseCase,
        private val sagaContentManager: SagaContentManager,
        private val globalShellService: GlobalShellService,
        private val database: SagaDatabase,
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
            val saved =
                messageRepository.saveMessage(
                    message.copy(
                        status = MessageStatus.OK,
                        timestamp = System.currentTimeMillis(),
                        // Nobody wants to watch their own text get typed back at them.
                        viewed = isFromUser,
                    ),
                )
            if (saved.senderType == SenderType.CHARACTER) {
                val character =
                    saved.characterId?.let { characterId ->
                        withContext(Dispatchers.IO) {
                            characterRepository.getCharacterById(
                                characterId,
                            )
                        }
                    }
                val icon =
                    character
                        ?.image
                        ?.takeIf { it.isNotBlank() }
                        ?.let { image ->
                            withContext(Dispatchers.IO) {
                                imageHelper.getImageBitmap(image, cropToCircle = true, targetSizePx = AVATAR_ICON_TARGET_PX).getSuccess()
                            }
                        }
                globalShellService.post(
                    NewMessageEffect(
                        messageId = saved.id,
                        sagaId = saga.data.id,
                        sagaTitle = saga.data.title,
                        genre = saga.data.genre,
                        speakerName = saved.speakerName ?: emptyString(),
                        rawText = saved.text,
                        character = character,
                        icon = icon,
                        deepLink = "saga://chat/${saga.data.id}/false",
                    ),
                )
            }
            saved
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
                            updateLimit = narrativeRules.loreUpdateLimit,
                            narrativeRules = narrativeRules,
                            characterArcsById = characterArcsById,
                            maxMessageLimit =
                                remoteConfigService
                                    .getLong(ChatPrompts.CHAT_INPUT_LIMIT_KEY)
                                    ?.toInt()
                                    ?.takeIf { it > 0 }
                                    ?: ChatPrompts.DEFAULT_CHAT_INPUT_LIMIT,
                        )
                    val conversationInstructions =
                        genreConfigService
                            .conversationInstructions(saga.data.genre)
                    val actContext =
                        promptService.buildSplitBlueprint(
                            saga.getDirectiveKey(),
                            emptyMap(),
                        )
                    // Deliberately the sync call, not generateStreaming. Streaming bought nothing
                    // here: the reply is one JSON object, so no partial text can be rendered, and
                    // the collector below only ever acts on Success. Worse, `alt=sse` does not
                    // carry thought parts at all — measured on gemini-3.5-flash-lite, the same
                    // request returns a reasoning summary when called normally and zero when
                    // streamed, while burning the thought tokens either way. The sync call gets
                    // that reasoning back, which is what makes it reviewable in the AI audit.
                    // The visible "thinking" text comes from the synthesizer's fallback regardless.
                    val generateStream =
                        flow {
                            val reply =
                                gemmaClient.generate<AIReply>(
                                    promptSplit =
                                        prompt.mergeInstructions(
                                            conversationInstructions,
                                            actContext.renderInstructions(),
                                        ),
                                    userInteraction = true,
                                    filterOutputFields = ChatPrompts.messageOutputExclusions,
                                    requirement = ModelRequirement.HIGH,
                                )
                            // gemmaClient.generate returns null rather than throwing on a final,
                            // non-retryable failure (spent daily quota, rejected key, exhausted
                            // retries — see GeminiGenerationEngine.executeSyncGenerationWithRetry).
                            // Wrapping that null in Success used to reach the `state.data!!` below
                            // and surface as a raw NullPointerException instead of a real error —
                            // the message never got marked failed and the underlying cause (e.g. the
                            // daily quota block already persisted by QuotaStatusService by this
                            // point) was lost from the message shown to the user.
                            if (reply == null) {
                                emit(
                                    StreamingState.Error(
                                        message = "Reply generation returned no result",
                                        throwable = IllegalStateException("AIReply generation failed"),
                                    ),
                                )
                            } else {
                                emit(StreamingState.Success(reply))
                            }
                        }
                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            generateStream,
                            "Generating a deep narrative reply",
                            genre = saga.data.genre,
                            details = message.message.text,
                        ).collect { state ->
                            if (state is StreamingState.Success) {
                                // A discovery whose name differs from the speaker is not a
                                // defect: someone can enter the fiction on a turn where another
                                // character holds the line, and a character can legitimately
                                // answer themselves (internal voice, hallucination). Both are
                                // narrative judgements the model makes, not invariants to enforce
                                // from here.
                                val reply = state.data!!
                                reply.sceneSummary?.let { summary ->
                                    saga.getCurrentTimeLine()?.let { timeline ->
                                        if (timeline.data.sceneSummary != summary) {
                                            timelineUseCase.updateTimeline(
                                                timeline.data.copy(
                                                    sceneSummary = summary,
                                                    currentObjective = summary.immediateObjective,
                                                ),
                                            )
                                        }
                                    }
                                }
                                val freshSaga =
                                    sagaRepository.getSagaMetadata(saga.data.id).first()
                                        ?: saga
                                val existingCharacter =
                                    resolveExistingCharacterForReply(freshSaga, reply)
                                // Everything the reply writes to the DB goes in one transaction so
                                // Room's invalidation tracker fires once at commit instead of once
                                // per statement — otherwise the chat list re-emits 3-5x per turn and
                                // the new bubble visibly stutters as it replays its entrance.
                                // resolveReplyCharacterLinks stays outside: it can hit the network to
                                // generate a character, and holding a DB transaction across that is
                                // how you get lock contention/ANRs.
                                val savedMessage =
                                    withContext(Dispatchers.IO) {
                                        database.withTransaction {
                                            val saved =
                                                insertAiReplyMessage(
                                                    saga,
                                                    reply,
                                                    character = existingCharacter,
                                                )
                                            reply.userTone?.let { tone ->
                                                updateMessage(
                                                    message.message.copy(emotionalTone = tone),
                                                )
                                            }
                                            saved
                                        }
                                    }
                                postNewMessageEffect(saga, savedMessage, existingCharacter)
                                // The fallout is NOT launched here. It has to run outside this
                                // flow's job: ChatGenerationService drops a new generate() call
                                // while jobs[sagaId] is still active, so anything left running here
                                // would silently swallow the player's next message. It is fired
                                // from there instead, on its own job.
                                sagaContentManager.resolveReplyCharacterLinks(
                                    saga = saga,
                                    reply = reply,
                                    savedMessage = savedMessage,
                                    sceneSummary = reply.sceneSummary ?: sceneSummary,
                                )
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
                val savedMessage =
                    database.withTransaction {
                        val saved = insertAiReplyMessage(saga, reply, character)
                        saved
                    }
                postNewMessageEffect(saga, savedMessage, character)
                savedMessage
            }

        /**
         * Resolves reactions and the notification hook for a turn that is already persisted and on
         * screen, on the LOW tier — a different model from the reply, and therefore a separate
         * per-minute token quota.
         *
         * Failures are swallowed: a turn without reactions reads as a quiet room, which is a far
         * better outcome than surfacing an error over a reply that arrived perfectly well.
         */
        override suspend fun resolveReplyFallout(
            saga: SagaMetadata,
            userMessage: Message,
            replyMessage: Message,
            sceneSummary: SceneSummary?,
        ) {
            val result =
                executeRequest {
                    val sagaContent =
                        sagaRepository.getSagaById(saga.data.id).first() as SagaContent
                    val prompt =
                        ChatPrompts.replyFalloutPrompt(
                            promptService = promptService,
                            saga = sagaContent,
                            userMessage = userMessage,
                            replyMessage = replyMessage,
                            sceneSummary = sceneSummary,
                        )
                    gemmaClient.generate<ReplyFallout>(
                        promptSplit =
                            prompt.mergeInstructions(
                                genreConfigService.conversationInstructions(saga.data.genre),
                            ),
                        requirement = ModelRequirement.LOW,
                    )
                }

            val fallout = result.getSuccess() ?: return

            database.withTransaction {
                handleAIReplyReactions(saga, replyMessage, fallout.replyReactions)
                handleAIReplyReactions(saga, userMessage, fallout.userReactions)
            }

            // Patched onto the scene the reply just wrote rather than written wholesale, so the
            // hook lands without clobbering the state the HIGH model established this turn.
            fallout.notificationHook?.takeIf { it.isNotBlank() }?.let { hook ->
                saga.getCurrentTimeLine()?.let { timeline ->
                    timeline.data.sceneSummary?.let { scene ->
                        timelineUseCase.updateTimeline(
                            timeline.data.copy(
                                sceneSummary =
                                    scene.copy(
                                        notificationHook = hook,
                                        notificationCharacterName = fallout.notificationCharacterName,
                                    ),
                            ),
                        )
                    }
                }
            }
        }

        /**
         * Pure DB write — safe to call inside a Room transaction. The bitmap decode and global-shell
         * post that used to live here moved to [postNewMessageEffect] so a transaction never has to
         * wrap image I/O.
         */
        private suspend fun insertAiReplyMessage(
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
                    viewed = false,
                    // The model fills this on every reply and it was being dropped here, so every
                    // character message persisted with a null tone — which is exactly what the
                    // review's expressiveness pages count.
                    emotionalTone = reply.message.emotionalTone,
                ),
            )
        }

        /** Side effect for a freshly persisted reply. Must run *outside* any DB transaction. */
        private suspend fun postNewMessageEffect(
            saga: SagaMetadata,
            savedMessage: Message,
            character: Character?,
        ) {
            if (savedMessage.senderType != SenderType.CHARACTER) return
            val icon =
                character
                    ?.image
                    ?.takeIf { it.isNotBlank() }
                    ?.let { image ->
                        withContext(Dispatchers.IO) {
                            imageHelper.getImageBitmap(image, cropToCircle = true, targetSizePx = AVATAR_ICON_TARGET_PX).getSuccess()
                        }
                    }
            globalShellService.post(
                NewMessageEffect(
                    messageId = savedMessage.id,
                    sagaId = saga.data.id,
                    sagaTitle = saga.data.title,
                    genre = saga.data.genre,
                    speakerName = savedMessage.speakerName ?: emptyString(),
                    rawText = savedMessage.text,
                    character = character,
                    icon = icon,
                    deepLink = "saga://chat/${saga.data.id}/false",
                ),
            )
        }

        private fun resolveExistingCharacterForReply(
            saga: SagaMetadata,
            reply: AIReply,
        ): Character? {
            val candidateNames =
                listOfNotNull(
                    reply.message.speakerName,
                    reply.newCharacter?.name,
                ).distinctBy { it.trim().lowercase() }

            if (candidateNames.isEmpty()) return null

            for (name in candidateNames) {
                saga.findCharacterStrict(name)?.let { return it }
                saga.findCharacter(name)?.let { return it }
            }
            return null
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

            val narrativeRules = fetchNarrativeRules()
            val prompt =
                ChatPrompts.generateReactionPrompt(
                    promptService = promptService,
                    summary = sceneSummary,
                    saga = sagaContent,
                    messageToReact = message,
                    conversationDirective = emptyString(),
                    narrativeRules = narrativeRules,
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

        override suspend fun markViewed(messageId: Int) {
            messageRepository.markViewed(messageId)
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
