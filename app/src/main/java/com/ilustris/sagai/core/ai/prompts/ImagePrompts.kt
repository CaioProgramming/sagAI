package com.ilustris.sagai.core.ai.prompts

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
                "- The artwork must fill the entire output canvas. Apply a **Zoom Out (Medium Long Shot)** to anchor the subject in the bottom 2/3rds.",
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
            appendLine("**REQUIRED OUTPUT (8-Point Visual Manifest):**")
            appendLine()
            appendLine(
                "1. **Compositional Framework:** How is the scene framed? (e.g., 'Off-center subject with negative space', 'Symmetrical and confronting', 'Dynamic diagonal tension').",
            )
            appendLine(
                "2. **Emotional Atmosphere:** What is the mood of the air itself? (e.g., 'Heavy, humid, and oppressive', 'Crisp, cold, and detached', 'Warm, nostalgic, and hazy').",
            )
            appendLine(
                "3. **Lighting Design:** How is the light shaped? (e.g., 'Hard sunlight with deep jagged shadows', 'Soft studio diffusion with no shadows', 'Neon rim-lighting against darkness').",
            )
            appendLine(
                "4. **Color Harmony:** What is the emotional palette? (e.g., 'Desaturated melancholy blues', 'Vibrant, aggressive pop-colors', 'Earthy, sun-baked terracottas').",
            )
            appendLine(
                "5. **Visual Texture & Fidelity:** How 'clean' or 'gritty' is the view? (e.g., 'High-ISO film grain', 'Crystal clear digital sharpness', 'Soft misty diffusion').",
            )
            appendLine(
                "6. **Depth & Spatial Focus:** How is the depth handled? (e.g., 'Razor-thin depth of field isolating the eye', 'Infinite focus from foreground to horizon').",
            )
            appendLine(
                "7. **The 'X-Factor' Accent:** What specific detail grabs the attention? (e.g., 'The way light creates a flare', 'The subtle reflection on wet surfaces', 'The intense contrast in the eyes').",
            )
            appendLine(
                "8. **The Camera's Personality:** What does the 'lens' feel like? (e.g., 'A voyeuristic telephoto lens', 'An intimate and wide documentary lens', 'A clinical and precise portrait lens').",
            )
            appendLine()
            appendLine("**FINAL CHECK:**")
            appendLine(
                "Did you mention 'painting', 'drawing', or 'illustration'? **DELETE IT.** Use photographic terms like 'exposure', 'focus', 'contrast', and 'atmosphere' instead.",
            )
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
            appendLine("**--- CANVAS & COMPOSITION SPECIFICATIONS (STRICT) ---**")
            appendLine("1. **Aspect Ratio:** 9:16 (Vertical Portrait).")
            appendLine(
                "2. **VERTICAL NEGATIVE SPACE (NON-NEGOTIABLE):** The subject MUST be positioned heavily towards the BOTTOM of the frame to enable a 'Lock Screen Depth Effect'.",
            )
            appendLine(
                "   • **The Empty Top Third:** The top 35% of the canvas MUST be clear of the main subject. It should contain *only* background extended elements (sky, architecture, void).",
            )
            appendLine(
                "   • **Subject Anchor:** Anchor the subject lower. If they are 'tall', zoom out further. Do NOT fill the top of the frame.")
            appendLine()
            appendLine("**--- THE SCENE ASSEMBLY ---**")
            appendLine("**1. The Narrative Core:** Find the 'emotional center'. Let it drive the lighting, pose, and expression.")
            appendLine("**2. Subject & Relations:** Focus on the internal state. For groups, show connection/tension, not just proximity.")
            appendLine(
                "**3. Genre-World Integration:** The character exists *in their world*, not a studio. Use Depth of Field to suggest the world without distraction.")
            appendLine()
            appendLine(imageHighlight(genre))
            appendLine()
            appendLine("**--- FINAL OUTPUT EXECUTION ---**")
            appendLine("Write a single, flowing, and descriptive prompt.")
            appendLine(
                "   • **FORBIDDEN MECHANICS:** Do NOT use phrases like 'Injecting X...' or 'Visual Reference Image'. Just describe the visual result.",
            )
            appendLine(
                "   • **LANGUAGE:** Use evocative, painterly adjectives to describe textures and lighting (e.g., 'crimson silk catching neon light' instead of 'red shirt').",
            )
        }
}
