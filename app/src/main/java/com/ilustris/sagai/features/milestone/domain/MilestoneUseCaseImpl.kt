package com.ilustris.sagai.features.milestone.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.MilestonePrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import timber.log.Timber
import javax.inject.Inject

class MilestoneUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val genreConfigService: GenreConfigService,
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
    ) : MilestoneUseCase {
        override suspend fun generateCongratsMessage(
            milestone: SagaMilestone,
            saga: SagaContent,
        ): RequestResult<String?> =
            executeRequest(false) {
                Timber.tag("MilestoneUseCase").d(
                    "Generating congrats message for ${milestone.javaClass.simpleName}",
                )

                if (milestone is SagaMilestone.Loading) {
                    error("Loading and TitleSplash don't need message")
                }

                val identity = genreConfigService.conversationBlueprint(saga.data.genre)

                val prompt =
                    MilestonePrompts.generateCongratsMessage(
                        promptService,
                        milestone,
                        saga,
                        identity,
                    )!!

                gemmaClient.generate<String>(
                    prompt,
                    temperatureRandomness = 1f,
                    requirement = GemmaClient.ModelRequirement.LOW,
                    blueprintKey = MilestonePrompts.MILESTONE_GENERATION_BLUEPRINT,
                )
            }
    }
