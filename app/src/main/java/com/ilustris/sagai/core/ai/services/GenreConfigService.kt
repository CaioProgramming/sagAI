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
        private val promptService: PromptService,
    ) {
        suspend fun getGenreConfig(
            genre: Genre,
            variationId: String? = null,
        ) = executeRequest {
            val baseConfig = remoteConfigService.getJson<GenreConfig>(genre.configKey)!!
            if (variationId == null) return@executeRequest baseConfig

            // Variations are now lightweight (name + description only).
            // Blueprint-based overrides are handled via separate variation blueprint keys.
            baseConfig.variations?.get(variationId) ?: baseConfig
            baseConfig
        }.getSuccess()!!

        /**
         * Fetches and renders the genre's conversation blueprint.
         * This is the narrative voice / storytelling soul used by chat agents.
         * No variables needed — the blueprint is self-contained.
         */
        suspend fun conversationBlueprint(genre: Genre): String =
            promptService.buildRemotePrompt(
                "${genre.name.lowercase()}_conversation_blueprint",
                emptyMap<String, String>(),
            )

        /**
         * Fetches and renders the genre's visual soul blueprint.
         * This is the world-grounding context used by Director/Artist/Reviewer agents.
         * No variables needed — the blueprint is self-contained.
         */
        suspend fun visualSoulBlueprint(genre: Genre): String =
            promptService.buildRemotePrompt(
                "${genre.name.lowercase()}_visual_blueprint",
                emptyMap<String, String>(),
            )

        suspend fun renderingBlueprint(genre: Genre): String =
            promptService.buildRemotePrompt(
                "${genre.name.lowercase()}_rendering_blueprint",
            emptyMap(),
        )
    }
