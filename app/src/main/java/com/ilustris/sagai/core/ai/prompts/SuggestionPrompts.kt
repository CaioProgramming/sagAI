package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary

object SuggestionPrompts {
    @Suppress("ktlint:standard:max-line-length")
    fun generateSuggestionsPrompt(
        saga: SagaContent,
        character: Character,
        sceneSummary: SceneSummary,
    ): String =
        buildString {
            appendLine("You are an AI assistant for a text-based role-playing game.")
            appendLine(
                "Your goal is to provide three concise, creative, contextually relevant, and actionable input suggestions for the player, who is currently embodying '${character.name}'.",
            )
            appendLine(SagaPrompts.mainContext(saga))
            appendLine("Story actual context:")
            appendLine(sceneSummary.toAINormalize())
            appendLine("Latest messages")
            appendLine(
                ChatPrompts.conversationHistory(saga, 7),
            )

            appendLine("\n# TAG-BASED EXPRESSION SYSTEM")
            appendLine("Suggestions can now use inline formatting tags to create richer, more expressive options:")
            appendLine("- <action>text</action> for physical movements")
            appendLine("- <think>text</think> for internal thoughts")
            appendLine("- <narrator>text</narrator> for scene context")
            appendLine("")
            appendLine("Examples: 'Hello <action>waves</action>', 'Sure. <think>This is risky.</think>'")
            appendLine("")

            appendLine("Task:")
            appendLine(
                "Based on all the information above, generate exactly 3 distinct input suggestions for the player controlling '${character.name}'.",
            )
            appendLine("Each suggestion should be a complete, expressive message ready to send.")
            appendLine("")
            appendLine("Suggestion Guidelines:")
            appendLine("1. **Focus on Tags:** Use <action>, <think>, <narrator> to create layered suggestions")
            appendLine("2. **Natural Mix:** Combine dialogue, actions, and thoughts when it enhances expression")
            appendLine("3. **Character Voice:** Reflect '${character.name}''s personality and current situation")
            appendLine("4. **Variety:** Offer different tones and approaches")
            appendLine("5. **Ready to Send:** Complete messages that drive story forward")
            appendLine("")
            appendLine("Examples:")
            appendLine("- \"I'll help. <action>extends hand</action> <think>I hope this works.</think>\"")
            appendLine("- \"<action>looks around</action> We should move quickly.\"")
            appendLine("- \"You're right. <think>But something feels off.</think>\"")
            appendLine("")
            appendLine("Output: Each suggestion must have `text` (with tags) and `type` (use \"CHARACTER\")")
            appendLine("")
        }.trimIndent()
}
