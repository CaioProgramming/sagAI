package com.ilustris.sagai.features.newsaga.data.service

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.prompts.CosmicLibraryArgs
import com.ilustris.sagai.core.ai.prompts.NewSagaPrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.LibraryPitchesResponse
import com.ilustris.sagai.features.newsaga.data.model.SacredContract
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.UniverseSuggestions
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SagaIdeationService
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
    ) {
        suspend fun generateCosmicLibrary(userPrompt: String): Flow<StreamingState<LibraryPitchesResponse>> {
            val themes = Genre.entries.joinToString(", ") { it.name }
            val blueprint =
                promptService.buildRemotePrompt(
                    NewSagaPrompts.COSMIC_LIBRARY_BLUEPRINT,
                    CosmicLibraryArgs(userPrompt = userPrompt, themes = themes),
                )
            return gemmaClient.generateStreaming<LibraryPitchesResponse>(
                blueprint,
                requirement = GemmaClient.ModelRequirement.HIGH,
                temperatureRandomness = 1f,
                filterOutputFields = listOf("id", "variationId"),
            )
        }

        suspend fun suggestUniverseEchoes() =
            executeRequest {
                val themes = Genre.entries.joinToString(", ") { it.name }
                val blueprint =
                    promptService.buildRemotePrompt(
                        NewSagaPrompts.UNIVERSE_ECHOES_BLUEPRINT,
                        mapOf("themes" to themes),
                    )
                gemmaClient.generate<UniverseSuggestions>(
                    blueprint,
                    requirement = GemmaClient.ModelRequirement.LOW,
                    temperatureRandomness = 1f,
                    blueprintKey = NewSagaPrompts.UNIVERSE_ECHOES_BLUEPRINT,
                )!!
            }

        suspend fun sealSacredContract(
            sagaDraft: SagaDraft,
            characterInfo: CharacterInfo,
            identity: String,
        ): Flow<StreamingState<SacredContract>> {
            val blueprint =
                NewSagaPrompts.sacredBindingPrompt(promptService, sagaDraft, characterInfo, identity)
            return gemmaClient.generateStreaming<SacredContract>(
                blueprint,
                requirement = GemmaClient.ModelRequirement.HIGH,
                filterOutputFields =
                    listOf(
                        "id",
                        "sagaId",
                        "createdAt",
                        "joinedAt",
                        "mainCharacterId",
                        "emotionalReview",
                        "playTimeMs",
                        "characterEvents",
                        "voice",
                        "narratorVoice",
                        "timelineId",
                    ),
            )
        }
    }
