package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.prompts.NewSagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.CreationAssist
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.ui.presentation.FlowPages
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import javax.inject.Inject

class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
        private val genreConfigService: GenreConfigService,
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
    ) : NewSagaUseCase {
        override suspend fun createSaga(saga: Saga): RequestResult<Saga> =
            executeRequest {
                sagaRepository.saveChat(saga)
            }

        override suspend fun updateSaga(saga: Saga): RequestResult<Saga> =
            executeRequest {
                sagaRepository.updateChat(saga)
            }

        override suspend fun generateSaga(
            sagaForm: SagaDraft,
            miniChatContent: List<ChatMessage>,
        ): RequestResult<Saga> =
            executeRequest {
                val config = genreConfigService.getGenreConfig(sagaForm.genre)
                gemmaClient.generate<Saga>(
                    NewSagaPrompts.createSagaPrompt(
                        promptService,
                        sagaForm,
                        miniChatContent,
                        config.variations ?: mapOf(),
                        config.companion,
                    ),
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

                // Single AI call to extract, enhance, and provide helpful suggestions
                val response =
                    gemmaClient.generate<SagaCreationGen>(
                        NewSagaPrompts.conversationalSagaReply(
                            promptService = promptService,
                            currentSagaDraft = currentFormData.saga,
                            userInput = userInput,
                            conversationHistory = recentMessages,
                            availableVariations = config.variations ?: mapOf(),
                            companion = config.companion,
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

        override suspend fun generateCharacterSavedMark(
            character: Character,
            saga: Saga,
        ): RequestResult<String> =
            executeRequest {
                val config = genreConfigService.getGenreConfig(saga.genre)
                gemmaClient.generate(
                    NewSagaPrompts.characterSavedPrompt(
                        promptService,
                        character,
                        saga,
                        config.companion,
                    ),
                )!!
            }

        override suspend fun generateProcessMessage(
            process: SagaProcess,
            sagaDescription: String,
            characterDescription: String,
            genre: Genre?,
        ): RequestResult<String> =
            executeRequest {
                val config = genre?.let { genreConfigService.getGenreConfig(it) }
                gemmaClient.generate(
                    NewSagaPrompts.generateProcessPrompt(
                        promptService,
                        process,
                        sagaDescription,
                        characterDescription,
                        config?.companion,
                    ),
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                )!!
            }

        override suspend fun adaptSagaToGenre(sagaDraft: SagaDraft): RequestResult<SagaCreationGen> =
            executeRequest {
                val config = genreConfigService.getGenreConfig(sagaDraft.genre)
                gemmaClient.generate(
                    NewSagaPrompts.genreAdaptationPrompt(
                        promptService,
                        sagaDraft,
                        config.companion,
                    ),
                )!!
            }

        override suspend fun generateGenreSuggestions(genre: Genre): RequestResult<SagaCreationGen> =
            executeRequest {
                val config = genreConfigService.getGenreConfig(genre)
                gemmaClient.generate(
                    NewSagaPrompts.genreSuggestionsPrompt(
                        promptService,
                        genre,
                        config.companion,
                    ),
                )!!
            }

        override suspend fun refineDraft(
            rawInput: String,
            genre: Genre,
        ): RequestResult<SagaCreationGen> =
            executeRequest {
                val config = genreConfigService.getGenreConfig(genre)
                gemmaClient.generate(
                    NewSagaPrompts.refineDraftPrompt(
                        promptService,
                        rawInput,
                        genre,
                        config.companion,
                    ),
                    requireTranslation = true,
                )!!
            }

        override suspend fun assistCreation(
            flow: FlowPages,
            sagaDraft: SagaDraft?,
            characterInfo: CharacterInfo?,
        ): RequestResult<CreationAssist> =
            executeRequest {
                val config = sagaDraft?.genre?.let { genreConfigService.getGenreConfig(it) }
                gemmaClient.generate(
                    NewSagaPrompts.creationAssistPrompt(
                        promptService,
                        flow,
                        sagaDraft,
                        characterInfo,
                        config,
                    ),
                    requireTranslation = true,
                )!!
            }
    }
