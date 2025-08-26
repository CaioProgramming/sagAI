package com.ilustris.sagai.features.wiki.domain.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import javax.inject.Inject

class EmotionalUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) : EmotionalUseCase {
        override suspend fun generateEmotionalReview(content: List<String>): RequestResult<Exception, String> =
            try {
                val prompt = EmotionalPrompt.generateEmotionalReview(content)
                gemmaClient
                    .generate<String>(
                        prompt = prompt,
                        requireTranslation = true,
                    )!!
                    .asSuccess()
            } catch (e: Exception) {
                e.asError()
            }
    }
