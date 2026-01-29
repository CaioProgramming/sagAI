package com.ilustris.sagai.core.ai.prompts

import androidx.compose.ui.graphics.toArgb
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.COWBOY
import com.ilustris.sagai.features.newsaga.data.model.Genre.CRIME
import com.ilustris.sagai.features.newsaga.data.model.Genre.CYBERPUNK
import com.ilustris.sagai.features.newsaga.data.model.Genre.FANTASY
import com.ilustris.sagai.features.newsaga.data.model.Genre.HEROES
import com.ilustris.sagai.features.newsaga.data.model.Genre.HORROR
import com.ilustris.sagai.features.newsaga.data.model.Genre.PUNK_ROCK
import com.ilustris.sagai.features.newsaga.data.model.Genre.SHINOBI
import com.ilustris.sagai.features.newsaga.data.model.Genre.SPACE_OPERA
import com.ilustris.sagai.features.newsaga.data.model.colorPalette

object GenrePrompts {
    fun artStyle(genre: Genre) =
        when (genre) {
            FANTASY -> {
                buildString {
                    appendLine("Art Technique: HIGH BAROQUE OIL (Tenebrism style).")
                    appendLine(
                        "Tracing: Soft, blended edges with no visible outlines. Form is defined purely by light and shadow transitions.",
                    )
                    appendLine(
                        "Shadowing: Dramatic Chiaroscuro with deep, near-black voids (Tenebrism) contrasting with luminous focal points.",
                    )
                    appendLine(
                        "Lighting: Grandiose, 'divine' volumetric light beams. Translucent glazing layers create a 'glow from within' on skin.",
                    )
                    appendLine("Anatomy: Grandiose and idealized proportions. Physical forms follow classical Greek sculpture aesthetics.")
                    appendLine("Palette: CRIMSON RED dominance with RADIANT GOLD reflective highlights.")
                }
            }

            CYBERPUNK -> {
                buildString {
                    appendLine(
                        "Art Technique: VINTAGE 80s/90s CEL ANIMATION (Bubblegum Crisis/Akira style), Analog Aesthetic, Hand-Painted Backgrounds.")
                    appendLine(
                        "Tracing: HAND-DRAWN CEL INK, Organic Line Weight, Slight Bleed, Not Vector-Perfect, Traditional Animation Acetate.",
                    )
                    appendLine(
                        "Shadowing: HARD CEL SHADING, High Contrast, No Soft Gradients, Solid Black Shadows, Dramatic Noir-like Contrast.",
                    )
                    appendLine(
                        "Lighting: RETRO HALATION, Analog Bloom, Harsh Neon Backlight, 'Ghosting' effects, No Modern Digital Softness.",
                    )
                    appendLine(
                        "Anatomy: RETRO ANIME REALISM, Expressive Eyes (Not Moe), Sharp Chins, Detailed Mechanical Gaskets, 'Hard' Character Design.",
                    )
                    appendLine(
                        "Palette: DARK INDUSTRIAL, Saturated Reds and Blues, Deep Blacks, Muted Background Tones vs Glowing Foregrounds.",
                    )
                    appendLine("Filters: HEAVY FILM GRAIN, VHS Noise, Chromatic Aberration, Scanlines, Analog Tape Saturation.")
                }
            }

            HORROR -> {
                buildString {
                    appendLine("Art Technique: 32-BIT RETRO 3D (Fixed-Camera Horror style).")
                    appendLine("Tracing: Low-polygon silhouettes with visible vertex-jitter. Edges are jagged and dithered.")
                    appendLine(
                        "Shadowing: Dithered gradient patterns (checkerboard pixel dots) to simulate shadows. Heavy use of 'Z-buffer' depth fog.",
                    )
                    appendLine("Lighting: Flashlight-driven 'cone' lighting. Low-ambient darkness with high-contrast 'hot spots'.")
                    appendLine("Anatomy: Blocky, simplified forms with dithered skin textures. Faces have limited, 'drawn-on' expressions.")
                    appendLine("Palette: Limited 256-color range. Dominated by dark cyans, dull grays, and pale cerulean.")
                }
            }

            HEROES -> {
                buildString {
                    appendLine("Art Technique: TEXTURED HAND-DRAWN NOIR (Ink & Charcoal on Rough Paper).")
                    appendLine(
                        "Tracing: UNPREDICTABLE and organic. Visible paper grain, uneven ink bleed, and scratchy charcoal textures. Avoid perfect, smooth digital lines.",
                    )
                    appendLine(
                        "Shadowing: Deep, velvety spot-blacks applied with a dry brush. Shadows must feel physical and tactile, not flat or gradient-based.",
                    )
                    appendLine(
                        "Lighting: Naturalistic Character Lighting using warm/neutral tones to reveal skin texture. Shadows are absolute noir blacks.",
                    )
                    appendLine("Anatomy: 100% GROUNDED REALISM. Humans look like humans (pores, wrinkles, weight). Avoid plastic/doll-like smoothness.",
                    )
                    appendLine(
                        "Environment: SHARP NARRATIVE URBANISM. Backgrounds must be in focus, textured, and detailed (neon signs, brickwork, fire escapes). NO blur/bokeh.",
                    )
                    appendLine(
                        "Palette: NATURAL REALISTIC COLORS for skin/clothes. ELECTRIC BLUE is strictly a TINY ACCENT (eye glint, device LED) and NEVER a scene filter.",
                    )
                }
            }

            CRIME -> {
                buildString {
                    appendLine("Art Technique: RENAISSANCE-MODERN FUSION (Art Deco Noir).")
                    appendLine(
                        "Tracing: Polished, seamless transitions. Outlines are nearly invisible, with forms defined by subtle shifts in skin temperature and material sheen.",
                    )
                    appendLine(
                        "Shadowing: Mastery of 'High-Society Chiaroscuro'. Shadows are deep navy, rich umber, or champagne-gray—never 'dirty' or gritty.",
                    )
                    appendLine(
                        "Lighting: Saturated 'Golden Hour' luxury glow. Volumetric light reflecting off marble, silk, and turquoise water. High-gloss specular highlights on gold jewelry.",
                    )
                    appendLine(
                        "Anatomy: 'Divine Perfection'. Statuesque proportions with a soft, marble-like skin luster. Posing is relaxed, candid, and untouchably elegant.",
                    )
                    appendLine(
                        "Palette: Noble pigments (Navy, Champagne, Gold) with HOT PINK (#E91E63) strictly as sharp localized highlights (neon, drink, or silk detail).",
                    )
                }
            }

            SHINOBI -> {
                buildString {
                    appendLine("Art Technique: BRUTAL ZEN SUMI-E (Aggressive Ink Wash).")
                    appendLine("Tracing: Jagged, impulsive, and sharp ink-brush strokes. Excessive use of 'flying white' (dry brush) and chaotic ink splatters.",
                    )
                    appendLine(
                        "Shadowing: High-contrast heavy black ink pools. Shadows are brutal, jagged shapes that carve into the form. NO soft gradients.",
                    )
                    appendLine("Lighting: Harsh high-contrast negative space. Light feels like it's 'slashing' through the black ink.")
                    appendLine(
                        "Anatomy: Harsh, hard, and weathered. Sharp realistic features with an intense, menacing gaze. NO anime-eyes or soft features.",
                    )
                    appendLine(
                        "Palette: Pure high-contrast Carbon Black on raw washi paper. SINGLE VIBRANT CRIMSON RED accent strictly for MINUTE DETAILS (eyes, a blade’s edge, or a single drop of blood) to ensure the scene remains 99% monochrome.",
                    )
                }
            }

            SPACE_OPERA -> {
                buildString {
                    appendLine("Art Technique: MID-CENTURY SCI-FI GOUACHE.")
                    appendLine("Tracing: Fine, precise structural outlines for vessels. Soft, blended edges for cosmic phenomena.")
                    appendLine("Shadowing: Smooth airbrushed gradients. Shadows are deep indigos with warm ambient light reflections.")
                    appendLine("Lighting: Technicolor-saturated glow. Bright, blooming 'hot-spots' from engines and stars.")
                    appendLine("Anatomy: Heroic, barrel-chested proportions. Skin has a smooth, painterly gouache finish (board painting).")
                    appendLine("Palette: Cosmic indigos and purples with 'ROCKET ORANGE' combustion flares.")
                }
            }

            COWBOY -> {
                buildString {
                    appendLine("Art Technique: ALLA PRIMA FRONTIER OIL.")
                    appendLine("Tracing: No outlines. Form is defined by thick, confidence-driven brush loads (Impasto).")
                    appendLine("Shadowing: Earth-tone shadows using raw umber. Deepest tones in the folds of leather and weathered skin.")
                    appendLine("Lighting: Harsh, unidirectional 'Plein Air' desert sun. Volumetric dust motes caught in golden hour light.")
                    appendLine("Anatomy: Rugged, lean, and weathered proportions. Focus on visible tendon texture and sun-damaged skin.")
                    appendLine("Palette: Raw ochre and burnt sienna with 'SUNSET GOLD' Chiaroscuro.")
                }
            }

            PUNK_ROCK -> {
                buildString {
                    appendLine("Art Technique: UNDERGROUND 2D ZINE.")
                    appendLine("Tracing: Sketchy, high-energy ink outlines. Intentional ink-blooms and 'messy' line overlaps.")
                    appendLine("Shadowing: Mechanical halftone dot patterns and coarse cross-hatching. Flat color blocks with no gradients.")
                    appendLine("Lighting: Flat, high-contrast values. Occasional 'vibrant radioactive' neon bloom effects.")
                    appendLine("Anatomy: Lanky, exaggerated, and anti-establishment proportions. Stylized negative-space Void eyes.")
                    appendLine("Palette: Urban slate and olive with TOXIC YELLOW chemical accents.")
                }
            }
        }

