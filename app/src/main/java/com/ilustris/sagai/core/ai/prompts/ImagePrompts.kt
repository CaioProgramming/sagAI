package com.ilustris.sagai.core.ai.prompts

import androidx.compose.ui.graphics.toArgb
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette

object ImagePrompts {
    private fun getHexPalette(genre: Genre): String =
        genre.colorPalette(null).joinToString(", ") {
            "#%06X".format(0xFFFFFF and it.toArgb())
        }

    /**
     * Genre-critical rules that MUST be at the TOP of prompts.
     * These define the SOUL of each genre - the elements that make it unmistakably that style.
     */
    private fun genreCriticalRules(
        genre: Genre,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
    ): String =
        buildString {
            if (config.criticalRules.isNotBlank()) {
                appendLine("⚠️⚠️⚠️ ${genre.name} ARTISTIC SOUL (READ FIRST) ⚠️⚠️⚠️")
                appendLine()
                appendLine(config.criticalRules)
                appendLine()
            }
        }

    /**
     * Genre-critical VALIDATION rules for the reviewer.
     * Put at TOP of validation to ensure they're checked first.
     */
    private fun genreCriticalValidation(
        genre: Genre,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
    ): String =
        buildString {
            if (config.criticalValidation.isNotBlank()) {
                appendLine("⚠️⚠️⚠️ ${genre.name} SOUL CHECK (DO FIRST) ⚠️⚠️⚠️")
                appendLine()
                appendLine(config.criticalValidation)
                appendLine()
            }
        }

