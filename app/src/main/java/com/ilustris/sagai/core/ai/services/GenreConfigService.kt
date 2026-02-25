package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenreConfigService
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
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
                artStyle = variation.artStyle ?: baseConfig.artStyle,
                renderingInstructions =
                    variation.renderingInstructions
                        ?: baseConfig.renderingInstructions,
                appearanceGuidelines =
                    variation.appearanceGuidelines
                        ?: baseConfig.appearanceGuidelines,
                conversationDirective =
                    variation.conversationDirective
                        ?: baseConfig.conversationDirective,
                criticalRules = variation.criticalRules ?: baseConfig.criticalRules,
            )
        }.getSuccess()!!
    }
