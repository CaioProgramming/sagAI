package com.ilustris.sagai.features.newsaga.data.service

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.NewSagaPrompts
import com.ilustris.sagai.core.ai.prompts.SagaIdeationArgs
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import javax.inject.Inject

class SagaIdeationService
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
    ) {
        suspend fun suggestSagas(userPrompt: String) =
            executeRequest {
                val genreMaps =
                    Genre.entries.map {
                        mapOf(
                            "theme" to it.name,
                            "themeAesthetic" to genreConfigService.conversationBlueprint(it),
                        )
                    }
                val blueprint =
                    promptService.buildRemotePrompt(
                        NewSagaPrompts.SAGA_IDEATION_BLUEPRINT,
                        SagaIdeationArgs(userPrompt = userPrompt, themes = genreMaps.toJsonFormat()),
                    )
                gemmaClient.generate<SagaIdeas>(
                    blueprint,
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                    temperatureRandomness = 1f,
                    filterOutputFields = listOf("id", "variationId"),
                )
            }
    }

data class SagaIdeas(
    val ideas: List<SagaDraft>,
    val message: String = "",
)
