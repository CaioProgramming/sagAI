package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
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

        override suspend fun generateEmotionalProfile(emotionalSummary: List<String>): RequestResult<String> =
            executeRequest {
                if (emotionalSummary.isEmpty()) error("No summary provided can't generate profile.")
                gemmaClient.generate<String>(
                    prompt = EmotionalPrompt.generateEmotionalProfile(emotionalSummary),
                )!!
            }
    }
