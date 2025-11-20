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

            appendLine("Core Task for THIS Generation:")
            appendLine("- Lean into EXACTLY ONE vibe inspired by these genres: $genreEnumNames.")
            appendLine(
                "- Adapt language and imagery to that single vibe without naming the genre directly or using enum names ($genreEnumNames).",
            )

            appendLine("Never mention or imply AI, assistant, model, generation, or automation. The text must feel organic and human.")

            appendLine("Instructions for 'title':")
            appendLine("- Very short (2–5 words).")
            appendLine(
                "- Clear and creative call to action for starting a new story or universe. Emphasize creation and new beginnings.",
            )
            appendLine("- Examples: 'Craft Your Epic', 'Begin a New Tale', 'Forge a Universe', 'Write Your Legend'.")

            appendLine("Instructions for 'subtitle':")
            appendLine("- Concise (max 15 words), engaging, and creative.")
            appendLine("- Provide a subtle hint or intriguing premise for a story, evoking ONLY ONE genre vibe.")
            appendLine("- Avoid generic hooks. Instead, offer a glimpse into a potential narrative.")
            appendLine(
                "- Examples: 'Where ancient magic meets forgotten realms.', 'Unravel mysteries in a neon-drenched city.', 'Survive the horrors of a pixelated nightmare.', 'Heroic deeds await in a world of titans.'",
            )

            appendLine("Strict output rules:")
            appendLine("- Output ONLY the JSON object with fields 'title' and 'subtitle'.")
            appendLine("- Do NOT include genre enum names ($genreEnumNames) or quote the genres as a list.")

            appendLine("EXPECTED JSON STRUCTURE:")
            appendLine(toJsonMap(DynamicSagaPrompt::class.java))
        }.trimIndent()
    }
}
