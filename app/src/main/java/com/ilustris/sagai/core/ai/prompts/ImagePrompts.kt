package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.GenrePrompts.artStyle
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre

object ImagePrompts {
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

    fun extractComposition() =
        buildString {
            appendLine("You are analyzing TWO visual references:")
            appendLine("1. **Image A (Composition Reference):** Dictates layout, framing, and light.")
            appendLine("2. **Image B (Style Reference):** Dictates artistic rendering, texture, and aesthetic era.")
            appendLine("")
            appendLine(
                "Your task is to extract EIGHT critical, technical components by separating the focus as instructed. Your response must be in **ENGLISH ONLY** and contain **ONLY** the structured information.",
            )
            appendLine("")
            appendLine("**I. Extract from IMAGE A (Composition Reference):**")
            appendLine("1.  **Framing:** [The most accurate, literal photographic framing term, e.g., FULL BODY SHOT, ULTRA CLOSE-UP]")
            appendLine("2.  **Zoom Level / Proximity:** [The camera's closeness to the subject, e.g., Extreme closeness, Medium proximity]")
            appendLine(
                "3.  **Cropping Intention:** [How the subject is intentionally cut by the frame. **MUST INCLUDE pose/gaze guidance,** e.g., Tightly cropped for dramatic effect, no straight-on gaze]",
            )
            appendLine("4.  **Key Lighting Style:** [The primary lighting technique, e.g., Rembrandt Lighting, Dramatic Backlighting]")
            appendLine("")
            appendLine("**II. Extract from IMAGE B (Style Reference):**")
            appendLine("6.  **Art Technique:** [Technical details of the texture/application, e.g., Impasto Texture, Visible Brushstrokes]")
            appendLine(
                "7.  **Aesthetic Era / Influence:** [The dominant artistic movement or time period, e.g., High Classicism (17th Century), Romanticism]",
            )
            appendLine(
                "8.  **Vibe / Mood Aesthetic:** [A technical aesthetic term defining the final mood, e.g., Ethereal and Majestic, Foreboding and Heroic]",
            )
        }

    fun imageHighlight(genre: Genre) =
        buildString {
            appendLine("**Injected Detail - Localized Color Accent (Crucial for Aesthetic Focus):**")
            appendLine(
                "The Agent MUST inject a short phrase dedicated to a localized color accent. This element MUST be subtle and enhance the drama by acting as a **visual focal point**.",
            )
            appendLine("")
            appendLine("* **Color Source:** The accent color MUST be a dynamic, high-contrast color ${GenrePrompts.colorAccent(genre)}.")
            appendLine("* **Placement:** Inject this phrase immediately before the main 'Narrative & Composition Core' block.")
            appendLine(
                "* **MANDATE:** The Agent MUST select a **single, specific, and logical existing detail** from the current character's description (e.g., a weapon, an accessory, a lining of clothing, or an armor accent) and use the accent color on it to make it pop against the surrounding dark palette.",
            )
            appendLine(
                "* **CRITICAL:** The Agent MUST use its analysis of the character details to create a **new and unique** localized color accent, ensuring it is coherent with the character's description (e.g., If the character wears sapphires, the accent cannot be a ruby ring). The narrative purpose (e.g., blood) must be avoided.",
            )
        }