    fun criticalGenerationRule(imageConfig: com.ilustris.sagai.core.ai.model.ImageConfig) =
        buildString {
            if (imageConfig.criticalRules.isNotBlank()) {
                appendLine(imageConfig.criticalRules)
            } else {
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
        }

    @Suppress("ktlint:standard:max-line-length")
    fun artComposition(
        genre: Genre,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
        context: String,
        visualDirection: String?,
    ) = buildString {
        appendLine(
            "ROLE: The Soulful Artist AI. You are not just a translator; you are the visionary who captures the essence, meaning, and soul of the saga.",
        )
        appendLine()

        val criticalRules = genreCriticalRules(genre, config)
        if (criticalRules.isNotBlank()) {
            appendLine(criticalRules)
        }

        appendLine("**STYLE CROSS-REFERENCE (ABSOLUTE):**")
        appendLine("- Technical Specification: ${config.artStyle}")
        appendLine("- Appearance Guidelines: ${config.appearanceGuidelines}")
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

        appendLine(
            criticalGenerationRule(
                imageConfig =
                    com.ilustris.sagai.core.ai.model
                        .ImageConfig(),
            ),
        )

        visualDirection?.let {
            appendLine("**DIRECTOR'S VISION (THE 'HOW'):**")
            appendLine(it)
            appendLine()
            appendLine("PAINTING THE VISION:")
            appendLine("- CHARACTER SOUL: Describe characters not as traits, but as living beings with history.")
            appendLine(
                "- NARRATIVE PROXIMITY: Respect the extracted 'SUBJECT_PROXIMITY'. If focus is 'Intimate', describe the micro-textures of the skin and eyes. If 'Far', focus on the silhouette's relationship to the vast environment.",
            )
            appendLine(
                "- NARRATIVE COMPOSITION: Do not just place subjects; weave them into the scene. Their bodies should react to the environment, the light, and each other.",
            )
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
     * Genre-dependent elements (environment, mood, atmosphere, texture, color) come from GenreConfig via Remote Config.
     */

    /**
     * THE VISUAL DIRECTOR AGENT.
     *
     * Responsibility: Converts a textual Story Context into a concrete Visual Plan.
     * - Defines the Camera (Angle, Framing).
     * - Defines the Subject (Posing, Action).
     * - Defines the Atmosphere (Lighting, Mood).
     *
     * LOGIC:
     * - IF ImageType == ICON/PORTRAIT: Forces strict "Headshot/Bust" framing to ensure UI consistency.
     * - IF ImageType == COVER/SCENE: Grants "Cinematic Freedom" to choose the best angle for the narrative (Wide, Dutch, Close-up, etc.).
     */
    @Suppress("ktlint:standard:max-line-length")
    fun generateDirectorialVision(
        genre: Genre,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
        imageConfig: com.ilustris.sagai.core.ai.model.ImageConfig,
        context: String,
        imageType: ImageType,
    ) = buildString {
        val imageTypeLabel = imageType.name.replace("_", " ")

        appendLine("ROLE: The Visionary Film Director. You are scouting the perfect shot for a $imageTypeLabel.")
        appendLine("GENRE: ${genre.name} (The visual laws of this world).")
        appendLine()

        appendLine("INPUT CONTEXT (The Story/Subject):")
        appendLine("\"$context\"")
        appendLine()

        appendLine("TASK: Create a 12-point VISUAL BRIEF for the Artist.")
        appendLine("Your goal is to translate the emotional/narrative context into concrete visual instructions (Camera, Light, Pose).")
        appendLine(
            "CRITICAL character fidelity: You MUST reference the '#### SUBJECTS DETAILS' section in the context. Use unique character elements (accessories, weapons, traits) as compositional anchors.",
        )
        appendLine()

        appendLine("--- IMAGE TYPE RULES ($imageTypeLabel) ---")
        val typeDescription = imageConfig.typeDescriptions[imageType.name]
        if (typeDescription != null) {
            appendLine(typeDescription)
        }
        appendLine("1. NO GRAPHICAL ELEMENTS: Strictly FORBIDDEN to mention text, HUDs, logos, borders, or UI frames in your visual plan.")
        appendLine("2. FULL-BLEED: The plan must imply an edge-to-edge cinematic frame.")
        if (imageType == ImageType.ICON || imageType == ImageType.CHARACTER) {
            appendLine("3. ICON/PORTRAIT STRICTNESS: Framing MUST be 'Close-Up' / 'Chest-Up'. Focus on the character's face.")
            appendLine()
            appendLine("⚠️⚠️⚠️ CANDID MOMENT DIRECTIVE (READ CAREFULLY) ⚠️⚠️⚠️")
            appendLine("This is the MOST IMPORTANT rule for this image type.")
            appendLine("The BODY_LANGUAGE_INTENTION parameter MUST describe a SPECIFIC candid micro-action.")
            appendLine("Think of the subject as being captured by a hidden photographer in their real life.")
            appendLine(
                "GOOD examples: 'glancing sideways mid-conversation', 'caught mid-laugh with eyes crinkling', 'looking down adjusting a glove', 'turning back over shoulder with wind in hair', 'leaning forward with curious eyes', 'exhaling with a half-smirk'.",
            )
            appendLine(
                "FORBIDDEN: 'standing confidently', 'looking at viewer', 'neutral expression', 'arms crossed facing forward', or any pose that could work as a document photo or mugshot.",
            )
            appendLine(
                "The ORIENTATION must NEVER be a flat, symmetrical front-facing pose. Favor 3/4 angles, over-the-shoulder, slight tilts, or profile views.",
            )
            appendLine("⚠️⚠️⚠️ END CANDID DIRECTIVE ⚠️⚠️⚠️")
        }
        appendLine("------------------------------------------")
        appendLine()

        appendLine("GENRE ATMOSPHERE & CREATIVITY (${genre.name}):")
        appendLine(config.conversationDirective) // Use the tone to guide the director
        appendLine(
            "- BEYOND CLICHÉS: Explore the full breadth of the genre. Do not default to 'rain-slicked' or common tropes unless essential. Think about unique environments (e.g. industrial hush, oppressive heat, morning fog, or high-tech sterilization).",
        )
        appendLine(
            "- PALETTE GUIDANCE: Explicitly guide the colors to match the ${genre.name} aesthetic (e.g. 'high-contrast teals and oranges', 'monochrome with single crimson accent', or 'warm sepia grit').",
        )
        appendLine("- Ensure the Lighting and Mood match this genre's soul.")
        appendLine()

        if (imageConfig.directorialVisionRules.isNotBlank()) {
            appendLine(imageConfig.directorialVisionRules)
            appendLine()
        }

        appendLine("OUTPUT FORMAT (13 Parameters - Numbered List):")
        appendLine("1. ORIENTATION: TO_CAMERA (Front/3-4) or SCENE_RELATIVE (Profile/Back).")
        appendLine(
            "2. FRAMING_LEVEL: [Answer based on Mode constraints above]. Be explicit about the exact crop (e.g. 'Close-up, focusing on the face and upper torso').",
        )
        appendLine("3. SUBJECT_PROXIMITY: How close is the viewer's soul to the subject?")
        appendLine("4. VISIBLE_CONTENT: Precise list of what is in frame (e.g. 'Head, Collar, Pendant').")
        appendLine("5. VIEWPOINT: [Eye-Level / Low-Angle (Heroic) / High-Angle (Vulnerable)].")
        appendLine(
            "6. COMPOSITION_PLACEMENT: [Center / Rule of Thirds / Negative Space]. Specify if a subject detail (e.g. 'hilt' or 'pendant') anchors a specific corner.",
        )
        appendLine("7. BODY_LANGUAGE_INTENTION: The verb of the body (e.g. 'Recoiling', 'Dominating'). Emphasize READINESS FOR ACTION.")
        appendLine("8. EMOTIONAL_PROJECTION: The feeling the viewer receives.")
        appendLine("9. LIGHTING_ATMOSPHERE: The dramatic lighting setup (e.g. 'Rembrandt', 'Harsh Neon', 'Silhouette').")
        appendLine("10. COLOR_PALETTE_GUIDE: Specific hex codes or color names matching the genre's aesthetic.")
        appendLine("11. NARRATIVE_DNA: One sentence summary of the shot's story.")
        appendLine(
            "12. ENVIRONMENTAL_CUES: Specific, concrete details that add depth (e.g. 'faint hum of servers', 'exposed cooling pipes', 'flickering interface glow').",
        )
        appendLine("13. FORBIDDEN_CONTENT: What must NOT be seen (based on framing). STRICTLY enforce Portrait rules if applicable.")
        appendLine()
        appendLine("OUTPUT: The 13 numbered points only.")
    }

    fun generateArtistPrompt(
        genre: Genre,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
        imageConfig: com.ilustris.sagai.core.ai.model.ImageConfig,
        visualDirection: String?,
        context: String,
    ): String =
        buildString {
            appendLine("ROLE: The Visionary Concept Artist (The Soul).")
            appendLine(
                "GOAL: To breathe life, emotion, and atmosphere into the scene. Focus on REINFORCING THE CORE GENRE ELEMENTS (Drama, Tension, Aesthetic Soul).",
            )
            appendLine()
            appendLine("THEME & SOUL: ${config.artStyle}")
            appendLine()
            appendLine("NARRATIVE CONTEXT: \"$context\"")
            appendLine("DIRECTOR'S VISION: \"$visualDirection\"")
            appendLine()

            if (imageConfig.artistPromptRules.isNotBlank()) {
                appendLine(imageConfig.artistPromptRules)
                appendLine()
            }

            appendLine(
                "1. NO INTRODUCTIONS (STRICT): Open IMMEDIATELY with a concrete visual detail. Avoid useless atmospheric entry text.",
            )
            appendLine(
                "2. NOIR & TENSION: Inject a sense of dynamic tension. ",
            )
            appendLine(
                "3. SHOW, DON'T TELL (EMOTION): Do NOT say a character is 'anxious' or 'determined'. Describe the physical manifestation: the 'tightening of a jawline', 'unwavering narrow gaze', or the 'white-knuckled grip on a hilt'.",
            )
            appendLine(
                "4. CONCRETE OVER POETIC: Avoid overly flowery language that doesn't define the visual. If you mention 'heat', describe the 'mirage-like vibration of the air' or 'visible steam rising from pipes'. Be a describer, not a poet.",
            )
            appendLine(
                "5. ACTION READINESS: The subject should feel 'alive' and ready to move. Describe the 'coil of muscles' or the 'forward lean of a body about to launch'.",
            )
            appendLine(
                "6. ABSTRACTION OVER TECHNICALITY: Do not describe 'pixels', 'brush strokes', or 'camera settings'. Those are handled by the Rendering Engine.",
            )
            val isComicGenre =
                genre == Genre.HEROES || genre == Genre.CYBERPUNK || genre == Genre.PUNK_ROCK
            if (isComicGenre) {
                appendLine(
                    "   - STYLISTIC PURITY: Do NOT use photography terms like 'depth of field', 'bokeh', 'blurred background', or 'soft focus'. Comics use HARD SHADOWS and INK to define depth, not lens blur.",
                )
            }
            appendLine(
                "7. FULL-BLEED ARTISTRY: Strictly describe the artwork as filling the entire frame. No borders or multi-scene layouts.",
            )
            appendLine(
                "8. ABSOLUTE CHARACTER FIDELITY: You MUST explicitly describe the physical traits of ALL characters from the context (hair, eyes, skin, attire).",
            )
            appendLine(
                "9. ENVIRONMENTAL SYNERGY: Use the Director's 'ENVIRONMENTAL_CUES' and 'COLOR_PALETTE_GUIDE' to bind the subject to the world.",
            )
            appendLine(
                "10. CANDID SOUL (CRITICAL): Capture them in a moment of incidental action or quiet, unobserved reality—never a photoshoot pose.",
            )
            appendLine(
                "    - You MUST describe a SPECIFIC physical micro-action the subject is performing (e.g., 'chin tilted up as wind catches loose strands of hair', 'fingers mid-reach for a pendant at their collarbone', 'eyes narrowed with a knowing half-smile directed at something off-frame').",
            )
            appendLine(
                "    - The subject must feel ALIVE and UNAWARE of the viewer. If their pose could work as a passport photo, you have FAILED.",
            )
            appendLine(
                "    - BANNED POSES: neutral forward stare, arms at sides, symmetrical front-facing stance, 'looking directly at camera', 'standing tall', 'arms crossed'. These are DEAD compositions.",
            )
            appendLine()
            appendLine(
                "9. STRICT NO-META RULE: Do NOT use preachy or philosophical commentary about what the image 'represents'. BANNED: 'This isn't a portrait...'.",
            )
            appendLine()
            appendLine(
                "OUTPUT FORMAT: A single, rich, cohesive paragraph. NO preambles. Start with the most impactful visual detail.",
            )
        }

    /**
     * TOKEN-OPTIMIZED: Validates prompt against 10 core parameters + genre rules.
     */
    fun reviewImagePrompt(
        visualDirection: String?,
        config: com.ilustris.sagai.core.ai.model.GenreConfig,
        imageConfig: com.ilustris.sagai.core.ai.model.ImageConfig,
        finalPrompt: String,
        genre: Genre = Genre.CYBERPUNK,
        context: String,
    ) = buildString {
        appendLine(config.reviewerStrictness.description)
        appendLine()
        appendLine(
            "ROLE: The Master Critique & Supportive Mentor. Your goal is to refine the final prompt to perfection while identifying exactly where the pipeline can improve.",
        )
        appendLine()

        if (imageConfig.reviewerScopes.isNotBlank()) {
            appendLine(imageConfig.reviewerScopes)
            appendLine()
        }

        // Genre-critical validation at TOP
        appendLine(genreCriticalValidation(genre, config))

        appendLine(
            "TASK: Validate the prompt for extreme technical alignment. Ensure the Artist has not deviated from the Genre's core DNA.",
        )
        appendLine()

        appendLine("=== SCOPE 1: DIRECTORIAL COMPLIANCE (The 'How' & 'Where') ===")
        appendLine("Validation for the VISUAL DIRECTOR logic:")
        appendLine("1. ORIENTATION & PERSPECTIVE: Does the Body/Head/Gaze match the extracted direction?")
        appendLine("2. CONTENT HALLUCINATION (STRICT): Check against 'FORBIDDEN_CONTENT'.")
        appendLine("   - If an element is in the forbidden list, it MUST NOT appear in the prompt.")
        appendLine("   - Describing forbidden parts = HALLUCINATION_VIOLATION.")
        appendLine(
            "3. FRAMING & PROXIMITY STRICTNESS: Are only permitted elements described? (e.g. If 'Portrait', legs must NOT be described).",
        )
        appendLine("   - VIOLATION: VISIBILITY_VIOLATION, HALLUCINATION_VIOLATION.")
        appendLine("4. CONTEXT FIDELITY (STRICT PHYSICAL CHECK): Does the artwork respect the provided context?")
        appendLine("   - Context: \"$context\"")
        appendLine(
            "   - MANDATORY: Cross-reference the description with the '#### SUBJECTS DETAILS' in the context.",
        )
        appendLine(
            "   - CHECK: Is hair color correct? Are eye colors mentioned? Are specific outfits/accessories present?",
        )
        appendLine(
            "   - VIOLATION: If a character has 'blue eyes' in the context but the prompt says 'dark eyes' or omits it → CHARACTER_TRAIT_MISMATCH.",
        )
        appendLine(
            "   - ACTION: You MUST inject the exact missing traits (hair color, eye color, scars, tattoos, specific clothes) into the 'correctedPrompt'.",
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

        appendLine("=== SCOPE 2: SCENE NATURALITY & COHERENCE (The 'Why' & 'Feel') ===")
        appendLine("This is NOT a technical keyword checklist. Validate the ORGANIC feel of the scene.")
        appendLine()
        appendLine("1. ORGANIC LIGHTING (CRITICAL):")
        appendLine("   - Does the lighting feel like a natural part of the scene, or is it 'hard-injected' like a digital effect?")
        appendLine("   - FORBIDDEN: Prompts that describe light bars, neon streaks, or lens flares crossing the character's face.")
        appendLine("   - If lighting feels like 'digital overlays on a photo', it is a violation.")
        appendLine("   - GOOD: 'Warm streetlight glow' or 'rim-lit by the city neon'. BAD: 'A blue light stripe crossing the eyes'.")
        appendLine("   - VIOLATION: ARTIFICIAL_LIGHTING_INJECTION.")
        appendLine()
        appendLine("2. CHARACTER-SCENE FUSION & CANDID SOUL (⚠️ CRITICAL FOR ICONS/PORTRAITS):")
        appendLine("   - Does the character feel like they BELONG in the environment, or are they 'pasted' into it?")
        appendLine("   - The character's body language and positioning should react to the world around them.")
        appendLine(
            "   - ANTI-POSING (CRITICAL for ICONS/PORTRAITS): Does the character look like they are 'posing for a camera'? (e.g., staring directly at the viewer with a studio-neutral pose).",
        )
        appendLine("   - CANDID MICRO-ACTION CHECK: Does the prompt describe a SPECIFIC physical action or gesture?")
        appendLine(
            "     - PASS examples: 'glancing over shoulder', 'caught mid-laugh', 'adjusting collar', 'wind in hair while looking away', 'leaning forward mid-thought'.",
        )
        appendLine(
            "     - FAIL examples: 'standing confidently', 'looking at the viewer', 'neutral expression', 'arms crossed', 'facing forward'. These produce DEAD document-photo compositions.",
        )
        appendLine("     - If the described pose could work as a passport photo, ID badge, or mugshot → CRITICAL VIOLATION.")
        appendLine("   - FAVOR: Incidental actions, mid-gesture moments, or unobserved quiet reality that reflects their personality.")
        appendLine("   - VIOLATION: SUBJECT_SCENE_DISCONNECT, ARTIFICIAL_POSING, STATIC_PORTRAIT.")
        appendLine()
        appendLine("3. ENVIRONMENT COHERENCE & CREATIVITY (CRITICAL):")
        appendLine("   - Is the environment described, or is it vague/missing?")
        appendLine(
            "   - Does the environment match the narrative context? (e.g., A chase scene should describe the alleyway, not just 'a dark background').",
        )
        appendLine(
            "   - ANTI-TROPE CHECK: Does the prompt start with a generic cliché like 'The air hangs thick...' or 'rain-slicked asphalt' when the story context doesn't mention weather?",
        )
        appendLine("   - VIOLATION: ENVIRONMENT_VAGUE, CONTEXT_MISMATCH, REPETITIVE_TROPE.")
        appendLine("   - ACTION: If a trope is used, replace it with a unique detail derived from the story context.")
        appendLine()
        appendLine("4. META-CONTENT & BOT-SPEAK (CRITICAL):")
        appendLine("   - Does the prompt include philosophical meta-commentary like 'This isn't a portrait, it's a reckoning'?")
        appendLine("   - Does it attempt to 'explain' the meaning of the image to the viewer?")
        appendLine("   - VIOLATION: META_COMMENTARY_VIOLATION, AI_BOT_SPEAK.")
        appendLine("   - ACTION: Strip out all non-visual narrative flourishes. Keep ONLY the sensory and descriptive details.")
        appendLine()
        appendLine("5. GENRE SOUL (CONTRADICTION CHECK ONLY):")

        appendLine("=== SCOPE 3: PRESENTATION & FINAL POLISH (The 'Canvas') ===")
        appendLine("1. FULL-BLEED ARTWORK: Strictly ban any mention of borders, frames, gutters, comic panels, or multi-panel layouts.")
        appendLine("2. NO TEXT/UI: No text, logos, or digital overlays.")
        appendLine("   - VIOLATION: GRAPHICAL_CONTAMINATION, BORDER_VIOLATION, PANEL_VIOLATION.")
        appendLine("   - AUTO-FIX: KILL any mention of borders/panels. Ensure the description implies a single edge-to-edge frame.")
        appendLine()

        visualDirection?.let {
            appendLine("**ORIGINAL DIRECTOR'S VISION:** \"$it\"")
            appendLine()
        }

        appendLine("**GENRE MANDATES:**")
        appendLine(config.getValidationRules(genre.name, getHexPalette(genre)))
        appendLine()

        appendLine("SUPPORTIVE AUTO-FIX PHILOSOPHY:")
        appendLine("- If Directorial Error: Re-align with the vision WITHOUT stripping the artist's poetic language.")
        appendLine("- If Artistic Error: Inject the missing genre soul using concrete visual nouns that help AI generation.")
        appendLine("- If Banned Elements: Gracefully remove them while enhancing the remaining visible details.")
        appendLine()

        appendLine("⚠️ MANDATORY FEEDBACK (NEVER NULL):")
        appendLine(
            "- artistImprovementSuggestions (THE ART MENTOR): Cross-reference the description with the 'RENDERING INSTRUCTIONS'. Suggest how the Artist can use better visual imagery that complements the medium (e.g. 'Instead of soft shadows, use the rendering's spot-black shapes to define depth').",
        )
        appendLine(
            "- visualDirectorSuggestions (THE FILM MENTOR): How the Director can better command the scene, color palette, and environmental variety.",
        )
        appendLine(
            "- renderingSuggestions (THE TECHNICAL MENTOR): Scrutinize the 'RENDERING INSTRUCTIONS' themselves. Suggest how we could improve the technical recipe (grit, texture, ink weight, palette) to better capture the intended aesthetic.",
        )
        appendLine()

        appendLine("OUTPUT JSON (ImagePromptReview structure):")
        appendLine("- originalPrompt: input prompt")
        appendLine("- correctedPrompt: fixed masterful prompt")
        appendLine("- violations[]: {type, severity, description, example}")
        appendLine("- artistImprovementSuggestions: Mentorship to improve style and technical alignment.")
        appendLine("- visualDirectorSuggestions: Mentorship to improve composition and environmental scouting.")
        appendLine("- renderingSuggestions: Mentorship to improve the technical rendering recipe.")
        appendLine("- wasModified: boolean")
        appendLine()
        appendLine("PROMPT TO SCRUTINIZE:")
        appendLine(finalPrompt)
    }
}