    fun appearanceGuidelines(genre: Genre): String =
        when (genre) {
            FANTASY -> {
                buildString {
                    appendLine("Attire: RENAISSANCE ELEGANCE. Flowing silk robes, velvet tunics with gold embroidery, and classical togas.")
                    appendLine("Details: Golden amulets, jeweled circlets, elegant sashes. Focus on LUMINOUS fabrics and divine grace.")
                    appendLine("Forbidden: Heavy armor, gritty leather, dark combat gear. Prioritize beauty and ethereal mysticism.")
                }.trimIndent()
            }

            CYBERPUNK -> {
                buildString {
                    appendLine(
                        "Mandatory Cyberware: Every character MUST have visible chrome. Neural ports, mechanical spines, or synthetic eyes.",
                    )
                    appendLine(
                        "Style: Y2K Retro-Futurism meets Neo-Tokyo. Layered techwear, oversized bomber jackets, metallic fabrics, and cargo pants.",
                    )
                    appendLine(
                        "Aura: Uncanny Valley. Machine parts designed to mimic human anatomy but revealing their cold metallic nature through visible seams.",
                    )
                    appendLine("Details: LED-embedded fabrics, tactical harnesses, wraparound visors, and personalized tech wear-and-tear.")
                }.trimIndent()
            }

            HORROR -> {
                buildString {
                    appendLine("Attire: DISTRESSED & HAUNTED. Worn, tattered clothing in muted blues and ash grays.")
                    appendLine(
                        "Style: Retro PS1-era survival horror aesthetic. Faded uniforms, oversized coats, and vintage, unsettling garments.",
                    )
                    appendLine(
                        "Details: Visible wear, dirt, and age. Appearance should evoke mystery, isolation, and creeping psychological dread.",
                    )
                }.trimIndent()
            }

            HEROES -> {
                buildString {
                    appendLine("Attire: MODERN SUPERSUITS. Kinetic materials, sleek textures, and high-tech urban armor.")
                    appendLine("Style: Bold ink lines and iconic emblems. Clean silhouettes that emphasize power and determined movement.")
                    appendLine(
                        "Details: Practical accessories (utility belts, tech-gauntlets) that reflect their specific powers. No generic outfits.",
                    )
                }.trimIndent()
            }

            CRIME -> {
                buildString {
                    appendLine("Attire: ELITE LUXURY RESORT. Tailored linen, flowing silk, and high-end designer swimwear.")
                    appendLine("Style: Art Deco Glamour. Statement gold jewelry, oversized sunglasses, and luxury watches.")
                    appendLine("Aura: Serene Superiority. Effortless elegance that radiates untouchable wealth and coastal sophistication.")
                }.trimIndent()
            }

            SHINOBI -> {
                buildString {
                    appendLine("Attire: FEUDAL TRADITION. Layered monochrome robes, hakama pants, and tabi boots.")
                    appendLine("Style: Sumi-e Ink Wash aesthetic. Rough textures, hemp fabrics, and functional armor plates.")
                    appendLine(
                        "Details: Hidden weapons, combat sashes, and a single crimson red accent. Focus on stealth and agile silhouettes.",
                    )
                }.trimIndent()
            }

            SPACE_OPERA -> {
                buildString {
                    appendLine("Attire: RAYGUN GOTHIC. Sleek retro-futuristic uniforms, metallic jumpsuits, and dramatic capes.")
                    appendLine("Style: 1950s Atomic Age illustration. Bright, clean colors and polished chrome accents.")
                    appendLine("Aura: Cosmic Adventure. Optimistic, dashing silhouettes prepared for interstellar discovery.")
                }.trimIndent()
            }

            COWBOY -> {
                buildString {
                    appendLine("Attire: FRONTIER RUGGEDNESS. Weathered leather, raw denim, and heavy canvas.")
                    appendLine("Style: Expressive Western Oil Painting. Thick textures, dust-coated fabrics, and rugged layering.")
                    appendLine("Details: Wide-brimmed hats, spurs, and well-worn holsters. Appearance should feel lived-in and stoic.")
                }.trimIndent()
            }

            PUNK_ROCK -> {
                buildString {
                    appendLine("Attire: ANARCHIC STREETWEAR. Ripped tees, leather jackets, and heavy combat boots.")
                    appendLine("Style: 2D Cartoon/Gorillaz aesthetic. Exaggerated, lanky, or angular silhouettes with bold ink outlines.")
                    appendLine(
                        "Details: Creative stylized eyes (Voids), acid green accents, and a defiant, 'ugly-cute' personality-driven design.",
                    )
                }.trimIndent()
            }
        }