    fun descriptionRules(genre: Genre) =
        buildString {
            appendLine("This description must:")
            appendLine("*   Integrate the **Character Details**.")
            appendLine(
                "*Develop a **Dramatic and Expressive Pose** for the character. This pose should be dynamic and reflect the character's essence, drawing from their **Character Details** (e.g., occupation, personality traits, role, equipped items). The pose should be original and compelling for an icon, not a static or default stance.",
            )
            appendLine("**Character Focus and Framing (CRITICAL - INJECTION OF VISUAL DIRECTION):**")
            appendLine("**Final Prompt Structure (Mandatory Order - BLOCK INJECTION):**")
            appendLine("")
            appendLine("1.  **Technical Foundation (Composed of Injected Data):**")
            appendLine(
                "* Start the prompt by injecting: **[Framing]**, **[Zoom Level / Proximity]**, **[Cropping Intention]**. (Using extracted data)",
            )
            appendLine(
                "* Inject the **Render Style**, **Art Technique**, **Aesthetic Era / Influence**, and **Vibe / Mood Aesthetic** to form the core style description. (Using extracted data)",
            )
            appendLine("* Inject the **Key Lighting Style**. (Using extracted data)")
            appendLine("")

            appendLine("2. **NARRATIVE & COMPOSITION CORE (Dynamic Scene Assembly - Final Mandate):**")
            appendLine("")
            appendLine("**A. NARRATIVE TENSION SOURCE (CRITICAL):**")
            appendLine(
                "The Agent MUST analyze ALL descriptive texts in the JSON and prioritize the one that provides the richest, most emotionally charged narrative context (The Central Conflict). This text will define the scene's emotional state and primary action.",
            )
            appendLine("")

            appendLine("**B. SUBJECT & RELATIONAL MANDATE (STRICT SUBJECT COUNT):**")
            appendLine(
                "The Agent MUST determine the number of characters to be rendered based *only* on the 'featuredCharacters' list provided in the Overall context JSON.",
            )
            appendLine("")
            appendLine(
                "* **IF 'featuredCharacters' list is NOT provided, is EMPTY, or contains only ONE name (Render GOAL: Icon/Portrait):**",
            )
            appendLine("* The prompt **MUST ONLY** describe the primary character.")
            appendLine(
                "* The narrative core focuses entirely on the character's **Internal Conflict** and **Expression**, and **MUST NOT** include any other human or humanoid subjects, unless that subject is explicitly named as a carried item or accessory (e.g., a doll).",
            )
            appendLine("* The Negative Prompts **MUST** include terms like 'multiple subjects' and 'group shot'.")
            appendLine("")
            appendLine("* **IF 'featuredCharacters' list contains TWO or more names (Render GOAL: Chapter Cover/Scene):**")
            appendLine(
                "* The Agent MUST generate a **unified, multi-subject scene** based on the relationship and tension extracted from the Central Conflict.",
            )
            appendLine(
                "* The prompt description **MUST clearly introduce and detail the primary two characters** (e.g., Character 1 and Character 2) and their **INTERACTION**.",
            )
            appendLine("* The Negative Prompts **MUST NOT** include terms like 'multiple subjects' and 'group shot'.")
            appendLine("")
            appendLine("**C. POSE & FRAMING INTEGRATION (RULE OF PROXIMITY ACTIVATED):**")
            appendLine(
                "* The description MUST synthesize the character's physical details with the action required by the **Central Conflict** and the **Framing** (Fase 1).",
            )
            appendLine(
                "* **Action Focus:** The pose and setting MUST reflect the extracted **Vibe / Mood Aesthetic**. The **LOW PROXIMITY MANDATE** requires the composition to integrate all characters into a detailed, atmospheric environment.",
            )
            appendLine("")
            appendLine(
                "3.  **Guardrail Final:** Use a universal set of negatives, adapting to the new framing (e.g., exclude \"close-up\").",
            )
            appendLine(imageHighlight(genre))
            appendLine(
                "* **CRITICAL:** Replace passive descriptions (e.g., \"Her jaw is clenched\") with active, dynamic phrasing (e.g., \"**Lunges forward** from the darkness, her neck strained, the motion captured as **strands of hair whip across her jawline**.\"). The description must convey **energy and tension**.",
            )
            appendLine(
                "* **Goal:** Use these terms to enhance the \"vibe\" and intensity. For example: \"ULTRA CLOSE-UP on  characters eye, Very tight shot, Subject fills entire frame, subtly cropped for intense focus.\"",
            )
            appendLine(
                "**CRITICAL:** The Agent MUST rewrite the character description to be consistent with the first element of the prompt, ensuring the focus is unambiguous.",
            )
            appendLine(
                "*Incorporate the **Overall Compositional Framing** and compatible **Visual Details & Mood** inspired by the general Visual Reference Image, but ensure the **Character\'s Pose** itself is uniquely dramatic and primarily informed by their provided **Character Details**.",
            )
            appendLine(
                "***CRUCIAL: Your output text prompt MUST NOT mention the Visual Reference Image.** It must be a self-contained description.",
            )
            appendLine(
                "- **Looks:** Describe the character's facial features and physical build (e.g., 'a rugged man with a lean physique', 'a Latina woman with a sophisticated haircut').",
            )
            appendLine(
                "- **Clothing:** Detail their attire, including style, color, and accessories (e.g., 'a vibrant Hawaiian-style shirt', 'a sleek two-piece swimsuit').",
            )
            appendLine(
                "- **Expression:** The face should not be neutral. It must convey a strong emotion or intention. Use terms like 'a hardened, protective gaze', 'a piercing, fatal stare', 'a sardonic smile'.",
            )
            appendLine(
                "- **Pose & Body Language:** Describe their posture and how they interact with the environment. Use dynamic phrases like 'relaxed yet alert posture', 'casually lounging on a car hood', 'body language exuding confidence'.",
            )
            appendLine(
                "Dramatic icon of [Character Name], a [Character's key trait/role]. Rendered in a distinct [e.g., 80s cel-shaded anime style with bold inked outlines].",
            )
            appendLine("The background is a vibrant [e.g., neon purple as per genre instructions].")
            appendLine(
                "Specific character accents include [e.g., luminous purple cybernetic eye details and thin circuit patterns on their blackpopover, as per genre instructions].",
            )
            appendLine(
                "The character's skin tone remains natural, and their primary hair color is [e.g., black], with lighting appropriate to the cel-shaded anime style and studio quality.",
            )
            appendLine("Desired Output: A single, striking image. NO TEXT SHOULD BE GENERATED ON THE IMAGE ITSELF.")
        }
}
