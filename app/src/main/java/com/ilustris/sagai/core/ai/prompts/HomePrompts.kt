package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.jvm.java

object HomePrompts {
    @Suppress("ktlint:standard:max-line-length")
    fun dynamicSagaCreationPrompt(): String {
        val genreEnumNames = Genre.entries.joinToString(", ") { it.name }
        val genreDisplayTitles =
            Genre.entries.joinToString(", ") { it.title }

        return buildString {
            appendLine(
                "You are a highly creative writing prompt generator. Create ONE unique and engaging call-to-action card (a title and a subtitle) inviting the user to create a new saga.",
            )
            appendLine(
                "Your output MUST be a single JSON object with exactly two fields: 'title' and 'subtitle'. Output ONLY the JSON object, no extra text.",
            )

            appendLine("Core Task for THIS Generation:")
            appendLine("- Lean into EXACTLY ONE vibe inspired by these genres: $genreDisplayTitles.")
            appendLine(
                "- Adapt language and imagery to that single vibe without naming the genre directly or using enum names ($genreEnumNames).",
            )

            appendLine("Never mention or imply AI, assistant, model, generation, or automation. The text must feel organic and human.")

            appendLine("Instructions for 'title':")
            appendLine("- Very short (2–4 words).")
            appendLine(
                "- Assertive, clear call to create a saga (e.g., 'Create Your Saga', 'Start a New Saga', 'Forge Your Saga'). No ambiguity.",
            )
            appendLine("- Must emphasize the action of creating a new saga or universe.")

            appendLine("Instructions for 'subtitle':")
            appendLine("- Concise (max 12 words), engaging, creative, imperative tone.")
            appendLine("- Subtly evoke ONLY ONE vibe (do not list multiple genres; avoid slashes or comma-separated lists).")
            appendLine(
                "- Use punchy hooks like: 'Begin your epic journey', 'Shape a new world', 'Unleash your imagination', 'Start the adventure of a lifetime'.",
            )
            appendLine("- Encourage the user to begin now.")

            appendLine("Strict output rules:")
            appendLine("- Output ONLY the JSON object with fields 'title' and 'subtitle'.")
            appendLine("- Do NOT include genre enum names ($genreEnumNames) or quote the genres as a list.")

            appendLine("EXPECTED JSON STRUCTURE:")
            appendLine(toJsonMap(DynamicSagaPrompt::class.java))
        }.trimIndent()
    }
}
