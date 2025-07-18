package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.model.SuggestionsReponse
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.joinMessage

object SuggestionPrompts {
    fun generateSuggestionsPrompt(
        sagaData: SagaData,
        character: Character,
        chatHistory: List<MessageContent>, // ViewModel will send the last 2-3 relevant messages
    ): String {
        val historyFormatted =
            chatHistory.joinToString("\n") {
                // Assuming formatToString is an extension on Pair<String, String> or similar
                // and joinMessage returns something compatible.
                // If joinMessage returns Pair<String, String> (sender, text)
                val (sender, text) = it.joinMessage(showSender = true)
                "- $sender: \"$text\""
            }

        return """
            You are an AI assistant for a text-based role-playing game.
            Your goal is to provide three concise, creative, contextually relevant, and actionable input suggestions for the player,
            who is currently embodying '${character.name}'.

            Saga Context:
            - Title: ${sagaData.title}
            - Summary: ${sagaData.description.take(300)}

            Character in Focus (${character.name}):
            - Backstory Snippet: ${character.backstory.take(200)}
            - Personality/Vibe: ${character.details.personality.take(150)}

            Recent Chat History (for immediate context, newest messages are most relevant):
            $historyFormatted

            Task:
            Based on all the information above, generate exactly 3 distinct input suggestions for the player controlling '${character.name}'.
            Each suggestion must include the `text` of the suggestion and its `type`.
            The suggestions should guide the player's next interaction.

            Allowed `type` values (from your game's SenderType system):
            - "USER": For dialogue spoken by '${character.name}'. The text should be what the character says.
            - "THOUGHT": For an internal thought of '${character.name}'. The text should be the character's thought.
            - "ACTION": For a physical action '${character.name}' performs. The text should be a concise description of the action in the present tense (e.g., "Open the ancient chest", "Look through the keyhole", "Follow the mysterious footprints"). Avoid phrases like "I will..." or "Character will...".
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
            ${OutputRules.outputRule(SuggestionsReponse.example().toJsonFormat())}
            """.trimIndent()
    }
}
