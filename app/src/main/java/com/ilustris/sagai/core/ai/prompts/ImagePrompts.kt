package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.GenrePrompts.artStyle
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre

object ImagePrompts {
    fun wallpaperGeneration(
        saga: Saga,
        description: String,
    ) = description.trimIndent()

    fun generateImage(description: String) =
        """
        ${CharacterRules.IMAGE_CRITICAL_RULE}
        $description
        """.trimIndent()

    fun conversionGuidelines(genre: Genre) =
        """
        **Guidelines for Conversion and Expansion:**
        // Section 1: Artistic Style - Highest Priority
        ${artStyle(genre)}
        2.  **Translate Accurately:** Translate all values from the input fields into precise English. Remove redundant adverbs and adjectives
        3. Filter Physical Attributes: Strictly exclude any accessories or clothing details that cannot be clearly seen in the 'Extreme Close-Up' focus area (e.g., items clipped to the belt or below the neck).
        **Exclusion Rule (ABSOLUTE):** Strictly exclude **ALL** accessories and clothing details that are not fully visible in a head-and-face close-up. **Specifically exclude** any mention of: communicator on wrist, tablet on belt, gloves, shoes, pants, and anything below the clavicle.
        **Literal Fidelity (MANDATORY TRANSLATION):**
        The agent MUST perform a direct and literal English translation of the full description provided in the facialDetails context (Hair, Eyes, Jawline, Mouth, Distinguishing Marks) and include it in Segment C.
        NO ADAPTATION RULE: The agent must not infer, shorten, adapt, simplify, or modify any characteristic, especially color, length, texture, or style. If the input says 'long', the output MUST include 'long'. If the input mentions a specific texture (e.g., straight, wavy), it must be included.

        Example (Literal): "Cabelos longos e lisos, de um tom platinado" MUST become "Long, straight platinum blonde hair."
        
        4. Infer Visuals from Context: This is critical.
            Expression Translation (MANDATORY): From the Character Context fields (personality, backstory, and Current Mood/Situation), CRITICALLY infer the primary dramatic emotion that the character is experiencing. This emotion MUST be translated into a clear and distinct facial expression that directly reflects the character's internal state.

            Example Translation: "Calma, observadora, mas sobrecarregada" → Intense, reserved gaze, slight frown of profound weariness.
            
            Example Translation: "Empatia, resolvendo conflito" → Focused, empathetic expression, hint of concern.
            
            Dynamic Pose (CINEMATIC & ANGULAR - MANDATORY): The composition MUST incorporate a dramatic, non-straight-on camera angle (e.g., Low-Angle Shot, Dutch Angle, or High-Angle Shot). The pose and action must be intensely dynamic, suggesting imminent conflict or high tension, and the description MUST use cinematic terms to define this angle (e.g., Shot from below, Worm's-eye perspective, Angular composition).
            The characters' body language (torso, arms, shoulders) should convey maximum action and readiness for battle.
            
            Head Angle Variation: A strong tilt of the head, a worried look down, or a resolute gaze directed slightly off-camera.
            
            Shoulder and Body Language: A subtle tense turn of the shoulders or torso, suggesting readiness, conflict, or apprehension, matching the inferred dramatic emotion.
                        
            Important Objects/Elements: Include relevant objects only if they can be shown concisely in the shoulders-up area, without obscuring the face.
        5. **Integrate Character (Dominant Central Focus & Red Accents):
           The final framing (Headshot, Bust-Up, or Extreme Close-up) MUST be derived from and match the overall Composition Reference Image provided to the model.
           The character MUST fill a significant portion of the frame, with the primary emphasis always on the facial features and the dramatic expression.
        6. Composition Fidelity (1:1 Portrait Focus):
        Analyze the Composition Reference Image to determine the appropriate framing (e.g., Bust-Up, Waist-Up, or Headshot). Formulate the prompt to ensure a 1:1 aspect ratio (square), with the character centralized, and the framing must match the reference while adapting it to a compelling square portrait.
        7.  **Exclusions:** NO TEXT, NO WORDS, NO TYPOGRAPHY, NO LETTERS, NO UI ELEMENTS.    
        8.  **Art Style & Mood (CRITICAL - Reference Image Dictates Style)**:
            *   **The provided reference image is the ABSOLUTE and DEFINITIVE source for the entire art style.** This includes, but is not limited to: art medium (e.g., oil painting, digital art, pixel art, anime cel shading), rendering technique (e.g., brushstrokes, line work, shading style), color palette, lighting scheme, overall mood, and aesthetic.
            *   **Extract the ESSENCE** of the reference image's style and meticulously apply it to the character and any relevant background elements described in the prompt.
            *   **Style Precedence Rule:** If any stylistic descriptions found within the input text (e.g., from a character description that says "a character with a cartoonish look") conflict with the style depicted in the reference image, **the style of the reference image ALWAYS takes precedence.** The primary goal is to render the described *content* (character, objects, scene) in the *style* of the reference image.

        9.Negative Prompts: full body, standing, sitting, legs, feet, wide shot, medium shot, landscape, scenery.
        """.trimIndent()

    fun simpleEmojiRendering(
        backgroundHexCode: String,
        character: Character,
    ) = buildString {
        appendLine(
            "Your task is to act as an AI Image Prompt Engineer specializing in generating concepts for **Apple Memoji Headshots**.",
        )
        appendLine(
            "Make sure to instruct the render style to be **EXACTLY** identical to **Apple Memojis**.",
        )
        appendLine(
            "Focus **ONLY** on the **single character's head and face**, isolated from any body, neck, or shoulders, with a **dynamic expression or pose**. **Strictly exclude all details related to body, torso, neck, and clothing (spacesuit, gloves, belt, height, weight)**. The output must be a head-only composition, as if it were a detached emoji head.",
        )
        appendLine(
            "Your goal is to convert the character's description and context below into a single, highly detailed, unambiguous, and visually rich English text description.",
        )
        appendLine(
            "This text description will be used by an AI image generation model, IN CONJUNCTION with the aforementioned image references.",
        )
        appendLine(
            "The generated prompt must be optimized to leverage the visual information from the character details, ensuring the final output image captures the character's face, hair, expression, **head pose (e.g., tilted, looking sideways, slightly angled)**, and a **subtly dynamic camera angle (e.g., slightly from below/above, slight rotation)**, while strictly adhering to the specified **Apple Memoji style** and **isolated head-only composition**.",
        )

        appendLine("**Character Context:**")
        appendLine(
            character.toJsonFormatExcludingFields(
                listOf(
                    "id",
                    "image",
                    "sagaId",
                    "joinedAt",
                    "emojified",
                ),
            ),
        )

        appendLine(
            "Remember to consider any accessories or unique features mentioned in the character description, such as glasses, hats, piercings, or distinctive hairstyles, and incorporate them into the headshot rendering.",
        )

        appendLine(
            "YOUR SOLE OUTPUT MUST BE THE GENERATED IMAGE PROMPT STRING. DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS. PROVIDE ONLY THE RAW, READY-TO-USE IMAGE PROMPT TEXT.",
        )
        appendLine(
            "The rendering style must be identical to Apple Memojis:",
        )
        appendLine(
            "vibrant and saturated colors, soft shading for a subtle 3D effect, soft and rounded surfaces, and clean edges.",
        )

        appendLine(
            "Character must be an **ISOLATED FLOATING HEAD**, centered and occupying most of the image space with a clear focus on the **face and expression, without any neck or shoulders visible**.",
        )

        appendLine(
            "Apply a solid black background.",
        )
    }
}