    fun nameDirectives(genre: Genre) =
        when (genre) {
            FANTASY -> "Lyrical and noble names inspired by Renaissance and classical mythology (e.g., Aurelius, Seraphina, Valerius, Lysandra)."
            CYBERPUNK -> "Hard-edged tech names, street nicknames, and corporate surnames (e.g., Jax, Kestrel, Case, Tanaka, Arasaka)."
            HORROR -> "Mundane, unsettling, or archaic names that evoke a sense of history or psychological unease."
            HEROES -> "Modern, punchy city names and iconic aliases (e.g., Miles, Riley, Titan, Ghost)."
            CRIME -> "Sophisticated high-society names and sleek aliases (e.g., Sebastian, Vittoria, The Contessa)."
            SHINOBI -> "Traditional Japanese names and clan-based surnames (e.g., Hanzo, Kaguya, Hattori)."
            SPACE_OPERA -> "Adventurous, melodic 1950s sci-fi names and cosmic titles (e.g., Commander Starling, Dr. Orion)."
            COWBOY -> "Rugged frontier names and grit-filled nicknames (e.g., Silas, West, 'Lefty' Miller)."
            PUNK_ROCK -> "Rebellious stage names, energetic nicknames, and band-culture monikers (e.g., Sid, Roxy, 'The Bassist')."
        }

