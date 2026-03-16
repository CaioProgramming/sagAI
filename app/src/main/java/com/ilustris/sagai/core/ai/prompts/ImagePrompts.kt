package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.model.ImageConfig
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.model.ReviewerStrictness
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.newsaga.data.model.Genre

object AgentIds {
    const val DIRECTOR = "director"
    const val ARTIST = "artist"
    const val REVIEWER = "reviewer"
}

data class ImagePromptArgs(
    val genre: String,
    val context: String,
    val imageType: String,
    val artStyle: String,
    val conversationDirective: String,
    val appearanceGuidelines: String,
    val colorPalette: String,
    val criticalValidation: String,
    val reviewerStrictness: String,
    val validationRules: String,
    val criticalRules: String,
    val visualDirection: String,
    val finalPrompt: String,
    val aspectRatio: String,
)

object ImagePrompts {
    /**
     * A unified god function that executes any AI agent by feeding it the ImagePromptArgs.
     */
    fun generateAgentPrompt(
        promptService: PromptService,
        agentId: String,
        genre: Genre,
        config: GenreConfig,
        imageConfig: ImageConfig,
        imageType: ImageType,
        context: String,
        visualDirection: String? = null,
        finalPrompt: String? = null,
    ): String {
        val agentTemplate = imageConfig.typeConfigs[imageType.name]?.getAgentTemplate(agentId) ?: ""

        val args =
            ImagePromptArgs(
                genre = genre.name,
                context = context,
                imageType = imageType.name.replace("_", " "),
                artStyle = config.artStyle,
                conversationDirective = config.conversationDirective,
                appearanceGuidelines = config.appearanceGuidelines,
                colorPalette = config.colorPalette,
                criticalValidation = config.criticalValidation.takeIf { it.isNotBlank() } ?: "",
                reviewerStrictness =
                    (
                        config.reviewerStrictness
                            ?: ReviewerStrictness.STRICT
                    ).description,
                validationRules = config.getValidationRules(genre.name, config.colorPalette),
                criticalRules = imageConfig.criticalRules,
                visualDirection = visualDirection ?: "",
                finalPrompt = finalPrompt ?: "",
                aspectRatio =
                    when (imageType) {
                        ImageType.ICON -> {
                            config.iconAspectRatio
                                ?: imageConfig.typeConfigs[imageType.name]?.aspectRatio ?: ""
                        }

                        ImageType.COVER -> {
                            config.coverAspectRatio
                                ?: imageConfig.typeConfigs[imageType.name]?.aspectRatio ?: ""
                        }
                    },
            )

        return promptService.buildPrompt(agentTemplate, args)
    }

    fun generateDirectorialVision(
        promptService: PromptService,
        genre: Genre,
        config: GenreConfig,
        imageConfig: ImageConfig,
        imageType: ImageType,
        context: String,
    ) = generateAgentPrompt(
        promptService,
        AgentIds.DIRECTOR,
        genre,
        config,
        imageConfig,
        imageType,
        context,
    )

    fun generateArtistPrompt(
        promptService: PromptService,
        genre: Genre,
        config: GenreConfig,
        imageConfig: ImageConfig,
        imageType: ImageType,
        visualDirection: String?,
        context: String,
    ) = generateAgentPrompt(
        promptService,
        AgentIds.ARTIST,
        genre,
        config,
        imageConfig,
        imageType,
        context,
        visualDirection,
    )

    fun reviewImagePrompt(
        promptService: PromptService,
        visualDirection: String?,
        config: GenreConfig,
        imageConfig: ImageConfig,
        imageType: ImageType,
        finalPrompt: String,
        genre: Genre = Genre.CYBERPUNK,
        context: String,
    ) = generateAgentPrompt(
        promptService,
        AgentIds.REVIEWER,
        genre,
        config,
        imageConfig,
        imageType,
        context,
        visualDirection,
        finalPrompt,
    )
}
