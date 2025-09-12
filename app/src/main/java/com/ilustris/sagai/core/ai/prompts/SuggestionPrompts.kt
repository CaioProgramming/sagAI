package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.data.model.SuggestionGen
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage

object SuggestionPrompts {
    fun generateSuggestionsPrompt(
        saga: Saga,
        character: Character,
        chatHistory: List<MessageContent>,
    ): String {
        val historyFormatted = chatHistory.joinToString { it.joinMessage().formatToString() }

        return """
                                                            You are an AI assistant for a text-based role-playing game.
                                                            Your goal is to provide three concise, creative, contextually relevant, and actionable input suggestions for the player,
                                                            who is currently embodying '${character.name}'.

                                                            Saga Context:
                                                            ${saga.toJsonFormatExcludingFields(
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
        )}

                                                            Character context:
                                                            ${character.toJsonFormatExcludingFields(
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
        )}

                                                            Recent Chat History (for immediate context, newest messages are most relevant):
                                                            $historyFormatted

                                                            Task:
                                                            Based on all the information above, generate exactly 3 distinct input suggestions for the player controlling '${character.name}'.
                                                            Each suggestion must include the `text` of the suggestion and its `type`.
                                                            The suggestions should guide the player's next interaction.

                                                            Allowed `type` values (from your game's SenderType system):
                                                            - "USER": For dialogue spoken by '${character.name}'. 
                                                               The text should be in first person a direct speech.
                                                            - "THOUGHT": For an internal thought of '${character.name}'. 
                                                                The text should be a internal thought, not a direct speech.
                                                            - "ACTION": For a physical action '${character.name}' performs. 
                                                            The text should be a concise description of the action in the present tense (e.g., "Open the ancient chest", "Look through the keyhole", "Follow the mysterious footprints"). Avoid phrases like "I will..." or "Character will...".
                                                            - "NARRATOR": To suggest a focus on an environmental detail or a subtle observation that '${character.name}' might notice or want to investigate further. The text should be a statement of that observation (e.g., "The dusty tome on the lectern seems to call out.", "A faint scratching sound comes from behind the wall."). This type prompts the player to explore or inquire.

                                                            Ensure suggestions are creative, contextually relevant, and brief, suitable for quick selection.

                                                            Output Format:
                                                            Return ONLY a valid JSON array of objects. Each object must have two fields:
                                                            1.  `text`: (string) The suggestion text.
                                                            2.  `type`: (string) One of the allowed SenderType values: "USER", "THOUGHT", "ACTION", "NARRATOR".

                                                            Do not include any other text, explanations, or markdown.

                                                            Example Output:
                                                            [
                                                              {"text": "What secrets does this map hold?", "type": "THOUGHT"},
                                                              {"text": "Unroll the weathered map.", "type": "ACTION"},
                                                              {"text": "The symbols on the map pulse with a faint, ethereal glow.", "type": "NARRATOR"}
                                                            ]
                                                            ${OutputRules.outputRule(toJsonMap(SuggestionGen::class.java))}
            """.trimIndent()
    }
}
