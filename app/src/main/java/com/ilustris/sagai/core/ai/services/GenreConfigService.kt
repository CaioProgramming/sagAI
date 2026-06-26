package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.asMap
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

        suspend fun formatGenreAesthetics(genres: Collection<Genre> = Genre.entries): String =
            buildString {
                genres.forEach { genre ->
                    val summary = getGenreConfig(genre).aesthetic
                    if (summary.isBlank()) {
                        appendLine("- **${genre.name}**")
                    } else {
                        appendLine("- **${genre.name}**: $summary")
                    }
                }
        }.trimEnd()

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

        suspend fun renderingInstructions(genre: Genre) =
            promptService
                .fetchBlueprintData(
                    "${genre.name.lowercase()}_rendering_blueprint",
                ).asMap()

        @Deprecated(
            message = "Use conversationInstructions() merged into SplitPrompt via mergeInstructions()",
            replaceWith = ReplaceWith("conversationInstructions(genre)"),
        )
        suspend fun conversationBlueprint(genre: Genre): String =
            promptService
                .buildSplitBlueprint("${genre.name.lowercase()}_conversation_blueprint")
                .processedTemplate
    }
