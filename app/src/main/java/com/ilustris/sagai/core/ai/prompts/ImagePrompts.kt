package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.model.ImageConfig
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.newsaga.data.model.Genre

object ImagePrompts {
    suspend fun buildUnifiedImagePrompt(
        promptService: PromptService,
        genre: Genre,
        config: GenreConfig,
        imageConfig: ImageConfig,
        imageType: ImageType,
        context: String,
    ): SplitPrompt {
        val aspectRatio =
            when (imageType) {
                ImageType.ICON -> {
                    config.iconAspectRatio
                        ?: imageConfig.typeConfigs[imageType.name]?.aspectRatio.orEmpty()
                }

                ImageType.COVER -> {
                    config.coverAspectRatio
                        ?: imageConfig.typeConfigs[imageType.name]?.aspectRatio.orEmpty()
                }
            }

        val remoteConfigKey = "unified_${imageType.name.lowercase()}_blueprint"

        return promptService.buildSplitBlueprint(
            remoteConfigKey,
            mapOf(
                "context" to context,
                "aesthetic" to config.aesthetic,
                "genre" to genre.name,
                "imageType" to imageType.name.replace("_", " "),
                "aspectRatio" to aspectRatio,
            ),
        )
    }
}
