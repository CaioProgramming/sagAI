package com.ilustris.sagai.features.wiki.domain.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import javax.inject.Inject

class EmotionalUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) : EmotionalUseCase {
        override suspend fun generateEmotionalReview(
            content: List<String>,
            emotionalRanking: Map<String, Int>,
        ): RequestResult<Exception, String> =
            try {
                val prompt = EmotionalPrompt.generateEmotionalReview(content, emotionalRanking)
                gemmaClient
                    .generate<String>(
                        prompt = prompt,
                        requireTranslation = true,
                    )!!
                    .asSuccess()
            } catch (e: Exception) {
                e.asError()
            }

        override suspend fun generateEmotionalProfile(saga: SagaContent): RequestResult<Exception, String> =
            executeRequest {
                gemmaClient.generate<String>(
                    prompt = EmotionalPrompt.generateEmotionalProfile(saga.emotionalSummary()),
                )!!
            }
    }