    fun conversationDirective(genre: Genre) =
        when (genre) {
            FANTASY -> {
                buildString {
                    appendLine("Tone: Ethereal Renaissance. Poetic, noble, and graceful.")
                    appendLine("Vocabulary: Classical elegance ('divine', 'celestial'). Lyrical metaphors from nature and heavens.")
                    appendLine("Voice: Luminous and painterly descriptions focusing on crimson/gold beauty. No profanity.")
                }
            }

            CYBERPUNK -> {
                buildString {
                    appendLine("Tone: Gritty & Cynical. World-weary street-smart vibe.")
                    appendLine("Vocabulary: Tech-jargon ('chrome', 'flatline') and corporate/street argot.")
                    appendLine("Voice: Edginess meeting the 'Beauty of Decay'. Focus on the humanity-tech fusion.")
                }
            }

            HORROR -> {
                buildString {
                    appendLine("Tone: Psychological Dread. Tense, paranoid, and unsettling.")
                    appendLine("Vocabulary: Mundane vs. Sinister contrast. Subtle hints of the inexplicable.")
                    appendLine("Voice: Slow-building suspense, focusing on reality distorting and the unknown.")
                }
            }

            HEROES -> {
                buildString {
                    appendLine("Tone: Dynamic & Larger-than-life. Modern comic book energy.")
                    appendLine("Vocabulary: Street-smart urban jargon. Emphasis on hidden potential.")
                    appendLine("Voice: Urgent pacing, vivid urban scale, and verticality. High-stakes city rhythm.")
                }
            }

            CRIME -> {
                buildString {
                    appendLine("Tone: Elegant & Sophisticated. 'Serene Superiority' and 80s high-society.")
                    appendLine("Vocabulary: Luxury, Art Deco glamour, and high-end criminal/legal terms.")
                    appendLine("Voice: Calm, articulate, and dangerously polite. 80s pop-neon mystery.")
                }
            }

            SHINOBI -> {
                buildString {
                    appendLine("Tone: Minimalist & Disciplined. Feudal drama and underlying tension.")
                    appendLine("Vocabulary: Martial tradition, honor, and espionage terms.")
                    appendLine("Voice: Concise, deliberate dialogue. Focus on atmosphere (bamboo, blades, silence).")
                }
            }

            SPACE_OPERA -> {
                buildString {
                    appendLine("Tone: Awe-inspiring & Optimistic. 1950s Atomic Age wonder.")
                    appendLine("Vocabulary: Galactic exploration and philosophical cosmicponderings.")
                    appendLine("Voice: Aspirational and epic. The majesty of the infinite cosmos.")
                }
            }

            COWBOY -> {
                buildString {
                    appendLine("Tone: Laconic & Stoic. Rugged frontier grit.")
                    appendLine("Vocabulary: Western slang ('reckon', 'drawl') and earthy idioms.")
                    appendLine("Voice: Sparse, tough-minded dialogue. Focus on sensory frontier details (dust, leather).")
                }
            }

            PUNK_ROCK -> {
                buildString {
                    appendLine("Tone: Anarchic & Rebellious. High-energy Gorillaz attitude.")
                    appendLine("Vocabulary: Music subculture jargon and modern street slang.")
                    appendLine("Voice: Fast-paced, irreverent, and authentic. Capturing the exhilaration of music.")
                }
            }
        }

