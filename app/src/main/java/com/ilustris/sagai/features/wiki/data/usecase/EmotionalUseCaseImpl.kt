package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import kotlinx.coroutines.delay
import javax.inject.Inject

class EmotionalUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) : EmotionalUseCase {
        override suspend fun generateEmotionalReview(
            content: List<String>,
            emotionalRanking: Map<String, Int>,
        ): RequestResult<String> =
            executeRequest {
                val prompt = EmotionalPrompt.generateEmotionalReview(content, emotionalRanking)
                gemmaClient
                    .generate<String>(
                        prompt = prompt,
                        requireTranslation = true,
                    )!!
            }

        override suspend fun generateEmotionalProfile(saga: SagaContent): RequestResult<String> =
            executeRequest {
                gemmaClient.generate<String>(
                    prompt = EmotionalPrompt.generateEmotionalProfile(saga.emotionalSummary()),
                )!!
            }
    }
