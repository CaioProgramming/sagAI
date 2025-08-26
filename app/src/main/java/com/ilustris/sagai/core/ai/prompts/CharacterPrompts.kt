package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre

object CharacterPrompts {
    fun details(character: Character?) = character?.toJsonFormat() ?: emptyString()

    fun descriptionTranslationPrompt(
        character: Character,
        genre: Genre,
    ) = """
        Your task is to act as an AI Image Prompt Engineer specializing in generating concepts for **Dramatic Character Portraits**.
        The final image generation model WILL HAVE ACCESS to two direct image inputs alongside the text prompt you generate: a **Style Reference Image** and a **Composition Reference Image**.
        Your goal is to convert the character's description and context below into a single, highly detailed, unambiguous, and visually rich English text description. This text description will be used by an AI image generation model, IN CONJUNCTION with the aforementioned image references.

        YOUR SOLE OUTPUT MUST BE THE GENERATED IMAGE PROMPT STRING. DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS. PROVIDE ONLY THE RAW, READY-TO-USE IMAGE PROMPT TEXT.

        **Key Instructions for your generated text prompt:**

        1.  **Extract, Describe, and Adapt from Visual References**:
            Your primary role is to act as an expert art director observing the provided **Style Reference Image** and **Composition Reference Image** (which the final image model will also receive as Bitmaps). Your generated text prompt MUST:
            *   **From the Style Reference Image**: Identify and verbally articulate its key artistic elements (e.g., 'impressionistic oil painting style', 'vibrant cel-shaded anime aesthetic', 'gritty photorealistic textures', 'specific color palettes', 'lighting techniques').
            *   **From the Composition Reference Image**: Identify and verbally articulate its core compositional features (e.g., 'is it a close-up, medium shot, or full body?', 'what is the camera angle?', 'how is the subject framed or posed?', 'what is the depth of field like?'). If the Composition Reference Image's overall framing isn't a direct portrait (e.g., it's a wider scene), your description must explain how its compositional essence can be effectively **translated and adapted into a compelling Dramatic Character Portrait**.
            Your goal is to translate the *vibe, style, and compositional cues* of BOTH reference images into a rich textual description. Do not just say 'replicate the references'; instead, *describe WHAT defining characteristics to replicate and adapt* in vivid textual detail.

        2.  **Character Fidelity**: The character's own details (name, backstory, personality, race, gender, specific appearance details, clothing, weapons from the `Character Context` below) define *WHO* or *WHAT* is being depicted. This is the primary subject.

        3.  **Synthesis**: The character's details should be seamlessly integrated with the style derived from the Style Reference Image and the composition derived (and adapted, if necessary) from the Composition Reference Image.

        4.  **Dramatic Portrait Framing**: The final image should still be a 'Dramatic Portrait,' conveying the character's essence and mood. This is the overall goal, even when adapting a non-portrait composition reference.

        5.  **Genre Consistency**: Adhere strictly to the theme (${genre.title}). ${ImagePrompts.conversionGuidelines(genre)}

        **Image Generation Model Inputs Overview (for your awareness when crafting the text prompt):**
        *   **Text Prompt:** (The string you will generate)
        *   **Style Reference Image:** (Direct Bitmap input to the image model)
        *   **Composition Reference Image:** (Direct Bitmap input to the image model)

        **Character Context:**
        ${character.toJsonFormatExcludingFields(listOf("id", "image", "sagaId", "joinedAt", "backstory"))}
        """.trimIndent()

    fun appearance(character: Character) =
        """
        ${character.details.race},${character.details.gender},${character.details.ethnicity}
        ${character.details.facialDetails}, ${character.details.clothing}."
        ${character.details.appearance}
        """.trimIndent()

    fun charactersOverview(characters: List<Character>): String =

        """
        CURRENT SAGA CAST:
        [ 
        ${
            characters.joinToString(",\n") {
                it.toJsonFormatExcludingFields(
                    listOf(
                        "id",
                        "image",
                        "sagaId",
                        "joinedAt",
                        "details",
                    ),
                )
            }
        }
        ]
        """.trimIndent()

    fun characterGeneration(
        saga: SagaContent,
        description: String,
    ) = buildString {
        appendLine("Write a character description for a new character in the story.")
        appendLine("Saga Context:")
        appendLine(saga.data.toJsonFormatExcludingFields(ChatPrompts.sagaExclusions)) // Assuming ChatPrompts.sagaExclusions is defined
        appendLine("/ 🚨🚨🚨 NEW CHARACTER CREATION SOURCE MATERIAL (CRITICAL) 🚨🚨🚨")
        appendLine("// The following JSON object is the **ABSOLUTE AND UNALTERABLE SOURCE** for the new character's core identity.")
        appendLine("// You MUST extract all character details exclusively from this object.")
        appendLine("// IGNORE ALL OTHER character descriptions in the prompt, including the saga's main description.")
        appendLine("// This is the ONLY source for the character to be created.")
        appendLine(description)
        appendLine(CharacterGuidelines.creationGuideline) // Assuming CharacterGuidelines.creationGuideline is defined
        appendLine("// **IMPORTANT**: It must be HIGHLY CONTEXTUALIZED TO THE THEME ${saga.data.genre.title}.")
    }.trimIndent()
}
