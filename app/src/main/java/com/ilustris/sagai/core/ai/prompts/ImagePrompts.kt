package com.ilustris.sagai.core.ai.prompts

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
            appendLine("CINEMATOGRAPHY EXTRACTION — Senior DP analyzing reference image")
            appendLine("Extract PHOTOGRAPHIC DNA (NOT art style/subject). Use ONLY camera/lighting terms.")
            appendLine("Fix missing or vague parameters autonomously. Return ONLY the 15 parameters below.")
            appendLine()
            appendLine("OUTPUT 15 PARAMETERS (Format: 'NAME: value'):")
            appendLine()
            appendLine("1. ANGLE: [eye-level / low XY° / high XY° / dutch XY°]")
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
            appendLine("11. PERSPECTIVE: [converging/parallel/barrel/foreshortening]")
            appendLine("12. TEXTURE: [razor-sharp/film-grain/digital-noise/soft-diffused/gritty]")
            appendLine("13. TIME: [golden-hour/midday/blue-hour/night/overcast/studio]")
            appendLine("14. SIGNATURE: One unique unforgettable detail")
            appendLine("15. DEPTH_LAYERS: [background/midground/foreground elements and spacing]")
        }

    /**
     * Validates and autonomously corrects image descriptions for generation.
     * Acts as rigid QA layer to enforce cinematography framing, art style, and composition rules.
     * CRITICAL: Validates ALL 15 cinematography parameters against visual direction.
     * Returns ImagePromptReview data class with corrections and violations detected.
     */
    fun reviewImagePrompt(
        visualDirection: String?,
        artStyleValidationRules: String,
        strictness: com.ilustris.sagai.core.ai.models.ReviewerStrictness,
        finalPrompt: String,
    ) = buildString {
        appendLine("=== IMAGE PROMPT QA REVIEWER (AI-to-AI) — RIGID CINEMATOGRAPHY ENFORCEMENT ===")
        appendLine(strictness.description)
        appendLine()
        appendLine("TASK: Validate and AUTONOMOUSLY FIX cinematography & art style violations.")
        appendLine("Return violations found + corrected prompt. RIGID ENFORCEMENT: No deviations permitted.")
        appendLine()

        appendLine("CINEMATOGRAPHY VALIDATION & FIX (RIGID):")
        appendLine("Required: All 15 cinematography parameters MUST be explicitly defined and exact.")
        appendLine()
        appendLine("MANDATORY CINEMATOGRAPHY PARAMETERS (ALL MUST BE PRESENT & COMPLIANT):")
        appendLine("1. ANGLE: MUST be one of [eye-level / low / high / dutch]. NO vague descriptions.")
        appendLine("2. LENS: MUST be one of [ultra-wide / wide / normal / portrait / tele / super-tele]. NO technical f-stops.")
        appendLine("3. FRAMING: MUST be one of [ECU / CU / MCU / MS / MWS / FS / WS / EWS]. EXACT terminology required.")
        appendLine("4. PLACEMENT: MUST specify [H: left/center/right] [V: upper/center/lower]. NO ambiguity.")
        appendLine("5. LIGHTING: MUST specify direction [front/side/back/top/under/omni] + quality [hard/soft]. Both required.")
        appendLine("6. COLOR: MUST specify temperature [cool/neutral/warm] + exact dominant colors. No optional palettes.")
        appendLine("7. ENVIRONMENT: MUST describe location type, scale, and key environmental elements explicitly.")
        appendLine("8. MOOD: MUST specify ONE emotional tone from [epic/intimate/oppressive/nostalgic/tense/serene/mysterious/majestic].")
        appendLine("9. DOF: MUST be one of [razor/shallow/moderate/deep/infinite]. EXACT depth-of-field specification.")
        appendLine("10. ATMOSPHERE: MUST be one of [clear/hazy/misty/foggy/dusty/smoky/ethereal]. NO vague atmospheric terms.")
        appendLine("11. PERSPECTIVE: MUST be one of [converging/parallel/barrel/foreshortening]. Spatial rules enforced.")
        appendLine("12. TEXTURE: MUST be one of [razor-sharp/film-grain/digital-noise/soft-diffused/gritty]. Image quality strict.")
        appendLine("13. TIME: MUST be one of [golden-hour/midday/blue-hour/night/overcast/studio]. Lighting time explicit.")
        appendLine("14. SIGNATURE: MUST have one unique, unforgettable detail. REQUIRED, not optional.")
        appendLine("15. DEPTH_LAYERS: MUST explicitly describe background/midground/foreground spacing and elements.")
        appendLine()

        visualDirection?.let {
            appendLine("REFERENCE VISUAL DIRECTION: $it")
            appendLine("ACTION: Extract expected cinematography parameters from this direction.")
            appendLine("Any missing or conflicting parameters MUST be corrected to match this reference.")
        }
        appendLine()

        appendLine("CINEMATOGRAPHY COMPLIANCE RULES:")
        appendLine("- If ANY of the 15 parameters is missing, vague, or non-compliant: CORRECT it immediately.")
        appendLine("- If technical jargon (f-stops/Kelvin/degrees) is present: REPLACE with visual descriptors.")
        appendLine("- If cinematography contradicts visual direction: FORCE alignment with the direction.")
        appendLine("- Verify final prompt describes CAMERA/LIGHTING terms, NOT art style or subject attributes.")
        appendLine("- Ensure VERTICAL COMPOSITION BIAS is explicit (top 1/3 empty, subject anchored bottom 2/3).")
        appendLine()

        appendLine("ART STYLE VALIDATION & FIX:")
        appendLine("Genre Rules: $artStyleValidationRules")
        appendLine("- Apply ALL validation rules autonomously — fix violations with zero tolerance.")
        appendLine("- Verify REQUIRED elements are present; remove FORBIDDEN elements.")
        appendLine("- Respect character traits, skin tones, hair styles, cultural details — maintain accuracy.")
        appendLine("- Keep token-optimized: omit redundant descriptors but retain critical attributes.")
        appendLine("- Output prompt must be assertive and direct: AI-to-AI communication style.")
        appendLine()

        appendLine("CRITICAL COMPOSITION ENFORCEMENT:")
        appendLine("- Full-bleed raster artwork (NO borders, frames, or simulated edges).")
        appendLine("- Vertical lock-screen bias: Subject in bottom 2/3rds, top 1/3rd clear/background only.")
        appendLine("- NO text, logos, watermarks, UI elements, or interface chrome.")
        appendLine("- NO transparent regions, alpha channels, or layered/panelled compositions.")
        appendLine()

        appendLine("OUTPUT JSON (ImagePromptReview):")
        appendLine(
            """{
  "originalPrompt": "...",
  "correctedPrompt": "...",
  "cinematographyValidation": {
    "parametersValidated": 15,
    "parametersCompliant": 15,
    "missingOrVague": [],
    "correctedParameters": ["..."]
  },
  "violations": [
    {"type": "MISSING_CINEMATOGRAPHY_PARAMETER", "severity": "CRITICAL", "parameter": "ANGLE", "description": "...", "correctedTo": "..."},
    {"type": "FRAMING_VIOLATION", "severity": "CRITICAL", "description": "...", "correctedTo": "..."},
    {"type": "BANNED_TERMINOLOGY", "severity": "MAJOR", "description": "...", "example": "..."}
  ],
  "changesApplied": ["Corrected ANGLE from vague to eye-level", "...", "..."],
  "wasModified": true,
  "complianceStatus": "FULL_COMPLIANCE"
}""",
        )
        appendLine()
        appendLine("PROMPT TO REVIEW & FIX:")
        appendLine(finalPrompt)
    }
}
