package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.GenrePrompts // Added import
import com.ilustris.sagai.core.ai.prompts.ImagePrompts // Added import
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterUpdate
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.timeline.data.model.Timeline

object CharacterPrompts {
    fun details(character: Character?) = character?.toJsonFormat() ?: emptyString()

    @Suppress("ktlint:standard:max-line-length")
    fun descriptionTranslationPrompt(
        character: Character,
        genre: Genre,
    ): String =
        buildString {
            appendLine(
                "Your task is to act as an AI Image Prompt Engineer specializing in generating concepts for **Dramatic Character Portraits**.",
            )
            appendLine(
                "The final image generation model WILL HAVE ACCESS to two direct image inputs alongside the text prompt you generate: a **Style Reference Image** and a **Composition Reference Image**.",
            )
            appendLine(
                "Your goal is to convert the character's description and context below into a single, highly detailed, unambiguous, and visually rich English text description.",
            )
            appendLine(
                "This text description will be used by an AI image generation model, IN CONJUNCTION with the aforementioned image references.",
            )

            appendLine(
                "YOUR SOLE OUTPUT MUST BE THE GENERATED IMAGE PROMPT STRING. DO NOT INCLUDE ANY INTRODUCTORY PHRASES, EXPLANATIONS, RATIONALES, OR CONCLUDING REMARKS. PROVIDE ONLY THE RAW, READY-TO-USE IMAGE PROMPT TEXT.",
            )
            appendLine("**CORE STYLISTIC AND COLOR DIRECTIVES (MANDATORY):**")
            appendLine("1.  **Foundational Art Style:**")
            appendLine("The primary rendering style for the portrait MUST be:")
            appendLine(GenrePrompts.artStyle(genre))
            appendLine("2.  **Specific Color Application Instructions:**")
            appendLine("*The following rules dictate how the genre's key colors (derived from \"${genre.title}\") are applied:")
            appendLine(GenrePrompts.getColorEmphasisDescription(genre))
            appendLine("**Important Clarification on Color:**")
            appendLine("*These color rules are primarily for:")
            appendLine(
                "*The **background's dominant color** (if a simple background is used) or for **atmospheric tinting/elements** if a more complex background is inspired by the Composition Reference.",
            )
            appendLine(
                "***Small, discrete, isolated accents on character features** (e.g., eyes, specific clothing patterns, small tech details, minimal hair streaks).",
            )
            appendLine(
                "***CRUCIAL: DO NOT use these genre colors to tint the character's overall skin, hair (beyond tiny accents), or main clothing areas.** The character's base colors should be preserved and appear natural within the overall art style.",
            )
            appendLine(
                "*Lighting on the character should be primarily dictated by the foundational art style (e.g., chiaroscuro for fantasy, cel-shading for anime) and should aim for realism or stylistic consistency within that art style, not an overall color cast from the genre accents.",
            )
            appendLine("*The genre accents are design elements, not the primary light source for the character.")
            appendLine("**Key Instructions for your generated text prompt:**")
            appendLine("1.  **Extract, Describe, and Adapt from Visual References**:")
            appendLine("Your primary role is to act as an expert art director observing the provided **Style Reference Image**")
            appendLine(
                "* * Composition Reference Image * * (which the final image model will also receive as Bitmaps).Your generated text prompt MUST:",
            )
            appendLine(
                "***From the Style Reference Image**: Identify and verbally articulate its key artistic elements (e.g., 'impressionistic oil painting style', 'vibrant cel-shaded anime aesthetic', 'gritty photorealistic textures', 'specific color palettes', 'lighting techniques').",
            )
            appendLine(
                "***From the Composition Reference Image**: Identify and verbally articulate its core compositional features (e.g., 'is it a close-up, medium shot, or full body?', 'what is the camera angle?', 'how is the subject framed or posed?', 'what is the depth of field like?'). If the Composition Reference Image's overall framing isn't a direct portrait (e.g., it's a wider scene), your description must explain how its compositional essence can be effectively **translated and adapted into a compelling Dramatic Character Portrait**.",
            )
            appendLine(
                "Your goal is to translate the *vibe, style, and compositional cues* of BOTH reference images into a rich textual description. Do not just say 'replicate the references'; instead, *describe WHAT defining characteristics to replicate and adapt* in vivid textual detail, ensuring these are rendered within the **Foundational Art Style** and adhere to the **Color Application Instructions**.",
            )
            appendLine(
                "1.  **Character Fidelity**: The character's own details (name, backstory, personality, race, gender, specific appearance details, clothing, weapons from the `Character Context` below) define *WHO* or *WHAT* is being depicted. This is the primary subject.",
            )
            appendLine(
                "2.  **Synthesis**: The character's details should be seamlessly integrated with the style derived from the Style Reference Image, the composition derived (and adapted, if necessary) from the Composition Reference Image, AND all rendered according to the **Foundational Art Style** and **Color Application Instructions**.",
            )
            appendLine(
                "3.  **Dramatic Portrait Framing**: The final image should still be a 'Dramatic Portrait,' conveying the character's essence and mood. This is the overall goal, even when adapting a non-portrait composition reference.",
            )
            // --- RACE & ETHNICITY FIDELITY (MANDATORY) ---
            appendLine(
                "4.  **Race and Ethnicity Fidelity (MANDATORY)**: Explicitly restate the character's race and ethnicity from the Character Context early in the prompt and prioritize them over any style, color, or genre cues. Skin tone, hair texture, and facial anatomy must clearly match the specified race/ethnicity. Do NOT lighten, change, or neutralize the skin tone; do NOT default to Eurocentric features; do NOT translate or replace the provided race/ethnicity terms—use them verbatim. If both race and ethnicity are present, include both.",
            )
            // --- ASPECT RATIO / FRAMING ---
            appendLine(
                "5.  **Square Aspect Ratio Focus**: Compose for a square 1:1 portrait. Center the subject and frame as bust or waist-up unless the context demands otherwise. If any reference suggests a different framing, ADAPT it to a compelling square portrait. Avoid tall vertical outputs (e.g., 9:16 or 3:4); avoid full-body unless explicitly required.",
            )

            appendLine("6.  **Genre Consistency**: Adhere strictly to the theme (${genre.title}).")
            appendLine(ImagePrompts.conversionGuidelines(genre))
            appendLine("**Image Generation Model Inputs Overview (for your awareness when crafting the text prompt):**")
            appendLine("*   **Text Prompt:** (The string you will generate)")
            appendLine("*   **Style Reference Image:** (Direct Bitmap input to the image model)")
            appendLine("*   **Composition Reference Image:** (Direct Bitmap input to the image model)")
            appendLine("**Character Context:**")
            appendLine(
                character.toJsonFormatExcludingFields(
                    listOf(
                        "id",
                        "image",
                        "sagaId",
                        "joinedAt",
                        "appearance",
                    ),
                ),
            )
            appendLine(
                "*CRITICAL RULE*: ENSURE THAT NO BORDER ARE RENDERED ONLY FULL ART COMPOSITION",
            )
        }

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
        appendLine("// This is the ONLY source for the character to be created .")
        appendLine(description)
        appendLine(CharacterGuidelines.creationGuideline)
        appendLine("// **IMPORTANT**: It must be HIGHLY CONTEXTUALIZED TO THE THEME ${saga.data.genre.title}.")
    }.trimIndent()

    fun characterLoreGeneration(
        timeline: Timeline,
        characters: List<Character>,
    ) = """
                                             You are a narrative AI assistant tasked with tracking individual character progression based on specific timeline events.
                                             The 'Current Timeline Event' below describes a recent occurrence in the saga.
                                             The 'List of Characters in Saga' provides context on all characters currently part of the story.

                                             // CORE OBJECTIVE: Extract and summarize individual character events from a narrative.

                                             // --- CONTEXT ---
                                             // TimelineContext: ${
        timeline.toJsonFormatExcludingFields(
            listOf("id", "emotionalReview", "chapterId"),
        )
    }

                                             // Characters Context: ${
        characters.toJsonFormatExcludingFields(
            fieldsToExclude = listOf("details", "id", "image", "hexColor", "sagaId", "joinedAt"),
        )
    }

                                             // --- MANDATORY OUTPUT STRUCTURE ---
                                             // The output MUST be a JSON array. The response must NOT include any additional text, explanations, or parentheses.
                                             // The structure of each object in the array MUST follow this exact format:
                                             /*
                                             [
                                               {
                                                 "characterName": "Character Name",
                                                 "description": "A concise update (1-2 sentences) about the character's actions or impact in this event.",
                                                 "title": "Short, descriptive title for the character's event."
                                               },
                                               {
                                                 "characterName": "Another Character",
                                                 "description": "Update about the second character.",
                                                 "title": "Event title for this character."
                                               }
                                             ]
                                             */

                                             // --- STEP-BY-STEP INSTRUCTIONS ---
                                             // Follow these steps rigorously to generate the JSON array:

                                             // 1. ANALYSIS: Carefully read the 'TimelineContext' and identify which characters from the 'Characters Context' were **directly involved** or **significantly impacted** by the event.
                                             // 
2. FILTERING: Exclude characters that had no discernible role.
                                             // 3. GENERATION: For EACH identified character, write a brief 'description' and a relevant 'title', focusing ONLY on the events described in the 'TimelineContext'.
                                             // 4. ASSEMBLY: Construct the JSON array with the character objects.

                                             // --- CURRENT EVENT ---
                                             // TimelineContext:
                                             ${
        timeline.toJsonFormatExcludingFields(
            listOf(
                "id",
                "emotionalReview",
                "chapterId",
            ),
        )
    }


                                             // Characters Context:
                                             ${
        characters.toJsonFormatExcludingFields(
            fieldsToExclude = listOf("details", "id", "image", "hexColor", "sagaId", "joinedAt"),
        )
    }
                                          
        """.trimIndent()

    fun generateCharacterRelation(
        timeline: Timeline,
        saga: SagaContent,
    ) = """
        You are an expert narrative analyst and relationship extractor AI.

        GOAL: Analyze the provided Timeline Event and the list of Characters and determine if there is any relationship established between any two characters based on this specific event (or solidifying an ongoing bond). If there are one or more relationships, output them strictly as a JSON array following the required schema below. If no relationship can be inferred, output an empty JSON array [].

        IMPORTANT CONSTRAINTS:
        - Output MUST be ONLY valid JSON. No prose, no markdown, no explanations.
        - Use EXACT character names as they appear in the Characters list (case-sensitive match required for best results).
        - Title: short and assertive (3–6 words max).
        - Description: concise, 1–2 sentences.
        - Emoji (relationEmoji): choose an emoji that reflects the relationship feeling (e.g., 🤝 allies, ❤️ love, 💔 heartbreak, ⚔️ rivalry, 🛡️ protector, 🧪 tension, 💫 admiration, 😠 conflict, 🧠 mentorship, 🕊️ truce, 🌀 complicated, 🌩️ betrayal). One emoji only.
        - Do not fabricate characters not present in the Characters list.
        - Prefer relationships that are explicitly or strongly implied by the Timeline Event.

        REQUIRED OUTPUT SCHEMA (JSON array of objects):
        [
          {
            "firstCharacter": "ExactNameFromList",
            "secondCharacter": "ExactNameFromList",
            "relationEmoji": "🤝",
            "title": "Short, assertive title",
            "description": "1–2 sentence concise summary of the relationship established or evolved in this event."
          }
        ]

        FIELD DEFINITIONS (for precision):
        - firstCharacter: First character's name (string). Must match EXACTLY a name from Characters list.
        - secondCharacter: Second character's name (string). Must match EXACTLY a name from Characters list. Must not be the same as firstCharacter.
        - relationEmoji: Single emoji symbol that best represents the relationship feeling.
        - title: Very short, assertive label (3–6 words max) capturing the essence of the relationship.
        - description: A brief, clear explanation (1–2 sentences) describing how this event establishes or changes their relationship.

        STEP-BY-STEP INSTRUCTIONS:
        1) Read the Timeline Event carefully and identify interactions, emotional beats, support/opposition, trust/distrust, alliances, mentorship, romance, betrayal, rivalry, etc.
        2) Cross-check mentions with the Characters list and ensure all names used are exactly as listed.
        3) For each strong relationship signal, create one object following the schema.
        4) Keep the title short and assertive; keep the description 1–2 sentences; pick a fitting emoji.
        5) If multiple distinct relationships are present, include multiple objects in the array. If none, return [].

        CONTEXT:
        // Timeline Event (sanitized):
        ${timeline.toJsonFormatExcludingFields(listOf("id", "emotionalReview", "chapterId"))}

        // Characters (names must be used EXACTLY as listed):
        ${saga.getCharacters().toJsonFormatExcludingFields(listOf("id", "image", "hexColor", "details", "sagaId", "joinedAt"))}
        """
}
