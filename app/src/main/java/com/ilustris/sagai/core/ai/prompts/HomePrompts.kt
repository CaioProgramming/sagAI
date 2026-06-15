package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.features.newsaga.data.model.Genre

data class DynamicSagaCreationArgs(
    val genreEnumNames: String,
)

object HomePrompts {
    const val DYNAMIC_SAGA_CREATION_BLUEPRINT = "dynamic_saga_creation_blueprint"

    suspend fun dynamicSagaCreationPrompt(promptService: PromptService): SplitPrompt {
        val args =
            DynamicSagaCreationArgs(
                genreEnumNames = Genre.entries.joinToString(", ") { it.name },
            )

        return promptService.buildSplitBlueprint(DYNAMIC_SAGA_CREATION_BLUEPRINT, args.asMap())
    }
}
