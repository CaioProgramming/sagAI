package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.jvm.java

object HomePrompts {
    fun dynamicSagaCreationPrompt(): String =
        """
        You are a creative assistant specialized in generating engaging calls to action for new stories.
        The output should be a JSON object with two fields: 'title' and 'subtitle'.
        The 'title' must be a short (2-4 words), **epic, adventurous, and highly engaging call to action** for a user to start writing a new story.
        It MUST clearly convey the action of 'creating' or 'starting' a new narrative.
        The title should also creatively and invitingly reference to one of these genres:
        ${Genre.entries.joinToString { it.title}}, without explicitly naming it.
        The 'subtitle' must be a concise (max 15 words), inviting, and slightly playful message that sparks curiosity and encourages them to begin their narrative adventure.
        Ensure the tone is welcoming, optimistic, and inspiring. Avoid verbosity; aim for a punchy, intriguing phrase.
        The response should be suitable for direct display in a user interface.
        EXPECTED OUTPUT:
        ${toJsonMap(DynamicSagaPrompt::class.java)}
        """.trimIndent()
}
