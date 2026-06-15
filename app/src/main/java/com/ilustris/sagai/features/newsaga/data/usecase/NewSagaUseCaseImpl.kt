package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.LibraryPitchesResponse
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.service.SagaIdeationService
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NewSagaUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val characterUseCase: CharacterUseCase,
        private val sagaIdeationService: SagaIdeationService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
    ) : NewSagaUseCase {
        override fun executePrompt(
            prompt: String,
            excludedGenres: List<Genre>,
        ): Flow<StreamingState<LibraryPitchesResponse?>> =
            flow {
                val sourceFlow = sagaIdeationService.generateCosmicLibrary(prompt, excludedGenres)
                emitAll(
                    reasoningSynthesizerService.synthesizeReasoning(
                        sourceFlow = sourceFlow,
                        context = "Curating your cosmic library",
                    ),
                )
            }

        override suspend fun provideInitialEchoes() = sagaIdeationService.suggestUniverseEchoes()

        override fun sealSacredContract(
            sagaDraft: SagaDraft,
            characterInfo: CharacterInfo,
        ): Flow<SagaCreationState> =
            flow {
                try {
                    reasoningSynthesizerService
                        .synthesizeReasoning(
                            sourceFlow =
                                sagaIdeationService.sealSacredContract(
                                    sagaDraft,
                                    characterInfo,
                                ),
                            context = "Sealing the Sacred Contract for ${sagaDraft.title}",
                            genre = sagaDraft.genre,
                        ).collect { streamingState ->
                            when (streamingState) {
                                is StreamingState.Reasoning -> {
                                    emit(SagaCreationState.Loading(streamingState.chunk))
                                }

                                is StreamingState.Success -> {
                                    val contract = streamingState.data!!
                                    val savedSagaResult = saveSaga(contract.saga)
                                    val savedSaga =
                                        savedSagaResult.getSuccess()
                                            ?: throw Exception("Failed to save saga")

                                    val characterToSave =
                                        contract.character.copy(
                                            sagaId = savedSaga.id,
                                            image = emptyString(),
                                        )
                                    val savedCharacter =
                                        characterUseCase.insertCharacter(characterToSave)

                                    val finalizedSaga =
                                        savedSaga.copy(mainCharacterId = savedCharacter.id)
                                    val updatedSaga =
                                        updateSaga(finalizedSaga).getSuccess() ?: finalizedSaga

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
                                                    sagaRepository
                                                        .generateSagaIconStream(
                                                            updatedSaga,
                                                            listOf(savedCharacter),
                                                        ).collect { sagaIconState ->
                                                            when (sagaIconState) {
                                                                is StreamingState.Success -> {
                                                                    emit(
                                                                        SagaCreationState.Success(
                                                                            sagaIconState.data,
                                                                            savedCharacter,
                                                                        ),
                                                                    )
                                                                }

                                                                is StreamingState.Error -> {
                                                                    emit(
                                                                        SagaCreationState.Success(
                                                                            updatedSaga,
                                                                            savedCharacter,
                                                                        ),
                                                                    )
                                                                }

                                                                is StreamingState.Reasoning -> {
                                                                    emit(
                                                                        SagaCreationState.Loading(
                                                                            sagaIconState.chunk,
                                                                        ),
                                                                    )
                                                                }
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

        private suspend fun saveSaga(saga: Saga): RequestResult<Saga> =
            executeRequest {
                sagaRepository.saveChat(saga)
            }

        private suspend fun updateSaga(saga: Saga): RequestResult<Saga> =
            executeRequest {
                sagaRepository.updateSaga(saga)
            }
    }
