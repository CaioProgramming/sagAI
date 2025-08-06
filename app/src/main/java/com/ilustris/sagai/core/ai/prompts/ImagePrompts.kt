package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts.coverCompositions
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre

object ImagePrompts {
    fun wallpaperGeneration(
        saga: Saga,
        description: String,
    ) = GenrePrompts
        .iconPrompt(
            genre = saga.genre,
            description = description,
        ).trimIndent()

    fun chapterCover(
        saga: Saga,
        characters: List<Character>,
    ) = """
        ${GenrePrompts.artStyle(saga.genre)}
        ${CharacterFraming.MEDIUM_SHOT.description}
        Featuring: ${coverCompositions(characters)}
        ${GenrePrompts.coverComposition(saga.genre)}
        """

    fun generateImage(
        character: Character,
        saga: Saga,
        description: String,
    ) = """
        ${CharacterRules.IMAGE_CRITICAL_RULE}
        $description
        """.trimIndent()

    fun conversionGuidelines(genre: Genre) =
        """
        **Guidelines for Conversion and Expansion:**
        
        1.  **Translate Accurately:** Translate all Portuguese values from the input fields into precise English.
        2.  **Infer Visuals from Context:** **This is critical.** From the `Character Description` and `Current Mood/Situation`, infer and elaborate on:
            * **Primary Setting/Environment (for background):** The background should be **extremely minimalist and somber**, serving only to enhance the character's presence.
            * **Dominant Mood/Atmosphere & Lighting Theme: ${GenrePrompts.moodDescription(genre)}.
            * **Key Pose/Expression:** Based on `Current Mood/Situation`, generate a **dynamic, expressive, and dramatic pose/facial expression** that captures the character's essence in a portrait.
            * **Important Objects/Elements:** Include any significant items, or symbols mentioned in the description, ensuring they are either held by or directly related to the central character and are potential candidates for the vivid highlights.
        3.  **Integrate Character (Dominant Central Focus & Red Accents):** The primary character **MUST be the absolute central and dominant focus of the image, filling a significant portion of the frame.** Frame the character as a **close-up portrait** (e.g., headshot to waist-up, or very close full body if the pose demands it, but always prioritizing the character's face/expression). **Incorporate vivid red details on or around the character that are thematically relevant**, ensuring they are the immediate focal points against the somber background.
        4.  **Integrate Character (Dominant Central Focus, Artistic Lighting & Subtle Cybernetics - Tight Framing):** The primary character **MUST be the absolute central and dominant focus**, **framed tightly as a close-up portrait (from the chest or shoulders up), filling a significant portion of the frame.** Emphasize **strong, artistic lighting that defines their form and creates dramatic shadows**. Their expression should be clearly visible and convey the mood.
        5.  **Composition for Dramatic Portrait (Tight & Centralized Focal Distance):** Formulate the prompt to suggest a **tight, portrait-oriented composition with the main character centrally and dominantly positioned, capturing a headshot or upper-body shot.** Utilize strong, focused lighting to emphasize the character, their expression, and their key elements.
        * **Suggested terms to use:** "tight shot," "close-up portrait," "headshot," "upper body shot," "from the chest up," "shoulders up," "central composition," "dramatic shadows,", "character-focused,"
        6.  **Exclusions:** NO TEXT, NO WORDS, NO TYPOGRAPHY, NO LETTERS, NO UI ELEMENTS.    
        7. **Use the art style to improve your response and attach to ${genre.title} style the art style should be:
         ${GenrePrompts.artStyle(genre)}    
        """
}
