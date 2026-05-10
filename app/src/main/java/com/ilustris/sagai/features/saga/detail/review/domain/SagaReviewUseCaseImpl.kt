package com.ilustris.sagai.features.saga.detail.review.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.features.saga.detail.data.usecase.ReviewState
import com.ilustris.sagai.features.saga.detail.review.domain.model.ReviewSteps
import com.ilustris.sagai.features.saga.detail.review.domain.model.buildArgs
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
        override suspend fun createReview(content: SagaContent) =
            flow {
                executeRequest {
                    genreConfigService.getGenreConfig(content.data.genre, content.data.variationId)
                    val stages = mutableMapOf<ReviewSteps, ReviewStage>()

                    ReviewSteps.entries.forEach { step ->
                        val prompt =
                            promptService.buildRemotePrompt(
                                step.blueprintKey,
                                step.buildArgs(
                                    content,
                                    genreConfigService.conversationBlueprint(
                                        content.data.genre,
                                    ),
                                ),
                            )

                        val sourceFlow =
                            gemmaClient.generateStreaming<ReviewStage>(
                                prompt,
                                requirement = GemmaClient.ModelRequirement.HIGH,
                                blueprintKey = step.blueprintKey,
                            )

                        synthesizerService
                            .synthesizeReasoning(
                                sourceFlow = sourceFlow,
                                context = step.name,
                                conversationStyle = genreConfigService.conversationBlueprint(content.data.genre),
                                genre = content.data.genre.name,
                            ).collect { state ->
                                when (state) {
                                    is StreamingState.Reasoning -> emit(ReviewState.Loading(state.chunk))
                                    is StreamingState.Success -> stages[step] = state.data!!
                                    is StreamingState.Error -> error(state.message)
                                }
                            }
                    }

                    val review = stages.buildReview()
                    val finalSaga = content.data.copy(review = review)
                    sagaRepository.updateSaga(finalSaga)
                    emit(ReviewState.Success(finalSaga))
                }
            }

        private fun Map<ReviewSteps, ReviewStage>.buildReview() =
            Review(
                introduction = getValue(ReviewSteps.INTRO),
                playstyle = getValue(ReviewSteps.PLAYSTYLE),
                topCharacters = getValue(ReviewSteps.CHARACTERS_STEP),
                actsInsight = getValue(ReviewSteps.ACTS_INSIGHT),
                expressiveness = getValue(ReviewSteps.EXPRESSIVENESS),
                conclusion = getValue(ReviewSteps.CONCLUSION),
            )
    }
