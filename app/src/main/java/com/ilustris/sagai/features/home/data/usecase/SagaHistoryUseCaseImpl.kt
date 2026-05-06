package com.ilustris.sagai.features.home.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SagaHistoryUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val textGenClient: TextGenClient,
        private val gemmaClient: GemmaClient,
        private val genreConfigService: GenreConfigService,
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
        private val remoteConfigService: com.ilustris.sagai.core.services.RemoteConfigService,
    ) : SagaHistoryUseCase {
        override suspend fun getSagaById(sagaId: Int): Flow<SagaContent?> = sagaRepository.getSagaById(sagaId)

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
                val conversationDirective =
                    genreConfigService.conversationBlueprint(saga.data.genre)
                gemmaClient
                    .generate<String>(
                        SagaPrompts.endCredits(promptService, saga, conversationDirective),
                        blueprintKey = SagaPrompts.SAGA_END_CREDITS_BLUEPRINT,
                    )!!
            }

        override fun generateEndMessageStream(saga: SagaContent): Flow<StreamingState<GeneratedContent<String>>> =
            kotlinx.coroutines.flow.flow {
                try {
                    genreConfigService.getGenreConfig(saga.data.genre)
                    val conversationDirective =
                        genreConfigService.conversationBlueprint(saga.data.genre)
                    gemmaClient
                        .generateStreaming<GeneratedContent<String>>(
                            prompt = SagaPrompts.endCredits(promptService, saga, conversationDirective),
                            requireTranslation = true,
                            useCore = true,
                            requirement = GemmaClient.ModelRequirement.HIGH,
                            blueprintKey = SagaPrompts.SAGA_END_CREDITS_BLUEPRINT,
                        ).collect { state ->
                            emit(state)
                        }
                } catch (e: Exception) {
                    emit(StreamingState.Error(e.message ?: "Unknown error"))
                }
            }

        override fun generateSagaEndingStream(
            saga: SagaContent,
        ): Flow<StreamingState<GeneratedContent<com.ilustris.sagai.features.home.data.model.SagaEnding>>> =
            kotlinx.coroutines.flow.flow {
                try {
                    genreConfigService.getGenreConfig(saga.data.genre)
                    val conversationDirective =
                        genreConfigService.conversationBlueprint(saga.data.genre)
                    gemmaClient
                        .generateStreaming<GeneratedContent<com.ilustris.sagai.features.home.data.model.SagaEnding>>(
                            prompt =
                                SagaPrompts.generateSagaEnding(
                                    promptService,
                                    saga,
                                    conversationDirective,
                                ),
                            requireTranslation = true,
                            useCore = true,
                            requirement = GemmaClient.ModelRequirement.HIGH,
                            blueprintKey = SagaPrompts.SAGA_ENDING_BLUEPRINT,
                        ).collect { state ->
                            emit(state)
                        }
                } catch (e: Exception) {
                    emit(StreamingState.Error(e.message ?: "Unknown error"))
                }
            }

        override suspend fun backupSaga(saga: SagaContent) = executeRequest { sagaRepository.backupSaga(saga) }
    }
