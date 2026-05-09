package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.prompts.EmotionalPrompt
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.MascotEmotionService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.emotional.data.model.EmotionalProfile
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaEnding
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class EmotionalUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val remoteConfigService: RemoteConfigService,
        private val mascotEmotionService: MascotEmotionService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
    ) : EmotionalUseCase {
        override suspend fun generateEmotionalReview(
            sagaContent: SagaContent,
            context: String,
        ): RequestResult<EmotionalProfile> =
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
                    .generate<EmotionalProfile>(
                        prompt = prompt,
                        requirement = GemmaClient.ModelRequirement.MEDIUM,
                    )!!
            }

        override suspend fun generateEmotionalConclusion(sagaContent: SagaContent) =
            executeRequest {
                val conversationDirective =
                    genreConfigService.conversationBlueprint(sagaContent.data.genre)

                val result =
                    gemmaClient.generate<SagaEnding>(
                        prompt =
                            SagaPrompts.generateSagaEnding(
                                promptService,
                                sagaContent,
                                conversationDirective,
                            ),
                        requirement = GemmaClient.ModelRequirement.HIGH,
                        blueprintKey = SagaPrompts.SAGA_ENDING_BLUEPRINT,
                    )!!
                result
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

        override fun streamEmotionalReview(
            sagaContent: SagaContent,
            context: String,
        ): Flow<StreamingState<EmotionalProfile>> =
            flow {
                reasoningSynthesizerService
                    .synthesizeReasoning(
                        gemmaClient.generateStreaming<EmotionalProfile>(
                            prompt =
                                EmotionalPrompt.generateEmotionalReview(
                                    promptService,
                                    sagaContent,
                                    context,
                                    genreConfigService.conversationBlueprint(sagaContent.data.genre),
                                ),
                            requirement = GemmaClient.ModelRequirement.MEDIUM,
                        ),
                        "Generating emotional review...",
                    ).collect { emit(it) }
            }

        override fun streamEmotionalConclusion(sagaContent: SagaContent): Flow<StreamingState<SagaEnding>> =
            flow {
                reasoningSynthesizerService
                    .synthesizeReasoning(
                        gemmaClient.generateStreaming<SagaEnding>(
                            prompt =
                                SagaPrompts.generateSagaEnding(
                                    promptService,
                                    sagaContent,
                                    genreConfigService.conversationBlueprint(sagaContent.data.genre),
                                ),
                            requirement = GemmaClient.ModelRequirement.HIGH,
                            blueprintKey = SagaPrompts.SAGA_ENDING_BLUEPRINT,
                        ),
                        "Generating emotional conclusion...",
                    ).collect { emit(it) }
            }

        override fun streamEmotionalCard(sagaContent: SagaContent): Flow<StreamingState<String>> =
            flow {
                reasoningSynthesizerService
                    .synthesizeReasoning(
                        gemmaClient.generateStreaming<String>(
                            prompt = "Get mental card icon for emotional profile",
                            requirement = GemmaClient.ModelRequirement.LOW,
                        ),
                        "Retrieving emotional card...",
                    ).collect { emit(it) }
            }

        override fun streamEmotionalMascot(
            sagaContent: SagaContent,
            timelineContent: TimelineContent,
        ): Flow<StreamingState<String>> =
            flow {
                reasoningSynthesizerService
                    .synthesizeReasoning(
                        gemmaClient.generateStreaming<String>(
                            prompt = "Get mascot emotion URL for ${sagaContent.data.genre} and ${
                                timelineContent.emotionalRanking().first().first
                            }",
                            requirement = GemmaClient.ModelRequirement.LOW,
                        ),
                        "Loading emotional mascot...",
                    ).collect { emit(it) }
            }
    }
