package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.SagaContent
import javax.inject.Inject

class EmotionalUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) : EmotionalUseCase {
        override suspend fun generateEmotionalReview(
            sagaContent: SagaContent,
            context: String,
        ): RequestResult<String> =
            executeRequest {
                val prompt = EmotionalPrompt.generateEmotionalReview(sagaContent, context)
                gemmaClient
                    .generate<String>(
                        prompt = prompt,
                        requirement = GemmaClient.ModelRequirement.MEDIUM,
                    )!!
            }

        override suspend fun generateEmotionalProfile(
            sagaContent: SagaContent,
            emotionalSummary: String,
        ): RequestResult<String> =
            executeRequest {
                if (emotionalSummary.isEmpty()) error("No summary provided can't generate profile.")
                gemmaClient.generate<String>(
                    prompt = EmotionalPrompt.generateEmotionalProfile(emotionalSummary),
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                )!!
            }

        override suspend fun generateEmotionalConclusion(sagaContent: SagaContent): RequestResult<String> =
            executeRequest {
                gemmaClient.generate<String>(
                    prompt = EmotionalPrompt.generateEmotionalConclusion(sagaContent),
                    requirement = GemmaClient.ModelRequirement.HIGH,
                )!!
            }
    }
