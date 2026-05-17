package com.ilustris.sagai.features.wiki.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.prompts.SagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.MascotEmotionService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaEnding
import com.ilustris.sagai.features.timeline.data.model.Timeline
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

        override suspend fun getEmotionalCard(saga: Saga): RequestResult<String> =
            executeRequest {
                if (saga.emotionalReview == null) error("Emotional profile not ready yet")
                remoteConfigService.getString("mental_card_icon")!!
            }

        override suspend fun getEmotionalMascot(
            sagaContent: Saga,
            timelineContent: Timeline?,
        ) = executeRequest {
            mascotEmotionService
                .getEmotionUrl(
                    genre = sagaContent.genre,
                    tone = timelineContent!!.emotionalTone!!,
                )!!
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
    }
