package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.ReviewerStrictness

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
            appendLine("GENRE STYLE ADHERENCE (CRITICAL):")
            appendLine("- The artwork MUST strictly follow the genre's art style requirements (technique, color palette, mood, era)")
            appendLine("- FORBIDDEN elements specified in the genre rules MUST NOT appear in the image")
            appendLine("- REQUIRED elements specified in the genre rules MUST be present")
            appendLine("- Character design, clothing, and aesthetics MUST match the genre specifications exactly")
            appendLine("- DO NOT mix incompatible styles or include elements from other genres")
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

    @Suppress("ktlint:standard:max-line-length")
    fun extractComposition() =
        buildString {
            appendLine("SYSTEM ROLE: Senior Director of Photography & AI Prompt Engineer")
            appendLine("TASK: Perform a technical CINEMATOGRAPHY EXTRACTION from the provided reference image.")
            appendLine("OBJECTIVE: Extract pure PHOTOGRAPHIC DNA (lens, lighting, angle, composition). Ignore subject and art style.")
            appendLine()
            appendLine("CRITICAL DIRECTIVE (NON-NEGOTIABLE):")
            appendLine("- NO PREAMBLE: Do not start with 'Okay', 'Let's break down', or any introductory filler.")
            appendLine("- NO SUMMARY: Do not provide a concluding summary or 'In summary' section.")
            appendLine("- NO CONVERSATION: Do not address the user. Do not offer future help. Do not say 'let me know'.")
            appendLine("- ASSERTIVE TONE: Provide raw, actionable technical data. Use declarative statements.")
            appendLine("- AI-READY: The output must be directly usable as a visual direction for another AI model.")
            appendLine()
            appendLine("OUTPUT STRUCTURE (START DIRECTLY WITH PARAMETERS):")
            appendLine()
            appendLine("15 CINEMATOGRAPHY PARAMETERS (Format: 'NAME: [Value]'):")
            appendLine()
            appendLine(
                "1. ANGLE & VIEWPOINT: [eye-level / low-angle looking up / high-angle looking down / dutch-angle (specify tilt) / worm's-eye view / bird's-eye view / point-of-view (POV)] - Be specific and dramatic.")
            appendLine(
                "2. LENS: [14-24mm ultra-wide / 24-35mm wide / 35-50mm normal / 50-85mm portrait / 85-200mm tele / 200mm+ super-tele]",
            )
            appendLine(
                "3. FRAMING: [ECU face / CU head-shoulders / MCU head-chest / MS head-waist / MWS head-knees / FS full-body / WS body+env / EWS small-in-vast]",
            )
            appendLine("4. PLACEMENT: [H: left/center/right third] [V: upper/center/lower third]")
            appendLine("5. LIGHTING: [front/side/back/top/under/omni] + [hard/soft]")
            appendLine("6. COLOR: [cool / neutral / warm] + dominant palette")
            appendLine("7. ENVIRONMENT: Location type, scale, key elements")
            appendLine("8. MOOD: Emotional tone (epic/intimate/oppressive/nostalgic/etc)")
            appendLine("9. DOF: [razor / shallow / moderate / deep / infinite]")
            appendLine("10. ATMOSPHERE: [clear/hazy/misty/foggy/dusty/smoky]")
            appendLine(
                "11. PERSPECTIVE: [one-point / two-point / three-point / forced / atmospheric / barrel distortion / dramatic foreshortening]")
            appendLine("12. TEXTURE: [razor-sharp/film-grain/digital-noise/soft-diffused/gritty]")
            appendLine("13. TIME: [golden-hour/midday/blue-hour/night/overcast/studio]")
            appendLine("14. SIGNATURE: One unique unforgettable detail")
            appendLine("15. DEPTH_LAYERS: [background/midground/foreground elements and spacing]")
            appendLine()
            appendLine("VISIBILITY ANALYSIS:")
            appendLine("Classify each element: VISIBLE / PARTIAL / HIDDEN / OCCLUDED")
            appendLine("Analyze: 1.HEAD 2.FACE 3.TORSO 4.ARMS/HANDS 5.LEGS/FEET 6.DISTINCTIVE MARKS 7.BODY LANGUAGE 8.ENVIRONMENT")
            appendLine()
            appendLine("MANDATORY HIDDEN BY FRAMING (Bullet points of elements GUARANTEED not visible):")
            appendLine("- ECU: Mid-neck and below (Keep chin, jawline, and top of headgear/accessories).")
            appendLine("- CU: Lower-chest and below (Keep neck, shoulders, and upper-piece collars/accessories).")
            appendLine("- MCU: Waist and below (Keep full torso, including jackets or shirts).")
            appendLine("- MS: Mid-thigh and below (Keep waist and hips).")
            appendLine("- MWS: Ankles and below (ALL footwear).")
            appendLine("- FS/WS/EWS: Fine details (scars, intricate patterns) obscured by scale.")
            appendLine()
            appendLine("IDENTITY ANCHOR:")
            appendLine("- Define key non-subject traits: [Skin Tone / Hair Color / Key Accessory / Silhouette] that MUST persist.")
            appendLine()
            appendLine("ANGLE & PERSPECTIVE ENFORCEMENT:")
            appendLine("- BANNED: 'eye-level', 'straight-on', 'plain view' (unless strictly required by composition).")
            appendLine("- REQUIRED: Establish a dominant, dynamic viewpoint that directs the viewer's eye.")
            appendLine()
            appendLine("FINAL VERIFICATION: Is the output 100% technical? Is it devoid of pleasantries? If yes, output now.")
        }

    /**
     * Validates and autonomously corrects image descriptions for generation.
     * Returns JSON-structured ImagePromptReview data class with corrections and violations.
     * TOKEN-OPTIMIZED: Compact instructions, redundancy removed. GemmaClient handles output formatting.
     * CRITICAL: Validates 15 cinematography parameters, visibility matrix, pose/expression, and art style rules.
     */
    fun reviewImagePrompt(
        visualDirection: String?,
        artStyleValidationRules: String,
        strictness: ReviewerStrictness,
        finalPrompt: String,
    ) = buildString {
        appendLine(strictness.description)
        appendLine()
        appendLine("TASK: Analyze the prompt against these criteria. Respond ONLY with JSON matching ImagePromptReview structure.")
        appendLine()

        appendLine("VALIDATION CRITERIA:")
        appendLine()
        appendLine(
            "1. CINEMATOGRAPHY (15 params): angle, lens, framing, placement, lighting, color, environment, mood, DOF, atmosphere, perspective, texture, time, signature, depth_layers",
        )
        appendLine("   - All MUST be explicit, specific, and match visual direction.")
        appendLine(
            "   - **ANGLE & PERSPECTIVE ENFORCEMENT:** The prompt's angle MUST be specific, non-generic, and creative, as mandated by the visual direction. It must avoid 'banned perspectives' like flat or plain views. The description must clearly reflect the chosen viewpoint (e.g., 'The camera looks up at the towering figure...').",
        )
        appendLine("   - NO technical jargon (f-stops/Kelvin/degrees) - use visual descriptors")
        appendLine()

        appendLine("2. VISIBILITY ENFORCEMENT & CHARACTER CONTINUITY:")
        visualDirection?.let {
            appendLine("   VISUAL DIRECTION: \"$it\"")
            appendLine("   - Analyze this direction to determine what is VISIBLE vs. HIDDEN.")
            appendLine(
                "   - **IDENTITY PRESERVATION:** Do NOT omit core character details like headgear, facial features, or upper-body accessories (necklaces, distinctive collars) unless they are truly outside the frame. These define the character.",
            )
            appendLine(
                "   - **PORTRAIT FLEXIBILITY:** For CU (Head-Shoulders) or MCU (Head-Chest), ensure subtle outfit details from the chest up are included to ground the character. Do not strip clothing descriptions that characterize the upper-body silhouette.",
            )
            appendLine(
                "   - ABSOLUTE RULE: The final prompt MUST NOT contain descriptions of elements explicitly marked as HIDDEN (e.g., footwear in a headshot).",
            )
            appendLine(
                "   - ACTION: If the prompt describes a truly hidden element (e.g., 'wearing leather boots' in a CU), DELETE it. But retain upper-body details that provide context for the character's style.",
            )
            appendLine()
        }

        appendLine("3. POSE & EXPRESSION (ORGANIC EMOTION & NARRATIVE):")
        appendLine(
            "   - FACIAL EXPRESSION: MUST convey a specific, nuanced emotion (e.g., 'weary resignation', 'mischievous glee', NOT 'has emotion').",
        )
        appendLine(
            "   - BODY LANGUAGE/POSE: MUST organically express emotion and intent, supporting the facial expression and narrative (e.g., 'shoulders slumped in defeat', 'hands clenched in anticipation', NOT just 'standing' or 'sitting').",
        )
        appendLine("   - SYNERGY: Facial expression + body language MUST seamlessly and powerfully communicate a unified emotional state and story moment.")
        appendLine("   - MOMENT: Character 'caught in a pivotal moment' (mid-action, reacting to an unseen event, lost in thought), NOT 'posed for portrait' or static.")
        appendLine()

        appendLine("4. GOOGLE IMAGE GENERATION BEST PRACTICES:")
        appendLine("   - CLARITY: Concrete descriptions, NO metaphors/abstractions")
        appendLine("   - EXPLICIT: Say what IS present, NOT what to avoid")
        appendLine("   - FEATURE HIERARCHY: Lead with critical details, then context")
        appendLine("   - AMBIGUITY: Zero vague adjectives - every descriptor must be actionable")
        appendLine()

        appendLine("5. ART STYLE RULES:")
        appendLine("   Genre: $artStyleValidationRules")
        appendLine("   - Apply ALL rules with ZERO tolerance for violations")
        appendLine("   - Verify REQUIRED elements present, FORBIDDEN elements absent")
        appendLine("   - Respect character traits (skin tone, hair, body type, distinctive marks)")
        appendLine()

        appendLine("VIOLATION DETECTION:")
        appendLine("- VISIBILITY_VIOLATION: Describes body parts/clothing out of frame")
        appendLine("- MISSING_FACIAL_EXPRESSION: No specific emotion or generic descriptor")
        appendLine("- MISSING_DYNAMIC_POSE: Static/neutral pose without emotional content")
        appendLine("- POSE_EXPRESSION_VIOLATION: Expression + pose don't work together or character seems posed, not in moment")
        appendLine("- FRAMING_VIOLATION: Describes elements not visible at this framing level")
        appendLine("- PERSPECTIVE_VIOLATION: Uses a generic, flat, or banned perspective (e.g., 'eye-level' without justification, 'plain view') or the description does not match the specified angle.")
        appendLine("- BANNED_TERMINOLOGY: Uses forbidden words from art style")
        appendLine("- MISSING_CINEMATOGRAPHY_PARAMETER: Any of 15 params missing/vague")
        appendLine("- LIGHTING_MISSING/WRONG, COLOR_PALETTE_WRONG, ENVIRONMENT_MISSING, etc.")
        appendLine()

        appendLine("AUTO-FIX PATTERNS:")
        appendLine("- Missing expression → Add specific emotion matching archetype")
        appendLine("- Missing pose → Add dynamic body language with gesture")
        appendLine("- Expression + pose contradictory → Align emotionally")
        appendLine("- Out-of-frame descriptions → Remove, replace with visible details")
        appendLine("- Flat/generic angle → Replace with a more dynamic, descriptive angle (low-angle, high-angle, POV) that enhances the mood.")
        appendLine("- Static character → Add momentum language ('breathing', 'captured mid-action')")
        appendLine("- Generic cinematography → Specify exact values from 15 parameters")
        appendLine("- Missing background/environment → Add 3+ specific objects")
        appendLine()

        appendLine("OUTPUT JSON (ImagePromptReview):")

        appendLine()
        appendLine("PROMPT TO REVIEW:")
        appendLine(finalPrompt)
    }
}
