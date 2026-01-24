package com.ilustris.sagai.core.ai.prompts

import androidx.compose.ui.graphics.toArgb
import com.ilustris.sagai.core.ai.model.ReviewerStrictness
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette

object ImagePrompts {
    private fun getHexPalette(genre: Genre): String =
        genre.colorPalette().joinToString(", ") {
            "#%06X".format(0xFFFFFF and it.toArgb())
        }

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
            appendLine("**CRITICAL PRESENTATION RULES (ABSOLUTE):**")
            appendLine("- FULL-BLEED ARTWORK: The art must fill the entire canvas from edge to edge.")
            appendLine(
                "- NO GRAPHICAL ELEMENTS: Strictly FORBIDDEN: borders, white/black frames, comic panels, multi-panel insets, gutters, or letterbox bars.",
            )
            appendLine("- PURE ARTISTRY: FORBIDDEN: Text, logos, signatures, watermarks, UI elements, or digital overlays.")
            appendLine("- The output must look like a complete, unedited piece of art untouched by graphic design.")
            appendLine("- STRICT genre style adherence: technique, palette, mood, era as specified.")
            appendLine("- NO mixing incompatible styles. Character design MUST match genre.")
        }

    @Suppress("ktlint:standard:max-line-length")
    fun artComposition(
        genre: Genre,
        context: String,
        visualDirection: String?,
    ) = buildString {
        appendLine(
            "ROLE: The Soulful Artist AI. You are not just a translator; you are the visionary who captures the essence, meaning, and soul of the saga.",
        )
        appendLine()

        val criticalRules = genreCriticalRules(genre)
        if (criticalRules.isNotBlank()) {
            appendLine(criticalRules)
        }

        appendLine("**STYLE CROSS-REFERENCE (ABSOLUTE):**")
        appendLine("- Technical Specification: ${GenrePrompts.artStyle(genre)}")
        appendLine("- Appearance Guidelines: ${GenrePrompts.appearanceGuidelines(genre)}")
        appendLine("- COLOR PALETTE (MANDATORY): ${getHexPalette(genre)}")
        appendLine("- CROSS-REFERENCE: Use the EXACT terminology from the Technical Specification (Rendering, Technique, Aura).")
        appendLine("- If the genre requires '32-BIT PIXEL ART', do not let high-resolution concepts leak in.")
        appendLine()

        appendLine("**CONTEXTUAL ABSTRACTION (THE 'WHY'):**")
        appendLine("Analyze the context: \"$context\"")
        appendLine("- What is the emotional core of this moment?")
        appendLine("- How do the characters' surroundings reflect their internal state?")
        appendLine("- Why are we drawing this exact frame? Find the narrative weight.")
        appendLine(
            "- STRICT CONTEXT ADHERENCE: Use character appearance descriptions ONLY from the provided context. Do NOT invent body types, clothes, or features not present. If context is minimal, stick to the genre style and extracted visual direction.",
        )
        appendLine(
            "- MULTI-CHARACTER MANDATE: If the context identifies multiple characters, you MUST integrate ALL of them into the composition. Do not omit subjects to simplify the scene. Every subject mentioned is essential for the narrative weight of this frame.",
        )
        appendLine()

        appendLine(criticalGenerationRule())

        visualDirection?.let {
            appendLine("**DIRECTOR'S VISION (THE 'HOW'):**")
            appendLine(it)
            appendLine()
            appendLine("PAINTING THE VISION:")
            appendLine("- CHARACTER SOUL: Describe characters not as traits, but as living beings with history.")
            appendLine(
                "- NARRATIVE COMPOSITION: Do not just place subjects; weave them into the scene. Their bodies should react to the environment, the light, and each other.")
            appendLine("- MEANINGFUL ORIENTATION: Use the orientation to tell the story.")
            appendLine(
                "- GESTURAL ABSTRACTION: Use 'BODY_LANGUAGE_INTENTION' to define narrative interaction. If characters are together, show their connection or tension through physical proximity and subtle cues.",
            )
            appendLine(
                "- VISIBLE ATTIRE MANDATE: If the frame shows shoulders/chest/torso (based on 'VISIBLE_CONTENT'), you MUST describe the clothing from the context. Do NOT crop out the neckline or leave it vague/naked unless the context explicitly says so. 'Shoulders visible' = 'Shirt/Armor visible'.",
            )
            appendLine("- ATMOSPHERIC WEIGHT: Translate technical lighting into emotional atmosphere.")
            appendLine("- STRICT INVISIBILITY: Follow the 'VISIBLE_CONTENT' and 'FORBIDDEN_CONTENT' lists religiously.")
            appendLine(
                "- ABSOLUTE BAN: Any element in 'FORBIDDEN_CONTENT' MUST be purged from your description. If 'Pants' are forbidden, you cannot mention 'attire' or 'lower body' at all.",
            )
            appendLine()
        }

        appendLine("STRICT PRESERVATION:")
        appendLine("- Race, ethnicity, hair texture, and unique identifiers must remain absolute, but rendered with artistic depth.")
        appendLine(
            "- ETHNICITY & TRAIT ACCURACY: If the context specifies an ethnicity (e.g., Japanese, Afro-Latino, Scandinavian), physical features MUST reflect that authentically (bone structure, eyes, hair texture). Do NOT default to a generic look.",
        )
        if (genre == Genre.SHINOBI) {
            appendLine(
                "- SHINOBI MONOCHROME ENFORCEMENT: This genre requires a STRICT BLACK & WHITE palette with only CRIMSON RED accent. NO other colors (no blues, no greens, no warm skin tones). Think Sumi-e ink on rice paper.",
            )
        }
        appendLine()

        appendLine("FINAL DIRECTIVE:")
        appendLine(
            "Write a single, masterful, flowing paragraph. It must use the specific technical rendering terminology of the ${genre.name} genre (e.g. specific line work, lighting, and textures). It must be sharp enough for an image generator but soulful enough to inspire awe. Avoid generic labels like 'comic style' if the genre specifies 'Bold Ink Lines and Dynamic Foreshortening'.",
        )
        appendLine()
        appendLine(
            "PROMPT PATTERN: [Technical Art Style Essence] → [Character Spirit & Orientation] → [Meaningful Action & Visible Detail] → [Story-telling Environment] → [Emotional Light & Color Palette] → [Atmospheric Compositional Soul]",
        )
        appendLine()
        appendLine("OUTPUT: One flowing masterpiece paragraph.")
    }

    /**
     * TOKEN-OPTIMIZED Visual Direction Extraction.
     * Extracts PHOTOGRAPHY DNA (10 params) + NARRATIVE DNA (3 params).
     * Genre-dependent elements (environment, mood, atmosphere, texture, color) come from GenrePrompts.
     */
    @Suppress("ktlint:standard:max-line-length")
    fun extractComposition() =
        buildString {
            appendLine("TASK: Act as a Visual Director. Extract SUBJECT POSITIONING and NARRATIVE DNA from the reference image.")
            appendLine()
            appendLine("⚠️ CRITICAL PHILOSOPHY:")
            appendLine(
                "Capture the SUBJECT'S relationship with the camera. Focus on orientation, what is visible, and the narrative energy.",
            )
            appendLine("We use standard image generation terminology to ensure the Artist AI understands the intended composition.")
            appendLine()
            appendLine("RULES:")
            appendLine("- NO technical lens jargon (mm). Use descriptive framing levels.")
            appendLine("- BE HONEST: Identify what is actually visible. If it's a close-up, the legs do not exist.")
            appendLine("- ORIENTATION IS KEY: Explicitly identify if the subject is seen from front, side, or back.")
            appendLine()
            appendLine("10 PARAMETERS (Output numbered list, no preamble):")
            appendLine()
            appendLine("1. ORIENTATION - The subject's rotation relative to the camera:")
            appendLine("   • BODY: [Frontal / 3/4 View / Profile / Back-facing]")
            appendLine("   • HEAD: [Facing Camera / 3/4 Turn / Profile / Looking Over Shoulder]")
            appendLine("   • GAZE: [Direct / Away / Upward / Downward / Eyes Closed]")
            appendLine()
            appendLine("2. FRAMING_LEVEL - The 'window' around the subject:")
            appendLine("   [Extreme Close-Up / Face & Shoulders / Upper Body / Waist Up / Full Body / Distant Wide Shot]")
            appendLine()
            appendLine("3. VISIBLE_CONTENT - LIST EVERY PERMITTED ELEMENT:")
            appendLine("   - Be extremely strict. If Portrait: 'Head, neck, collarbone, upper clothing/neckline'.")
            appendLine("   - If Wide: 'Full figure, complete outfit, surroundings, ground, sky'.")
            appendLine(
                "   - EXPLICITLY LIST CLOTHING PARTS if visible (e.g., 'shirt', 'jacket', 'gloves'). Do NOT describe their style/brand/color - just what they are.",
            )
            appendLine("   - NO describing elements cut by the frame.")
            appendLine()
            appendLine("4. VIEWPOINT - Camera's vertical position relative to subject:")
            appendLine("   - [Eye-Level / Seen from Above / Seen from Below]")
            appendLine("   - Use the face tilt to decide: Face tilted up = Seen from Above. Face tilted down = Seen from Below.")
            appendLine()
            appendLine("5. COMPOSITION_PLACEMENT - Subject's position on screen:")
            appendLine("   [Centered / Far Left / Far Right / Foreground / Background]")
            appendLine()
            appendLine("6. BODY_LANGUAGE_INTENTION - The narrative energy of the physical form:")
            appendLine("   - Describe how the body reacts to the context. Is it a protective hunch? A confident stride?")
            appendLine(
                "   - Focus on INTERACTION: How the subject relates to the environment or other subjects (e.g., 'Leaning into the shadows', 'A hand reaching but hesitating', 'Weighted silence in their stance').",
            )
            appendLine("   - Avoid static verbs. Use 'narrative weight' to describe the pose.")
            appendLine()
            appendLine("7. EMOTIONAL_PROJECTION - The 'Soul' of the moment:")
            appendLine("   - What emotion is being projected to the viewer?")
            appendLine("   - [Aggressive / Melancholic / Fearful / Triumphant / Calm / Mysterious]")
            appendLine()
            appendLine("8. LIGHTING_ATMOSPHERE - Light direction and feel:")
            appendLine("   - [Side-lit / Back-lit (Silhouette) / Frontal Light / Soft Ambient / Harsh Contrast]")
            appendLine()
            appendLine("9. FOCUS_DEPTH - How much of the background is visible:")
            appendLine("   - [Isolated Subject (Blurry BG) / Sharp Environment / Deep Narrative Depth]")
            appendLine()
            appendLine("10. NARRATIVE_DNA - The story being told in one sentence:")
            appendLine("    - Combine Action + Emotion + Tension.")
            appendLine("    - Example: 'A defiant warrior standing their ground despite overwhelming odds'.")
            appendLine()
            appendLine("11. FORBIDDEN_CONTENT - LIST EVERY OFF-FRAME ELEMENT:")
            appendLine("    - Based on 'FRAMING_LEVEL', explicitly list what CANNOT be seen.")
            appendLine("    - If Portrait/MCU: [Pants, Shoes, Ground, Legs, Full Outfit, Feet, Sitting/Standing Stance].")
            appendLine("    - This is a hard filter for the next phases.")
            appendLine()
            appendLine("OUTPUT: 11 numbered parameters. No explanations. Start with '1. ORIENTATION:'")
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
        context: String,
    ) = buildString {
        appendLine(strictness.description)
        appendLine()
        appendLine(
            "ROLE: The Master Critique & Supportive Mentor. Your goal is to refine the final prompt to perfection while identifying exactly where the pipeline can improve.",
        )
        appendLine()

        // Genre-critical validation at TOP
        appendLine(genreCriticalValidation(genre))

        appendLine(
            "TASK: Validate the prompt for extreme technical alignment. Ensure the Artist has not deviated from the Genre's core DNA.")
        appendLine()

        appendLine("=== SCOPE 1: DIRECTORIAL COMPLIANCE (The 'How' & 'Where') ===")
        appendLine("Validation for the VISUAL DIRECTOR logic:")
        appendLine("1. ORIENTATION & PERSPECTIVE: Does the Body/Head/Gaze match the extracted direction?")
        appendLine("2. CONTENT HALLUCINATION (STRICT): Check against 'FORBIDDEN_CONTENT'.")
        appendLine("   - If an element is in the forbidden list, it MUST NOT appear in the prompt.")
        appendLine("   - Describing forbidden parts = HALLUCINATION_VIOLATION.")
        appendLine("3. FRAMING STRICTNESS: Are only permitted elements described?")
        appendLine("   - VIOLATION: ORIENTATION_MISMATCH, VISIBILITY_VIOLATION, HALLUCINATION_VIOLATION.")
        appendLine("4. CONTEXT FIDELITY (CRITICAL): Does the artwork respect the provided context?")
        appendLine("   - Context: \"$context\"")
        appendLine(
            "   - Verify that character details (hair, eyes, distinctive features) mentioned in context AND visible in the frame are present.",
        )
        appendLine("6. MULTI-CHARACTER COMPLIANCE (STRICT SUBJECT CHECK):")
        appendLine("   - Parse the ESSENTIAL characters list (usually between '[' and ']') at the start of the context.")
        appendLine("   - For EACH and EVERY name in that list, confirm it is represented in the 'finalPrompt'.")
        appendLine("   - DO NOT ACCEPT: Prompts that focus on only one character when multiple are listed.")
        appendLine("   - DO NOT ACCEPT: Prompts that generalize subjects as 'a group' without individual descriptions for each.")
        appendLine("   - If ANY name is missing from the description → CRITICAL VIOLATION: CHARACTER_OMISSION.")
        appendLine(
            "   - MANDATORY ACTION: Rewrite the prompt to ensure all subjects are present with their unique traits from '#### SUBJECTS DETAILS'.",
        )
        appendLine("7. SAFE CONTENT & CLOTHING (MANDATORY): Ensure NO unintended nudity.")
        appendLine("   - If 'VISIBLE_CONTENT' includes shoulders/chest, the prompt MUST describe the character's clothing (from Context).")
        appendLine("   - Ensure clothing is not omitted or vaguely described to appear naked.")
        appendLine("   - VIOLATION: NUDITY_RISK, CLOTHING_OMISSION.")
        appendLine()

        appendLine("=== SCOPE 2: ARTISTIC INTEGRITY & SOUL (The 'Why' & 'Feel') ===")
        appendLine("Validation for the SOULFUL ARTIST logic:")
        appendLine("1. GENRE ESSENCE: Does the prompt embody the ${genre.name} philosophy? (Check Art Style Rules).")
        if (genre == Genre.SHINOBI) {
            appendLine(
                "   - SHINOBI CHECK: Is the palette strictly monochrome (black/white/ink) with ONLY crimson red accent? Any other color is a VIOLATION.",
            )
        }
        appendLine("2. NARRATIVE SOUL: Is the description evocative and meaningful, or just technical?")
        appendLine("3. POSING & INTERACTION: Do the characters feel integrated into the scene?")
        appendLine("   - Check if body language matches the narrative weight. Are they just 'placed' or are they 'reacting'?")
        appendLine("4. RENDERING STYLE: Are the required shading, line work, and texture from the genre present?")
        appendLine("   - VIOLATION: GENRE_AURA_VIOLATION, RENDERING_MISSING, SOULLESS_DESCRIPTION, STATIC_COMPOSITION.")
        appendLine("   - FEEDBACK: Must go to 'artistImprovementSuggestions'.")
        appendLine("4. PALETTE & ACCENT COMPLIANCE (CRITICAL):")
        appendLine("   - Does the prompt explicitly mention the Mandatory Accent Color defined in 'GENRE MANDATES'?")
        appendLine("   - Are the colors consistent with the required palette (${getHexPalette(genre)})?")
        appendLine("   - VIOLATION: PALETTE_VIOLATION, ACCENT_MISSING.")
        appendLine()

        appendLine("=== SCOPE 3: PRESENTATION & FINAL POLISH (The 'Canvas') ===")
        appendLine("Check for any graphical contamination:")
        appendLine("1. BORDERS & FRAMES: Strictly ban any mention of panels, frames, gutters, or borders.")
        appendLine("2. TEXT & LOGOS: No text or UI.")
        appendLine("   - CRITICAL: Even if it's 'Comic Style', DO NOT USE panel borders. It must be full-bleed.")
        appendLine("   - VIOLATION: GRAPHICAL_CONTAMINATION, BORDER_VIOLATION, PANEL_VIOLATION.")
        appendLine("   - AUTO-FIX: KILL any mention of borders/panels. Replace with 'Full-bleed edge-to-edge artwork'.")
        appendLine()

        visualDirection?.let {
            appendLine("**ORIGINAL DIRECTOR'S VISION:** \"$it\"")
            appendLine()
        }

        appendLine("**GENRE MANDATES:**")
        appendLine(artStyleValidationRules)
        appendLine()

        appendLine("SUPPORTIVE AUTO-FIX PHILOSOPHY:")
        appendLine("- If Directorial Error: Re-align with the vision WITHOUT stripping the artist's poetic language.")
        appendLine("- If Artistic Error: Inject the missing genre soul using concrete visual nouns that help AI generation.")
        appendLine("- If Banned Elements: Gracefully remove them while enhancing the remaining visible details.")
        appendLine()

        appendLine("⚠️ MANDATORY FEEDBACK (NEVER NULL):")
        appendLine("- artistImprovementSuggestions: How the Artist can better capture the soul or adhere to genre rules.")
        appendLine("- visualDirectorSuggestions: How the Director can be more precise in the extraction/framing mandates.")
        appendLine()

        appendLine("OUTPUT JSON (ImagePromptReview structure):")
        appendLine("- originalPrompt: input prompt")
        appendLine("- correctedPrompt: fixed masterful prompt")
        appendLine("- violations[]: {type, severity, description, example}")
        appendLine("- artistImprovementSuggestions: Mentor the Artist AI")
        appendLine("- visualDirectorSuggestions: Mentor the Director AI")
        appendLine("- wasModified: boolean")
        appendLine()
        appendLine("PROMPT TO SCRUTINIZE:")
        appendLine(finalPrompt)
    }
}
