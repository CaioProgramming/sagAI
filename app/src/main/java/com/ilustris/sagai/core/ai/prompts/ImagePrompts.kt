package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.model.ReviewerStrictness
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.newsaga.data.model.Genre

object AgentIds {
    const val DIRECTOR = "director"
    const val ARTIST = "artist"
    const val REVIEWER = "reviewer"
}

object ImagePrompts {
    suspend fun generateAgentPrompt(
        promptService: PromptService,
        agentId: String,
        genre: Genre,
        config: GenreConfig,
        visualSoul: String,
        imageType: ImageType,
        context: String,
        visualDirection: String? = null,
        finalPrompt: String? = null,
    ): String {
        val blueprintKey = "${imageType.name.lowercase()}_${agentId}_blueprint"

        val argsMap =
            mutableMapOf(
                "genre" to genre.name,
                "context" to context,
                "imageType" to imageType.name.replace("_", " "),
                "genreVisualSoul" to visualSoul,
                "reviewerStrictness" to ReviewerStrictness.STRICT,
                "visualDirection" to (visualDirection ?: ""),
                "finalPrompt" to (finalPrompt ?: ""),
            )

        return promptService.buildRemotePrompt(blueprintKey, argsMap)
    }

    suspend fun generateDirectorialVision(
        promptService: PromptService,
        genre: Genre,
        config: GenreConfig,
        visualSoul: String,
        imageType: ImageType,
        context: String,
    ) = generateAgentPrompt(
        promptService,
        AgentIds.DIRECTOR,
        genre,
        config,
        visualSoul,
        imageType,
        context,
    )

    suspend fun generateArtistPrompt(
        promptService: PromptService,
        genre: Genre,
        config: GenreConfig,
        imageType: ImageType,
        visualSoul: String,
        visualDirection: String?,
        context: String,
    ) = generateAgentPrompt(
        promptService,
        AgentIds.ARTIST,
        genre,
        config,
        visualSoul,
        imageType,
        context,
        visualDirection,
    )

    suspend fun reviewImagePrompt(
        promptService: PromptService,
        visualDirection: String?,
        config: GenreConfig,
        visualSoul: String,
        imageType: ImageType,
        finalPrompt: String,
        genre: Genre = Genre.CYBERPUNK,
        context: String,
    ) = generateAgentPrompt(
        promptService,
        AgentIds.REVIEWER,
        genre,
        config,
        visualSoul,
        imageType,
        context,
        visualDirection,
        finalPrompt,
    )
}
