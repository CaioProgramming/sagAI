package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.MascotEmotionService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import javax.inject.Inject

class EmotionalUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val remoteConfigService: RemoteConfigService,
        private val mascotEmotionService: MascotEmotionService,
    ) : EmotionalUseCase {
        override suspend fun generateEmotionalReview(
            sagaContent: SagaContent,
            context: String,
        ): RequestResult<String> =
            executeRequest {
                val conversationDirective =
                    genreConfigService.conversationBlueprint(sagaContent.data.genre)
                val prompt =
                    EmotionalPrompt.generateEmotionalReview(
                        promptService,
                        sagaContent,
                        context,
                        conversationDirective,
                    )
                gemmaClient
                    .generate<String>(
                        prompt = prompt,
                        requirement = GemmaClient.ModelRequirement.MEDIUM,
                    )!!
            }

        override suspend fun generateEmotionalConclusion(sagaContent: SagaContent) =
            executeRequest {
                val conversationDirective =
                    genreConfigService.conversationBlueprint(sagaContent.data.genre)

                gemmaClient.generate<String>(
                    prompt =
                        EmotionalPrompt.generateEmotionalConclusion(
                            promptService,
                            promptService.getPromptDirectives(),
                            sagaContent,
                            conversationDirective,
                        ),
                    requirement = GemmaClient.ModelRequirement.HIGH,
                )!!
            }

        override suspend fun getEmotionalCard(sagaContent: SagaContent): RequestResult<String> =
            executeRequest {
                if (sagaContent.data.emotionalReview == null) error("Emotional profile not ready yet")
                remoteConfigService.getString("mental_card_icon")!!
            }

        override suspend fun getEmotionalMascot(
            sagaContent: SagaContent,
            timelineContent: TimelineContent,
        ) = executeRequest {
            mascotEmotionService
                .getEmotionUrl(
                    genre = sagaContent.data.genre,
                    tone = timelineContent.emotionalRanking().first().first!!,
            )!!
    }
    }
