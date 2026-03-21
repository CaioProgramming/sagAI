package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.newsaga.data.model.Genre

data class DynamicSagaCreationArgs(
    val genreEnumNames: String,
)

object HomePrompts {
    suspend fun dynamicSagaCreationPrompt(promptService: PromptService): String {
        val args =
            DynamicSagaCreationArgs(
                genreEnumNames = Genre.entries.joinToString(", ") { it.name },
            )

        return promptService.buildRemotePrompt("dynamic_saga_creation_blueprint", args)
    }
}
