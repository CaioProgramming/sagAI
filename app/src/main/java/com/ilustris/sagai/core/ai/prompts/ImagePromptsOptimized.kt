package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.models.ReviewerStrictness
import com.ilustris.sagai.features.newsaga.data.model.Genre

// OPTIMIZED VERSIONS - Token-efficient while maintaining precision

object ImagePromptsOptimized {
    /**
     * OPTIMIZED extractComposition - Reduced from ~2100 to ~450 tokens (78% reduction)
     */
    fun extractComposition() =
        buildString {
            appendLine("CINEMATOGRAPHY EXTRACTION — Senior DP analyzing reference image")
            appendLine("Extract PHOTOGRAPHIC DNA (NOT art style/subject). Use ONLY camera/lighting terms.")
            appendLine()
            appendLine("OUTPUT 15 PARAMETERS (Format: 'NAME: value'):")
            appendLine()
            appendLine("TIER 1 - CRITICAL:")
            appendLine("1. ANGLE: [eye-level / low XY° / high XY° / dutch XY°] (specify degrees)")
            appendLine(
                "2. LENS: [14-24mm ultra-wide / 24-35mm wide / 35-50mm normal / 50-85mm portrait / 85-200mm tele / 200mm+ super-tele]",
            )
            appendLine(
                "3. FRAMING: [ECU face / CU head-shoulders / MCU head-chest / MS head-waist / MWS head-knees / FS full-body / WS body+env / EWS small-in-vast] (LOCKED)",
            )
            appendLine("4. PLACEMENT: [H: left/center/right third] [V: upper/center/lower third]")
            appendLine()
            appendLine("TIER 2 - IMPORTANT:")
            appendLine("5. LIGHTING: [front/side/back/top/under/omni] + [hard/soft]")
            appendLine("6. COLOR: [cool 5500K+ / neutral 5000K / warm 2500-3500K] + dominant palette")
            appendLine("7. ENVIRONMENT: Location type, scale, key elements (NO brands)")
            appendLine("8. MOOD: Emotional tone (epic/intimate/oppressive/nostalgic/etc)")
            appendLine()
            appendLine("TIER 3 - REFINEMENT:")
            appendLine("9. DOF: [razor f/1.2 / shallow f/2-2.8 / moderate f/4-5.6 / deep f/8-11 / infinite f/16+]")
            appendLine("10. ATMOSPHERE: [clear/hazy/misty/foggy/dusty/smoky]")
            appendLine("11. PERSPECTIVE: [converging/parallel/barrel/foreshortening]")
            appendLine("12. TEXTURE: [razor-sharp/film-grain/digital-noise/soft-diffused/gritty]")
            appendLine("13. TIME: [golden-hour/midday/blue-hour/night/overcast/studio]")
            appendLine("14. SIGNATURE: One unique unforgettable detail")
            appendLine()
            appendLine("VALIDATE: ✓Angle w/degrees? ✓Lens mm? ✓Framing locked? ✓NO art-style? ✓ONLY photo terms?")
        }

    /**
     * OPTIMIZED iconDescription - Reduced from ~3500 to ~1200 tokens (66% reduction)
     */
    fun iconDescriptionOptimized(
        genre: Genre,
        context: String,
        visualDirection: String?,
        characterHexColor: String? = null,
    ) = buildString {
        appendLine("=== ART STYLE MANDATE (NON-NEGOTIABLE) ===")
        appendLine(GenrePrompts.artStyle(genre))
        appendLine()
        appendLine("PRIORITY: Art style > Cinematography > Character. All must work together.")
        appendLine()

        visualDirection?.let {
            appendLine("=== CINEMATOGRAPHY (MANDATORY) ===")
            appendLine(it)
            appendLine()
            appendLine("TRANSLATION GUIDE — Convert technical specs to visual language:")
            appendLine("• Angle: '45°' → 'from below, towers overhead'")
            appendLine("• Lens: '20mm' → 'exaggerated perspective, converging lines'")
            appendLine("• Lighting: '5500K side' → 'cool blue lighting from right'")
            appendLine("• DOF: 'f/2.8' → 'sharp subject, blurred background' (SKIP if flat/cartoon style)")
            appendLine("• Environment: Name 3+ specific objects (not 'urban setting')")
            appendLine()
            appendLine("FRAMING RULES (CRITICAL):")
            appendLine("• CU/Portrait: Face+upper shoulders ONLY. NO legs/feet/full-outfit/stance")
            appendLine("• Medium: Head-waist. NO legs/feet")
            appendLine("• Full: All body parts OK")
            appendLine("Filter character description by what CAMERA SEES.")
            appendLine()
        }

        appendLine("=== CREATIVE BRIEF ===")
        appendLine(context)
        appendLine()
        appendLine(
            "Extract RELEVANT details for framing. Preserve identity (skin/ethnicity/age). Adapt anatomy to art style (exaggerated proportions if style requires).",
        )
        appendLine()

        appendLine("CHARACTER MUST BE EXPRESSIVE:")
        appendLine("• Face: Specific emotion (not neutral stare) matching personality")
        appendLine("• Body: Posture showing traits (confident/hunched/coiled/relaxed)")
        appendLine("• Hands: Purposeful gesture (not hanging limply)")
        appendLine("• Gaze: Direction conveying intention (at/away/down/up viewer)")
        appendLine()

        characterHexColor?.let { hex ->
            appendLine("ACCENT COLOR: $hex in environment/lighting (NOT on character)")
            appendLine()
        }

        appendLine("=== OUTPUT STRUCTURE (MANDATORY) ===")
        appendLine()
        appendLine("PART 1 (1 sent): Art style statement")
        appendLine("Ex: 'A gritty Gorillaz-style illustration with bold ink outlines, flat cel shading.'")
        appendLine()

        if (visualDirection != null) {
            appendLine("PART 2 (2-3 sent): Cinematography Framework (NON-NEGOTIABLE)")
            appendLine("MUST explicitly state: angle, framing, lighting direction+quality, environment w/3+ objects")
            appendLine(
                "Ex: 'Captured from below, character towers overhead. Full-body frame anchored at bottom. Harsh cool lighting from above casts sharp shadows. Urban alley with graffiti walls, dumpsters, wires.'",
            )
            appendLine()
        }

        appendLine("PART 3: Character Description")
        appendLine("Include: expression, posture, hands, gaze. Filter by framing. Follow art style anatomy rules.")
        appendLine()
        appendLine("Combine into single flowing paragraph. NO technical jargon (degrees/mm/f-stops).")
    }

