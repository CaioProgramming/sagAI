package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.model.mergeInstructionBuckets
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.newsaga.data.model.Genre
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenreConfigService
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
        private val promptService: PromptService,
    ) {
        suspend fun getGenreConfig(
            genre: Genre,
            variationId: String? = null,
        ) = executeRequest {
            val baseConfig = remoteConfigService.getJson<GenreConfig>(genre.configKey)!!
            if (variationId == null) {
                return@executeRequest baseConfig
            }

            val variation =
                baseConfig.variations?.get(variationId) ?: return@executeRequest baseConfig

            baseConfig.copy(
                aesthetic = variation.aesthetic?.takeIf { it.isNotBlank() } ?: baseConfig.aesthetic,
            )
        }.getSuccess()!!

        suspend fun aesthetic(genre: Genre): String = getGenreConfig(genre).aesthetic

        suspend fun getRandomGenreAesthetic() = mapGenreAesthetic(Genre.entries.random()).toAINormalize()

        suspend fun mapGenreAesthetic(genre: Genre) = mapOf(genre.name to aesthetic(genre))

        suspend fun formatGenreAesthetics(genres: List<Genre> = Genre.entries): String =
            genres
                .map {
                    mapGenreAesthetic(it)
                }.normalizetoAIItems(describeName = false)

        suspend fun conversationInstructions(genre: Genre) =
            promptService
                .buildSplitBlueprint(
                    "${genre.name.lowercase()}_conversation_blueprint",
                ).renderInstructions()

        suspend fun appearanceInstructions(genre: Genre) =
            promptService
                .buildSplitBlueprint(
                    "${genre.name.lowercase()}_appearance_blueprint",
                ).renderInstructions()

        suspend fun buildAesthetic(genre: Genre) = mapOf("ThemeAesthetic" to getGenreConfig(genre).aesthetic)

        suspend fun renderingInstructions(genre: Genre) =
            buildAesthetic(genre).plus(
                promptService
                    .buildSplitBlueprint(
                        "${genre.name.lowercase()}_rendering_blueprint",
                    ).renderInstructions(),
            )

        /**
         * The theme's `${genre}_cover_composition_blueprint` as instruction buckets: how its covers are composed
         * and what may be their subject (a character, a key object, a place). Remote-only, so a theme without
         * that key has none and covers fall back to the generic cover blueprint.
         */
        suspend fun coverCompositionInstructions(genre: Genre): Map<String, Any> {
            val key = "${genre.name.lowercase()}_cover_composition_blueprint"
            if (remoteConfigService.getString(key, false).isNullOrBlank()) return emptyMap()
            return promptService.buildSplitBlueprint(key).renderInstructions()
        }

        /** Rendering instructions for an image; covers also get the theme's cover composition. */
        suspend fun imageInstructions(
            genre: Genre,
            imageType: ImageType,
        ): Map<String, Any> {
            val rendering = renderingInstructions(genre)
            if (imageType != ImageType.COVER) return rendering
            return mergeInstructionBuckets(rendering, coverCompositionInstructions(genre))
        }

        @Deprecated(
            message = "Use conversationInstructions() merged into SplitPrompt via mergeInstructions()",
            replaceWith = ReplaceWith("conversationInstructions(genre)"),
        )
        suspend fun conversationBlueprint(genre: Genre): String =
            promptService
                .buildSplitBlueprint("${genre.name.lowercase()}_conversation_blueprint")
                .processedTemplate
    }
