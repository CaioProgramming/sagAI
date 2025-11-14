package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.GenrePrompts // Added import
import com.ilustris.sagai.core.ai.prompts.ImagePrompts // Added import
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterUpdate
import com.ilustris.sagai.features.characters.relations.data.model.RelationGeneration
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.Message
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
            appendLine("**MANDATORY OUTPUT STRUCTURE & ORDER OF INFLUENCE (CRITICAL):**")
            appendLine(
                "The generated text prompt string **MUST** strictly adhere to the following segment order, prioritizing **keywords** over descriptive sentences, to ensure correct influence for the image generation model:",
            )
            appendLine(
                "**Segment A (Highest Priority - MUST be the first 10-15 words): Style and Technique Keywords.** (e.g., EXTREME CLOSE-UP PORTRAIT, TIGHT HEADSHOT, 1960s POP ART, LICHTENSTEIN STYLE, SOLID BLACK SHADOWS, BEN-DAY DOTS.)",
            )
            appendLine(
                "**Segment B (High Priority): Character Identity and Inferred Emotion** (e.g., Ellis, Asian human female, bounty hunter, determined and pragmatic expression...)",
            )
            appendLine(
                "**Segment C (Medium Priority): Physical Details and Attire** (e.g., Short black hair with red streaks, dark brown eyes, space suit...)",
            )
            appendLine(
                "**Segment D (Lowest Priority): Background and Final Directives** (e.g., Stylized starry sky. NO TEXT. Negative prompts:...)",
            )
            appendLine("**CORE STYLISTIC AND COLOR DIRECTIVES (MANDATORY):**")
            appendLine("1.  **Foundational Art Style:**")
            appendLine("The primary rendering style for the portrait **MUST** be:")
            appendLine(GenrePrompts.artStyle(genre))
            appendLine("2.  **Specific Color Application Instructions:**")
            appendLine("*The following rules dictate how the genre's key colors (derived from \"${genre.name}\") are applied:")
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
                "Analyze the Style Reference Image for its dominant rendering techniques, lighting style, and color palette. Describe these elements in detail.",
            )
            appendLine(
                "Deconstruct the Composition Reference Image, noting the camera angle, framing, subject placement, and depth of field. Explain how these elements can be adapted to create a compelling portrait.",
            )

            appendLine(
                "Ensure the character's details, style, and composition blend together to create a visually harmonious and believable image.",
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
                "5.  **Square Aspect Ratio Focus**: Compose for a square 1:1 portrait. Center the subject and frame as bust or waist-up unless the context demands otherwise. If any reference suggests a different framing, ADAPT it to a compelling square portrait. Avoid tall vertical outputs (e.g., 9:16 or 3:4); avoid full-body.",
            )

            appendLine("6.  **Genre Consistency**: Adhere strictly to the theme (${genre.name}).")
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
                        "emojified",
                        "abilities",
                    ),
                ),
            )
            appendLine(
                "*CRITICAL RULE*: ENSURE THAT NO BORDER ARE RENDERED ONLY FULL ART COMPOSITION",
            )
        }

    fun charactersOverview(characters: List<Character>): String =
        buildString {
            val characterExclusions =
                listOf(
                    "id",
                    "image",
                    "sagaId",
                    "joinedAt",
                    "details",
                    "events",
                    "relationshipEvents",
                    "relationshipsAsFirst",
                    "relationshipsAsSecond",
                    "physicalTraits",
                    "hexColor",
                    "firstSceneId",
                    "emojified",
                )
            appendLine("CURRENT SAGA CAST OVERVIEW:")
            appendLine(characters.formatToJsonArray(characterExclusions))
        }

    fun characterGeneration(
        saga: SagaContent,
        description: String,
    ) = buildString {
        appendLine("You are an expert character designer for a story.")
        appendLine(
            "Your task is to create a complete and detailed character profile in JSON format based on the provided saga context and a source description.",
        )
        appendLine("The generated character MUST be deeply contextualized within the saga's theme, genre, and narrative.")

        appendLine("## Saga Context:")
        appendLine(saga.data.toJsonFormatExcludingFields(ChatPrompts.sagaExclusions))

        appendLine("## 🚨 NEW CHARACTER CREATION SOURCE MATERIAL (CRITICAL) 🚨")
        appendLine("// The following description is the **ABSOLUTE AND UNALTERABLE SOURCE** for the new character's core identity.")
        appendLine("// You MUST extract all character details exclusively from this description.")
        appendLine("// This is the ONLY source for the character to be created.")
        appendLine(description)

        appendLine("## Guidelines for generating the character JSON:")
        appendLine(CharacterGuidelines.creationGuideline)
        appendLine("// **IMPORTANT**: The character must be HIGHLY CONTEXTUALIZED TO THE SAGA'S THEME: ${saga.data.genre.name}.")
    }.trimIndent()

    fun characterLoreGeneration(
        timeline: Timeline,
        characters: List<Character>,
    ) = buildString {
        appendLine(
            "You are a narrative AI assistant tasked with tracking individual character progression based on specific timeline events.",
        )
        appendLine("The 'Current Timeline Event' below describes a recent occurrence in the saga.")
        appendLine("The 'List of Characters in Saga' provides context on all characters currently part of the story.")
        appendLine("")
        appendLine("// CORE OBJECTIVE: Extract and summarize individual character events from a narrative.")
        appendLine("")
        appendLine("// --- CONTEXT ---")
        appendLine(
            "// TimelineContext: ${
                timeline.toJsonFormatExcludingFields(
                    listOf("id", "emotionalReview", "chapterId"),
                )
            }",
        )
        appendLine("")
        appendLine(
            "// Characters Context: ${
                characters.toJsonFormatExcludingFields(
                    fieldsToExclude =
                        listOf(
                            "details",
                            "id",
                            "image",
                            "hexColor",
                            "sagaId",
                            "joinedAt",
                        ),
                )
            }",
        )
        appendLine("")
        appendLine("// --- MANDATORY OUTPUT STRUCTURE ---")
        appendLine("// The output MUST be a JSON array. The response must NOT include any additional text, explanations, or parentheses.")
        appendLine("// The structure of each object in the array MUST follow this exact format:")
        appendLine("/*")
        appendLine("[")
        appendLine("  {")
        appendLine("    \"characterName\": \"Character Name\",")
        appendLine("    \"description\": \"A concise update (1-2 sentences) about the character's actions or impact in this event.\",")
        appendLine("    \"title\": \"Short, descriptive title for the character's event.\"")
        appendLine("  },")
        appendLine("  {")
        appendLine("    \"characterName\": \"Another Character\",")
        appendLine("    \"description\": \"Update about the second character.\",")
        appendLine("    \"title\": \"Event title for this character.\"")
        appendLine("  }")
        appendLine("]")
        appendLine("*/")
        appendLine("")
        appendLine("// --- STEP-BY-STEP INSTRUCTIONS ---")
        appendLine("// Follow these steps rigorously to generate the JSON array:")
        appendLine("")
        appendLine(
            "// 1. ANALYSIS: Carefully read the 'TimelineContext' and identify which characters from the 'Characters Context' were **directly involved** or **significantly impacted** by the event.",
        )
        appendLine("// 2. FILTERING: Exclude characters that had no discernible role.")
        appendLine(
            "// 3. GENERATION: For EACH identified character, write a brief 'description' and a relevant 'title', focusing ONLY on the events described in the 'TimelineContext'.",
        )
        appendLine("// 4. ASSEMBLY: Construct the JSON array with the character objects.")
        appendLine("")
        appendLine("// --- CURRENT EVENT ---")
        appendLine("// TimelineContext:")
        appendLine(
            timeline.toJsonFormatExcludingFields(
                listOf(
                    "id",
                    "emotionalReview",
                    "chapterId",
                ),
            ),
        )
        appendLine("")
        appendLine("// Characters Context:")
        appendLine(
            characters.toJsonFormatExcludingFields(
                fieldsToExclude =
                    listOf(
                        "details",
                        "id",
                        "image",
                        "hexColor",
                        "sagaId",
                        "joinedAt",
                    ),
            ),
        )
    }.trimIndent()

    fun findNickNames(
        characters: List<Character>,
        messages: List<Message>,
    ) = buildString {
        appendLine("You are a linguistic analyst AI. Your task is to analyze a conversation and identify unique, context-specific nicknames for characters.")
        appendLine("Your goal is to find informal names that have emerged from the characters' interactions and story events, not just simple descriptors from their profiles.")
        appendLine()
        appendLine("## CORE INSTRUCTIONS:")
        appendLine("1.  **Analyze the provided messages** to find informal names or nicknames used to refer to the characters.")
        appendLine("2.  **Focus on Uniqueness:** The nicknames must be distinctive and not generic. They should feel like a unique identifier for that character within the story's context.")
        appendLine("3.  **Avoid Generic Terms:** Do NOT extract common nouns or roles as nicknames (e.g., \"the girl\", \"ninja\", \"captain\", \"the doctor\"). The name must be a proper noun or a very specific epithet.")
        appendLine("4.  **Context is Key:** The nickname should arise from the character's actions, relationships, or specific events in the narrative provided in the messages. It should not be a direct attribute from their profile (like their job or a visible characteristic).")
        appendLine("5.  **Do not use names already in the character's profile** (e.g., their actual name or known aliases).")
        appendLine()
        appendLine("## CONTEXT:")
        appendLine("### Characters List (Official Names):")
        appendLine("$characters")
        appendLine()
        appendLine("### Recent Messages (Conversation to Analyze):")
        appendLine("$messages")
        appendLine()
        appendLine("## REQUIRED OUTPUT FORMAT:")
        appendLine("Respond ONLY with a valid JSON array. Do not include any other text, explanations, or markdown.")
        appendLine("The JSON array must follow this exact format: `[{\"characterName\": \"Character Full Name\", \"newNicknames\": [\"nickname1\", \"nickname2\"]}]`")
        appendLine("- Only include characters for whom you found one or more valid, unique nicknames that meet the criteria above.")
        appendLine("- If no new, valid nicknames are found for any character, return an empty array `[]`.")
    }.trimIndent()

    fun generateCharacterRelation(
        timeline: Timeline,
        saga: SagaContent,
    ) = buildString {
        appendLine("You are an expert narrative analyst and relationship extractor AI.")
        appendLine("")
        appendLine(
            "GOAL: Analyze the provided Timeline Event and the list of Characters and determine if there is any relationship established between any two characters based on this specific event (or solidifying an ongoing bond). If there are one or more relationships, output them strictly as a JSON array following the required schema below. If no relationship can be inferred, output an empty JSON array [].",
        )
        appendLine("")
        appendLine("IMPORTANT CONSTRAINTS:")
        appendLine("- Output MUST be ONLY valid JSON. No prose, no markdown, no explanations.")
        appendLine("- Use EXACT character names as they appear in the Characters list (case-sensitive match required for best results).")
        appendLine("- Title: short and assertive (3–6 words max).")
        appendLine("- Description: concise, 1–2 sentences.")
        appendLine(
            "- Emoji (relationEmoji): choose an emoji that reflects the relationship feeling (e.g., 🤝 allies, ❤️ love, 💔 heartbreak, ⚔️ rivalry, 🛡️ protector, 🧪 tension, 💫 admiration, 😠 conflict, 🧠 mentorship, 🕊️ truce, 🌀 complicated, 🌩️ betrayal). One emoji only.",
        )
        appendLine("- Do not fabricate characters not present in the Characters list.")
        appendLine("- Prefer relationships that are explicitly or strongly implied by the Timeline Event.")
        appendLine("")
        appendLine("REQUIRED OUTPUT SCHEMA (JSON array of objects):")
        appendLine("[")
        appendLine("   ${toJsonMap(RelationGeneration::class.java)}")
        appendLine("]")
        appendLine("")
        appendLine("FIELD DEFINITIONS (for precision):")
        appendLine("- firstCharacter: First character's name (string). Must match EXACTLY a name from Characters list.")
        appendLine(
            "- secondCharacter: Second character's name (string). Must match EXACTLY a name from Characters list. Must not be the same as firstCharacter.",
        )
        appendLine("- relationEmoji: Single emoji symbol that best represents the relationship feeling.")
        appendLine("- title: Very short, assertive label (3–6 words max) capturing the essence of the relationship.")
        appendLine(
            "- description: A brief, clear explanation (1–2 sentences) describing how this event establishes or changes their relationship.",
        )
        appendLine("")
        appendLine("STEP-BY-STEP INSTRUCTIONS:")
        appendLine(
            "1) Read the Timeline Event carefully and identify interactions, emotional beats, support/opposition, trust/distrust, alliances, mentorship, romance, betrayal, rivalry, etc.",
        )
        appendLine("2) Cross-check mentions with the Characters list and ensure all names used are exactly as listed.")
        appendLine("3) For each strong relationship signal, create one object following the schema.")
        appendLine("4) Keep the title short and assertive; keep the description 1–2 sentences; pick a fitting emoji.")
        appendLine("5) If multiple distinct relationships are present, include multiple objects in the array. If none, return [].")
        appendLine("")
        appendLine("CONTEXT:")
        appendLine("Timeline Event:")
        appendLine(
            timeline.toJsonFormatExcludingFields(
                listOf(
                    "id",
                    "emotionalReview",
                    "chapterId",
                ),
            ),
        )
        appendLine("")
        appendLine("Characters (names must be used EXACTLY as listed):")
        appendLine(
            saga.getCharacters().toJsonFormatExcludingFields(
                listOf(
                    "id",
                    "image",
                    "hexColor",
                    "details",
                    "sagaId",
                    "joinedAt",
                    "profile",
                ),
            ),
        )
    }.trimIndent()
}
