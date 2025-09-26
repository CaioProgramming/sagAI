package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SuggestionGen
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage

object SuggestionPrompts {
    @Suppress("ktlint:standard:max-line-length")
    fun generateSuggestionsPrompt(
        saga: Saga,
        character: Character,
        chatHistory: List<MessageContent>,
    ): String {
        val historyFormatted = chatHistory.joinToString { it.joinMessage().formatToString() }

        return buildString {
            appendLine("You are an AI assistant for a text-based role-playing game.")
            appendLine(
                "Your goal is to provide three concise, creative, contextually relevant, and actionable input suggestions for the player,",
            )
            appendLine("who is currently embodying '${character.name}'.")

            appendLine("Saga Context:")
            appendLine(
                saga.toJsonFormatExcludingFields(
                    listOf(
                        "currentActId",
                        "isEnded",
                        "endedAt",
                        "isDebug",
                        "endMessage",
                        "review",
                        "id",
                        "createdAt",
                        "icon",
                        "mainCharacterId",
                        "endMessage",
                    ),
                ),
            )

            appendLine("Character context:")
            appendLine(
                character.toJsonFormatExcludingFields(
                    listOf(
                        "hexColor",
                        "image",
                        "id",
                        "sagaId",
                        "joinedAt",
                        "appearance",
                        "height",
                        "weight",
                        "ethnicity",
                        "facialDetails",
                        "clothing",
                        "weapons",
                    ),
                ),
            )

            appendLine("Recent Chat History (for immediate context, newest messages are most relevant):")
            appendLine(historyFormatted)

            appendLine("Task:")
            appendLine(
                "Based on all the information above, generate exactly 3 distinct input suggestions for the player controlling '${character.name}'.",
            )
            appendLine("Each suggestion must include the `text` of the suggestion and its `type`.")
            appendLine("The suggestions should guide the player's next interaction.")

            appendLine("Allowed `type` values (from your game's SenderType system):")
            appendLine("- \"USER\": For dialogue spoken by '${character.name}'.")
            appendLine("  The text must be the exact words spoken by the character, in the first person, as direct speech.")
            appendLine(
                "  For example, instead of \"I ask Elara about the old book\", generate \"Elara, what can you tell me about this old book?\".",
            )
            appendLine(
                "  Avoid phrases that describe the act of speaking or the intention to speak, such as \"I say...\", \"I ask...\", \"I tell them...\", \"I will ask about...\". Generate only the spoken words.",
            )
            appendLine("- \"THOUGHT\": For an internal thought of '${character.name}'.")
            appendLine("  The text should be a internal thought, not a direct speech.")
            appendLine("- \"ACTION\": For a physical action '${character.name}' performs.")
            appendLine(
                "  The text should be a concise description of the action in the present tense (e.g., \"Open the ancient chest\", \"Look through the keyhole\", \"Follow the mysterious footprints\"). Avoid phrases like \"I will...\" or \"Character will...\".",
            )
            appendLine(
                "- \"NARRATOR\": To suggest a focus on an environmental detail or a subtle observation that '${character.name}' might notice or want to investigate further. The text should be a statement of that observation (e.g., \"The dusty tome on the lectern seems to call out.\", \"A faint scratching sound comes from behind the wall.\"). This type prompts the player to explore or inquire.",
            )

            appendLine("Ensure suggestions are creative, contextually relevant, and brief, suitable for quick selection.")

            appendLine("Output Format:")
            appendLine("Return ONLY a valid JSON array of objects. Each object must have two fields:")
            appendLine("1.  `text`: (string) The suggestion text.")
            appendLine("2.  `type`: (string) One of the allowed SenderType values: \"USER\", \"THOUGHT\", \"ACTION\", \"NARRATOR\".")

            appendLine("Do not include any other text, explanations, or markdown.")

            appendLine("Example Output:")
            appendLine("[")
            appendLine("  {\"text\": \"What secrets does this map hold?\", \"type\": \"THOUGHT\"},")
            appendLine("  {\"text\": \"Unroll the weathered map.\", \"type\": \"ACTION\"},")
            appendLine("  {\"text\": \"The symbols on the map pulse with a faint, ethereal glow.\", \"type\": \"NARRATOR\"}")
            appendLine("]")
            appendLine(OutputRules.outputRule(toJsonMap(SuggestionGen::class.java)))
        }.trimIndent()
    }
}
