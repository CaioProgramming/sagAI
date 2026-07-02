package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService

data class DynamicSagaCreationArgs(
    val genreEnumNames: String,
    val genreAesthetics: String = "",
)

object HomePrompts {
    const val DYNAMIC_SAGA_CREATION_BLUEPRINT = "dynamic_saga_creation_blueprint"

    suspend fun dynamicSagaCreationPrompt(
        promptService: PromptService,
        genreAesthetics: String = "",
    ): SplitPrompt {
        val args =
            DynamicSagaCreationArgs(
                genreEnumNames = genreAesthetics,
                genreAesthetics = genreAesthetics,
            )

        return promptService.buildSplitBlueprint(
            DYNAMIC_SAGA_CREATION_BLUEPRINT,
            mapOf(
                "AvailableThemes" to genreAesthetics,
            ),
        )
    }
}
