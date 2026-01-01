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
            appendLine(
                "OBJECTIVE: Extract pure PHOTOGRAPHIC DNA (lens, lighting, angle, composition). ABSTRACTION IS KEY: Capture the *vibe* and *structure* of the background, NOT specific objects. (e.g., if reference shows spiderwebs, extract 'intricate foreground patterning' or 'dynamic chaotic background', do NOT mention 'webs').")
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
            appendLine("16 CINEMATOGRAPHY PARAMETERS (Format: 'NAME: [Value]'):")
            appendLine()
            appendLine(
                "1. ANGLE & VIEWPOINT: [eye-level / low-angle looking up / high-angle looking down / dutch-angle / worm's-eye view / bird's-eye view]. DEFINE the CAMERA'S position relative to the subject.",
            )
            appendLine(
                "2. LENS: [14-24mm ultra-wide / 24-35mm wide / 35-50mm normal / 50-85mm portrait / 85-200mm tele / 200mm+ super-tele]",
            )
            appendLine(
                "3. FRAMING: [ECU face / CU head-shoulders / MCU head-chest / MS head-waist / MWS head-knees / FS full-body / WS body+env / EWS small-in-vast]",
            )
            appendLine("4. PLACEMENT: [H: left/center/right third] [V: upper/center/lower third]")
            appendLine(
                "5. LIGHTING: [front/side/back/top/under/omni] + [hard/soft]. DESCRIBE the shadow interplay, contrast intensity, and how light composites the subject.",
            )
            appendLine("6. COLOR: [cool / neutral / warm] + dominant palette")
            appendLine(
                "7. ENVIRONMENT: General setting vibe (e.g., 'urban verticality', 'chaotic debris field', 'ethereal void'), NOT specific objects from reference.")
            appendLine("8. MOOD: Emotional tone (epic/intimate/oppressive/nostalgic/etc)")
            appendLine("9. DOF: [razor / shallow / moderate / deep / infinite]")
            appendLine("10. ATMOSPHERE: [clear/hazy/misty/foggy/dusty/smoky]")
            appendLine(
                "11. PERSPECTIVE: [one-point / two-point / three-point / forced / atmospheric / dramatic foreshortening]. Focus on DEPTH cues and VANISHING points.",
            )
            appendLine("12. TEXTURE: [razor-sharp/film-grain/digital-noise/soft-diffused/gritty]")
            appendLine("13. TIME: [golden-hour/midday/blue-hour/night/overcast/studio]")
            appendLine(
                "14. SIGNATURE: The core compositional/artistic element that makes this image impactful (e.g., 'intense portrait expressiveness', 'dramatic subject isolation', 'powerful gaze connection'). Focus on WHAT makes it compelling, NOT on specific physical attributes of the subject.")
            appendLine("15. DEPTH_LAYERS: [background/midground/foreground elements and spacing]")
            appendLine(
                "16. SUBJECT_ORIENTATION: [Front-facing / 3/4 turn left/right / Profile left/right / Back-facing]. CRITICAL PRECISION: Distinguish strictly between 3/4 and Profile (Profile = 90deg turn, 1 eye visible vs 3/4 = angled, far side partially visible). Define rotation relative to lens."
            )
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
            appendLine("COMPOSITIONAL PRESENCE:")
            appendLine("- Define structural visual traits: [Silhouette Contrast / Lighting Zones / Tonal Distribution / Edge Definition]")
            appendLine("- Focus on HOW the subject is shaped by light/shadow, NOT what the subject physically looks like")
            appendLine("- Example: 'Strong rim lighting defining silhouette edges' vs. 'Black hair with highlights'")
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
            "1. CINEMATOGRAPHY (16 params): angle, lens, framing, placement, lighting, color, environment, mood, DOF, atmosphere, perspective, texture, time, signature, depth_layers, subject_orientation",
        )
        appendLine("   - All MUST be explicit, specific, and match visual direction.")
        appendLine(
            "   - **ANGLE & PERSPECTIVE ENFORCEMENT:** The prompt's angle MUST be specific, non-generic, and creative, as mandated by the visual direction. It must avoid 'banned perspectives' like flat or plain views. The description must clearly reflect the chosen viewpoint (e.g., 'The camera looks up at the towering figure...').",
        )
        appendLine(
            "   - **SUBJECT ORIENTATION (NON-NEGOTIABLE):** STRICTLY enforce the subject's rotation from visual direction. If direction says 'Profile', prompt MUST describe a side profile. If '3/4', it MUST be 3/4. Mismatch = CRITICAL VIOLATION.",
        )
        appendLine(
            "   - **LIGHTING & CONTRAST FIDELITY:** The prompt MUST embrace the reference's lighting structure (e.g., strong shadows, specific light direction, contrast level) as a compositional tool. Art style lighting acts as a MOOD/PALETTE filter, but it must NOT flatten or compromise the dramatic lighting defined in the reference.",
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
                "   - **HANDS & GESTURE EXCEPTION:** Hands and arms ARE allowed in tighter frames (CU/MCU) if they interact with the face or body (e.g., 'hand covering mouth', 'adjusting glasses'). Do NOT delete these if they enhance the expression.",
            )
            appendLine(
                "   - ACTION: If the prompt describes a truly hidden element (e.g., 'wearing leather boots' in a CU), DELETE it. But retain upper-body details, expressive gestures, and context.",
            )
            appendLine()
        }

        appendLine("3. POSE & EXPRESSION (ALIVE, SOULFUL & EXPRESSIVE):")
        appendLine(
            "   - **GOAL:** The character must look ALIVE and SOULFUL, caught in a genuine moment, not a static mannequin.",
        )
        appendLine(
            "   - FACIAL EXPRESSION: MUST convey a specific, nuanced emotion (e.g., 'weary resignation', 'mischievous glee').",
        )
        appendLine(
            "   - BODY LANGUAGE & HANDS: Demand expressiveness beyond the face. Look for head tilts, neck tension, shoulder position, and HANDS interacting with the self/environment (e.g., 'tugging at collar', 'hand on forehead').",
        )
        appendLine(
            "   - SYNERGY: Facial expression + body language + hand placement MUST create a powerful, unified emotional storytelling moment.",
        )
        appendLine(
            "   - **BODY LANGUAGE IMPORTANCE:** The pose is as critical as the face. If visual direction implies a specific stance (e.g., 'hunching', 'looking back'), it MUST be present. Static/neutral bodies in dynamic scenes are VIOLATIONS.",
        )
        appendLine(
            "   - **OPEN ANGLE DYNAMICS (MS/FS/WS):** For wider shots, the ENTIRE visible body must express the narrative. Reject 'neutral standing'. Look for leaning, crouching, running, flying, sitting, or dynamic weight distribution.",
        )
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

        appendLine("6. GENRE-SPECIFIC POSE & AURA:")
        appendLine(
            "   - The subject's pose and emotional aura MUST match the genre's specific directive (e.g., Heroes = Dynamic/Vertical, Fantasy = Graceful/Contrapposto, Punk = Rebellious/Slouching).",
        )
        appendLine("   - Reject generic poses that don't convey the genre's unique energy.")
        appendLine()

        appendLine("7. ARTIST MENTORSHIP (FEEDBACK LOOP):")
        appendLine(
            "   - **`artistImprovementSuggestions` FIELD:** Act as a senior art director giving feedback to a student artist.",
        )
        appendLine(
            "   - Provide concrete, technical advice on how the artist can avoid the detected violations in the next prompt.",
        )
        appendLine(
            "   - Focus on precision: 'Include 3 specific environment objects', 'Use more dramatic verbs for body language', 'Ensure hands are always described if the framing is CU/MCU'.",
        )
        appendLine("   - Use a constructive but firm tone to guide better initial output.")
        appendLine()

        appendLine("8. VISUAL DIRECTOR MENTORSHIP (META-REVIEW):")
        appendLine(
            "   - **`visualDirectorSuggestions` FIELD:** Act as a Lead Technical Director optimizing an upstream AI.",
        )
        appendLine(
            "   - Analyze the `VISUAL DIRECTION` input for ambiguity or lack of technical specificity.",
        )
        appendLine(
            "   - Suggest *concrete terminology replacements* to improve future extractions. Tell the Visual Director exactly what terms to use instead.",
        )
        appendLine(
            "   - Examples: 'Replace generic \"close-up\" with \"CU: Head and Shoulders\" for clarity', 'Instead of \"dramatic angle\", request \"Low-Angle Dutch Tilt 15°\"', 'Specify exact lighting ratio (e.g., 3:1) instead of just \"contrast\"'.",
        )
        appendLine("   - GOAL: Calibrate the Visual Director to be a precise cinematographer, not a creative writer.")
        appendLine()

        appendLine("VIOLATION DETECTION:")
        appendLine("- VISIBILITY_VIOLATION: Describes body parts/clothing out of frame")
        appendLine("- MISSING_FACIAL_EXPRESSION: No specific emotion or generic descriptor")
        appendLine("- MISSING_DYNAMIC_POSE: Static/neutral pose without emotional content")
        appendLine("- POSE_EXPRESSION_VIOLATION: Expression + pose don't work together or character seems posed, not in moment")
        appendLine("- FRAMING_VIOLATION: Describes elements not visible at this framing level")
        appendLine(
            "- PERSPECTIVE_VIOLATION: Uses a generic, flat, or banned perspective (e.g., 'eye-level' without justification, 'plain view') or the description does not match the specified angle.")
        appendLine("- SUBJECT_ORIENTATION_VIOLATION: The described subject orientation does not match the visual direction.")
        appendLine(
            "- GENRE_AURA_VIOLATION: Subject pose or vibe contradicts the genre (e.g., static pose in Hero genre, happy pose in Cyberpunk).")
        appendLine("- BANNED_TERMINOLOGY: Uses forbidden words from art style")
        appendLine("- MISSING_CINEMATOGRAPHY_PARAMETER: Any of 16 params missing/vague")
        appendLine("- LIGHTING_MISSING/WRONG, COLOR_PALETTE_WRONG, ENVIRONMENT_MISSING, etc.")
        appendLine()

        appendLine("AUTO-FIX PATTERNS:")
        appendLine("- Missing expression → Add specific, INTENSE emotion matching archetype")
        appendLine(
            "- Missing/Weak pose → INJECT DRAMA: Suggest exaggerated gestures, dynamic foreshortening, and intensified body language")
        appendLine("- Expression + pose contradictory → Align emotionally and AMPLIFY the storytelling")
        appendLine("- Out-of-frame descriptions → Remove, replace with visible details")
        appendLine(
            "- Flat/generic angle → Replace with a more dynamic, descriptive angle (low-angle, high-angle, POV) that enhances the mood.")
        appendLine("- Wrong subject orientation → Correct the subject's rotation to match the visual direction.")
        appendLine(
            "- Wrong genre aura → Rewrite pose/expression to match genre (e.g., 'standing' → 'standing with heroic verticality' for Heroes).")
        appendLine("- Static character → Add momentum language ('breathing', 'captured mid-action')")
        appendLine("- Generic cinematography → Specify exact values from 16 parameters")
        appendLine("- Missing background/environment → Add 3+ specific objects")
        appendLine()

        appendLine("OUTPUT JSON (ImagePromptReview) - ENSURE ALL FIELDS ARE PRESENT:")
        appendLine("- `originalPrompt`: The raw input prompt")
        appendLine("- `correctedPrompt`: The final, high-quality, fixed prompt")
        appendLine(
            "- `violations`: List of objects { \"type\": \"ENUM_NAME\", \"severity\": \"CRITICAL/MAJOR/MINOR\", \"description\": \"...\", \"example\": \"...\" }")
        appendLine("- `changesApplied`: List of strings describing each fix")
        appendLine("- `artistImprovementSuggestions`: Concrete, technical feedback for the artist on how to improve next time")
        appendLine("- `visualDirectorSuggestions`: Feedback on the Visual Direction input quality (or null)")
        appendLine("- `wasModified`: Boolean indicating if any changes were made")
        appendLine()
        appendLine("PROMPT TO REVIEW:")
        appendLine(finalPrompt)
    }
}
