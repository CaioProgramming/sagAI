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
            appendLine("Fix missing or vague parameters autonomously. Return the 15 parameters + VISIBLE ELEMENTS analysis.")
            appendLine()
            appendLine("OUTPUT 15 CINEMATOGRAPHY PARAMETERS (Format: 'NAME: value'):")
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
            appendLine()
            appendLine("CRITICAL ADDITION - VISIBLE ELEMENTS ANALYSIS:")
            appendLine("After extracting the 15 parameters, ANALYZE what is VISIBLE vs HIDDEN by the framing:")
            appendLine()
            appendLine("VISIBILITY MATRIX:")
            appendLine("- FULLY VISIBLE: Face, torso, limbs, or features that are 100% within frame")
            appendLine("- PARTIALLY VISIBLE: Features cut by frame edges, partially obscured by props/environment")
            appendLine("- HIDDEN/OUT-OF-FRAME: Features not visible due to framing, angle, or occlusion")
            appendLine("- OCCLUDED: Features hidden behind other objects, hair, hands, clothing, etc.")
            appendLine()
            appendLine("SPECIFIC ANALYSIS REQUIRED:")
            appendLine("1. HEAD: [fully visible / partially cropped / profile visible / back of head / obscured]")
            appendLine("2. FACE DETAILS: List what IS visible (eyes, nose, mouth, jawline, ears, scars, marks) vs HIDDEN")
            appendLine("3. TORSO: [fully visible / partially visible / covered by environment/clothing]")
            appendLine("4. ARMS/HANDS: [both visible / one visible / one hidden / both out of frame / holding objects visible]")
            appendLine("5. LEGS/FEET: [both visible / partial view / one in frame / out of frame / obscured by clothing]")
            appendLine("6. DISTINCTIVE FEATURES: List ALL visible identifying marks (tattoos, scars, jewelry, unique clothing details)")
            appendLine("7. BODY LANGUAGE: Posture, gesture, expression elements that ARE visible in this framing")
            appendLine("8. ENVIRONMENT VISIBILITY: What background/props are visible based on composition and DOF")
            appendLine()
            appendLine("FRAMING IMPACT ON ATTRIBUTES:")
            appendLine("For each character attribute (skin tone, hair, body type, distinctive marks):")
            appendLine("- MUST SHOW: Critical identity markers visible at this framing level")
            appendLine("- CAN HIDE: Secondary details that don't impact recognition or can be implied")
            appendLine("- CANNOT HIDE: Core traits that define character (race/ethnicity, primary clothing, main distinctive features)")
            appendLine()
            appendLine("REVIEWER VALIDATION USE:")
            appendLine("The reviewer will use this VISIBILITY MATRIX to:")
            appendLine("1. Verify that hidden attributes are ONLY secondary details, not identity markers")
            appendLine("2. Ensure core traits remain visible and recognizable despite framing constraints")
            appendLine("3. Validate that framing choices don't inadvertently omit critical character attributes")
            appendLine("4. Confirm that what IS visible in frame accurately represents the character context")
            appendLine()
            appendLine("EXAMPLE OUTPUT FORMAT:")
            appendLine("1. ANGLE: eye-level")
            appendLine("2. LENS: 50-85mm portrait")
            appendLine("3. FRAMING: CU head-shoulders")
            appendLine("[... other 12 parameters ...]")
            appendLine()
            appendLine("VISIBILITY ANALYSIS:")
            appendLine("HEAD: Fully visible, facing forward")
            appendLine("FACE DETAILS: Eyes visible (conveying emotion), nose visible, mouth visible, ears partially visible behind hair")
            appendLine("TORSO: Upper chest visible, shoulders fully visible, arms below elbow out of frame")
            appendLine(
                "DISTINCTIVE FEATURES: Visible - facial scar on left cheekbone, neck tattoo (partial), clothing color/pattern visible",
            )
            appendLine("HIDDEN: Hands/fingers (framing cuts them), legs (out of frame), lower body details")
            appendLine(
                "BODY LANGUAGE: Confident posture visible in shoulder position and head angle, facial expression conveys determination",
            )
            appendLine("CORE IDENTITY PRESERVED: Skin tone, facial structure, hair visible - character remains identifiable")
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
        strictness: com.ilustris.sagai.core.ai.models.ReviewerStrictness,
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
        appendLine("   - All MUST be explicit, specific, and match visual direction")
        appendLine("   - NO technical jargon (f-stops/Kelvin/degrees) - use visual descriptors")
        appendLine()

        appendLine("2. VISIBILITY MATRIX (from director):")
        visualDirection?.let {
            appendLine("   Direction: $it")
            appendLine("   - Extract what IS visible vs OUT OF FRAME")
            appendLine("   - CRITICAL: Do NOT describe body parts/clothing not in frame")
            appendLine("   - NO pants/boots if legs out of frame, NO full arms if only elbow-down visible, etc.")
            appendLine()
        }

        appendLine("3. POSE & EXPRESSION (PREVENTS SOULLESS ART):")
        appendLine("   - FACIAL EXPRESSION: MUST be specific emotion (e.g., 'cynical smirk', NOT 'has emotion')")
        appendLine("   - DYNAMIC POSE: MUST suggest action/emotion (e.g., 'stands defiantly', NOT just 'standing')")
        appendLine("   - SYNERGY: Expression + pose MUST work together emotionally")
        appendLine("   - MOMENT: Character 'caught in a moment' (mid-action, reacting), NOT 'posed for portrait'")
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
        appendLine("- BANNED_TERMINOLOGY: Uses forbidden words from art style")
        appendLine("- MISSING_CINEMATOGRAPHY_PARAMETER: Any of 15 params missing/vague")
        appendLine("- LIGHTING_MISSING/WRONG, COLOR_PALETTE_WRONG, ENVIRONMENT_MISSING, etc.")
        appendLine()

        appendLine("AUTO-FIX PATTERNS:")
        appendLine("- Missing expression → Add specific emotion matching archetype")
        appendLine("- Missing pose → Add dynamic body language with gesture")
        appendLine("- Expression + pose contradictory → Align emotionally")
        appendLine("- Out-of-frame descriptions → Remove, replace with visible details")
        appendLine("- Static character → Add momentum language ('breathing', 'captured mid-action')")
        appendLine("- Generic cinematography → Specify exact values from 15 parameters")
        appendLine("- Missing background/environment → Add 3+ specific objects")
        appendLine()

        appendLine("OUTPUT JSON (ImagePromptReview):")
        appendLine(
            """{
  "originalPrompt": "string - the input prompt",
  "correctedPrompt": "string - fixed/enhanced prompt with all corrections applied",
  "cinematographyValidation": {
    "parametersValidated": 15,
    "parametersCompliant": number (0-15),
    "missingOrVague": ["param1", "param2"],
    "correctedParameters": ["param: old → new", ...]
  },
  "visibilityMatrix": {
    "head": "visibility state",
    "faceDetails": ["list of visible face parts"],
    "torso": "visibility state",
    "arms": "visibility state",
    "legs": "visibility state",
    "distinctiveFeatures": ["list of visible marks"],
    "coreTraitsVisible": boolean,
    "hiddenSecondaryDetails": ["list"]
  },
  "poseExpressionValidation": {
    "facialExpression": "specific emotion described - CONCRETE",
    "dynamicPose": "pose description - DYNAMIC",
    "expressionPoseSynergy": "ALIGNED or CONTRADICTION",
    "preventsSoullessArt": boolean,
    "capturedInMoment": boolean
  },
  "googleBestPractices": {
    "clarityScore": "HIGH/MEDIUM/LOW",
    "ambiguityIssues": ["list of vague terms or corrections needed"],
    "featureHierarchy": "CORRECT or NEEDS FIX",
    "framingVisibility": "HONORED or VIOLATED"
  },
  "violations": [
    {"type": "VIOLATION_TYPE", "severity": "CRITICAL/MAJOR/MINOR", "description": "string", "correctedTo": "string"}
  ],
  "changesApplied": ["change 1", "change 2", ...],
  "wasModified": boolean,
  "complianceStatus": "FULL_COMPLIANCE or NEEDS_FIXES or CRITICAL_ISSUES"
}""",
        )
        appendLine()
        appendLine("PROMPT TO REVIEW:")
        appendLine(finalPrompt)
    }
}
