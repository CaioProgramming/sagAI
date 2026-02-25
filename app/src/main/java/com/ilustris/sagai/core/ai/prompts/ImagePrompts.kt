package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.model.ImageConfig
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.model.ReviewerStrictness
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * The specific keys used to inject variables into Remote Config master templates.
 * This object serves as the documentation for which variables are available to developers in Firebase.
 */
object PromptKeys {
    const val GENRE = "genre"
    const val CONTEXT = "context"
    const val IMAGE_TYPE = "imageType"
    const val ART_STYLE = "artStyle"
    const val CONVERSATION_DIRECTIVE = "conversationDirective"
    const val APPEARANCE_GUIDELINES = "appearanceGuidelines"
    const val COLOR_PALETTE = "colorPalette"
    const val CRITICAL_VALIDATION = "criticalValidation"
    const val REVIEWER_STRICTNESS = "reviewerStrictness"
    const val VALIDATION_RULES = "validationRules"
    const val CRITICAL_RULES = "criticalRules"
    const val VISUAL_DIRECTION = "visualDirection"
    const val FINAL_PROMPT = "finalPrompt"
}

object AgentIds {
    const val DIRECTOR = "director"
    const val ARTIST = "artist"
    const val REVIEWER = "reviewer"
}

object ImagePrompts {

    /**
     * Dumb template engine: replaces {key} with the corresponding value.
     */
    private fun String.injectVariables(variables: Map<String, String>): String {
        var result = this
        variables.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }
        return result
    }

    /**
     * A unified god function that executes any AI agent by feeding it the corresponding PromptKeys.
     */
    fun generateAgentPrompt(
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

        val variables =
            mapOf(
                PromptKeys.GENRE to genre.name,
                PromptKeys.CONTEXT to context,
                PromptKeys.IMAGE_TYPE to imageType.name.replace("_", " "),
                PromptKeys.ART_STYLE to config.artStyle,
                PromptKeys.CONVERSATION_DIRECTIVE to config.conversationDirective,
                PromptKeys.APPEARANCE_GUIDELINES to config.appearanceGuidelines,
                PromptKeys.COLOR_PALETTE to config.colorPalette,
                PromptKeys.CRITICAL_VALIDATION to (
                    config.criticalValidation.takeIf { it.isNotBlank() }
                        ?: ""
                ),
                PromptKeys.REVIEWER_STRICTNESS to
                    (
                        config.reviewerStrictness
                            ?: ReviewerStrictness.STRICT
                    ).description,
                PromptKeys.VALIDATION_RULES to
                    config.getValidationRules(
                        genre.name,
                        config.colorPalette,
                    ),
                PromptKeys.CRITICAL_RULES to imageConfig.criticalRules,
                PromptKeys.VISUAL_DIRECTION to (visualDirection ?: ""),
            PromptKeys.FINAL_PROMPT to (finalPrompt ?: "")
        )

        return agentTemplate.injectVariables(variables)
    }

    // Wrappers to maintain compatibility with ImagenClientImpl until we refactor it too.
    fun generateDirectorialVision(
        genre: Genre,
        config: GenreConfig,
        imageConfig: ImageConfig,
        imageType: ImageType,
        context: String,
    ) = generateAgentPrompt(AgentIds.DIRECTOR, genre, config, imageConfig, imageType, context)

    fun generateArtistPrompt(
        genre: Genre,
        config: GenreConfig,
        imageConfig: ImageConfig,
        imageType: ImageType,
        visualDirection: String?,
        context: String,
    ) = generateAgentPrompt(
        AgentIds.ARTIST,
        genre,
        config,
        imageConfig,
        imageType,
        context,
        visualDirection
    )

    fun reviewImagePrompt(
        visualDirection: String?,
        config: GenreConfig,
        imageConfig: ImageConfig,
        imageType: ImageType,
        finalPrompt: String,
        genre: Genre = Genre.CYBERPUNK,
        context: String,
    ) = generateAgentPrompt(
        AgentIds.REVIEWER,
        genre,
        config,
        imageConfig,
        imageType,
        context,
        visualDirection,
        finalPrompt
    )
}