    /**
     * Get the reviewer strictness level for a specific genre.
     * Based on how rigid the art style requirements are.
     */
    fun reviewerStrictness(genre: Genre): com.ilustris.sagai.core.ai.model.ReviewerStrictness =
        when (genre) {
            // STRICT: Cartoon styles with specific anatomical requirements
            PUNK_ROCK -> com.ilustris.sagai.core.ai.model.ReviewerStrictness.STRICT

            HORROR -> com.ilustris.sagai.core.ai.model.ReviewerStrictness.STRICT

            // CONSERVATIVE: Stylized but with some flexibility
            CYBERPUNK -> com.ilustris.sagai.core.ai.model.ReviewerStrictness.CONSERVATIVE

            HEROES -> com.ilustris.sagai.core.ai.model.ReviewerStrictness.STRICT

            SPACE_OPERA -> com.ilustris.sagai.core.ai.model.ReviewerStrictness.CONSERVATIVE

            SHINOBI -> com.ilustris.sagai.core.ai.model.ReviewerStrictness.CONSERVATIVE

            // LENIENT: Traditional art with organic flexibility
            FANTASY -> com.ilustris.sagai.core.ai.model.ReviewerStrictness.LENIENT

            CRIME -> com.ilustris.sagai.core.ai.model.ReviewerStrictness.LENIENT

            COWBOY -> com.ilustris.sagai.core.ai.model.ReviewerStrictness.LENIENT
        }

