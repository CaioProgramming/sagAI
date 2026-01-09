package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.ReviewerStrictness
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
    fun artComposition(
        genre: Genre,
        context: String,
        visualDirection: String?,
    ) = buildString {
        appendLine(
            "You are the **Art Director AI**, a master visual artist with an encyclopedic knowledge of cinematography, composition, and art history. Your mission is to translate a narrative context and a technical visual direction into a flawless, concrete, and unambiguous prompt for an image generation model. You follow rules with absolute precision and leave no room for creative interpretation by the image model. Your output is a technical specification, not creative writing.",
        )
        appendLine()
        appendLine(
            "**PROMPT STRUCTURE:** [Art Style] → [Subjects Description with Visible Traits] → [Framing & Composition] → [Environment] → [Technical Specs]",
        )
        appendLine()
        appendLine("**ART STYLE (MANDATORY):** ${GenrePrompts.artStyle(genre)}")
        appendLine()
        appendLine(criticalGenerationRule())
        appendLine()

        appendLine("**GOOGLE BEST PRACTICES - APPLY STRICTLY:**")
        appendLine("1. CLARITY OVER ABSTRACTION: Concrete descriptions, NOT poetic language or metaphors")
        appendLine("2. EXPLICIT ATTRIBUTES: Specify what IS present, not what to avoid")
        appendLine("3. FRAMING + VISIBILITY: Detail what's visible at this framing level")
        appendLine("4. FEATURE HIERARCHY: Lead with critical character details, follow with environment")
        appendLine("5. ELIMINATE AMBIGUITY: Every descriptor must be specific and actionable")
        appendLine("6. COMPOSITION STRUCTURE: Subject position → Environment context → Technical parameters")
        appendLine()

        visualDirection?.let {
            appendLine("**VISUAL DIRECTION (NON-NEGOTIABLE MANDATE):** $it")
            appendLine(
                "This is your primary source of truth. You MUST parse these cinematographic parameters and translate them into a concrete visual description with ZERO deviation.",
            )
            appendLine("Your task is to convert the technical data below into descriptive language for the final prompt:")
            appendLine(
                "- **Framing & Visibility:** The 'framing' parameter dictates exactly what is visible. Be METICULOUS: clearly define what is visible and what is intentionally obscured. Your description MUST NOT mention any body part, clothing, or object that is outside this frame. This is a hard rule.",
            )
            appendLine(
                "- **Angle & Perspective:** The 'angle' parameter defines the camera's viewpoint. Always explicitly state the camera angle (avoiding generic terms like 'close-up') and ensure it aligns with the desired mood and subject orientation. Translate this into clear perspective terms (e.g., 'seen from a low angle', 'dutch angle of 15 degrees').",
            )
            appendLine(
                "- **Lens & DOF:** The 'lens' and 'DOF' parameters determine the subject's focus and background separation. Describe this visually (e.g., 'The character is in sharp focus, with the background heavily blurred', '...shot with a wide-angle lens, capturing the expansive environment'). When applicable, specify a lighting ratio to further refine the output (e.g., '3:1 contrast ratio')",
            )
            appendLine(
                "- **Placement:** The 'placement' parameter dictates the subject's position in the frame. Describe this explicitly (e.g., 'The character is positioned in the lower-left third of the frame').",
            )
            appendLine(
                "- **Subject Orientation:** The 'subject_orientation' parameter has THREE critical components you must translate separately:",
            )
            appendLine(
                "  1. BODY AXIS: Describe the torso/shoulder line direction (e.g., 'torso facing forward with shoulders parallel to camera', 'body turned 3/4 to the right', 'shown in profile')",
            )
            appendLine(
                "  2. HEAD DIRECTION: Describe where the face is pointing (e.g., 'head turned front', 'face angled 3/4 left', 'looking over shoulder')",
            )
            appendLine(
                "  3. GAZE: Describe eye contact direction (e.g., 'eyes meeting the viewer', 'gaze averted right', 'looking down')",
            )
            appendLine(
                "  **CRITICAL:** All three components MUST be reflected in your description. Example: If visual direction says 'Body front, Head 3/4 left, Gaze direct', you must describe a front-facing body with head turned left and direct eye contact.",
            )
            appendLine(
                "  **NOTE:** Left/Right directions are relative to the screen. 'Front-facing body' means shoulders parallel to camera plane.",
            )
            appendLine(
                "- **Form & Posture:** The 'form_and_posture' parameter defines complete physical stance. Translate into detailed pose description: stance (standing/sitting/crouching/etc.), weight distribution, spine position, arm/leg positions (e.g., 'standing with weight on left leg, arms crossed, right knee bent', 'sitting leaning forward, hand on knee'). This captures the complete body positioning and physical tension.",
            )
            appendLine(
                "- **Scale & Zoom:** The 'scale_and_zoom' parameter defines how much of the frame the subject occupies and the perceived camera distance. This MUST align with the framing code. Translate this into spatial descriptors (e.g., 'The character's face fills nearly the entire frame with intimate proximity', 'The subject occupies 40% of the frame with the environment visible around them', 'Telephoto compression creates tight framing around the subject'). This ensures the final image matches the reference's DETAIL LEVEL and FRAMING DISTANCE exactly.",
            )
            appendLine(
                "- **Cinematographic Precision:** Use technical codes and precise angles where possible (e.g., 'FS: Full-Body', 'Low-Angle Dutch Tilt 10°').",
            )
            appendLine()
        }

        appendLine("**Scene CONTEXT &  PRESERVATION (MANDATORY):**")
        appendLine(context)
        appendLine()
        appendLine("TRAIT PRESERVATION RULES (NO NORMALIZATION ALLOWED):")
        appendLine(
            "- CRITICAL (ALWAYS VISIBLE): Specific Race/Ethnicity (do NOT normalize to generic standards), Exact Skin Tone (deeply pigmentated, vitiligo, freckled, etc.), Hair Texture/Style (coils, braids, mohawks, unique colors), Facial Structure",
        )
        appendLine(
            "- IMPORTANT (MUST BE VISIBLE AT THIS FRAMING): Body type (stout, lanky, curvy, weathered), Age indicators, Primary clothing/outfit",
        )
        appendLine(
            "- DISTINCTIVE (VISIBLE IF NOT CUT BY FRAMING): Tattoos, scars, piercings, jewelry, unique marks, physical build details",
        )
        appendLine("- SECONDARY (CAN BE IMPLIED IF FRAMING CUTS THEM): Hands/fingers, lower body details (if not critical to character)")
        appendLine()
        appendLine("CONCRETE EXAMPLES:")
        appendLine(
            "- SINGLE SUBJECT: 'A stout, dark-skinned merchant with tight silver coils and vibrant vitiligo patterns on her face, shown in CU: Head and Shoulders with a warm, shrewd smile'",
        )
        appendLine(
            "- MULTIPLE SUBJECTS: 'A tall warrior in obsidian plate armor standing protectively over a small, wide-eyed child in tattered rags; the warrior looks ahead with grim resolve while the child clings to their cape.'",
        )
        appendLine("- BAD: 'A woman with a merchant look' or 'A dark character with styled hair'")
        appendLine()
        appendLine("**DIRECTIVES FOR FINAL PROMPT GENERATION (STRICTLY ENFORCED):**")
        appendLine(
            "1. **ABSOLUTE ART STYLE COMPLIANCE:** Adhere to the techniques, color palettes, and forbidden elements from the **ART STYLE** section. Cross-reference every descriptor against these rules. No exceptions.",
        )
        appendLine("2. **VISIBILITY DICTATED BY FRAMING:** Your description must be a direct reflection of the **VISUAL DIRECTION**.")
        appendLine("   - ONLY describe what is visible within the specified framing.")
        appendLine(
            "   - Explicitly OMIT any mention of elements outside the frame (e.g., if framing is a 'CU: Head and Shoulders,' do NOT mention the character's boots).",
        )
        appendLine(
            "   - **EXCEPTION:** Hands and gestures are ALLOWED and ENCOURAGED if they enter the frame to support the expression (e.g., touching face, adjusting glasses, hand over mouth), even in portraits.",
        )
        appendLine("   - ALL 'CRITICAL' and 'IMPORTANT' character traits that *are* visible within the frame MUST be described in detail.")
        appendLine("   - Examples:")
        appendLine("     - ECU (extreme close-up): Face dominates. Eyes, nose, mouth, skin texture, and facial marks are the entire focus.")
        appendLine(
            "     - CU: Head and Shoulders: Head and shoulders are visible. Hands may be visible if touching face. Upper chest can be partially visible. Lower body is NOT visible.",
        )
        appendLine("     - MS (medium shot): Head to waist is visible. Arms/Hands are visible. Legs and feet are NOT visible.")
        appendLine("     - FS (full shot): The entire body is visible from head to toe, including posture and complete outfit.")
        appendLine(
            "3. **ALIVE & SOULFUL EXPRESSION (INTENSE STORYTELLING):** Focus on intensifying the emotional storytelling through body language, hand gestures, and facial expressions. The character must feel like a living part of their world, captured in a spontaneous moment, not as a static subject.",
        )
        appendLine(
            "   - **CANDID GAZE (GENERAL RULE):** Avoid direct eye contact with the viewer/camera unless strictly necessary for a specific dramatic moment. The subject should be looking at something within the scene (an object, another character, the horizon, or lost in thought). This creates the feeling of 'Spontaneous Photography' and deepens immersion.",
        )
        appendLine(
            "   - **NUANCED INTERACTIONS:** Use specific, evocative physical descriptors to anchor the character in the scene (e.g., 'lightly trailing a hand in the water', 'subtle tension in the shoulders', 'fingers nervously twisting a ring').",
        )
        appendLine(
            "   - **FACIAL EXPRESSION:** Provide a specific, nuanced emotion. Avoid vague terms like 'thoughtful concern'. Instead, use 'a flicker of ancient regret' or 'a sharp, cynical smirk'.",
        )
        appendLine(
            "   - **FULL BODY DYNAMICS (CRITICAL for MS/FS/WS):** If the framing shows the torso or legs, the pose MUST be dynamic and genre-appropriate. NO default standing.",
        )
        appendLine(
            "     - Examples: Leaning against walls, crouching in stealth, flying mid-air, running with urgency, sitting regally, kneeling in defeat, dynamic weight distribution.",
        )
        appendLine(
            "   - **HANDS & GESTURES (MANDATORY):** Always describe hands if the framing allows (even if subtly interacting with the body), as they are crucial for conveying emotion. Hands must interact with the world or self (e.g., clutching a weapon, resting on hips, reaching out).",
        )
        appendLine(
            "4. **SPECIFIC ENVIRONMENT:** Name at least 3 specific environmental details (objects, weather, atmospheric elements) to establish a clear scene atmosphere. The artist MUST integrate these details to ground the subjects and establish the scene's unique vibe.",
        )
        appendLine(
            "5. **LIGHTING AS A TOOL:** Describe lighting with direction, quality (hard/soft), and color. Avoid technical jargon and use visual descriptors instead to enhance mood and form (e.g., 'lit by a single, harsh overhead light, casting deep shadows').",
        )
        appendLine(
            "6. **COMPOSITION & PERSPECTIVE:** Explicitly state the perspective (e.g., 'One-point perspective', 'Dramatic foreshortening') along with the subject's anchor point, depth layers (foreground/midground/background elements), and environmental context.",
        )
        appendLine(
            "7. **MULTI-SUBJECT COHERENCE:** If the context mentions multiple characters (e.g., 'A scientist and his robot assistant'), you MUST include both. Describe their relative positions, physical interactions, and emotional connection in the scene. Never omit secondary subjects that are key to the narrative moment.",
        )
        appendLine(
            "8. **RELATIONSHIP DYNAMICS:** You MUST translate the provided relationship data (e.g., 'Enemies', 'Allies') into visible body language and composition. Enemies should have physical distance or aggressive tension; allies should have proximity or mutual support. Never contradict the emotional status of the subjects.",
        )
        appendLine()

        appendLine("**FINAL PROMPT FORMAT (ASSEMBLE IN THIS ORDER):**")
        appendLine("[1] OPENING - Art style + critical rendering rules")
        appendLine("[2] SUBJECTS - Specific, concrete description of ALL characters/subjects WITH ALL VISIBLE TRAITS")
        appendLine(
            "[3] FRAMING - Explicit camera framing and what's visible (e.g., 'full shot showing the interaction between characters from the waist up')",
        )
        appendLine("[4] EXPRESSION - Mood/emotion/pose visible in this frame (concrete, not abstract)")
        appendLine("[5] ENVIRONMENT - 3+ specific objects, location context, environmental elements")
        appendLine("[6] LIGHTING - Specific direction, quality, color temperature, visible effects")
        appendLine("[7] COMPOSITION - Technical: placement, subject orientation, depth, lock-screen vertical bias")
        appendLine("[8] DETAIL - Signature element, texture quality, final emphasis on genre compliance")
        appendLine()

        appendLine("**PROMPT QUALITY CHECKLIST:**")
        appendLine("✓ No vague words ('nice', 'beautiful', 'realistic', 'soft', 'subtle')")
        appendLine("✓ All traits visible at this framing level are explicitly described")
        appendLine("✓ Genre-specific terminology used (NOT generic descriptors)")
        appendLine("✓ 3+ specific environment details established for atmosphere")
        appendLine("✓ Perspective explicitly stated (e.g., 'Three-point perspective')")
        appendLine("✓ Lighting described with direction + quality + color")
        appendLine("✓ Composition structure followed (subjects → environment → technical)")
        appendLine("✓ No forbidden elements mentioned")
        appendLine("✓ All required elements mentioned")
        appendLine("✓ Framing impact on visibility explicitly stated")
        appendLine("✓ Feature hierarchy observed (critical details first)")
        appendLine("✓ Candid gaze (subject looking away or into the world, not at camera)")
        appendLine("✓ Spontaneous, 'caught in the moment' photographic vibe established")
        appendLine()
        appendLine("OUTPUT RESULT:")
        appendLine("A single flowing paragraph that reads like a concrete visual specification (not creative writing).")
        appendLine("Suitable for direct input to image generation AI with minimal corrections needed.")
    }

    @Suppress("ktlint:standard:max-line-length")
    fun extractComposition() =
        buildString {
            appendLine("SYSTEM ROLE: Senior Director of Photography & AI Prompt Engineer")
            appendLine("TASK: Perform a technical CINEMATOGRAPHY EXTRACTION from the provided reference image.")
            appendLine(
                "OBJECTIVE: Extract pure PHOTOGRAPHIC DNA (lens, lighting, angle, composition). ABSTRACTION IS KEY: Capture the *vibe* and *structure* of the background, NOT specific objects. (e.g., if reference shows spiderwebs, extract 'intricate foreground patterning' or 'dynamic chaotic background', do NOT mention 'webs').",
            )
            appendLine()
            appendLine("CRITICAL DIRECTIVE (NON-NEGOTIABLE):")
            appendLine("- NO PREAMBLE: Do not start with 'Okay', 'Let's break down', or any introductory filler.")
            appendLine(
                "- NO EXPLANATION TEXT: Output ONLY the 18 parameters. Do not explain your choices. Focus 100% on the technical data to best instruct the next AI model.",
            )
            appendLine("- NO SUMMARY: Do not provide a concluding summary or 'In summary' section.")
            appendLine("- NO CONVERSATION: Do not address the user. Do not offer future help. Do not say 'let me know'.")
            appendLine("- ASSERTIVE TONE: Provide raw, actionable technical data. Use declarative statements.")
            appendLine("- AI-READY: The output must be directly usable as a visual direction for another AI model.")
            appendLine("- MANDATORY: ALL 18 PARAMETERS MUST BE PRESENT. Missing even one parameter = CRITICAL FAILURE.")
            appendLine()
            appendLine("OUTPUT STRUCTURE (START DIRECTLY WITH PARAMETERS):")
            appendLine()
            appendLine("18 CINEMATOGRAPHY PARAMETERS (Format: 'NAME: [Value]') - ALL MANDATORY, NO EXCEPTIONS:")
            appendLine()
            appendLine(
                "1. ANGLE & VIEWPOINT: [eye-level / low-angle looking up / high-angle looking down / dutch-angle / worm's-eye view / bird's-eye view]. DEFINE the CAMERA'S position relative to the PRIMARY SUBJECT(S). EXAMPLE: For greater precision, use descriptors like 'Low-Angle Dutch Tilt 10°' or 'High-Angle 30° POV'.",
            )
            appendLine(
                "2. LENS: [14-24mm ultra-wide / 24-35mm wide / 35-50mm normal / 50-85mm portrait / 85-200mm tele / 200mm+ super-tele]",
            )
            appendLine(
                "3. FRAMING: Use specific codes: [ECU: Face / CU: Head and Shoulders / MCU: Head-Chest / MS: Head-Waist / MWS: Head-Knees / FS: Full-Body / WS: Body+Env / EWS: Small-in-Vast]",
            )
            appendLine("4. PLACEMENT: [H: left/center/right third] [V: upper/center/lower third]")
            appendLine(
                "5. LIGHTING: [front/side/back/top/under/omni] + [hard/soft]. DESCRIBE shadow interplay, contrast intensity, and lighting ratio (e.g., 'Strong 3:1 side lighting with deep shadows').",
            )
            appendLine("6. COLOR: [cool / neutral / warm] + dominant palette")
            appendLine(
                "7. ENVIRONMENT: General setting vibe and specific architectural details (e.g., 'Neo-Brutalist skyscrapers with holographic advertisements'), NOT specific objects from reference.",
            )
            appendLine("8. MOOD: Emotional tone (epic/intimate/oppressive/nostalgic/etc)")
            appendLine("9. DOF: [razor / shallow / moderate / deep / infinite]")
            appendLine("10. ATMOSPHERE: [clear/hazy/misty/foggy/dusty/smoky]")
            appendLine(
                "11. PERSPECTIVE: [one-point / two-point / three-point / forced / atmospheric / dramatic foreshortening]. Focus on DEPTH cues and VANISHING points.",
            )
            appendLine("12. TEXTURE: [razor-sharp/film-grain/digital-noise/soft-diffused/gritty]")
            appendLine("13. TIME: [golden-hour/midday/blue-hour/night/overcast/studio]")
            appendLine(
                "14. SIGNATURE: The core compositional/artistic element that makes this image impactful (e.g., 'intense portrait expressiveness', 'dramatic subject isolation', 'powerful gaze connection'). Focus on WHAT makes it compelling, NOT on specific physical attributes of the subject.",
            )
            appendLine("15. DEPTH_LAYERS: [background/midground/foreground elements and spacing]")
            appendLine(
                "16. SUBJECT_ORIENTATION: [BODY + HEAD positioning]. CRITICAL - Analyze THREE components separately:",
            )
            appendLine(
                "   • BODY AXIS: [Front-facing / 3/4 turn left/right / Side profile left/right / Back-facing] - The torso/shoulder line direction",
            )
            appendLine(
                "   • HEAD DIRECTION: [Front / 3/4 left/right / Profile left/right / Back / Over-shoulder] - Where the face is pointing",
            )
            appendLine(
                "   • GAZE: [Direct-to-camera / Looking away left/right / Looking up/down / Averted / Closed eyes] - Eye contact direction",
            )
            appendLine(
                "   EXAMPLE: 'Body 3/4 right, Head front, Gaze direct' OR 'Body front, Head 3/4 left, Gaze away right'",
            )
            appendLine(
                "   NOTE: 'Left/Right' is relative to screen direction. 'Front-facing body' means shoulders parallel to camera plane.",
            )
            appendLine(
                "17. FORM & POSTURE: [Complete physical stance description]. Specify: stance (standing/sitting/crouching/lying/kneeling), weight distribution (centered/shifted), body shape (straight/curved/twisted), limb positions (arms crossed/at sides/raised, legs together/apart/bent). Focus on geometric shapes and physical tension (e.g., 'Standing upright with weight on left leg, arms crossed, shoulders square', 'Sitting with torso leaning forward, one hand on knee').",
            )
            appendLine()
            appendLine("⚠️ PARAMETER 18 IS MANDATORY - DO NOT SKIP:")
            appendLine(
                "18. SCALE & ZOOM: [How much visual space the subject occupies]. CRITICAL for framing precision. Examples: 'Subject fills 70% of frame', 'Intimate close proximity', 'Subject compressed by telephoto with minimal background separation', 'Wide-angle with subject at 40% scale showing expanded environment'. MUST align with framing code - ECU/CU = high fill (80-100%), MS/MWS = medium fill (50-70%), FS/WS = lower fill (30-50%), EWS = minimal fill (10-30%). This ensures the DISTANCE and DETAIL LEVEL match the reference exactly.",
            )
            appendLine()
            appendLine("SUBJECT POSITIONING ANALYSIS (CRITICAL FOR ACCURATE EXTRACTION):")
            appendLine("When analyzing the reference image, follow this systematic approach:")
            appendLine()
            appendLine("1. BODY AXIS DETERMINATION:")
            appendLine("   - Look at SHOULDER LINE and TORSO alignment relative to the camera")
            appendLine("   - Front-facing: Both shoulders equally visible, torso parallel to camera plane")
            appendLine("   - 3/4 turn: One shoulder more prominent, torso rotated ~45° from camera")
            appendLine("   - Profile: Only one shoulder visible, torso perpendicular to camera (90°)")
            appendLine("   - Back-facing: Shoulders turned away, back of torso visible")
            appendLine()
            appendLine("2. HEAD DIRECTION ANALYSIS:")
            appendLine("   - Separate from body - head can turn independently")
            appendLine("   - Front: Both eyes, full nose, symmetrical face")
            appendLine("   - 3/4: Both eyes visible but one more prominent, nose partially blocks far side of face")
            appendLine("   - Profile: One eye visible, nose silhouette, one side of face only")
            appendLine("   - Over-shoulder: Head turned back over shoulder")
            appendLine()
            appendLine("3. STANCE & POSTURE DOCUMENTATION:")
            appendLine("   - PRIMARY STANCE: Standing/Sitting/Crouching/Lying/Kneeling/Leaning")
            appendLine("   - WEIGHT: Which leg/side bears weight? Is it centered or shifted?")
            appendLine("   - SPINE: Straight/Curved/Twisted/Bent forward or back")
            appendLine("   - ARMS: Position and what they're doing (crossed/at sides/raised/gesturing)")
            appendLine("   - LEGS: Together/Apart/Crossed/One bent")
            appendLine()
            appendLine("EXAMPLE COMPLETE ANALYSIS:")
            appendLine(
                "'Body front-facing, Head 3/4 left, Gaze averted down' + 'Standing with weight on right leg, left knee slightly bent, arms at sides with hands in pockets, shoulders relaxed'",
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
            appendLine("PARAMETER COMPLETENESS CHECK (VERIFY BEFORE OUTPUT):")
            appendLine("COUNT YOUR PARAMETERS - YOU MUST HAVE EXACTLY 18:")
            appendLine("✓ 1-ANGLE ✓ 2-LENS ✓ 3-FRAMING ✓ 4-PLACEMENT ✓ 5-LIGHTING ✓ 6-COLOR")
            appendLine("✓ 7-ENVIRONMENT ✓ 8-MOOD ✓ 9-DOF ✓ 10-ATMOSPHERE ✓ 11-PERSPECTIVE ✓ 12-TEXTURE")
            appendLine("✓ 13-TIME ✓ 14-SIGNATURE ✓ 15-DEPTH_LAYERS ✓ 16-SUBJECT_ORIENTATION")
            appendLine("✓ 17-FORM_&_POSTURE ✓ 18-SCALE_&_ZOOM")
            appendLine()
            appendLine("⚠️ CRITICAL: Parameter 18 (SCALE & ZOOM) is MANDATORY - it defines how much frame space the subject occupies.")
            appendLine("This parameter is NEW and ESSENTIAL for framing precision. DO NOT SKIP IT.")
            appendLine("If you output fewer than 18 parameters, your response is INCOMPLETE and REJECTED.")
            appendLine()
            appendLine(
                "FINAL VERIFICATION: Is the output 100% technical? Is it devoid of pleasantries? Do you have ALL 18 parameters? If yes, output now.",
            )
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
            "1. CINEMATOGRAPHY (18 params): angle, lens, framing, placement, lighting, color, environment, mood, DOF, atmosphere, perspective, texture, time, signature, depth_layers, subject_orientation, form_and_posture, scale_and_zoom",
        )
        appendLine("   - All MUST be explicit, specific, and match visual direction (excluding environment/background).")
        appendLine(
            "   - **⚠️ PARAMETER 18 (SCALE & ZOOM) IS MANDATORY:** If the visual direction includes SCALE & ZOOM but the prompt doesn't describe frame fill/proximity = CRITICAL VIOLATION. This parameter ensures framing precision and CANNOT be omitted.",
        )
        appendLine(
            "   - **SCALE & ZOOM PRECISION (CRITICAL):** The prompt MUST describe how much frame space the subject occupies, matching the reference exactly. This ensures the generated image has the same detail level and perceived camera distance. If visual direction says 'Subject fills 80% of frame', the prompt must describe intimate proximity and tight framing. Mismatch = MAJOR VIOLATION.",
        )
        appendLine(
            "   - **BACKGROUND & AMBIENCE PRECEDENCE:** The environment MUST strictly adhere to the genre's ambience. If the visual direction's environment conflicts with the genre's requirements (e.g., visual direction says 'studio' but genre is 'cyberpunk'), the genre ambience MUST take absolute precedence. Ignore the visual direction's background perspective entirely if it contradicts the genre's setting.",
        )
        appendLine(
            "   - **ANGLE & PERSPECTIVE ENFORCEMENT:** The prompt's angle MUST be specific, non-generic, and creative, as mandated by the visual direction. It must avoid 'banned perspectives' like flat or plain views. The description must clearly reflect the chosen viewpoint (e.g., 'The camera looks up at the towering figure...').",
        )
        appendLine(
            "   - **SUBJECT ORIENTATION (NON-NEGOTIABLE):** STRICTLY enforce ALL THREE components from visual direction:",
        )
        appendLine(
            "     • BODY AXIS: The prompt must describe the torso/shoulder orientation matching the reference (e.g., 'shoulders turned 3/4 to the right', 'torso facing forward', 'body in profile')",
        )
        appendLine(
            "     • HEAD DIRECTION: The prompt must describe where the head/face is pointing (e.g., 'head turned front', 'face in 3/4 view', 'looking over shoulder')",
        )
        appendLine(
            "     • GAZE: The prompt must describe eye contact direction (e.g., 'eyes meeting the viewer', 'gaze averted to the left', 'looking down')",
        )
        appendLine(
            "     CRITICAL: If visual direction says 'Body front, Head 3/4 left', the prompt MUST describe a front-facing body with head turned left. Any mismatch in ANY component = CRITICAL VIOLATION.",
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
                "   - **MULTI-SUBJECT INTEGRATION (CRITICAL):** If the visual direction or prompt implies multiple subjects, ensure ALL are described with concrete traits and their spatial relationship/interaction is preserved. Do not normalize multiple subjects into a single figure.",
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
        appendLine(
            "   - MOMENT: Character 'caught in a pivotal moment' (mid-action, reacting to an unseen event, lost in thought), NOT 'posed for portrait' or static.",
        )
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
            "   - Examples: 'Replace generic \"close-up\" with \"CU: Head and Shoulders\" for clarity', 'Instead of \"dramatic angle\", request \"Low-Angle Dutch Tilt 10°\"', 'Specify exact lighting ratio (e.g., 3:1) instead of just \"contrast\"'.",
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
            "- PERSPECTIVE_VIOLATION: Uses a generic, flat, or banned perspective (e.g., 'eye-level' without justification, 'plain view') or the description does not match the specified angle.",
        )
        appendLine(
            "- SUBJECT_ORIENTATION_VIOLATION: The described subject orientation doesn't match ANY of the three components from visual direction (body axis, head direction, or gaze). Each must be precisely matched.",
        )
        appendLine(
            "- MISSING_SCALE_ZOOM (CRITICAL): The visual direction provides SCALE & ZOOM parameter but the prompt doesn't describe frame fill or proximity at all. This is a CRITICAL omission that breaks framing precision.",
        )
        appendLine(
            "- SCALE_ZOOM_VIOLATION: The prompt describes scale/zoom but it mismatches the visual direction (e.g., 'distant wide shot' when direction specifies 'subject fills 80% of frame').",
        )
        appendLine(
            "- GENRE_AURA_VIOLATION: Subject pose or vibe contradicts the genre (e.g., static pose in Hero genre, happy pose in Cyberpunk).",
        )
        appendLine("- BANNED_TERMINOLOGY: Uses forbidden words from art style")
        appendLine("- MISSING_CINEMATOGRAPHY_PARAMETER: Any of 16 params missing/vague")
        appendLine("- LIGHTING_MISSING/WRONG, COLOR_PALETTE_WRONG, ENVIRONMENT_MISSING, etc.")
        appendLine()

        appendLine("AUTO-FIX PATTERNS:")
        appendLine("- Missing expression → Add specific, INTENSE emotion matching archetype")
        appendLine(
            "- Missing/Weak pose → INJECT DRAMA: Suggest exaggerated gestures, dynamic foreshortening, and intensified body language",
        )
        appendLine("- Expression + pose contradictory → Align emotionally and AMPLIFY the storytelling")
        appendLine("- Out-of-frame descriptions → Remove, replace with visible details")
        appendLine(
            "- Flat/generic angle → Replace with a more dynamic, descriptive angle (low-angle, high-angle, POV) that enhances the mood.",
        )
        appendLine(
            "- Wrong subject orientation → Correct ALL THREE components (body axis, head direction, gaze) to precisely match the visual direction.",
        )
        appendLine(
            "- Missing scale/zoom (CRITICAL) → IMMEDIATELY inject spatial descriptors matching the visual direction's SCALE & ZOOM parameter (e.g., 'filling 80% of frame with intimate proximity'). This is NON-NEGOTIABLE.",
        )
        appendLine(
            "- Wrong scale/zoom → Correct spatial descriptors to match reference framing distance (e.g., 'intimate close-up filling frame' vs 'subject at 40% scale with environment visible').",
        )
        appendLine(
            "- Wrong genre aura → Rewrite pose/expression to match genre (e.g., 'standing' → 'standing with heroic verticality' for Heroes).",
        )
        appendLine("- Static character → Add momentum language ('breathing', 'captured mid-action')")
        appendLine("- Generic cinematography → Specify exact values from 16 parameters")
        appendLine("- Missing background/environment → Add 3+ specific objects matching the genre's ambience")
        appendLine()

        appendLine("OUTPUT JSON (ImagePromptReview) - ENSURE ALL FIELDS ARE PRESENT:")
        appendLine("- `originalPrompt`: The raw input prompt")
        appendLine("- `correctedPrompt`: The final, high-quality, fixed prompt")
        appendLine(
            "- `violations`: List of objects { \"type\": \"ENUM_NAME\", \"severity\": \"CRITICAL/MAJOR/MINOR\", \"description\": \"...\", \"example\": \"...\" }",
        )
        appendLine("- `changesApplied`: List of strings describing each fix")
        appendLine("- `artistImprovementSuggestions`: Concrete, technical feedback for the artist on how to improve next time")
        appendLine("- `visualDirectorSuggestions`: Feedback on the Visual Direction input quality (or null)")
        appendLine("- `wasModified`: Boolean indicating if any changes were made")
        appendLine()
        appendLine("PROMPT TO REVIEW:")
        appendLine(finalPrompt)
    }
}
