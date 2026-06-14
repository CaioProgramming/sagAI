package com.ilustris.sagai.features.newsaga.data.service

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
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
        suspend fun generateCosmicLibrary(
            userPrompt: String,
            excludedGenres: List<Genre> = emptyList(),
        ): Flow<StreamingState<LibraryPitchesResponse?>> {
            val availableGenres = Genre.entries - excludedGenres.toSet()
            val themes = availableGenres.joinToString(", ") { it.name }
            val blueprint =
                promptService.buildRemotePrompt(
                    NewSagaPrompts.COSMIC_LIBRARY_BLUEPRINT,
                    CosmicLibraryArgs(userPrompt = userPrompt, themes = themes),
                )
            return gemmaClient.generateStreaming<LibraryPitchesResponse>(
                blueprint,
                requirement = ModelRequirement.MEDIUM,
                temperatureRandomness = 1f,
                filterOutputFields = listOf("id", "variationId"),
                blueprintKey = NewSagaPrompts.COSMIC_LIBRARY_BLUEPRINT,
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
                    temperatureRandomness = 1f,
                    blueprintKey = NewSagaPrompts.UNIVERSE_ECHOES_BLUEPRINT,
                    requirement = ModelRequirement.MEDIUM,
                )!!
            }

        suspend fun sealSacredContract(
            sagaDraft: SagaDraft,
            characterInfo: CharacterInfo,
            identity: String,
        ): Flow<StreamingState<SacredContract?>> {
            val blueprint =
                NewSagaPrompts.sacredBindingPrompt(promptService, sagaDraft, characterInfo, identity)
            return gemmaClient.generateStreaming<SacredContract>(
                blueprint,
                requirement = ModelRequirement.HIGH,
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
                        "currentActId",
                        "endMessage",
                        "endedAt",
                        "icon",
                        "isDebug",
                        "isEnded",
                        "review",
                        "variationId",
                        "image",
                        "emojified",
                    ),
                blueprintKey = NewSagaPrompts.SACRED_BINDING_BLUEPRINT,
            )
        }
    }
