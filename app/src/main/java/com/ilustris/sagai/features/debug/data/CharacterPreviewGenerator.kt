package com.ilustris.sagai.features.debug.data

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.debug.ui.DesignSystemMocks
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.utils.getRandomColorHex
import javax.inject.Inject
import javax.inject.Singleton

/** A character as the generation produced it and as it came out of the design review. */
data class CharacterPreview(
    val generated: Character,
    val reviewed: Character,
)

/**
 * Runs the real character-generation pipeline and the design review for [Genre] against an in-memory
 * mock saga whose cast is [generate]'s `cast`. Never touches the database, so it is safe to spam while
 * tuning genre appearance blueprints. Shared by the design system preview and the dev harness.
 */
@Singleton
class CharacterPreviewGenerator
    @Inject
    constructor(
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
        private val gemmaClient: GemmaClient,
        private val characterUseCase: CharacterUseCase,
    ) {
        suspend fun generate(
            genre: Genre,
            cast: List<Character>,
        ): CharacterPreview {
            val sagaContent =
                DesignSystemMocks.mockSagaContent(genre).let { mock ->
                    mock.copy(characters = cast.map { CharacterContent(data = it) })
                }
            val themeColor = getRandomColorHex()
            val prompt =
                CharacterPrompts.characterGeneration(
                    promptService,
                    sagaContent,
                    description = PREVIEW_DESCRIPTION,
                    themeColor = themeColor,
                    sceneSummary = null,
                    aesthetic = genreConfigService.aesthetic(genre),
                )
            val generated =
                gemmaClient
                    .generate<Character>(
                        promptSplit =
                            prompt.mergeInstructions(
                                genreConfigService.buildAesthetic(genre),
                                genreConfigService.appearanceInstructions(genre),
                            ),
                        filterOutputFields =
                            listOf("id", "image", "joinedAt", "sagaId", "voice", "hexColor", "firstSceneId"),
                        requirement = ModelRequirement.HIGH,
                        temperatureRandomness = 1f,
                    )!!
                    .copy(hexColor = themeColor, image = emptyString())
            return CharacterPreview(
                generated = generated,
                reviewed = characterUseCase.reviewCharacterDesign(sagaContent, generated, PREVIEW_DESCRIPTION),
            )
        }

        private companion object {
            const val PREVIEW_DESCRIPTION = "A new character who could believably exist in this world."
        }
    }
