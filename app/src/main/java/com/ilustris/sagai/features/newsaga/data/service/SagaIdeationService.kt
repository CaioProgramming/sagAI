package com.ilustris.sagai.features.newsaga.data.service

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.CosmicLibraryArgs
import com.ilustris.sagai.core.ai.prompts.NewSagaPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
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
        private val genreConfigService: GenreConfigService,
    ) {
        suspend fun generateCosmicLibrary(
            userPrompt: String,
            excludedGenres: List<Genre> = emptyList(),
        ): Flow<StreamingState<LibraryPitchesResponse?>> {
            val availableGenres = Genre.entries - excludedGenres.toSet()
            val themes = availableGenres.joinToString(", ") { it.name }
            val genreAesthetics = genreConfigService.formatGenreAesthetics()
            val splitPrompt =
                promptService.buildSplitBlueprint(
                    NewSagaPrompts.COSMIC_LIBRARY_BLUEPRINT,
                    CosmicLibraryArgs(
                        userPrompt = userPrompt,
                        themes = genreConfigService.formatGenreAesthetics(),
                        genreAesthetics = genreAesthetics,
                    ),
                )
            return gemmaClient.generateStreaming<LibraryPitchesResponse>(
                promptSplit = splitPrompt,
                requirement = ModelRequirement.MEDIUM,
                temperatureRandomness = 1f,
                filterOutputFields = listOf("id", "variationId"),
            )
        }

        suspend fun suggestUniverseEchoes() =
            executeRequest {
                val genreAesthetics = genreConfigService.formatGenreAesthetics()
                val splitPrompt =
                    promptService.buildSplitBlueprint(
                        NewSagaPrompts.UNIVERSE_ECHOES_BLUEPRINT,
                        mapOf(
                            "themes" to genreAesthetics,
                        ),
                    )
                gemmaClient.generate<UniverseSuggestions>(
                    promptSplit = splitPrompt,
                    temperatureRandomness = .5f,
                    requirement = ModelRequirement.LOW,
                )!!
            }

        suspend fun sealSacredContract(
            sagaDraft: SagaDraft,
            characterInfo: CharacterInfo,
        ): Flow<StreamingState<SacredContract?>> {
            val splitPrompt =
                NewSagaPrompts
                    .sacredBindingPrompt(
                        promptService,
                        sagaDraft,
                        characterInfo,
                        themeStyle = genreConfigService.aesthetic(sagaDraft.genre),
                    ).mergeInstructions(genreConfigService.conversationInstructions(sagaDraft.genre))
            return gemmaClient.generateStreaming<SacredContract>(
                promptSplit = splitPrompt,
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
                        "emotionalProfile",
                    ),
            )
        }
    }
