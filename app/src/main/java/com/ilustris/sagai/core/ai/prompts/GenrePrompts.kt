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
                    appendLine("Art Technique: HIGH BAROQUE & ROMANTICISM (Caravaggio/Waterhouse fusion).")
                    appendLine("Rendering: Tenebrism and dramatic Chiaroscuro. Luminous glazing, soft sfumato, and 'divine' light sources.")
                    appendLine("Anatomy: Grandiose noble proportions. Translucent skin built through traditional oil layering.")
                    appendLine("Palette: CRIMSON RED dominance with RADIANT GOLD accents. Rich umbers and sienna. NO cool tones.")
                    appendLine("Environment: Sublime classical settings (Grand Castles, Enormous Ruins, Rose Groves).")
                    appendLine("Aura: Majestic, ethereal, and spiritually weighted grandeur.")
                }
            }

            CYBERPUNK -> {
                buildString {
                    appendLine("Art Technique: late 80s/early 90s CYBERPUNK OVA (Akira / Ghost in the Shell / Patlabor).")
                    appendLine(
                        "Rendering: HAND-PAINTED CEL aesthetic. Cel-shading (2-step), thick ink lines, analog CRT scanline texture, and laserdisc film grain.",
                    )
                    appendLine(
                        "Cyberware: MANDATORY & INTEGRATED. Mechanical layering following human anatomy with visible seams and hydraulic joints.",
                    )
                    appendLine("Palette: Muted slate blue and industrial steel with DEEP PURPLE accents. Harsh rim lighting.")
                    appendLine(
                        "Environment: Oppressive urban sprawl (neon, rain-slicked pavement, cables) with high-density technological detail.",
                    )
                    appendLine("Aura: Hardened, cynical, and tech-disrupted world-weariness.")
                }
            }

            HORROR -> {
                buildString {
                    appendLine("Art Technique: 32-BIT RETRO SURVIVAL HORROR (PS1/Saturn aesthetic).")
                    appendLine(
                        "Rendering: Low-poly blocky shading, DITHERED textures, and vertex-jitter artifacts. Filtered 240p resolution.",
                    )
                    appendLine("Atmosphere: Creeping dread and psychological unease. Volumetric fog and low-key lighting.")
                    appendLine("Environment: Eerie settings (Abandoned Hospital, Foggy Street) with 3+ specific 'narrative' props.")
                    appendLine("Aura: Unsettling paranoia and low-resolution mystery.")
                }
            }

            HEROES -> {
                buildString {
                    appendLine("Art Technique: MODERN MAINSTREAM COMIC (David Marquez / Pepe Larraz style).")
                    appendLine("Rendering: Sharp clean ink lines, high-definition digital coloring, and cinematic energy bloom for powers.")
                    appendLine("Anatomy: Idealized heroic proportions with highly expressive facial acting and fluid, kinetic posing.")
                    appendLine("Palette: Vibrant primary superhero tones with SUBTLE ELECTRIC BLUE atmospheric accents.")
                    appendLine("Environment: Dynamic urban cityscapes or situational interiors with kinetic particle effects.")
                    appendLine("Aura: Powerful, kinetic, and emotionally resonant heroism.")
                }
            }

            CRIME -> {
                buildString {
                    appendLine("Art Technique: HIGH RENAISSANCE MASTERPIECE meets VINTAGE ART DECO.")
                    appendLine("Rendering: Polished academic oil, seamless sfumato, and marble-like skin luster. Mastery of Chiaroscuro.")
                    appendLine("Palette: Noble pigments (Crimson, Navy, Champagne) with HOT PINK accents (#E91E63).")
                    appendLine("Environment: Tropical luxury coastal settings (Art Deco architecture, palms, yachts).")
                    appendLine("Aura: Untouchable elegance and 'serene superiority'.")
                }
            }

            SHINOBI -> {
                buildString {
                    appendLine("Art Technique: ZEN SUMI-E INK WASH (Traditional Japanese Brushwork).")
                    appendLine(
                        "Rendering: Bold, impulsive dry-brush strokes. Heavy use of 'MA' (Negative Space) as a compositional element. Washi paper texture.",
                    )
                    appendLine("Palette: B&W Monochrome with a SINGLE VIBRANT CRIMSON RED accent (eyes, blood, or sashes).")
                    appendLine("Atmosphere: Lethal silence, spiritual weight, and coiled potential energy.")
                    appendLine("Environment: Minimalist landscape suggested by a few precise ink strokes.")
                }
            }

            SPACE_OPERA -> {
                buildString {
                    appendLine("Art Technique: MID-CENTURY SCI-FI PULP (Chesley Bonestell / Golden Age style).")
                    appendLine("Rendering: Smooth gouache board finish with subtle airbrush gradients. Luminous 'Technicolor' glow.")
                    appendLine("Palette: Rich cosmic indigos and purples with 'ROCKET ORANGE' and 'CHERRY RED' combustion glow.")
                    appendLine("Environment: VAST COSMIC EXPANSE including ringed planets and swirling nebulae. No terrestrial settings.")
                    appendLine("Aura: Awe-inspired heroic optimism and the 'Grandeur of the Infinite'.")
                }
            }

            COWBOY -> {
                buildString {
                    appendLine("Art Technique: TRADITIONAL FRONTIER OIL (Alla Prima / Frederic Remington style).")
                    appendLine(
                        "Rendering: Thick impasto texture, bold alla-prima brushwork, and 'plein air' natural light. No digital smoothness.",
                    )
                    appendLine("Palette: Earthy ochre and raw umber tones with 'SUNSET GOLD' Chiaroscuro.")
                    appendLine("Atmosphere: Harsh frontier sunlight, airborne dust motes, and long dramatic shadows.")
                    appendLine("Aura: Stoic, weathered, and rugged survivalism.")
                }
            }

            PUNK_ROCK -> {
                buildString {
                    appendLine("Art Technique: UNDERGROUND 2D ZINE ART (Hewlett / Pop-Art fusion).")
                    appendLine(
                        "Rendering: Bold sketchy ink outlines, flat color blocks, and halftone dot textures. Offset printing artifacts.",
                    )
                    appendLine("Eyes (CRITICAL): Stylized negative-space eyes (Voids). NO realistic pupils or iris colors.")
                    appendLine("Palette: Sludgy urban olived and grays with 'ACID GREEN' or 'TOXIC YELLOW' chemical accents.")
                    appendLine("Environment: Gritty subculture settings (Basements, Stage-side, Record Shops) with 3+ urban props.")
                    appendLine("Aura: Rebellious, anarchic, and lanky anti-establishment character design.")
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
                - Palette: $palette (Muted Cold Steel, Deep Purple accents).
                - Mandatory: late 80s/early 90s CYBERPUNK OVA style. Hand-painted cel aesthetic, 2-step cel shading, and analog scanlines.
                - Cyberware: Visible mechanical layering with hydraulic joints and seams following human anatomy.
                - Banned: Soft gradients, photorealism, 3D CGI, or 'clean' futuristic aesthetics.
                - Fix: Inject 'hand-painted cel aesthetic', 'analog scanlines', and 'visible hydraulic cyber-joints'.
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
                - Palette: $palette (Vibrant Primaries, Electric Blue accents).
                - Mandatory: MODERN MAINSTREAM COMIC. Sharp clean ink lines, cinematic coloring, and energy bloom effects.
                - Banned: 3D CGI rendering, messy sketchy lines, or Spider-Verse artifacts (halftones/chromatic aberration).
                - Fix: Inject 'sharp clean ink lines', 'cinematic energy bloom', and 'expressive comic facial acting'.
                """.trimIndent()
            }

            CRIME -> {
                val palette = getHexPalette(genre)
                """
                **CRIME VALIDATION (Reviewer):**
                - Palette: $palette (Noble pigments, Hot Pink accents).
                - Mandatory: High-Renaissance Masterpiece meets Art Deco. Mastery of Chiaroscuro and polished oil luster.
                - Aura: Untouchable elegance, 'serene superiority'. Mid-breath candid posing.
                - Environment: Tropical coastal paradise (Turquoise seas, Art Deco architecture, palm trees).
                - Banned: Gritty/dirty settings, digital gradients, modern street wear, casual/low-class aesthetics.
                - Fix: If too gritty, polish with 'academic oil luster', 'marble-like skin', and 'luxurious Art Deco setting'.
                """.trimIndent()
            }

            SHINOBI -> {
                val palette = getHexPalette(genre)
                """
                **SHINOBI VALIDATION (Reviewer):**
                - Palette: $palette (Monochrome B&W, Crimson Red accents).
                - Mandatory: ZEN SUMI-E INK WASH. Bold dry-brush strokes, heavy 'MA' (negative space), and washi paper texture.
                - Focus: Spiritual weight, lethality, and minimalist focus.
                - Banned: Any color outside B&W + Crimson. No natural skin/environment colors.
                - Fix: Strip all color except Crimson; replace with 'high-contrast ink wash' and 'ink splatters'.
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
