package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.jvm.java

object HomePrompts {
    // In HomePrompts.kt

    fun dynamicSagaCreationPrompt(): String {
        val genreEnumNames = Genre.entries.joinToString(", ") { it.name }
        val genreDisplayTitles =
            Genre.entries.joinToString(", ") { it.title }

        return """
            You are a highly creative AI assistant. Your mission is to generate a unique and engaging call to action (a title and subtitle) for a user to start writing a new story.
            Your output MUST be a JSON object with two fields: 'title' and 'subtitle'.

            **Core Task for THIS Generation:**
            
            Generate a random call action for one of this theme: $genreDisplayTitles.
            
            **Instructions for 'title' (Must be NEW and based on your RANDOM genre theme selection):**
            *   Short (2-4 words).
            *   An epic, adventurous, and highly engaging call to action conveying 'creating' or 'starting' a narrative.
            *   Creatively and subtly HINT at the RANDOMLY CHOSEN genre theme.
            Do NOT explicitly name the genre or use the enum names ($genreEnumNames).
            

            **Instructions for 'subtitle' (Must be NEW and complement the new title's theme):**
            *   Concise (max 15 words), inviting, and slightly playful.
            *   Sparks curiosity and encourages users to begin their narrative adventure.
            *   **CRITICAL: Do NOT simply copy or minorly rephrase the subtitles from the examples below. Generate something fresh.**

            **Now, follow the Core Task: 
            1. RANDOMLY select a genre theme. 
            2. Generate a COMPLETELY NEW JSON object with an original title and subtitle that hints at your chosen theme.**

            EXPECTED JSON STRUCTURE:
            ${toJsonMap(DynamicSagaPrompt::class.java)}
            """.trimIndent()
    }
}
