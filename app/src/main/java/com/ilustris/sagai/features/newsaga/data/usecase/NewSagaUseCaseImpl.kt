package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.prompts.NewSagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.CreationAssist
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.service.CharacterIdeationService
import com.ilustris.sagai.features.newsaga.data.service.SagaIdeationService
import com.ilustris.sagai.features.newsaga.ui.presentation.FlowPages
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
        private val genreConfigService: GenreConfigService,
        private val promptService: PromptService,
        private val remoteConfigService: RemoteConfigService,
        private val characterUseCase: CharacterUseCase,
        private val sagaIdeationService: SagaIdeationService,
        private val characterIdeationService: CharacterIdeationService,
        private val loadingService: com.ilustris.sagai.core.services.LoadingService,
        private val reasoningSynthesizerService: com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService,
    ) : NewSagaUseCase {
        override fun createCompleteSagaFlow(
            sagaDraft: SagaDraft,
            characterInfo: CharacterInfo,
            sagaMessages: List<ChatMessage>,
        ): Flow<SagaCreationState> =
            flow {
                try {
                    val sagaForm = SagaForm(sagaDraft, characterInfo)

                    // Step 1: Create saga
                    var processMessage =
                        generateProcessMessage(
                            SagaProcess.CREATING_SAGA,
                            sagaForm,
                            characterInfo,
                            sagaDraft.genre,
                        ).getSuccess()
                            ?: "Creating saga..."
                    emit(SagaCreationState.Loading(processMessage))

                    val generatedSaga =
                        generateSaga(sagaDraft, sagaMessages).getSuccess()
                            ?: throw Exception("Failed to create saga")
                    val saga =
                        createSaga(generatedSaga).getSuccess() ?: throw Exception("Failed to save saga")

                    // Step 2: Create character
                    processMessage =
                        generateProcessMessage(
                            SagaProcess.CREATING_CHARACTER,
                            sagaForm,
                            characterInfo,
                            sagaDraft.genre,
                        ).getSuccess()
                            ?: "Creating character..."
                    emit(SagaCreationState.Loading(processMessage))

                    val characterResult =
                        characterUseCase
                            .generateCharacter(
                                SagaContent(saga),
                                characterInfo.toAINormalize(),
                            ).getSuccess()

                    val character =
                        characterResult ?: run {
                            deleteSaga(saga)
                            throw Exception("Failed to create character")
                        }

                    processMessage = generateProcessMessage(
                        SagaProcess.SAVED_CHARACTER,
                        sagaForm,
                        characterInfo,
                        sagaDraft.genre,
                    ).getSuccess() ?: "Character saved..."
                    emit(SagaCreationState.Loading(processMessage))

                    // Step 3: Update saga
                    val updatedSaga = saga.copy(mainCharacterId = character.id)
                    updateSaga(updatedSaga).getSuccess()
                        ?: throw Exception("Failed to update saga with character")

                    // Step 4: Generate icon
                    processMessage =
                        generateProcessMessage(
                            SagaProcess.FINALIZING,
                            sagaForm,
                            characterInfo,
                            sagaDraft.genre,
                        ).getSuccess()
                            ?: "Finalizing..."
                    emit(SagaCreationState.Loading(processMessage))

                    generateSagaIcon(updatedSaga, character)

                    // Step 5: Success
                    processMessage =
                        generateProcessMessage(
                            SagaProcess.SUCCESS,
                            sagaForm,
                            characterInfo,
                            sagaDraft.genre,
                        ).getSuccess() ?: "Success!"
                    emit(SagaCreationState.Loading(processMessage))

                    emit(SagaCreationState.Success(updatedSaga, character))
                } catch (e: Exception) {
                    emit(SagaCreationState.Error(e))
                }
            }

        override suspend fun createSaga(saga: Saga): RequestResult<Saga> =
            executeRequest {
                sagaRepository.saveChat(saga)
            }

        override suspend fun updateSaga(saga: Saga): RequestResult<Saga> =
            executeRequest {
                sagaRepository.updateChat(saga)
            }

        override suspend fun deleteSaga(saga: Saga): RequestResult<Unit> = sagaRepository.deleteChat(saga).asSuccess()

        override suspend fun generateSaga(
            sagaForm: SagaDraft,
            miniChatContent: List<ChatMessage>,
        ): RequestResult<Saga> =
            executeRequest {
                val config = genreConfigService.getGenreConfig(sagaForm.genre)
                val identity = genreConfigService.conversationBlueprint(sagaForm.genre)
                gemmaClient.generate<Saga>(
                    NewSagaPrompts.createSagaPrompt(
                        promptService,
                        sagaForm,
                        miniChatContent,
                        config.variations ?: mapOf(),
                        identity,
                    ),
                    requirement = GemmaClient.ModelRequirement.HIGH,
                    filterOutputFields =
                        listOf(
                            "id",
                            "icon",
                            "createdAt",
                            "mainCharacterId",
                            "currentActId",
                            "isEnded",
                            "endedAt",
                            "isDebug",
                            "endMessage",
                            "review",
                            "emotionalReview",
                        ),
                )!!
            }

        override suspend fun generateSagaIcon(
            sagaForm: Saga,
            character: Character,
        ) = executeRequest {
            sagaRepository
                .generateSagaIcon(
                    sagaForm,
                    listOf(character),
                ).getSuccess()!!
        }

        override suspend fun replyAiForm(
            currentMessages: List<ChatMessage>,
            latestMessage: String?,
            currentFormData: SagaForm,
        ): RequestResult<SagaCreationGen> =
            executeRequest {
                // Limit conversation history to last 10 messages for context
                val recentMessages = currentMessages.takeLast(10)

                val userInput = currentMessages.last().text
                val config = genreConfigService.getGenreConfig(currentFormData.saga.genre)
                val identity = genreConfigService.conversationBlueprint(currentFormData.saga.genre)

                // Single AI call to extract, enhance, and provide helpful suggestions
                val response =
                    gemmaClient.generate<SagaCreationGen>(
                        NewSagaPrompts.conversationalSagaReply(
                            promptService = promptService,
                            currentSagaDraft = currentFormData.saga,
                            userInput = userInput,
                            conversationHistory = recentMessages,
                            availableVariations = config.variations ?: mapOf(),
                            identity = identity,
                        ),
                        requireTranslation = true,
                    )!!

                response
            }

        override suspend fun generateIntroduction(): RequestResult<SagaCreationGen> =
            executeRequest {
                gemmaClient.generate(NewSagaPrompts.introPrompt(promptService))!!
            }

        override suspend fun generateCharacterIntroduction(sagaContext: SagaDraft?): RequestResult<SagaCreationGen> =
            executeRequest {
                gemmaClient.generate(
                    CharacterPrompts.characterIntroPrompt(
                        promptService,
                        sagaContext,
                    ),
                )!!
            }

        override suspend fun generateProcessMessage(
            process: SagaProcess,
            saga: SagaForm,
            character: CharacterInfo,
            genre: Genre?,
        ): RequestResult<String> =
            executeRequest {
                val identity = genre?.let { genreConfigService.conversationBlueprint(it) } ?: ""
                val processConfig =
                    remoteConfigService.getJson<Map<String, String>>("process_config") ?: emptyMap()
                val instruction = processConfig[process.name] ?: ""
                gemmaClient.generate(
                    NewSagaPrompts.generateProcessPrompt(
                        promptService,
                        process,
                        saga,
                        character,
                        identity,
                        instruction,
                    ),
                    filterOutputFields =
                        listOf(
                            "id",
                            "smartZoom",
                            "emojified",
                            "hexColor",
                            "voice",
                        ),
                )!!
            }

        override suspend fun adaptSagaToGenre(sagaDraft: SagaDraft): RequestResult<SagaCreationGen> =
            executeRequest {
                val identity = genreConfigService.conversationBlueprint(sagaDraft.genre)
                gemmaClient.generate(
                    NewSagaPrompts.genreAdaptationPrompt(
                        promptService,
                        sagaDraft,
                        identity,
                    ),
                )!!
            }

        override suspend fun generateGenreSuggestions(genre: Genre): RequestResult<SagaCreationGen> =
            executeRequest {
                val identity = genreConfigService.conversationBlueprint(genre)
                gemmaClient.generate(
                    NewSagaPrompts.genreSuggestionsPrompt(
                        promptService,
                        genre,
                        identity,
                    ),
                )!!
            }

        override suspend fun refineDraft(
            rawInput: String,
            genre: Genre,
        ): RequestResult<SagaCreationGen> =
            executeRequest {
                val identity = genreConfigService.conversationBlueprint(genre)
                gemmaClient.generate(
                    NewSagaPrompts.refineDraftPrompt(
                        promptService,
                        rawInput,
                        genre,
                        identity,
                    ),
                    requireTranslation = true,
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                )!!
            }

        override suspend fun assistCreation(
            flow: FlowPages,
            sagaDraft: SagaDraft?,
            characterInfo: CharacterInfo?,
        ): RequestResult<CreationAssist> =
            executeRequest {
                val objectives =
                    remoteConfigService.getJson<Map<String, String>>("creation_flow_objectives")!!
                val flowSpecificObjectives = objectives[flow.name] ?: ""
                val identity =
                    sagaDraft?.genre?.let { genreConfigService.conversationBlueprint(it) } ?: ""
                gemmaClient.generate(
                    NewSagaPrompts.creationAssistPrompt(
                        promptService,
                        flow,
                        sagaDraft,
                        characterInfo,
                        flowSpecificObjectives,
                        identity,
                    ),
                    requireTranslation = true,
                )!!
            }

        override fun executePrompt(
            prompt: String,
            lockedSaga: SagaDraft?,
            lockedCharacter: CharacterInfo?,
        ): Flow<AgenticFlowResponse> =
            flow {
                try {
                    val targetLanguage = getSessionLanguage()
                    if (lockedSaga == null) {
                        reasoningSynthesizerService
                            .synthesizeReasoning(
                                sourceFlow = sagaIdeationService.generateCosmicLibrary(prompt),
                                context = "Curating your cosmic library",
                                targetLanguage = targetLanguage,
                            ).collect { streamingState ->
                                when (streamingState) {
                                    is StreamingState.Reasoning -> {
                                        emit(AgenticFlowResponse.Log(streamingState.chunk))
                                    }

                                    is StreamingState.Success -> {
                                        emit(
                                            AgenticFlowResponse.LibraryPitches(
                                                books =
                                                    streamingState.data.books.map {
                                                        it.copy(
                                                            draft =
                                                                it.draft.copy(
                                                                    id = UUID.randomUUID().toString(),
                                                                ),
                                                            characters =
                                                                it.characters.map {
                                                                    it.copy(
                                                                        id =
                                                                            UUID
                                                                                .randomUUID()
                                                                                .toString(),
                                                                    )
                                                                },
                                                        )
                                                    },
                                                message = streamingState.data.welcomeMessage,
                                            ),
                                        )
                                    }

                                    is StreamingState.Error -> {
                                        emit(AgenticFlowResponse.Error(Exception(streamingState.message)))
                                    }
                                }
                            }
                    } else if (lockedCharacter == null) {
                        val conversationStyle =
                            genreConfigService.conversationBlueprint(lockedSaga.genre)
                        reasoningSynthesizerService
                            .synthesizeReasoning(
                                sourceFlow =
                                    characterIdeationService.suggestCharacters(
                                        lockedSaga,
                                        prompt,
                                    ),
                                context = "Creating characters for the saga",
                                conversationStyle = conversationStyle,
                                targetLanguage = targetLanguage,
                            ).collect { streamingState ->
                                when (streamingState) {
                                    is StreamingState.Reasoning -> {
                                        emit(AgenticFlowResponse.Log(streamingState.chunk))
                                    }

                                    is StreamingState.Success -> {
                                        emit(
                                            AgenticFlowResponse.CharacterPitches(
                                                personas = streamingState.data.ideas,
                                                message = streamingState.data.message,
                                            ),
                                        )
                                    }

                                    is StreamingState.Error -> {
                                        emit(AgenticFlowResponse.Error(Exception(streamingState.message)))
                                    }
                                }
                            }
                    } else {
                        emit(
                            AgenticFlowResponse.RefinedDraft(
                                lockedSaga,
                                lockedCharacter,
                            ),
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(
                        AgenticFlowResponse
                            .Error(e),
                    )
                }
            }

        override suspend fun provideInitialEchoes() = sagaIdeationService.suggestUniverseEchoes()

        override fun sealSacredContract(
            sagaDraft: SagaDraft,
            characterInfo: CharacterInfo,
        ): Flow<SagaCreationState> =
            flow {
                try {
                    val identity = genreConfigService.conversationBlueprint(sagaDraft.genre)
                    val targetLanguage = getSessionLanguage()

                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            sourceFlow =
                                sagaIdeationService.sealSacredContract(
                                    sagaDraft,
                                    characterInfo,
                                    identity,
                                ),
                            context = "Sealing the Sacred Contract for ${sagaDraft.title}",
                            conversationStyle = identity,
                            targetLanguage = targetLanguage,
                        ).collect { streamingState ->
                            when (streamingState) {
                                is StreamingState.Reasoning -> {
                                    emit(SagaCreationState.Loading(streamingState.chunk))
                                }

                                is StreamingState.Success -> {
                                    val contract = streamingState.data
                                    // Step 1: Save Saga
                                    val savedSagaResult = createSaga(contract.saga)
                                    val savedSaga =
                                        savedSagaResult.getSuccess()
                                            ?: throw Exception("Failed to save saga")

                                    // Step 2: Save Character
                                    val characterToSave = contract.character.copy(sagaId = savedSaga.id)
                                    val savedCharacter =
                                        characterUseCase.insertCharacter(characterToSave)

                                    // Step 3: Update Saga with character reference
                                    val finalizedSaga =
                                        savedSaga.copy(mainCharacterId = savedCharacter.id)
                                    val updatedSaga =
                                        updateSaga(finalizedSaga).getSuccess() ?: finalizedSaga

                                    // Step 4: Stream Character Icon Manifestation
                                    characterUseCase
                                        .generateCharacterImageStream(savedCharacter, updatedSaga)
                                        .collect { charIconState ->
                                            when (charIconState) {
                                                is StreamingState.Reasoning -> {
                                                    emit(SagaCreationState.Loading(charIconState.chunk))
                                                }

                                                is StreamingState.Success -> {
                                                    val finalCharacter =
                                                        charIconState.data.data.first
                                                    // Step 5: Stream Saga Icon Manifestation
                                                    sagaRepository
                                                        .generateSagaIconStream(
                                                            updatedSaga,
                                                            listOf(finalCharacter),
                                                        ).collect { sagaIconState ->
                                                            when (sagaIconState) {
                                                                is StreamingState.Reasoning -> {
                                                                    emit(
                                                                        SagaCreationState.Loading(
                                                                            sagaIconState.chunk,
                                                                        ),
                                                                    )
                                                                }

                                                                is StreamingState.Success -> {
                                                                    emit(
                                                                        SagaCreationState.Success(
                                                                            sagaIconState.data,
                                                                            finalCharacter,
                                                                        ),
                                                                    )
                                                                }

                                                                is StreamingState.Error -> {
                                                                    emit(
                                                                        SagaCreationState.Success(
                                                                            updatedSaga,
                                                                            finalCharacter,
                                                                        ),
                                                                    )
                                                                }
                                                            }
                                                        }
                                                }

                                                is StreamingState.Error -> {
                                                    // Fallback to Saga Icon even if character icon fails
                                                    sagaRepository
                                                        .generateSagaIconStream(
                                                            updatedSaga,
                                                            listOf(savedCharacter),
                                                        ).collect { sagaIconState ->
                                                            if (sagaIconState is StreamingState.Success) {
                                                                emit(
                                                                    SagaCreationState.Success(
                                                                        sagaIconState.data,
                                                                        savedCharacter,
                                                                    ),
                                                                )
                                                            } else if (sagaIconState is StreamingState.Error) {
                                                                emit(
                                                                    SagaCreationState.Success(
                                                                        updatedSaga,
                                                                        savedCharacter,
                                                                    ),
                                                                )
                                                            }
                                                        }
                                                }
                                            }
                                        }
                                }

                                is StreamingState.Error -> {
                                    emit(SagaCreationState.Error(Exception(streamingState.message)))
                                }
                            }
                        }
                } catch (e: Exception) {
                    emit(SagaCreationState.Error(e))
                }
            }

        private fun getSessionLanguage(): String =
            java.util.Locale
                .getDefault()
                .displayLanguage
    }
