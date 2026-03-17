package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary

data class SuggestionArgs(
    val characterName: String,
    val sagaMainContext: String,
    val sceneSummaryContext: String,
    val conversationHistory: String,
)

object SuggestionPrompts {
    @Suppress("ktlint:standard:max-line-length")
    suspend fun generateSuggestionsPrompt(
        promptService: PromptService,
        saga: SagaContent,
        character: Character,
        sceneSummary: SceneSummary,
        promptDirectives: PromptDirectives,
    ): String {
        val args =
            SuggestionArgs(
                characterName = character.name,
                sagaMainContext = SagaPrompts.mainContext(saga),
                sceneSummaryContext = sceneSummary.toAINormalize(),
                conversationHistory = ChatPrompts.conversationHistory(promptDirectives, saga, 7),
            )

        return promptService.buildRemotePrompt("saga_input_suggestions_blueprint", args)
    }
}
