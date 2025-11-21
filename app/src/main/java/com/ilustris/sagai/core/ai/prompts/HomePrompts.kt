package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.newsaga.data.model.Genre

object HomePrompts {
    @Suppress("ktlint:standard:max-line-length")
    fun dynamicSagaCreationPrompt(): String {
        val genreEnumNames = Genre.entries.joinToString(", ") { it.name }

        return buildString {
            appendLine(
                "You are a highly creative writing prompt generator. Create ONE unique and engaging call-to-action card (a title and a subtitle) inviting the user to create a new saga.",
            )
            appendLine(
                "Your output MUST be a single JSON object with exactly two fields: 'title' and 'subtitle'. Output ONLY the JSON object, no extra text.",
            )

            appendLine("Never mention or imply AI, assistant, model, generation, or automation. The text must feel organic and human.")

            appendLine("Instructions for 'title':")
            appendLine("- Very short (2–5 words).")
            appendLine("- Focus on ACTION verbs that imply creating a universe, history, or adventure.")
            appendLine("- Keep it simple, inviting, and direct.")
            appendLine("- Examples: 'Create a New Universe', 'Start an Adventure', 'Forge Your History', 'Write a New Saga'.")

            appendLine("Instructions for 'subtitle':")
            appendLine("- Concise (max 15 words), engaging, and creative.")
            appendLine(
                "- Provide a subtle whisper of inspiration that hints at the possibilities available in these genres: $genreEnumNames.",
            )
            appendLine("- Do NOT list the genres. Instead, invite the user's creativity to fill in the blanks.")
            appendLine("- The tone should be: 'Anything is possible here.'")
            appendLine(
                "- Examples: 'From deep space mysteries to ancient magical realms.', 'Whether it's high fantasy or a neon future, it starts here.', 'Your imagination is the only limit to the worlds you can build.'",
            )

            appendLine("Strict output rules:")
            appendLine("- Output ONLY the JSON object with fields 'title' and 'subtitle'.")
            appendLine("- Do NOT include genre enum names ($genreEnumNames) or quote the genres as a list.")

            appendLine("EXPECTED JSON STRUCTURE:")
            appendLine(toJsonMap(DynamicSagaPrompt::class.java))
        }.trimIndent()
    }
}
