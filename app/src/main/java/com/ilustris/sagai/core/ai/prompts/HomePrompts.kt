package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService

object HomePrompts {
    const val DYNAMIC_SAGA_CREATION_BLUEPRINT = "dynamic_saga_creation_blueprint"

    suspend fun dynamicSagaCreationPrompt(
        promptService: PromptService,
        genreAesthetic: String,
        userName: String = "",
    ): SplitPrompt =
        promptService.buildSplitBlueprint(
            DYNAMIC_SAGA_CREATION_BLUEPRINT,
            buildMap {
                put("genreAesthetic", genreAesthetic)
                if (userName.isNotBlank()) {
                    put("userName", userName)
                }
            },
        )
}
