package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.GenrePrompts.artStyle
import com.ilustris.sagai.core.utils.toJsonFormat
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
        2.  **Translate Accurately:** Translate all Portuguese values from the input fields into precise English.
        3. Filter Physical Attributes:
        Critically evaluate the Character Description and exclude any physical attributes that cannot
        be seen in a portrait framed from the shoulders up.
        Ignore details like height, weight, pants, shoes, and any clothing or items below the chest level.
        4. Infer Visuals from Context: This is critical.
            From the Character Description and Current Mood/Situation, infer and elaborate on:

            Key Pose/Expression: Based on the Current Mood/Situation, generate a clear and distinct facial expression that directly reflects the character's personality.
            The pose must include a dynamic element to avoid a straight-on, static portrait. This should involve:
            
            Head Angle Variation: A slight tilt of the head, a look over the shoulder, or a gaze directed slightly off-camera.
            
            Shoulder and Body Language: A subtle turn of the shoulders or torso, suggesting movement or a specific posture.
            
            Hand Placement (if included): A hand gently touching the face, chin, or hair to add a personal touch.
            
            Important Objects/Elements: Include relevant objects only if they can be shown concisely in the shoulders-up area, without obscuring the face.
        5. **Integrate Character (Dominant Central Focus & Red Accents):** The primary character **MUST be the absolute central and dominant focus of the image, filling a significant portion of the frame.** Frame the character as a **close-up portrait** (e.g., headshot to waist-up, or very close full body if the pose demands it, but always prioritizing the character's face/expression). 
        6. Composition for Avatar (Face Focus, Shoulders Up): Formulate the prompt to ensure a 1:1 aspect ratio (square), with the character centralized and the framing strictly on the shoulders and face.
           Absolutely no full-body, wide, or medium shots.
           Suggested terms to use: "avatar," "profile picture," "shoulders up portrait," "close-up on face," "emphasizing facial features," "dynamic pose," "head tilted," "looking over shoulder," "slight smirk," "engaging expression," "1:1 aspect ratio," "square crop," "central composition," "minimalist background."       
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
            "Create a high-quality, vibrant, and detailed digital illustration of a single character in a simple emoji style.",
        )
        appendLine("Character details: ${character.toJsonFormat()}")
        appendLine(
            "The rendering style must be identical to Apple Memojis:",
        )
        appendLine(
            "vibrant and saturated colors, soft shading for a subtle 3D effect, soft and rounded surfaces, and clean edges.",
        )

        appendLine(
            "Character must be centered and occupy most of the image space with a clear focus on the face and expression.",
        )

        appendLine(
            "Smooth gradient background with color $backgroundHexCode.",
        )
    }
}
