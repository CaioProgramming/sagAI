package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.SuggestionGen
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage

object SuggestionPrompts {
    @Suppress("ktlint:standard:max-line-length")
    fun generateSuggestionsPrompt(
        character: Character,
        sceneSummary: SceneSummary,
    ): String =
        buildString {
            appendLine("You are an AI assistant for a text-based role-playing game.")
            appendLine(
                "Your goal is to provide three concise, creative, contextually relevant, and actionable input suggestions for the player, who is currently embodying '${character.name}'.",
            )
            appendLine("Character context:")
            appendLine(
                character.toJsonFormatExcludingFields(listOf("hexColor", "image", "id", "sagaId", "joinedAt", "details", "emojified")),
            )
            appendLine("Story actual context:")
            appendLine(sceneSummary.toJsonFormat())
            appendLine("Task:")
            appendLine(
                "Based on all the information above, generate exactly 3 distinct input suggestions for the player controlling '${character.name}'.",
            )
            appendLine("Each suggestion must include the `text` of the suggestion and its `type`.")
            appendLine(
                "The suggestions should guide the player's next interaction and be strongly influenced by the character's personality, traits, motivations, and current mood.",
            )
            appendLine("")
            appendLine("Allowed `type` values (from your game's SenderType system):")
            appendLine("- \"USER\": For dialogue spoken by '${character.name}'.")
            appendLine("  The text must be the exact words spoken by the character, in the first person, as direct speech.")
            appendLine(
                "  Dialogue must reflect the character’s personality, current mood, and be coherent with the scene context and recent events.",
            )
            appendLine(
                "  Avoid phrases that describe the act of speaking or the intention to speak, such as 'I say...', 'I ask...', 'I tell them...', 'I will ask about...'. Generate only the spoken words.",
            )
            appendLine("- \"THOUGHT\": For an internal thought of '${character.name}'.")
            appendLine(
                "  The text should be an introspective, reflective thought, revealing doubts, motivations, or internal reactions to the scene.",
            )
            appendLine("- \"ACTION\": For a physical action '${character.name}' performs.")
            appendLine(
                "  The text should be a concise description of a physical action, directly related to the character’s intentions or goals in the current context. Avoid vague or generic actions.",
            )
            appendLine(
                "- \"NARRATOR\": To suggest a focus on an environmental detail or a subtle observation that '${character.name}' might notice or want to investigate further.",
            )
            appendLine(
                "  The text should focus on story progression, environmental cues, or new developments, not character inner thoughts.",
            )
            appendLine("")
            appendLine(
                "Ensure suggestions are creative, contextually relevant, brief, and feel natural for the character in the current context.",
            )
            appendLine("")
            appendLine("Output Format:")
            appendLine("Return ONLY a valid JSON array of objects. Each object must have two fields:")
            appendLine("{ \"suggestions\": [ ${toJsonMap(Suggestion::class.java)} }")
            appendLine("Do not include any other text, explanations, or markdown.")
            appendLine(OutputRules.outputRule(toJsonMap(SuggestionGen::class.java)))
        }.trimIndent()
}