    /**
     * Extract validation rules from the art style for the reviewer.
     * Provides structured data about what's banned, required, and allowed.
     */
    private fun getHexPalette(genre: Genre): String =
        genre.colorPalette().joinToString(", ") {
            "#%06X".format(0xFFFFFF and it.toArgb())
        }

    fun validationRules(genre: Genre): String =
        when (genre) {
            FANTASY -> {
                val palette = getHexPalette(genre)
                """
                **FANTASY VALIDATION (Reviewer):**
                - Palette: $palette (Crimson Red dominance, Radiant Gold accents).
                - Mandatory: HIGH BAROQUE & ROMANTICISM oil painting. Tenebrism, dramatic Chiaroscuro, and translucent glazing.
                - Anatomy: Grandiose noble proportions with spiritually weighted poses.
                - Banned: Modern digital art, sharp edges, neon colors, or clean game art aesthetics.
                - Fix: Replace with 'dramatic Tenebrism', 'translucent glazing', and 'Baroque grandiose proportions'.
                """.trimIndent()
            }

            CYBERPUNK -> {
                val palette = getHexPalette(genre)
                """
                **CYBERPUNK VALIDATION (Reviewer):**
                - Palette: $palette (Midnight Blue, Electric Cyan).
                - Mandatory Tokens: 'Vintage Cel Animation', 'Analog Film Grain', 'Hard Cel Shading', 'Retro Anime'.
                - Critical Lighting: Must include 'Retro Halation' or 'Harsh Neon'. BANNED: 'Soft Digital Bloom', 'Ethereal Glow'.
                - Critical Shading: Must be 'Hard Cel Shading'. BANNED: 'Soft Gradients', 'Digital Painting'.
                - Anatomy: 'Retro Realistic'.
                - Banned: 'Masterpiece', '4k', 'Digital Art', 'Smooth Lighting', 'Vector Lines', 'Modern Anime'.
                - Fix: Inject 'Vintage Cel Animation', 'Heavy Film Grain', and 'Hard Cel Shading'.
                """.trimIndent()
            }

            HORROR -> {
                val palette = getHexPalette(genre)
                """
                **HORROR VALIDATION (Reviewer):**
                - Palette: $palette (Dark/Pale Blue, Ash Gray accents).
                - Mandatory: 32-BIT RETRO SURVIVAL HORROR. Low-poly blocky shading, dithering, and vertex jitter artifacts.
                - Atmosphere: Psychological unease and low-resolution mystery.
                - Banned: Smooth gradients, high-res textures, vibrant colors, or modern 3D rendering.
                - Fix: Inject 'dithered textures', 'vertex jitter', and 'low-poly blocky shading'.
                """.trimIndent()
            }

            HEROES -> {
                val palette = getHexPalette(genre)
                """
                **HEROES VALIDATION (Reviewer):**
                - Palette: $palette (Natural Skin/Earth Tones + Deep Black). NO BLUE WASH.
                - Mandatory: HAND-DRAWN TEXTURE & SHARP BACKGROUNDS. No blur. Detailed urban props (pharmacy signs, neon, pipes).
                - Anatomy: NATURAL HUMANITY. Real skin texture, weight, and imperfections. No plastic dolls.
                - Accent: Electric Blue is a MICRO-DETAIL only.
                - Banned: Blurred backgrounds, bokeh, blue filters, cyan washing, digital smoothness.
                - Fix: Inject 'sharp detailed cityscape', 'rough paper texture', and 'natural skin tones'.
                """.trimIndent()
            }

            CRIME -> {
                val palette = getHexPalette(genre)
                """
                **CRIME VALIDATION (Reviewer):**
                - Palette: $palette (Noble Pigments, Hot Pink accents).
                - Mandatory: ART DECO NOIR. Polished oil luster, seamless sfumato, and marble-like skin.
                - Anatomy: DIVINE PERFECTION. Posing must be 'untouchably elegant' and candid (not facing camera).
                - Setting: LUXURY RESORT. Tropical coastal elements (palms, turquoise pools, Art Deco architecture).
                - Banned: Grit, dirt, visible thick brushwork, or modern casual street wear.
                - Fix: Inject 'polished marble skin luster', 'seamless sfumato transitions', and 'Art Deco luxury detail'.
                """.trimIndent()
            }

            SHINOBI -> {
                val palette = getHexPalette(genre)
                """
                **SHINOBI VALIDATION (Reviewer):**
                - Palette: $palette (Monochrome B&W, Crimson Red accents).
                - Mandatory: BRUTAL ZEN SUMI-E. Jagged ink strokes, heavy high-contrast black, and chaotic ink splatters.
                - Accent Rule: Crimson Red must be a MINUTE DETAIL (blade edge, eye, or blood drop). It must NOT shift the B&W feel.
                - Anatomy: HARSH & WEATHERED. Sharp realistic features, intense gaze.
                - Banned: Anime eyes, 'cute' aesthetics, soft rounded features, or standard B&W anime shading.
                - Fix: Inject 'aggressive ink splatters', 'jagged black-ink shadows', and 'weathered realistic sharp features'.
                """.trimIndent()
            }

            SPACE_OPERA -> {
                val palette = getHexPalette(genre)
                """
                **SPACE_OPERA VALIDATION (Reviewer):**
                - Palette: $palette (Cosmic Indigo, Rocket Orange accents).
                - Mandatory: MID-CENTURY SCI-FI PULP. Gouache finish, subtle airbrushing, and Technicolor glow.
                - Banned: Modern CGI sci-fi, realistic black space, or terrestrial grounding.
                - Fix: Inject 'gouache board finish', 'Technicolor glow', and 'airbrush gradients'.
                """.trimIndent()
            }

            COWBOY -> {
                val palette = getHexPalette(genre)
                """
                **COWBOY VALIDATION (Reviewer):**
                - Palette: $palette (Earthy Ochre, Sunset Gold accents).
                - Mandatory: TRADITIONAL FRONTIER OIL. Thick impasto, Alla Prima brushwork, and 'plein air' lighting.
                - Banned: Digital smoothness, photorealism, or modern Western aesthetics.
                - Fix: Inject 'thick impasto texture', 'alla prima brushwork', and 'airborne dust motes'.
                """.trimIndent()
            }

            PUNK_ROCK -> {
                val palette = getHexPalette(genre)
                """
                **PUNK_ROCK VALIDATION (Reviewer):**
                - Palette: $palette (Urban Grays, Acid Green accents).
                - Mandatory: UNDERGROUND 2D ZINE ART. Sketchy ink lines, flat colors, offset printing halftone dots.
                - Eyes: Negative-space Voids only.
                - Banned: Any soft shading, realistic eyes, or 3D rendering elements.
                - Fix: Inject 'halftone dot textures', 'bold sketchy ink lines', and 'offset printing artifacts'.
                """.trimIndent()
            }
        }
}
