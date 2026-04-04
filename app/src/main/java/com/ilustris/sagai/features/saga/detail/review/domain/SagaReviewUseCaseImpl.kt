package com.ilustris.sagai.features.saga.detail.review.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.LoadingService
import com.ilustris.sagai.core.services.LoadingType
import com.ilustris.sagai.core.utils.emptyString
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
        val loadingService: LoadingService,
        val sagaRepository: SagaRepository,
    ) : SagaReviewUseCase {
        override suspend fun createReview(content: SagaContent) =
            flow {
                executeRequest {
                    val config =
                        genreConfigService.getGenreConfig(content.data.genre, content.data.variationId)

                    val stepsMap =
                        ReviewSteps.entries.associateWith {
                            val loadingMessage =
                                loadingService.generateLoadingMessage(
                                    LoadingType(it.loadingKey),
                                    conversationStyle = config.conversationDirective,
                                ) ?: emptyString()
                            emit(ReviewState.Loading(loadingMessage))
                            val review =
                                gemmaClient.generate<ReviewStage>(
                                    promptService.buildRemotePrompt(
                                        it.blueprintKey,
                                        it.buildArgs(content, config.conversationDirective),
                                    ),
                                )!!
                            review
                        }

                    val review = stepsMap.buildReview()

                    val finalSaga = content.data.copy(review = review)
                    sagaRepository.updateChat(finalSaga)
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
