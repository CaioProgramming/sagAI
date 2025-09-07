package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.jvm.java

object HomePrompts {
    fun dynamicSagaCreationPrompt(): String {
        val genreEnumNames = Genre.entries.joinToString(", ") { it.name }
        val genreDisplayTitles =
            Genre.entries.joinToString(", ") { it.title }

        return """
            You are a highly creative writing prompt generator. Create ONE unique and engaging call-to-action card (a title and a subtitle) inviting the user to create a new saga.
            Your output MUST be a single JSON object with exactly two fields: 'title' and 'subtitle'. Output ONLY the JSON object, no extra text.

            Core Task for THIS Generation:
            - Lean into EXACTLY ONE vibe inspired by these genres: $genreDisplayTitles.
            - Adapt language and imagery to that single vibe without naming the genre directly or using enum names ($genreEnumNames).

            Never mention or imply AI, assistant, model, generation, or automation. The text must feel organic and human.

            Instructions for 'title':
            - Very short (2–4 words).
            - Assertive, clear call to create a saga (e.g., "Create Your Saga", "Start a New Saga", "Forge Your Saga"). No ambiguity.
            - May hint at the chosen vibe through mood/imagery without explicit genre names.

            Instructions for 'subtitle':
            - Concise (max 12 words), engaging, creative, imperative tone.
            - Subtly evoke ONLY ONE vibe (do not list multiple genres; avoid slashes or comma-separated lists).
            - Use punchy hooks like: "Defeat mythical creatures", "Unveil the cyberworld", "Dive into the unknown", "Be the hero the world needs".
            - Encourage the user to begin now.

            Strict output rules:
            - Output ONLY the JSON object with fields 'title' and 'subtitle'.
            - Do NOT include genre enum names ($genreEnumNames) or quote the genres as a list.

            EXPECTED JSON STRUCTURE:
            ${toJsonMap(DynamicSagaPrompt::class.java)}
            """.trimIndent()
    }
}
