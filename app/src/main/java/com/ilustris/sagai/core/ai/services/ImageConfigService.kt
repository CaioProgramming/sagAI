package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.ImageType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageConfigService
    @Inject
    constructor(
        private val promptService: PromptService,
    ) {
        suspend fun getImageConfig(imageType: ImageType): String {
            val configBlueprintKey = "${imageType.name.lowercase()}_config_blueprint"
            return promptService.buildRemotePrompt(configBlueprintKey, emptyMap())
        }
    }
