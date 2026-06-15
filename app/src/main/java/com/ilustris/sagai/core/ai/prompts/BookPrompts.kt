package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService

data class BookGenerationArgs(
    val sagaContext: String,
    val actSummary: String,
    val characters: String,
    val conversationDirective: String,
    val isFinalVolume: Boolean,
)

object BookPrompts {
    const val BOOK_CHRONICLE_BLUEPRINT = "book_chronicle_blueprint"

    suspend fun generateBookChronicle(
        promptService: PromptService,
        args: BookGenerationArgs,
    ): SplitPrompt =
        promptService.buildSplitBlueprint(
            BOOK_CHRONICLE_BLUEPRINT,
            args,
        )
}
