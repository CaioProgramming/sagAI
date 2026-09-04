package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaEnding
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SagaHistoryUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
        private val genreConfigService: GenreConfigService,
        private val promptService: PromptService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
    ) : SagaHistoryUseCase {
        override suspend fun getSagaById(sagaId: Int?): Flow<SagaContent?> = sagaRepository.getSagaById(sagaId)

        override suspend fun getSagaMetadata(sagaId: Int): Flow<com.ilustris.sagai.features.home.data.model.SagaMetadata?> =
            sagaRepository.getSagaMetadata(sagaId)

        override suspend fun updateSaga(saga: Saga) = sagaRepository.updateSaga(saga)

        override suspend fun createFakeSaga(): RequestResult<Saga> =
            executeRequest {
                sagaRepository
                    .saveChat(
                        Saga(
                            title = "Debug Saga",
                            description = "This saga was created to debug purposes only.",
                            genre = Genre.entries.random(),
                            isDebug = true,
                        ),
                    )
            }

        override suspend fun generateEndMessage(saga: SagaContent): RequestResult<String> =
            executeRequest {
                genreConfigService.getGenreConfig(saga.data.genre)
                val prompt = SagaPrompts.endCredits(promptService, saga, emptyString())
                gemmaClient
                    .generate<String>(
                        promptSplit =
                            prompt.mergeInstructions(
                                genreConfigService.conversationInstructions(saga.data.genre),
                            ),
                    )!!
            }

        override fun generateEndMessageStream(saga: SagaContent): Flow<StreamingState<GeneratedContent<String>?>> =
            flow {
                try {
                    genreConfigService.getGenreConfig(saga.data.genre)
                    val prompt = SagaPrompts.endCredits(promptService, saga, emptyString())
                    gemmaClient
                        .generateStreaming<GeneratedContent<String>>(
                            promptSplit =
                                prompt.mergeInstructions(
                                    genreConfigService.conversationInstructions(saga.data.genre),
                                ),
                            requireTranslation = true,
                            requirement = ModelRequirement.HIGH,
                        ).collect { state ->
                            emit(state)
                        }
                } catch (e: Exception) {
                    emit(StreamingState.Error(e.message ?: "Unknown error"))
                }
            }

        override fun generateSagaEndingStream(saga: SagaContent): Flow<StreamingState<GeneratedContent<SagaEnding>?>> =
            flow {
                try {
                    val prompt =
                        SagaPrompts.generateSagaEnding(
                            promptService,
                            saga,
                            emptyString(),
                        )
                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            gemmaClient
                                .generateStreaming<GeneratedContent<SagaEnding>>(
                                    promptSplit =
                                        prompt.mergeInstructions(
                                            genreConfigService.conversationInstructions(saga.data.genre),
                                        ),
                                    requireTranslation = true,
                                    requirement = ModelRequirement.HIGH,
                                ),
                            "Generating saga ending... ",
                            genre = saga.data.genre,
                        ).collect { state ->

                            if (state is StreamingState.Success) {
                                val ending = state.data!!.data
                                sagaRepository.updateSaga(
                                    saga.data.copy(
                                        endMessage = ending.endingMessage,
                                        emotionalProfile = ending.emotionalProfile,
                                        emotionalReview = ending.endingMessage,
                                        isEnded = true,
                                        endedAt = System.currentTimeMillis(),
                                    ),
                                )
                            }
                            emit(state)
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(StreamingState.Error(e.message ?: "Unknown error"))
                }
            }

        override suspend fun backupSaga(saga: SagaContent) = executeRequest { sagaRepository.backupSaga(saga) }
    }
