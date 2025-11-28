package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.GenrePrompts.artStyle
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre

object ImagePrompts {
    fun criticalGenerationRule() =
        buildString {
            appendLine("*CRITICAL RULE* — ABSOLUTE FULL-BLEED FINAL ART (NON-NEGOTIABLE):")
            appendLine(
                "The generated image MUST be a finished, full-canvas (full-bleed) raster artwork that fills the entire frame. Under no circumstances may the image contain borders, frames, panels, inset artwork, or any graphic element that implies a framed or unfinished asset.",
            )
            appendLine()
            appendLine("ABSOLUTE FORBIDDEN ELEMENTS (Do NOT render any of the following within the image):")
            appendLine("- Any text or typography (titles, captions, labels, EXIF overlays)")
            appendLine("- Logos, brand marks, signatures, trademarks, watermarks, stamps, or artist credits")
            appendLine(
                "- Borders, decorative frames, matting, rounded-corner masks, inset panels, picture-in-picture, film strips, polaroid edges",
            )
            appendLine("- UI elements, overlays, HUDs, icons, progress bars, buttons, or any interface chrome")
            appendLine("- Letterbox/pillarbox bars, black bars, bleed/crop marks, registration marks, rulers, or guide lines")
            appendLine("- Transparent background, alpha channel output, or any partially rendered region implying non-final art")
            appendLine()
            appendLine("COMPOSITION ENFORCEMENTS:")
            appendLine(
                "- The artwork must fill the entire output canvas. If necessary, allow natural subject cropping at edges to maintain a full-bleed composition.",
            )
            appendLine(
                "- The output must be a flattened raster (e.g., PNG/JPEG with no alpha) representing final artwork; do not present layered, masked, or panelled compositions.",
            )
            appendLine("- Do not render frames or simulated frames as visual effects (no faux-matte or simulated print borders).")
            appendLine()
        }

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
            "Apply a solid background using the provided hex color: $backgroundHexCode.",
        )
    }

    @Suppress("ktlint:standard:max-line-length")
    fun extractComposition() =
        buildString {
            appendLine("You are analyzing ONE composition reference image:")
            appendLine(
                "1. **Image A (Composition Reference):** This single reference dictates layout, framing, lens feel, and photographic treatment, and most importantly, the **emotional and dramatic intent**.",
            )
            appendLine("")
            appendLine(
                "Your task is to extract FIVE concise, photography-and-mood-focused composition components based only on Image A. Your response **MUST BE A CONCISE, UNINTERRUPTED LIST** of the extracted phrases/terms, each prepended by its respective label (for example: 'Framing: '). **DO NOT INCLUDE ANY INTRODUCTORY TEXT, HEADINGS, OR EXPLANATIONS.**",
            )
            appendLine("")
            appendLine("**MANDATORY OUTPUT ORDER AND LABELS (5 points):**")
            appendLine("1. Framing & Crop Intention: [Value]")
            appendLine("2. Mood & Dramatic Direction: [Value]")
            appendLine("3. Lens Feel & Perspective: [Value]")
            appendLine("4. Depth of Field & Focus: [Value]")
            appendLine("5. Key Lighting Style & Contrast: [Value]")
            appendLine("")

            appendLine("**Guidelines for filling each label (be precise, concise, and model-friendly):**")

            appendLine(
                "1. Framing & Crop Intention: Use the exact photographic framing term (e.g., CLOSE-UP, BUST-UP, FULL-BODY) and explicitly describe the body crop and subject fill (e.g., 'CLOSE-UP, subject fills ~85%, cropped at shoulders, tight focus on eyes').",
            )
            appendLine(
                "2. Mood & Dramatic Direction: Describe the overall *feeling* and the subject's *pose/gaze* to guide the AI's artistic direction. (e.g., 'Intense and focused, direct eye contact, powerful stance, cinematic drama' or 'Melancholic, head tilted back in pain/ecstasy, high emotional energy').",
            )
            appendLine(
                "3. Lens Feel & Perspective: Describe the perceptual effect and provide an approximate focal length range: e.g., 'Short tele compressed portrait look (85–135mm approx), natural facial proportions' or 'Mild wide-angle feel (35–50mm approx), slight foreground exaggeration'.",
            )
            appendLine(
                "4. Depth of Field & Focus: State DOF and its effect on the background. **Avoid mentioning 'bokeh' unless strong blur is clearly visible and necessary.** (e.g., 'Shallow DOF, soft background blur for subject isolation' or 'Deep focus, sharp background, full scene detail').",
            )
            appendLine(
                "5. Key Lighting Style & Contrast: Describe main light direction/quality (soft/hard key) and contrast: e.g., 'Soft key from camera-right, subtle fill, low-medium contrast portrait' or 'Hard side light, high contrast, dramatic rim lighting'.",
            )

            appendLine("")
            appendLine("**OUTPUT RULES (STRICT):**")
            appendLine(
                "- Output MUST consist of exactly the five labeled lines above in the same order, each with a concise bracketed value. No extra lines, headings, or commentary are allowed.",
            )
            appendLine(
                "- Use measured, unambiguous terms (percent ranges, approximate focal-length ranges). Prefer short, declarative phrases focused on visual and emotional effect.",
            )
            appendLine(
                "- Base every field solely on directly observable photographic and emotional cues (crop lines, perspective, shadow falloff, pose, and perceived mood).",
            )
            appendLine("- Do not mention the reference image or instructions in the output. Provide only the labeled values.")
        }

    @Suppress("ktlint:standard:max-line-length")
    fun imageHighlight(genre: Genre) =
        buildString {
            appendLine("")
            appendLine("Core Intent:")
            appendLine("1. Make the accent feel organic and photographic — not a graphic neon outline or harsh glow contour.")
            appendLine(
                "2. Use the accent to subtly lift and define a single small area (jawline, eye catch, collarbone, hair edge) so it becomes a striking, theme-defining detail.",
            )
            appendLine("")
            appendLine("Light Properties:")
            appendLine("1. Role: Secondary/supporting light only — not the main illumination")
            appendLine("2. Purpose: Create a focused highlight that guides the viewer's eye and adds a tactile sense of materiality")
            appendLine(
                "3. Character: Smooth, organic, and photographic — think diffusion, soft rim, micro-speculars, slight halation — NOT a hard neon contour",
            )
            appendLine("")
            appendLine("Required Elements (include all):")
            appendLine("1. Position: Name exact spot ('edge of jawline', 'upper right hairline', 'inner corner of eye')")
            appendLine(
                "2. Intensity & Falloff: State level (soft/medium), spread (tight/gradual), and emphasize a natural falloff (no abrupt halo)",
            )
            appendLine(
                "3. Surface Interaction: Describe one material interaction ('soft specular on skin', 'subtle sheen on wet lips', 'diffused highlight in hair')",
            )
            appendLine("")
            appendLine("Technical Constraints:")
            appendLine("* Keep color influence subtle (max 8-12% of total lighting) — enough to be noticeable but not overpowering")
            appendLine(
                "* No glow contour, halo, or hard neon outline around the subject; avoid terms like 'glowing rim', 'thick neon edge', or 'haloed silhouette'.",
            )
            appendLine(
                "* Prefer descriptions that imply scattering or diffusion (e.g., 'organic wash', 'soft edge', 'gradual falloff', 'micro-speculars')",
            )
            appendLine("* Must integrate with the main lighting — not compete with it; works as an accent, not a separate light source")
            appendLine("")
            appendLine("Output Examples (use these tones as templates):")
            appendLine("")
            appendLine("Formatting and Placement:")
            appendLine("- Output this single short phrase (6-14 words) before the Narrative Core section.")
            appendLine(
                "- Phrase must mention exact position, intensity/falloff, and one surface interaction. Keep it photographic and concise.",
            )
            appendLine("")
            appendLine("Why this matters:")
            appendLine(
                "A well-placed, smoothly rendered color accent can transform a portrait from flat to cinematic — it should read as a believable photographic artifact that reinforces the genre color and theme without feeling graphic or artificial.",
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
            appendLine("1. **Technical Foundation (Composed of injected composition data points):**")
            appendLine(
                "* Start the prompt by injecting the **Framing**, **Zoom Level / Proximity**, and **Cropping Intention**. (From extractComposition)",
            )
            appendLine(
                "* Inject **Key Lighting Style**, **Camera Angle**, **Lens / Focal Length**, and **Depth of Field / Motion Treatment** as supporting composition cues.",
            )
            appendLine("* Inject the **Key Lighting Style** prominently where it affects mood and shading.")
            appendLine(
                "* Inject the **Art Style** from the provided genre data (use GenrePrompts.artStyle(genre) — do NOT infer or extract art style from the reference images). This hardcoded art style must be applied to the character rendering and overall final look.",
            )
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

            appendLine("**C. POSE & FRAMING INTEGRATION (CRITICAL - ACTION DYNAMISM MANDATE):**")
            appendLine(
                "* The Agent MUST replace passive descriptions with a **unique, active, and dynamically phrased action** that conveys the character's core tension and mood.",
            )
            appendLine(
                "* **MANDATE OF ORIGINALITY:** The generated action phrase MUST be **structurally original** for the current image. The Agent **MUST NOT** use the terms 'lunges forward', 'neck strained', or 'strands of hair whip' or any close structural variation (e.g., 'hair flicks', 'head strained') unless the Central Conflict explicitly describes a sudden lunge or attack.",
            )
            appendLine(
                "* The action phrase must be highly relevant to the Vibe/Mood Aesthetic (e.g., if 'Contemplative', use 'Her posture radiates quiet defiance, her focus absolute').",
            )
            appendLine(
                "* The description MUST synthesize the character's physical details with the action required by the **Central Conflict** and the **Framing**.",
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
