package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.CharacterExamples
import com.ilustris.sagai.core.ai.model.GenreConfig
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
                ).renderInstructions() + characterExamples(genre)

        /**
         * A few concrete looks and temperaments of the genre, a different draw on every call so the
         * model sees the level of specificity wanted without a fixed set to copy. Empty when the genre
         * has none in Remote Config.
         */
        suspend fun characterExamples(genre: Genre): Map<String, Any> {
            val examples =
                remoteConfigService.getJson<CharacterExamples>(
                    "${genre.name.lowercase()}_character_examples",
                    logEnabled = false,
                ) ?: return emptyMap()
            val looks = examples.looks.shuffled().take(LOOKS_PER_CALL)
            val temperaments = examples.temperaments.shuffled().take(TEMPERAMENTS_PER_CALL)
            if (looks.isEmpty() && temperaments.isEmpty()) return emptyMap()
            return buildMap {
                if (examples.note.isNotBlank()) put("ExamplesNote", examples.note)
                if (looks.isNotEmpty()) put("ExampleLooks", looks.toAINormalize())
                if (temperaments.isNotEmpty()) put("ExampleTemperaments", temperaments.toAINormalize())
            }
        }

        suspend fun buildAesthetic(genre: Genre) = mapOf("ThemeAesthetic" to getGenreConfig(genre).aesthetic)

        suspend fun renderingInstructions(genre: Genre) =
            buildAesthetic(genre).plus(
                promptService
                    .buildSplitBlueprint(
                        "${genre.name.lowercase()}_rendering_blueprint",
                    ).renderInstructions(),
            )

        @Deprecated(
            message = "Use conversationInstructions() merged into SplitPrompt via mergeInstructions()",
            replaceWith = ReplaceWith("conversationInstructions(genre)"),
        )
        suspend fun conversationBlueprint(genre: Genre): String =
            promptService
                .buildSplitBlueprint("${genre.name.lowercase()}_conversation_blueprint")
                .processedTemplate

        private companion object {
            const val LOOKS_PER_CALL = 4
            const val TEMPERAMENTS_PER_CALL = 3
        }
    }
