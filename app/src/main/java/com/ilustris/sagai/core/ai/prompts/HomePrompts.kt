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
            - Randomly lean into ONE vibe inspired by these genres: $genreDisplayTitles.
            - Adapt language and imagery to that vibe without naming the genre directly or using enum names ($genreEnumNames).

            Never mention or imply AI, assistant, model, generation, or automation. The text must feel like an organic, creative quote.

            Instructions for 'title':
            - Short (2–5 words).
            - Clearly signal this is to CREATE/START a new saga (e.g., "Start Your Saga", "Begin a New Saga").
            - May hint at the chosen vibe through mood/imagery without explicit genre names.

            Instructions for 'subtitle':
            - Concise (max 15 words), inviting, creative, slightly playful.
            - May weave in or allude to any of: $genreDisplayTitles to set the vibe subtly, not as a list.
            - Encourage the user to begin now.

            EXPECTED JSON STRUCTURE:
            ${toJsonMap(DynamicSagaPrompt::class.java)}
            """.trimIndent()
    }
}
