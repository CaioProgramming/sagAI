package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.ReviewerStrictness
import com.ilustris.sagai.features.newsaga.data.model.Genre

object ImagePrompts {
    /**
     * Genre-critical rules that MUST be at the TOP of prompts.
     * These define the SOUL of each genre - the elements that make it unmistakably that style.
     */
    private fun genreCriticalRules(genre: Genre): String =
        buildString {
            when (genre) {
                Genre.CYBERPUNK -> {
                    appendLine("⚠️⚠️⚠️ CYBERPUNK MANDATORY RULES (READ FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("1. MANDATORY CYBERWARE - EVERYONE has chrome. Not optional. Add 2-3 visible augmentations:")
                    appendLine("   • Prosthetic limb (chrome arm/leg shaped like human limb, visible joints)")
                    appendLine("   • Artificial eyes (electronic iris, scanner lines, unnatural glow - fits in socket)")
                    appendLine("   • Neural port (at temple/neck/spine - visible seam where metal meets skin)")
                    appendLine("   • Mechanical spine (chrome vertebrae at back of neck)")
                    appendLine("   REPLACEMENTS that fit human form. NOT circuit tattoos. NOT giant robot parts.")
                    appendLine()
                    appendLine("2. MATURE TONE - Gritty, melancholic, world-weary. NO cute/innocent/playful elements.")
                    appendLine()
                    appendLine("3. 1980s OVA STYLE - Cel shading, hard shadows, film grain, muted colors, thick ink lines.")
                    appendLine()
                }

                Genre.PUNK_ROCK -> {
                    appendLine("⚠️⚠️⚠️ PUNK ROCK MANDATORY RULES (READ FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("1. CARTOON STYLE - This is GORILLAZ/KND style cartoon, NOT realistic art.")
                    appendLine("   Characters are 2D CARTOON CHARACTERS with exaggerated features.")
                    appendLine()
                    appendLine("2. EYES - ONLY these 3 options (NO realistic eye colors):")
                    appendLine("   • Simplified stylized (almond shape, X-eyes, single line)")
                    appendLine("   • Full white with no pupil")
                    appendLine("   • Black void (classic Gorillaz)")
                    appendLine("   BANNED: 'brown eyes', 'blue eyes', 'green eyes', any natural eye color.")
                    appendLine()
                    appendLine("3. MANDATORY BACKGROUND - Every image needs detailed environment.")
                    appendLine("   Include: graffiti, amps, neon signs, posters, brick walls.")
                    appendLine("   BANNED: plain/gradient/white/solid color backgrounds.")
                    appendLine()
                    appendLine("4. FLAT RENDERING - Hard cel-shading, flat colors, bold outlines.")
                    appendLine("   BANNED: soft lighting, soft gradients, realistic rendering.")
                    appendLine()
                }

                Genre.HORROR -> {
                    appendLine("⚠️⚠️⚠️ HORROR MANDATORY RULES (READ FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("1. 32-BIT PIXEL ART - PS1/Sega Saturn retro game aesthetic.")
                    appendLine("   Blocky shading, pixelated texture, limited color palette.")
                    appendLine()
                    appendLine("2. DARK BLUE PALETTE - Limited to dark blues, pale blues, ash grays.")
                    appendLine("   BANNED: bright colors, vibrant tones, warm colors.")
                    appendLine()
                    appendLine("3. MANDATORY EERIE ENVIRONMENT - NO empty backgrounds.")
                    appendLine("   Include 3+ elements: abandoned hospital, foggy street, dark forest, etc.")
                    appendLine()
                    appendLine("4. PSYCHOLOGICAL HORROR - Oppressive mood, creeping dread.")
                    appendLine("   Focus on atmosphere over explicit gore.")
                    appendLine()
                }

                Genre.FANTASY -> {
                    appendLine("⚠️⚠️⚠️ FANTASY MANDATORY RULES (READ FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("1. CLASSICAL OIL PAINTING - Romantic academic style (Waterhouse, Leighton).")
                    appendLine("   Visible brushwork, luminous glazing, sfumato transitions.")
                    appendLine("   BANNED: digital art, concept art, anime, modern illustration.")
                    appendLine()
                    appendLine("2. CRIMSON RED DOMINANCE - Red must dominate the composition.")
                    appendLine("   Crimson skies, red fabrics, rose gardens, sunset light.")
                    appendLine("   Accent: Radiant gold for divine/mystical elements.")
                    appendLine()
                    appendLine("3. CLASSICAL ANATOMY - Renaissance proportions, graceful poses.")
                    appendLine("   BANNED: exaggerated muscles, comic book proportions.")
                    appendLine()
                    appendLine("4. MANDATORY BACKGROUND - Classical environments with red integration.")
                    appendLine("   BANNED: plain backgrounds, cool blues, greens.")
                    appendLine()
                }

                Genre.HEROES -> {
                    appendLine("⚠️⚠️⚠️ HEROES MANDATORY RULES (READ FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("1. MODERN COMIC BOOK STYLE - Bold ink lines, dynamic foreshortening.")
                    appendLine("   Think: Into the Spider-Verse meets Arkham City concept art.")
                    appendLine()
                    appendLine("2. URBAN VERTICALITY - Vast cityscapes, towering skyscrapers.")
                    appendLine("   Dizzying perspectives, looking up/down at buildings.")
                    appendLine()
                    appendLine("3. ELECTRIC BLUE ACCENT - In sky, reflections, rim lighting.")
                    appendLine("   Subtle unifying element, not dominant wash.")
                    appendLine()
                    appendLine("4. HEROIC POSING - Dynamic, vertical, larger-than-life.")
                    appendLine("   Low angles looking up, or high vantage points.")
                    appendLine()
                }

                Genre.CRIME -> {
                    appendLine("⚠️⚠️⚠️ CRIME MANDATORY RULES (READ FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("1. RENAISSANCE MASTERPIECE - High Renaissance + Art Deco glamour.")
                    appendLine("   Perfect anatomy, polished academic oil texture, sfumato.")
                    appendLine()
                    appendLine("2. DIVINE PERFECTION - Subjects are 'Deities of Paradise'.")
                    appendLine("   Untouchable elegance, relaxed dominance, serene superiority.")
                    appendLine()
                    appendLine("3. TROPICAL COASTAL PARADISE - Miami Vice golden hour.")
                    appendLine("   Turquoise seas, palm trees, luxury pools, yachts.")
                    appendLine()
                    appendLine("4. HOT PINK ACCENT - Sparingly in fabrics, sunset hues, neon.")
                    appendLine()
                    appendLine("5. CANDID POSING - NOT facing camera. Lounging, looking away.")
                    appendLine()
                }

                Genre.SHINOBI -> {
                    appendLine("⚠️⚠️⚠️ SHINOBI MANDATORY RULES (READ FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("1. SUMI-E INK WASH - Japanese brush painting technique.")
                    appendLine("   Bold imperfect strokes, economy of brushwork.")
                    appendLine()
                    appendLine("2. BLACK AND WHITE ONLY - Pure monochrome, no color tints.")
                    appendLine("   Form through ink density and negative space.")
                    appendLine()
                    appendLine("3. SINGLE CRIMSON RED ACCENT - One vital color element only.")
                    appendLine()
                    appendLine("4. NEGATIVE SPACE - White paper is part of composition.")
                    appendLine("   Environment suggested, not fully drawn.")
                    appendLine()
                    appendLine("5. RICE PAPER TEXTURE - Visible washi paper grain.")
                    appendLine()
                }

                Genre.SPACE_OPERA -> {
                    appendLine("⚠️⚠️⚠️ SPACE OPERA MANDATORY RULES (READ FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("1. 1950s ATOMIC AGE ILLUSTRATION - Gouache/oil, painterly finish.")
                    appendLine("   Retro-futurism, raygun gothic, pulp cover art style.")
                    appendLine()
                    appendLine("2. VAST COSMIC ENVIRONMENTS - Nebulae, star fields, galaxies.")
                    appendLine("   Overwhelming scale and beauty of space.")
                    appendLine()
                    appendLine("3. CHERRY RED/ROCKET ORANGE ACCENT - For engines, energy, stars.")
                    appendLine()
                    appendLine("4. OPTIMISTIC HEROIC TONE - Awe-inspired, adventurous.")
                    appendLine("   BANNED: dark, brooding, cynical postures.")
                    appendLine()
                }

                Genre.COWBOY -> {
                    appendLine("⚠️⚠️⚠️ COWBOY MANDATORY RULES (READ FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("1. WESTERN OIL PAINTING - Remington/Russell style.")
                    appendLine("   Bold visible brushstrokes, thick impasto, canvas texture.")
                    appendLine("   BANNED: photorealism, smooth digital blending.")
                    appendLine()
                    appendLine("2. EXPRESSIVE BRUSHWORK - Loose gestural strokes.")
                    appendLine("   'Painted with passion' not 'photographed and polished'.")
                    appendLine()
                    appendLine("3. WARM EARTHY PALETTE - Burnt sienna, raw ochre, desert sand.")
                    appendLine("   Accent: Burnt orange/sunset gold for warmth.")
                    appendLine()
                    appendLine("4. GOLDEN HOUR LIGHTING - Dramatic chiaroscuro, long shadows.")
                    appendLine()
                }
            }
        }

    /**
     * Genre-critical VALIDATION rules for the reviewer.
     * Put at TOP of validation to ensure they're checked first.
     */
    private fun genreCriticalValidation(genre: Genre): String =
        buildString {
            when (genre) {
                Genre.CYBERPUNK -> {
                    appendLine("⚠️⚠️⚠️ CYBERPUNK CRITICAL CHECKS (DO FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("CHECK 1: CYBERWARE COUNT - Minimum 2-3 visible augmentations required.")
                    appendLine("□ Valid: prosthetic limbs, artificial eyes, neural ports, mechanical spine")
                    appendLine("□ Invalid: circuit tattoos, silver scars, 'enhanced' without visible mod")
                    appendLine("□ If <2 → INSUFFICIENT_CYBERWARE → ADD chrome arm/artificial eyes/neural port")
                    appendLine()
                    appendLine("CHECK 2: CYBERWARE STYLE - Must be 'cyborg pretending to be human'")
                    appendLine("□ TOO SUBTLE → UPGRADE to replacement parts")
                    appendLine("□ TOO HEAVY → SCALE DOWN to fit human anatomy")
                    appendLine()
                    appendLine("CHECK 3: MATURE TONE - No cute/kawaii/innocent/playful.")
                    appendLine()
                    appendLine("CHECK 4: 80s OVA STYLE - cel shading, hard shadows, film grain, muted colors.")
                    appendLine()
                }

                Genre.PUNK_ROCK -> {
                    appendLine("⚠️⚠️⚠️ PUNK ROCK CRITICAL CHECKS (DO FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("CHECK 1: CARTOON STYLE - Must be Gorillaz/KND 2D cartoon, NOT realistic.")
                    appendLine()
                    appendLine("CHECK 2: EYES - Only 3 valid options:")
                    appendLine("□ Simplified stylized | Full white no pupil | Black void")
                    appendLine("□ VIOLATION if: 'brown eyes', 'blue eyes', any realistic eye color")
                    appendLine()
                    appendLine("CHECK 3: BACKGROUND - Must have detailed environment (graffiti, amps, etc.)")
                    appendLine("□ VIOLATION if: plain/gradient/white/empty background")
                    appendLine()
                    appendLine("CHECK 4: FLAT RENDERING - Hard cel-shading, flat colors.")
                    appendLine("□ VIOLATION if: soft lighting, soft gradients, realistic shading")
                    appendLine()
                }

                Genre.HORROR -> {
                    appendLine("⚠️⚠️⚠️ HORROR CRITICAL CHECKS (DO FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("CHECK 1: PIXEL ART STYLE - 32-bit PS1/Saturn aesthetic required.")
                    appendLine()
                    appendLine("CHECK 2: COLOR PALETTE - Dark blues, pale blues, ash grays only.")
                    appendLine("□ VIOLATION if: bright/vibrant/warm colors")
                    appendLine()
                    appendLine("CHECK 3: ENVIRONMENT - Eerie setting with 3+ details required.")
                    appendLine("□ VIOLATION if: empty/plain background")
                    appendLine()
                }

                Genre.FANTASY -> {
                    appendLine("⚠️⚠️⚠️ FANTASY CRITICAL CHECKS (DO FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("CHECK 1: CLASSICAL OIL PAINTING - Must feel like Renaissance art.")
                    appendLine("□ VIOLATION if: digital art, anime, concept art, modern illustration")
                    appendLine()
                    appendLine("CHECK 2: CRIMSON RED DOMINANCE - Red must be present in composition.")
                    appendLine()
                    appendLine("CHECK 3: CLASSICAL ENVIRONMENT - Renaissance setting required.")
                    appendLine("□ VIOLATION if: plain background, cool blues/greens dominating")
                    appendLine()
                }

                Genre.HEROES -> {
                    appendLine("⚠️⚠️⚠️ HEROES CRITICAL CHECKS (DO FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("CHECK 1: COMIC BOOK STYLE - Bold ink lines, dynamic foreshortening.")
                    appendLine()
                    appendLine("CHECK 2: URBAN ENVIRONMENT - Cityscape with verticality required.")
                    appendLine()
                    appendLine("CHECK 3: ELECTRIC BLUE ACCENT - Present in sky/reflections/rim light.")
                    appendLine()
                }

                Genre.CRIME -> {
                    appendLine("⚠️⚠️⚠️ CRIME CRITICAL CHECKS (DO FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("CHECK 1: RENAISSANCE QUALITY - Perfect anatomy, academic oil texture.")
                    appendLine()
                    appendLine("CHECK 2: TROPICAL PARADISE SETTING - Coastal, golden hour, luxury.")
                    appendLine()
                    appendLine("CHECK 3: HOT PINK ACCENT - Present somewhere in composition.")
                    appendLine()
                    appendLine("CHECK 4: CANDID POSING - NOT stiff facing camera.")
                    appendLine()
                }

                Genre.SHINOBI -> {
                    appendLine("⚠️⚠️⚠️ SHINOBI CRITICAL CHECKS (DO FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("CHECK 1: SUMI-E INK STYLE - Brush strokes, ink wash technique.")
                    appendLine()
                    appendLine("CHECK 2: MONOCHROME - Black and white only, no color tints.")
                    appendLine("□ Only exception: single crimson red accent")
                    appendLine()
                    appendLine("CHECK 3: NEGATIVE SPACE - White paper as compositional element.")
                    appendLine()
                }

                Genre.SPACE_OPERA -> {
                    appendLine("⚠️⚠️⚠️ SPACE OPERA CRITICAL CHECKS (DO FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("CHECK 1: 1950s ILLUSTRATION STYLE - Gouache/oil, retro-futurism.")
                    appendLine()
                    appendLine("CHECK 2: COSMIC ENVIRONMENT - Nebulae, stars, vast space required.")
                    appendLine()
                    appendLine("CHECK 3: CHERRY RED/ORANGE ACCENT - For energy, engines, stars.")
                    appendLine()
                    appendLine("CHECK 4: OPTIMISTIC TONE - Heroic, adventurous, awe-inspired.")
                    appendLine()
                }

                Genre.COWBOY -> {
                    appendLine("⚠️⚠️⚠️ COWBOY CRITICAL CHECKS (DO FIRST) ⚠️⚠️⚠️")
                    appendLine()
                    appendLine("CHECK 1: OIL PAINTING STYLE - Visible brushstrokes, impasto texture.")
                    appendLine("□ VIOLATION if: photorealism, smooth digital blending")
                    appendLine()
                    appendLine("CHECK 2: WARM EARTHY PALETTE - Burnt sienna, ochre, desert tones.")
                    appendLine()
                    appendLine("CHECK 3: GOLDEN HOUR LIGHTING - Sunset warmth, long shadows.")
                    appendLine()
                }
            }
        }

    fun criticalGenerationRule() =
        buildString {
            appendLine("**CRITICAL RULES:**")
            appendLine("- Full-bleed artwork filling entire canvas. NO borders, frames, panels, insets.")
            appendLine("- FORBIDDEN: Text, logos, watermarks, UI elements, letterbox bars, transparent backgrounds.")
            appendLine("- STRICT genre style adherence: technique, palette, mood, era as specified.")
            appendLine("- NO mixing incompatible styles. Character design MUST match genre.")
        }

    @Suppress("ktlint:standard:max-line-length")
    fun artComposition(
        genre: Genre,
        context: String,
        visualDirection: String?,
    ) = buildString {
        appendLine("ROLE: Art Director AI. Convert narrative + visual direction into precise image generation prompt.")
        appendLine()

        // Genre-critical rules at TOP for maximum visibility
        val criticalRules = genreCriticalRules(genre)
        if (criticalRules.isNotBlank()) {
            appendLine(criticalRules)
        }

        appendLine("**ART STYLE (MANDATORY):** ${GenrePrompts.artStyle(genre)}")
        appendLine()
        appendLine(criticalGenerationRule())

        visualDirection?.let {
            appendLine("**VISUAL DIRECTION:** $it")
            appendLine()
            appendLine("TRANSLATE PARAMETERS:")
            appendLine("- FRAMING: Describe exactly what's visible. OMIT anything outside frame.")
            appendLine("- ANGLE: State camera position explicitly ('seen from high angle', 'low angle looking up')")
            appendLine("- LENS/DOF: Describe focus ('sharp focus, blurred background', 'wide-angle capturing environment')")
            appendLine("- PLACEMENT: State subject position ('positioned in lower-left third')")
            appendLine("- SUBJECT ORIENTATION: Describe body axis + head direction + gaze separately")
            appendLine("- SCALE_ZOOM: State frame fill % and perceived distance")
            appendLine()
            appendLine("NARRATIVE DNA (CRITICAL):")
            appendLine("- ACTION: What subject is DOING. Adapt to our character's world, preserve energy.")
            appendLine("- EMOTIONAL_BEAT: Emotion being PROJECTED. Drives expression + body language.")
            appendLine("- TENSION_SOURCE: What creates visual interest. Include equivalent element.")
            appendLine()
        }

        appendLine("**CONTEXT:** $context")
        appendLine()
        appendLine("TRAIT PRESERVATION:")
        appendLine("- CRITICAL: Race/ethnicity, exact skin tone, hair texture/style, facial structure")
        appendLine("- IMPORTANT: Body type, age, primary outfit (if visible at framing)")
        appendLine("- DISTINCTIVE: Tattoos, scars, piercings, jewelry (if not cut by framing)")
        appendLine()
        appendLine("DIRECTIVES:")
        appendLine("1. STRICT ART STYLE COMPLIANCE: Follow genre rendering exactly")
        appendLine("2. FRAMING DICTATES VISIBILITY: Only describe what's visible. Include scale/zoom.")
        appendLine("3. EMOTIONAL UNITY: Expression + pose + hands must convey ONE specific emotion")
        appendLine("4. ENVIRONMENT: 3+ specific details to establish atmosphere")
        appendLine("5. LIGHTING: Direction + quality + color (match genre requirements)")
        appendLine("6. COMPOSITION: State perspective, placement, depth layers")
        appendLine()
        appendLine(
            "PROMPT FORMAT: [Art Style] → [Subject + Traits] → [Action] → [Framing] → [Expression] → [Environment] → [Lighting] → [Composition] → [Tension]",
        )
        appendLine()
        appendLine("OUTPUT: Single flowing paragraph. Concrete visual specification, not creative writing.")
    }

    /**
     * TOKEN-OPTIMIZED Visual Direction Extraction.
     * Extracts PHOTOGRAPHY DNA (10 params) + NARRATIVE DNA (3 params).
     * Genre-dependent elements (environment, mood, atmosphere, texture, color) come from GenrePrompts.
     */
    @Suppress("ktlint:standard:max-line-length")
    fun extractComposition() =
        buildString {
            appendLine("TASK: Extract PHOTOGRAPHY DNA + NARRATIVE DNA from reference image. Output 13 parameters.")
            appendLine()
            appendLine("⚠️ CRITICAL PHILOSOPHY:")
            appendLine("A reference image tells a STORY, not just composition. Extract WHAT is happening, not just HOW it's framed.")
            appendLine("The goal is NOT to copy the reference, but to capture its EMOTIONAL INTENT and NARRATIVE ENERGY.")
            appendLine("The artist will adapt this to our specific character while preserving the story/action/feeling.")
            appendLine()
            appendLine("RULES:")
            appendLine("- NO preamble/summary/explanation. Parameters ONLY.")
            appendLine("- BE HONEST: Don't describe body parts not visible. 'Not visible' > wrong guess.")
            appendLine("- Genre provides: environment, mood, color, texture, atmosphere. Extract camera/composition + narrative intent.")
            appendLine()
            appendLine("10 PARAMETERS (Format: 'N. NAME: value'):")
            appendLine()
            appendLine("1. ANGLE - WHERE IS THE CAMERA? (CRITICAL - Most frequently misidentified)")
            appendLine()
            appendLine("   ⚠️⚠️⚠️ PRIMARY TEST (DO THIS FIRST): WHERE IS THE SUBJECT'S FACE POINTING?")
            appendLine("   - If subject's FACE is TILTED UP toward camera → CAMERA IS ABOVE → HIGH-ANGLE")
            appendLine("   - If subject's FACE is TILTED DOWN toward camera → CAMERA IS BELOW → LOW-ANGLE")
            appendLine("   - If subject's FACE is LEVEL/STRAIGHT → EYE-LEVEL")
            appendLine("   This is the MOST RELIABLE indicator. Trust the face orientation!")
            appendLine()
            appendLine("   HIGH-ANGLE (camera ABOVE, looking DOWN at subject):")
            appendLine("   - FACE IS TILTED UP toward camera (chin raised, eyes looking up)")
            appendLine("   - Top of head/hair PROMINENT and CLOSER to camera")
            appendLine("   - Forehead appears larger/closer than chin")
            appendLine("   - Background shows floor/ground/lower areas")
            appendLine("   - Subject may appear vulnerable, introspective, or intimate")
            appendLine(
                "   - COMMON in portrait photography - camera held at photographer's eye height looking down at seated/shorter subject",
            )
            appendLine()
            appendLine("   LOW-ANGLE (camera BELOW, looking UP at subject):")
            appendLine("   - FACE IS TILTED DOWN toward camera (chin tucked, eyes looking down)")
            appendLine("   - Jaw/chin PROMINENT and CLOSER to camera")
            appendLine("   - Can see UNDER chin/nostrils")
            appendLine("   - Shoulders appear ABOVE eye line, looming over camera")
            appendLine("   - Background shows ceiling/sky/upper areas")
            appendLine("   - Subject appears dominant, imposing, powerful")
            appendLine()
            appendLine("   EYE-LEVEL (camera at SAME HEIGHT as subject's eyes):")
            appendLine("   - Face is LEVEL, neither tilted up nor down")
            appendLine("   - Neutral perspective, no vertical distortion")
            appendLine("   - Eyes roughly at center of vertical frame")
            appendLine("   - Forehead and chin appear equal distance from camera")
            appendLine()
            appendLine("   DUTCH-TILT: Frame is rotated/tilted sideways (can combine with any vertical angle)")
            appendLine()
            appendLine(
                "   ⚠️ SELF-CHECK: If you say 'LOW-ANGLE' but the subject's face is TILTED UP → YOU ARE WRONG. Switch to HIGH-ANGLE.",
            )
            appendLine()
            appendLine("2. LENS - Analyze visual compression (CRITICAL for perspective feel):")
            appendLine("   ULTRA-WIDE 14-24mm: Visible barrel distortion, exaggerated depth, close objects HUGE")
            appendLine("   WIDE 24-35mm: Environment prominent, slight distortion at edges, sense of space")
            appendLine("   NORMAL 35-50mm: Natural perspective, balanced subject/background, no obvious compression")
            appendLine("   PORTRAIT 50-85mm: Flattering compression, background slightly compressed, intimate feel")
            appendLine("   TELEPHOTO 85-200mm+: Strong compression, background flattened, very intimate/tight")
            appendLine(
                "   ⚠️ CUES: Tight head/face shots with blurred background → portrait/tele. Wide environmental context → wide/normal.",
            )
            appendLine()
            appendLine(
                "3. FRAMING: [ECU: Face Only | CU: Head-Shoulders | MCU: Head-Chest | MS: Head-Waist | MWS: Head-Knees | FS: Full-Body | WS: Body+Env | EWS: <30% frame]",
            )
            appendLine()
            appendLine("4. PLACEMENT: [H: left/center/right] [V: upper/center/lower]")
            appendLine()
            appendLine(
                "5. LIGHTING: [direction: front/front-left/front-right/side-left/side-right/back/top] + [quality: hard/soft] + shadow description",
            )
            appendLine()
            appendLine("6. DOF: [razor/shallow/moderate/deep/infinite]")
            appendLine()
            appendLine("7. PERSPECTIVE: [one-point/two-point/three-point/forced/foreshortening]")
            appendLine()
            appendLine("8. SUBJECT_ORIENTATION - BE SPECIFIC about direction:")
            appendLine("   • BODY AXIS: [Front / 3/4-left / 3/4-right / Profile-left / Profile-right / Back-facing]")
            appendLine(
                "   • HEAD: [Front / 3/4-left / 3/4-right / Profile-left / Profile-right / Over-shoulder-left / Over-shoulder-right]",
            )
            appendLine("   • GAZE: [Direct / Away-left / Away-right / Up / Down / Closed]")
            appendLine("   (left/right = direction subject is turned TOWARD)")
            appendLine()
            appendLine("9. FORM_POSTURE - Describe with DETAIL (not just 'relaxed'):")
            appendLine("   ECU/CU: head tilt (direction), neck angle, shoulder position (level/raised/tension)")
            appendLine("   Example: 'Head tilted slight right, neck extended, left shoulder raised with tension'")
            appendLine()
            appendLine("10. SCALE_ZOOM: [Frame fill %] + [Distance: intimate/close/comfortable/observational/distant]")
            appendLine()
            appendLine("=== NARRATIVE DNA (What STORY is being told?) ===")
            appendLine()
            appendLine("11. ACTION - What is the subject DOING? (CRITICAL for storytelling)")
            appendLine("   - Describe the SPECIFIC action/gesture/movement in the image")
            appendLine("   - Include objects being interacted with (gun, sword, book, cigarette, etc.)")
            appendLine("   - Include hand positions and what they're doing")
            appendLine("   Examples:")
            appendLine("   - 'Pointing a gun directly at viewer with extended arm'")
            appendLine("   - 'Lighting a cigarette with cupped hands, face illuminated by flame'")
            appendLine("   - 'Mid-stride running, hair and coat trailing behind'")
            appendLine("   - 'Leaning against wall with arms crossed, cigarette dangling from lips'")
            appendLine("   - 'Standing still, no active gesture' (if truly static)")
            appendLine("   ⚠️ DO NOT write 'standing' if there's ANY action happening!")
            appendLine()
            appendLine("12. EMOTIONAL_BEAT - The micro-story/feeling of this exact moment:")
            appendLine("   - What emotion is the subject PROJECTING? (not just feeling)")
            appendLine("   - Is this a moment of power, vulnerability, tension, release, anticipation?")
            appendLine("   - What would you caption this frame if it was a movie still?")
            appendLine("   Examples:")
            appendLine("   - 'Aggressive confrontation - daring the viewer to make a move'")
            appendLine("   - 'Quiet melancholy - lost in painful memory'")
            appendLine("   - 'Predatory anticipation - about to strike'")
            appendLine("   - 'Defiant last stand - wounded but refusing to fall'")
            appendLine("   - 'Cocky superiority - knows they've already won'")
            appendLine()
            appendLine("13. TENSION_SOURCE - What creates VISUAL/NARRATIVE tension?")
            appendLine("   - What element draws the eye and creates interest?")
            appendLine("   - What makes this image DYNAMIC rather than static?")
            appendLine("   - Consider: weapon presence, body asymmetry, implied motion, emotional conflict")
            appendLine("   Examples:")
            appendLine("   - 'Gun barrel pointed at camera creates direct threat'")
            appendLine("   - 'Contrast between relaxed pose and intense eyes'")
            appendLine("   - 'Wind-blown elements suggest motion/urgency'")
            appendLine("   - 'Clenched fist contradicts calm expression'")
            appendLine("   - 'Over-shoulder glance creates mystery of what they're looking at'")
            appendLine()
            appendLine("OUTPUT: 13 numbered parameters. No explanations. Start with '1. ANGLE:'")
        }

    /**
     * TOKEN-OPTIMIZED: Validates prompt against 10 core parameters + genre rules.
     */
    fun reviewImagePrompt(
        visualDirection: String?,
        artStyleValidationRules: String,
        strictness: ReviewerStrictness,
        finalPrompt: String,
        genre: Genre = Genre.CYBERPUNK,
    ) = buildString {
        appendLine(strictness.description)
        appendLine()
        appendLine("TASK: Validate prompt. Output JSON (ImagePromptReview structure).")
        appendLine()

        // Genre-critical validation at TOP
        appendLine(genreCriticalValidation(genre))

        appendLine("VALIDATION RULES:")
        appendLine()
        appendLine("1. CINEMATOGRAPHY (10 params from visual direction):")
        appendLine("   - angle, lens, framing, placement, lighting, DOF, perspective, subject_orientation, form_posture, scale_zoom")
        appendLine("   - All must match visual direction. Environment/mood/color come from GENRE, not reference.")
        appendLine("   - SCALE_ZOOM MANDATORY: Frame fill % + distance descriptor must be present.")
        appendLine()
        appendLine("2. NARRATIVE DNA (3 params - CRITICAL FOR STORYTELLING):")
        appendLine("   - ACTION: Subject must be DOING something that captures reference's narrative energy")
        appendLine("   - EMOTIONAL_BEAT: Facial expression + body language must project the specified emotion")
        appendLine("   - TENSION_SOURCE: An equivalent tension-creating element must be present")
        appendLine("   - STATIC_SUBJECT_VIOLATION if: character is just standing/posing with no action when reference had clear action")
        appendLine("   - MISSING_NARRATIVE_INTENT if: prompt lacks emotional projection or tension element")
        appendLine()

        appendLine("3. SUBJECT ORIENTATION (3 components):")
        appendLine("   - BODY: [Front/3/4/Profile/Back-facing/Back-with-head-turn] - must match exactly")
        appendLine("   - HEAD: [Front/3/4/Profile/Over-shoulder] - must match exactly")
        appendLine("   - GAZE: [Direct/Away/Up-Down/Closed] - must match exactly")
        appendLine("   - Back-facing body described as front = CRITICAL VIOLATION")
        appendLine()

        appendLine("4. VISIBILITY BY FRAMING:")
        appendLine("   - ECU/CU: head, neck, shoulders ONLY. NO hands/legs/stance.")
        appendLine("   - MCU/MS: add torso, arms. NO legs.")
        appendLine("   - MWS+: full body OK.")
        appendLine("   - Describing invisible parts = VISIBILITY_VIOLATION")
        appendLine()

        visualDirection?.let {
            appendLine("5. VISUAL DIRECTION TO ENFORCE: \"$it\"")
            appendLine()
        }

        appendLine("6. GENRE RULES (APPLY STRICTLY):")
        appendLine("   $artStyleValidationRules")
        appendLine()

        appendLine("7. EXPRESSION: Must be specific emotion, not generic ('thoughtful'→'weary resignation')")
        appendLine()

        appendLine("8. RENDERING STYLE (CRITICAL):")
        appendLine("   Prompt MUST specify: shading, shadows, line work, colors, texture matching genre.")
        appendLine("   RENDERING_VIOLATION if: wrong style (e.g., modern anime for vintage OVA genre) or missing rendering specs.")
        appendLine()

        appendLine("VIOLATIONS:")
        appendLine("- VISIBILITY_VIOLATION: body parts out of frame")
        appendLine("- SUBJECT_ORIENTATION_VIOLATION: body/head/gaze mismatch")
        appendLine("- MISSING_SCALE_ZOOM: no frame fill or distance")
        appendLine("- RENDERING_VIOLATION: wrong/missing rendering style for genre")
        appendLine("- MISSING_80S_OVA_STYLE (CYBERPUNK): lacks 80s anime markers (cel shading, film grain, etc.)")
        appendLine("- STATIC_SUBJECT_VIOLATION: character just standing/posing when reference had dynamic action")
        appendLine("- MISSING_NARRATIVE_INTENT: no emotional projection, no tension element, artwork lacks story")
        appendLine("- INSUFFICIENT_CYBERWARE (CYBERPUNK): <3 major visible cyberware elements")
        appendLine("- GENRE_AURA_VIOLATION: pose/vibe contradicts genre")
        appendLine("- BANNED_TERMINOLOGY: forbidden words")
        appendLine()

        appendLine("AUTO-FIX PATTERNS:")
        appendLine()
        appendLine("NARRATIVE (CRITICAL - transforms static portraits into storytelling moments):")
        appendLine("- STATIC_SUBJECT → Add action from visual direction: weapon handling, gesture, interaction")
        appendLine("- MISSING_EMOTIONAL_BEAT → Inject specific emotional projection into expression and body")
        appendLine("- MISSING_TENSION → Add tension element: weapon, confrontational pose, dramatic gesture")
        appendLine("- 'standing' or 'posing' → Replace with specific action verb and gesture from reference")
        appendLine()
        appendLine("RENDERING: If missing/wrong, inject genre's required rendering style from GENRE RULES above.")
        appendLine()
        appendLine("CYBERWARE (CYBERPUNK):")
        appendLine("- 'enhanced eyes' → 'mechanical eye with LED array, scanner reticle'")
        appendLine("- 'silver scars' → 'chrome neural port with cables, junction box'")
        appendLine("- 'glowing implants' → 'chrome plate with exposed circuitry'")
        appendLine("- 'circuit tattoo/data bracelet' → 'chrome forearm with hydraulic pistons'")
        appendLine("- If <3 cyberware: ADD mechanical eye, chrome port, or limb replacement")
        appendLine()

        appendLine("⚠️ MANDATORY FEEDBACK FIELDS (NEVER NULL):")
        appendLine()
        appendLine("artistImprovementSuggestions (REQUIRED - provide concrete advice):")
        appendLine("- What specific techniques could improve the prompt?")
        appendLine("- What details were missing or too vague?")
        appendLine("- How could framing/expression/pose be better described?")
        appendLine("- Example: 'Specify exact eye color and catchlight position for CU frames'")
        appendLine()
        appendLine("visualDirectorSuggestions (REQUIRED - provide extraction improvements):")
        appendLine("- Was the visual direction extraction precise enough?")
        appendLine("- What parameters were missing or ambiguous?")
        appendLine("- How could the extraction be more specific?")
        appendLine("- Example: 'Include frame fill % in SCALE_ZOOM, specify shadow direction in LIGHTING'")
        appendLine()

        appendLine("OUTPUT JSON (ImagePromptReview):")
        appendLine("- originalPrompt: input prompt")
        appendLine("- correctedPrompt: fixed prompt")
        appendLine("- violations[]: {type, severity, description, example}")
        appendLine("- changesApplied[]: list of fixes made")
        appendLine("- artistImprovementSuggestions: NEVER NULL - concrete feedback for prompt writer")
        appendLine("- visualDirectorSuggestions: NEVER NULL - feedback for extraction AI")
        appendLine("- wasModified: boolean")
        appendLine()
        appendLine("PROMPT TO REVIEW:")
        appendLine(finalPrompt)
    }
}
