package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.home.data.model.SagaContent
import javax.inject.Inject

class EmotionalUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
        private val genreConfigService: com.ilustris.sagai.core.ai.services.GenreConfigService,
        private val remoteConfigService: RemoteConfigService,
    ) : EmotionalUseCase {
        override suspend fun generateEmotionalReview(
            sagaContent: SagaContent,
            context: String,
        ): RequestResult<String> =
            executeRequest {
                val config = genreConfigService.getGenreConfig(sagaContent.data.genre)
                val prompt =
                    EmotionalPrompt.generateEmotionalReview(
                        promptService,
                        promptService.getPromptDirectives(),
                        sagaContent,
                        context,
                        config,
                    )
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
                val config = genreConfigService.getGenreConfig(sagaContent.data.genre)
                gemmaClient.generate<String>(
                    prompt =
                        EmotionalPrompt.generateEmotionalProfile(
                            promptService,
                            promptService.getPromptDirectives(),
                            emotionalSummary,
                            config,
                        ),
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                )!!
            }

        override suspend fun generateEmotionalConclusion(sagaContent: SagaContent): RequestResult<String> =
            executeRequest {
                val config = genreConfigService.getGenreConfig(sagaContent.data.genre)
                gemmaClient.generate<String>(
                    prompt =
                        EmotionalPrompt.generateEmotionalConclusion(
                            promptService,
                            promptService.getPromptDirectives(),
                            sagaContent,
                            config,
                        ),
                    requirement = GemmaClient.ModelRequirement.HIGH,
                )!!
            }
    }
