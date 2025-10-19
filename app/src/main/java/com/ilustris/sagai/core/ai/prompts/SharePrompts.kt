package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent

object SharePrompts {
    fun playStylePrompt(
        character: Character,
        sagaContent: SagaContent,
    ) = buildString {
        val sagaExcludedFields =
            listOf(
                "id",
                "icon",
                "createdAt",
                "endedAt",
                "mainCharacterId",
                "currentActId",
                "isDebug",
                "emotionalReview",
                "topCharacters",
                "actsInsight",
                "conclusion",
                "introduction",
            )
        val characterExcludedFields =
            listOf(
                "id",
                "image",
                "hexColor",
                "sagaId",
                "joinedAt",
                "firstSceneId",
                "emojified",
                "details",
            )
        // --- 1. Role Assignment and Style Definition ---
        appendLine(
            "You are a world-class marketing copywriter, specialized in minimalistic, highly impactful, and curiosity-driven slogans (Apple, Google, Nike style).",
        )
        appendLine("Your final output must be a single, short, evocative phrase. Do NOT provide any explanation or extra text.")

        // --- 2. Constraints and Output Format ---
        appendLine("The phrase MUST adhere to these rules:")
        appendLine("1. CONCISE: Maximum 8 words.")
        appendLine("2. EVOCATIVE: Focus on the character's legacy, archetype, or the core feeling of their completed quest.")
        appendLine("3. SPOILER-FREE: Do not mention specific plot points, names of enemies, or final locations.")
        appendLine("4. CURIOSITY-DRIVEN: It must raise curiosity about the game/app and the character's journey.")
        appendLine(
            "5. STYLE: Aim for a tagline that suggests action, destiny, or profound change. (e.g., 'The future isn't built. It's fought for.', 'Destiny is for spectators.')",
        )

        // --- 3. Contextual Data Injection ---
        appendLine("--- CHARACTER AND SAGA CONTEXT FOR INSPIRATION ---")
        appendLine("Character context (Archetype/Core Role): ")
        appendLine(character.toJsonFormatExcludingFields(characterExcludedFields))
        appendLine("Saga context (Core Conflict/Resolution): ")
        appendLine(sagaContent.data.toJsonFormatExcludingFields(sagaExcludedFields))

        appendLine("Generate the impactful, short marketing slogan based on the provided context.")
    }
}
