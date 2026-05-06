package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.model.ImageConfig
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.features.newsaga.data.model.Genre

data class UnifiedImageArgs(
    val genre: String,
    val context: String,
    val imageType: String,
    val artStyle: String,
    val appearanceGuidelines: String,
    val colorPalette: String,
    val validationRules: String,
    val criticalRules: String,
    val renderingInstructions: String,
    val aspectRatio: String,
)

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

    suspend fun buildUnifiedImagePrompt(
        promptService: com.ilustris.sagai.core.ai.services.PromptService,
        genre: Genre,
        config: GenreConfig,
        imageConfig: ImageConfig,
        imageType: ImageType,
        context: String,
    ): String {
        val args =
            UnifiedImageArgs(
                genre = genre.name,
                context = context,
                imageType = imageType.name.replace("_", " "),
                artStyle = config.artStyle,
                appearanceGuidelines = config.appearanceGuidelines,
                colorPalette = config.colorPalette,
                validationRules = config.getValidationRules(genre.name),
                criticalRules = imageConfig.criticalRules,
                renderingInstructions = config.renderingInstructions,
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

        val remoteConfigKey = "unified_${imageType.name.lowercase()}_blueprint"

        return promptService.buildRemotePrompt(remoteConfigKey, args)
    }
}
