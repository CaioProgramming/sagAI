package com.ilustris.sagai.features.saga.detail.review.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
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

class SagaReviewUseCaseImpl
    @Inject
    constructor(
        val genreConfigService: GenreConfigService,
        val gemmaClient: GemmaClient,
        val promptService: PromptService,
        val synthesizerService: ReasoningSynthesizerService,
        val sagaRepository: SagaRepository,
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

                executeRequest {
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