    /**
     * OPTIMIZED reviewImagePrompt - Reduced from ~2800 to ~800 tokens (71% reduction)
     */
    fun reviewImagePromptOptimized(
        visualDirection: String?,
        artStyleValidationRules: String,
        strictness: ReviewerStrictness,
        finalPrompt: String,
    ) = buildString {
        appendLine("=== IMAGE PROMPT QA REVIEWER ===")
        appendLine(strictness.description)
        appendLine()
        appendLine("VALIDATE: Cinematography (from Visual Direction) + Art Style (from Rules)")
        appendLine()

        visualDirection?.let {
            appendLine("A. CINEMATOGRAPHY (12 checks):")
            appendLine("Extract from Visual Direction:")
            appendLine(it)
            appendLine()
            appendLine("CHECK final prompt has:")
            appendLine("✓ A1: Angle described visually (not degrees) - CRITICAL if missing/wrong")
            appendLine("✓ A2: Lens effects as perspective (not mm) - MAJOR if mismatch")
            appendLine("✓ A3: Framing matches, NO body parts outside view - CRITICAL violation")
            appendLine("✓ A4: Placement specified (H/V positioning) - MAJOR if missing")
            appendLine("✓ A5: DOF as visual effect (not f-stops), SKIP if flat style - MAJOR")
            appendLine("✓ A6: Lighting direction+quality visible (not Kelvin) - CRITICAL if wrong")
            appendLine("✓ A7: Color temp as mood (not numbers) - MAJOR if wrong")
            appendLine("✓ A8: Atmosphere+emotional tone - MAJOR if missing")
            appendLine("✓ A9: Environment w/3+ objects - MAJOR if vague")
            appendLine("✓ A10: Perspective distortion mentioned - MAJOR for dramatic angles")
            appendLine("✓ A11: Signature detail - MAJOR if defines uniqueness")
            appendLine("✓ A12: NO tech jargon (f/, °, mm, K) - MAJOR if found")
            appendLine()
        }

        appendLine("B. ART STYLE (5 checks):")
        appendLine("Rules: $artStyleValidationRules")
        appendLine()
        appendLine("✓ B1: No banned terms - MAJOR")
        appendLine("✓ B2: Required elements present (backgrounds etc) - CRITICAL if mandatory")
        appendLine("✓ B3: Anatomy matches style - MAJOR")
        appendLine("✓ B4: Background appropriate for framing - CRITICAL/MAJOR")
        appendLine("✓ B5: No style contradictions - MAJOR")
        appendLine()

        appendLine("OUTPUT JSON:")
        appendLine(
            """{
  "correctedPrompt": "...",
  "violations": [{"type":"...", "severity":"CRITICAL/MAJOR/MINOR", "description":"...", "example":"..."}],
  "changesApplied": ["..."],
  "wasModified": true,
  "cinematographyScore": 0-100,
  "artStyleScore": 0-100,
  "overallReadiness": "READY/NEEDS_REVIEW/CRITICAL_ISSUES"
}""",
        )
        appendLine()
        appendLine("PROMPT TO REVIEW:")
        appendLine(finalPrompt)
    }
}
