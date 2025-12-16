package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.characters.data.model.Character

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
                "- The artwork must fill the entire output canvas. The subject must be anchored in the bottom 2/3rds of the frame.",
            )
            appendLine("- The top 1/3rd MUST be empty of main subject details (sky/background only).")
            appendLine("- The composition must be **VERTICALLY BIASED** for lock-screen usage.")
            appendLine(
                "- The output must be a flattened raster (e.g., PNG/JPEG with no alpha) representing final artwork; do not present layered, masked, or panelled compositions.",
            )
            appendLine("- Do not render frames or simulated frames as visual effects (no faux-matte or simulated print borders).")
            appendLine()
        }

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
            appendLine(
                "You are a **Director of Photography and Visual Storyteller** analyzing a reference image to extract its **'Visual Soul'**.",
            )
            appendLine(
                "The goal is to apply this image's **Mood, Lighting, and Composition** to a completely NEW artwork with a FIXED Art Style (defined elsewhere).",
            )
            appendLine()
            appendLine("**CRITICAL RULE: IGNORE THE MEDIUM.**")
            appendLine("- Do NOT describe the art style (e.g., 'Oil Painting', '3D Render', 'Anime', 'Sketch').")
            appendLine("- Do NOT describe the literal subject (e.g., 'Woman in pool', 'Knight in armor').")
            appendLine("- **FOCUS ONLY** on the *Photography, Atmosphere, and Emotional Gaze*.")
            appendLine()
            appendLine("**YOUR MISSION: EXTRACT THE PHOTOGRAPHIC DNA**")
            appendLine(
                "If this image were a photograph taken by a master photographer, how would they describe their settings? How is the light hitting the lens? What is the depth of field doing?",
            )
            appendLine()
            appendLine("**REQUIRED OUTPUT (10-Point Visual Manifest):**")
            appendLine()
            appendLine(
                "1. **CAMERA DISTANCE / FRAMING (CRITICAL - MUST EXTRACT FIRST):** What is the shot type? This is NON-NEGOTIABLE and must be preserved exactly.",
            )
            appendLine(
                "   • CLOSE-UP / PORTRAIT / HEADSHOT: Frame shows primarily face, head, neck, upper shoulders. NO full body visible.",
            )
            appendLine(
                "   • MEDIUM SHOT / BUST: Frame shows head to waist/chest area. Lower body NOT visible.",
            )
            appendLine(
                "   • FULL BODY / WIDE: Entire body visible from head to feet.",
            )
            appendLine(
                "   • **OUTPUT FORMAT:** State clearly: 'FRAMING: [Close-up Portrait / Medium Bust Shot / Full Body Wide Shot]'",
            )
            appendLine(
                "   • **WARNING:** This framing MUST be respected in the final artwork. A portrait reference must produce a portrait output—DO NOT zoom out or change the camera distance. If the reference is a Close-up/Portrait, the output MUST be a Close-up/Portrait.",
            )
            appendLine()
            appendLine(
                "2. **Compositional Framework:** How is the scene framed beyond distance? (e.g., 'Off-center subject with negative space', 'Symmetrical and confronting', 'Dynamic diagonal tension').",
            )
            appendLine(
                "3. **Emotional Atmosphere:** What is the mood of the air itself? (e.g., 'Heavy, humid, and oppressive', 'Crisp, cold, and detached', 'Warm, nostalgic, and hazy').",
            )
            appendLine(
                "4. **Lighting Design:** How is the light shaped? (e.g., 'Hard sunlight with deep jagged shadows', 'Soft studio diffusion with no shadows', 'Neon rim-lighting against darkness').",
            )
            appendLine(
                "5. **Color Harmony:** What is the emotional palette? (e.g., 'Desaturated melancholy blues', 'Vibrant, aggressive pop-colors', 'Earthy, sun-baked terracottas').",
            )
            appendLine(
                "6. **Visual Texture & Fidelity:** How 'clean' or 'gritty' is the view? (e.g., 'High-ISO film grain', 'Crystal clear digital sharpness', 'Soft misty diffusion').",
            )
            appendLine(
                "7. **Depth & Spatial Focus:** How is the depth handled? (e.g., 'Razor-thin depth of field isolating the eye', 'Infinite focus from foreground to horizon').",
            )
            appendLine(
                "8. **The 'X-Factor' Accent:** What specific detail grabs the attention? (e.g., 'The way light creates a flare', 'The subtle reflection on wet surfaces', 'The intense contrast in the eyes').",
            )
            appendLine(
                "9. **The Camera's Personality:** What does the 'lens' feel like? (e.g., 'A voyeuristic telephoto lens', 'An intimate and wide documentary lens', 'A clinical and precise portrait lens').",
            )
            appendLine(
                "10. **Subject Vertical Position:** Where is the subject positioned vertically in the frame? (e.g., 'Centered', 'Lower third anchored', 'Upper biased'). This helps preserve composition intent.",
            )
            appendLine()
            appendLine("**FINAL CHECK:**")
            appendLine(
                "1. Did you clearly state the FRAMING type (Close-up/Medium/Full Body)? This is MANDATORY.",
            )
            appendLine(
                "2. Did you mention 'painting', 'drawing', or 'illustration'? **DELETE IT.** Use photographic terms like 'exposure', 'focus', 'contrast', and 'atmosphere' instead.",
            )
            appendLine(
                "3. Did you describe what the subject IS (their identity/actions)? **DELETE IT.** Focus only on HOW the camera captures them, not WHO they are.",
            )
        }

    fun descriptionRules() =
        buildString {
            appendLine("**--- CRITICAL COMPOSITION RULES ---**")
            appendLine(
                "1. **VERTICAL SUBJECT POSITIONING (Y-AXIS SHIFT):** Position the subject LOWER in the frame by shifting them DOWN on the vertical axis. Do NOT zoom out or change the camera distance—maintain the exact framing/zoom level from the Visual Direction. Simply anchor the subject in the lower portion of the canvas.",
            )
            appendLine(
                "   • For CLOSE-UP/PORTRAIT shots: Position the face/head in the lower 70% of the frame, allowing subtle breathing room at the top. DO NOT ZOOM OUT to show the torso if the reference is a headshot.",
            )
            appendLine(
                "   • For MEDIUM shots: Anchor the subject's torso in the bottom 2/3rds of the frame.",
            )
            appendLine(
                "   • For FULL BODY shots: Anchor feet near the bottom edge, leaving the top 30% for sky/environment.",
            )
            appendLine(
                "   • **CRITICAL:** This is a COMPOSITION adjustment, NOT a zoom adjustment. The camera distance stays the same—only the subject's vertical position changes.",
            )
            appendLine(
                "2. **NO WHITE FRAMES:** The image must be full-bleed artwork with no borders, frames, or white edges.",
            )
            appendLine()
        }

    /**
     * Creates a reviewer prompt that validates and corrects image descriptions before generation.
     * Acts as a Quality Assurance layer to catch violations of art style, framing, and composition rules.
     */
    fun reviewImagePrompt(
        visualDirection: String?,
        artStyleValidationRules: String,
        strictness: com.ilustris.sagai.core.ai.models.ReviewerStrictness,
        finalPrompt: String,
    ) = buildString {
        appendLine("**=== IMAGE PROMPT QUALITY ASSURANCE REVIEWER ===**")
        appendLine()
        appendLine("**YOUR ROLE:**")
        appendLine(strictness.description)
        appendLine()
        appendLine("**YOUR TASK:**")
        appendLine("Review the IMAGE PROMPT below and check it against the ART STYLE RULES and VISUAL DIRECTION.")
        appendLine("Your goal is to catch violations BEFORE expensive image generation occurs.")
        appendLine()
        appendLine("**WHAT TO CHECK:**")
        appendLine()
        appendLine("1. **FRAMING COMPLIANCE:**")
        visualDirection?.let {
            appendLine("   Visual Direction specifies:")
            appendLine("   ```")
            appendLine("   $it")
            appendLine("   ```")
            appendLine()
            appendLine("   VALIDATION:")
            appendLine("   - Extract the FRAMING type (Close-up/Portrait, Medium, Full Body)")
            appendLine("   - Check if the prompt describes body parts OUTSIDE the camera view:")
            appendLine("     * Close-up/Portrait: Should NOT mention legs, feet, full outfit, stance")
            appendLine("     * Medium shot: Should NOT mention legs, feet")
            appendLine("     * Full body: Can describe everything")
            appendLine("   - If violations found: REMOVE those descriptions")
        } ?: appendLine("   No specific visual direction provided. Skip framing check.")
        appendLine()
        appendLine("2. **ART STYLE COMPLIANCE:**")
        appendLine("   Art Style Validation Rules:")
        appendLine("   ```")
        appendLine("   $artStyleValidationRules")
        appendLine("   ```")
        appendLine()
        appendLine("   VALIDATION:")
        appendLine("   - Scan the prompt for BANNED TERMS (e.g., 'brown eyes', 'soft lighting', 'plain background')")
        appendLine("   - Check if REQUIRED ELEMENTS are present (e.g., background details, specific art techniques)")
        appendLine("   - Verify anatomy descriptions match the style (e.g., 'cartoon proportions' vs 'realistic')")
        appendLine("   - If violations found: REPLACE banned terms with correct alternatives, ADD missing elements")
        appendLine()
        appendLine("3. **BACKGROUND VALIDATION:**")
        appendLine("   - If art style REQUIRES backgrounds (check validation rules), ensure 3+ specific objects are described")
        appendLine("   - If prompt says 'plain background' or 'gradient background' when forbidden: ADD environmental details")
        appendLine(
            "   - For portrait/close-up framing with mandatory backgrounds: adapt background to tight framing (wall, neon glow, posters visible)",
        )
        appendLine()
        appendLine("**OUTPUT FORMAT (JSON):**")
        appendLine("Return a JSON object with this EXACT structure:")
        appendLine(
            """
            {
              "correctedPrompt": "The full corrected image prompt (or original if no changes needed)",
              "violations": [
                {
                  "type": "FRAMING_VIOLATION|BANNED_TERMINOLOGY|MISSING_ELEMENTS|ANATOMY_MISMATCH|STYLE_CONTRADICTION",
                  "severity": "CRITICAL|MAJOR|MINOR",
                  "description": "What was wrong",
                  "example": "Specific text from the prompt that violated the rule"
                }
              ],
              "changesApplied": [
                "Human-readable description of each fix applied"
              ],
              "wasModified": true
            }
            """.trimIndent(),
        )
        appendLine()
        appendLine("**SEVERITY GUIDELINES:**")
        appendLine(
            "- CRITICAL: Would break image generation or produce completely wrong output (wrong framing, missing mandatory elements)",
        )
        appendLine("- MAJOR: Significantly degrades quality or misses key requirements (banned terminology, wrong anatomy)")
        appendLine("- MINOR: Small deviation that might affect polish but not core functionality")
        appendLine()
        appendLine("**IMPORTANT:**")
        appendLine("- If no violations found, return the original prompt unchanged with empty violations array and wasModified: false")
        appendLine("- When correcting, preserve the original's tone, personality, and artistic intent")
        appendLine("- Only change what clearly violates the rules based on your strictness level")
        appendLine("- The correctedPrompt should be ready for direct use in image generation")
        appendLine()
        appendLine("**IMAGE PROMPT TO REVIEW:**")
        appendLine("```")
        appendLine(finalPrompt)
        appendLine("```")
    }
}
