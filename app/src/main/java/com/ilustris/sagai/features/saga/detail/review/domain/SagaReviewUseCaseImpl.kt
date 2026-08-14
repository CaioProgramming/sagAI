package com.ilustris.sagai.features.saga.detail.review.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.player.domain.UserIdentityUseCase
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.detail.data.model.Farewell
import com.ilustris.sagai.features.saga.detail.data.model.FarewellSet
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.data.usecase.ReviewState
import com.ilustris.sagai.features.saga.detail.review.domain.model.ReviewSteps
import com.ilustris.sagai.features.saga.detail.review.domain.model.buildArgs
import com.ilustris.sagai.features.saga.detail.review.domain.model.isPresentIn
import com.ilustris.sagai.features.saga.detail.review.domain.model.withStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

private const val FAREWELL_CHARACTER_COUNT = 4

class SagaReviewUseCaseImpl
    @Inject
    constructor(
        val genreConfigService: GenreConfigService,
        val gemmaClient: GemmaClient,
        val promptService: PromptService,
        val synthesizerService: ReasoningSynthesizerService,
        val sagaRepository: SagaRepository,
        val userIdentityUseCase: UserIdentityUseCase,
    ) : SagaReviewUseCase {
        override suspend fun createReview(content: SagaContent): Flow<ReviewState> =
            flow {
                executeRequest {
                    var currentReview = content.data.review ?: Review()
                    if (currentReview.isComplete()) {
                        emit(ReviewState.Success(content.data))
                        return@executeRequest
                    }

                    for (step in ReviewSteps.entries) {
                        if (step.isPresentIn(currentReview)) continue
                        generateStep(content, step, currentReview).collect { state ->
                            when (state) {
                                is ReviewState.Loading -> {
                                    emit(state)
                                }

                                is ReviewState.StepComplete -> {
                                    currentReview = state.saga.review ?: currentReview
                                    emit(state)
                                }

                                is ReviewState.Success -> {
                                    currentReview = state.saga.review ?: currentReview
                                    emit(state)
                                }

                                is ReviewState.Error -> {
                                    error(state.message)
                                }
                            }
                        }
                    }

                    val finalSaga =
                        sagaRepository.getSagaById(content.data.id).first()?.data
                            ?: content.data.copy(review = currentReview)
                    emit(ReviewState.Success(finalSaga))
                }
            }

        override suspend fun generateStep(
            content: SagaContent,
            step: ReviewSteps,
            existingReview: Review?,
        ): Flow<ReviewState> =
            flow {
                if (step.isPresentIn(existingReview)) {
                    emit(ReviewState.Success(content.data))
                    return@flow
                }

                val userName = userIdentityUseCase.getNameNow().ifBlank { "Player" }

                if (step == ReviewSteps.FAREWELLS) {
                    executeRequest {
                        val protagonist = content.mainCharacter
                        val topCharacters =
                            content
                                .flatMessages()
                                .rankTopCharacters(
                                    content.characters
                                        .filter { it.data.id != protagonist?.data?.id }
                                        .map { it.data },
                                ).take(FAREWELL_CHARACTER_COUNT)
                                .mapNotNull { (character, _) ->
                                    content.characters.find { it.data.id == character.id }
                                }

                        val arcsByCharacterId = topCharacters.associate { it.data.id to it.arcs }

                        val prompt =
                            promptService.buildSplitBlueprint(
                                step.blueprintKey,
                                buildMap {
                                    putAll(genreConfigService.buildAesthetic(content.data.genre))
                                    put(
                                        "Story",
                                        content.data.toAINormalize(SagaPrompts.SAGA_EXCLUDED_FIELDS),
                                    )
                                    put(
                                        "MainCharacter",
                                        content.mainCharacter!!.data.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS),
                                    )
                                    put(
                                        "Characters",
                                        CharacterPrompts.sceneCharactersContext(
                                            topCharacters,
                                            arcsByCharacterId,
                                            protagonist,
                                        ),
                                    )
                                },
                            )

                        val result =
                            gemmaClient.generate<FarewellSet>(
                                promptSplit =
                                    prompt.mergeInstructions(
                                        genreConfigService.conversationInstructions(content.data.genre),
                                    ),
                                requirement = step.requirement,
                            ) ?: error("Failed to generate review step: ${step.name}")

                        // Zip by position, not by an AI-echoed id: the model was only asked for
                        // messages in the same order the characters were listed in the prompt.
                        val farewells =
                            topCharacters.zip(result.messages) { character, message ->
                                Farewell(character.data.id, message)
                            }
                        val partialReview = (existingReview ?: Review()).copy(farewells = farewells)
                        val updatedSaga = content.data.copy(review = partialReview)
                        sagaRepository.updateSaga(updatedSaga)
                        emit(ReviewState.StepComplete(step, updatedSaga))
                        if (partialReview.isComplete()) {
                            emit(ReviewState.Success(updatedSaga))
                        }
                    }
                    return@flow
                }

                executeRequest {
                    val prompt =
                        promptService.buildSplitBlueprint(
                            step.blueprintKey,
                            buildMap {
                                putAll(genreConfigService.buildAesthetic(content.data.genre))
                                put("playerName", userName)
                                put(
                                    "Story",
                                    content.data.toAINormalize(SagaPrompts.SAGA_EXCLUDED_FIELDS),
                                )
                                put(
                                    "MainCharacter",
                                    content.mainCharacter!!.data.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS),
                                )
                                put("Context", step.buildArgs(content).toAINormalize())
                            },
                        )

                    val sourceFlow =
                        gemmaClient.generateStreaming<ReviewStage>(
                            promptSplit =
                                prompt.mergeInstructions(
                                    genreConfigService.conversationInstructions(content.data.genre),
                                ),
                            requirement = step.requirement,
                        )

                    var stage: ReviewStage? = null
                    synthesizerService
                        .synthesizeReasoning(
                            sourceFlow = sourceFlow,
                            context = "Creating Saga review.",
                            genre = content.data.genre,
                        ).collect { state ->
                            when (state) {
                                is StreamingState.Reasoning -> {
                                    emit(ReviewState.Loading(state.chunk, step))
                                }

                                is StreamingState.Success -> {
                                    stage = state.data!!
                                }

                                is StreamingState.Error -> {
                                    error(state.message)
                                }
                            }
                        }

                    val completedStage =
                        stage ?: error("Failed to generate review step: ${step.name}")
                    val partialReview = (existingReview ?: Review()).withStep(step, completedStage)
                    val updatedSaga = content.data.copy(review = partialReview)
                    sagaRepository.updateSaga(updatedSaga)
                    emit(ReviewState.StepComplete(step, updatedSaga))
                    if (partialReview.isComplete()) {
                        emit(ReviewState.Success(updatedSaga))
                    }
                }
            }
    }
