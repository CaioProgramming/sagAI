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
    fun renderingInstructions(genre: Genre) =
        when (genre) {
            FANTASY -> {
                buildString {
                    appendLine("MEDIUM: Classical Oil Painting (High Baroque Period).")
                    appendLine("TECHNIQUE: Sfumato (soft, imperceptible transitions). No visible line art.")
                    appendLine("LIGHTING: Tenebrism (Violent contrast between deep darks and divine light). Volumetric 'God Rays'.")
                    appendLine("TEXTURE: Canvas grain, rich impasto brushwork, translucent glazing on skin.")
                    appendLine("PALETTE_BEHAVIOR: Deep shadows swallow details; highlights burn with gold and crimson.")
                    appendLine("PRESENTATION: Full-bleed edge-to-edge artwork. Strictly no borders, frames, or gutters.")
                }
            }

            CYBERPUNK -> {
                buildString {
                    appendLine("MEDIUM: Vintage 1980s Cel Animation (Acetate & Ink).")
                    appendLine("TECHNIQUE: Hand-drawn ink outlines (organic line weight). Hard Cel Shading (blocks of color).")
                    appendLine("LIGHTING: 'Retro Halation' (bloom) on highlights. Harsh neon backlight. Deep blacks.")
                    appendLine("TEXTURE: Heavy Film Grain, chromatic aberration, VHS tape noise, scanlines.")
                    appendLine("PALETTE_BEHAVIOR: High saturation neon vs. crushed blacks. Analog color bleed.")
                    appendLine("PRESENTATION: Full-bleed edge-to-edge artwork. Strictly no borders, frames, or gutters.")
                }
            }

            HORROR -> {
                buildString {
                    appendLine("MEDIUM: 32-Bit Retro 3D Render (PS1 Era).")
                    appendLine("TECHNIQUE: Low-polygon meshes. Vertex jitter (wobbly geometry).")
                    appendLine("LIGHTING: Flashlight 'cone' lighting. Darkness implies the unknown.")
                    appendLine("TEXTURE: Pixelated textures (Nearest-Neighbor filtering). Dithering patterns for gradients.")
                    appendLine("PALETTE_BEHAVIOR: Limited 256-color look. Muted, desaturated, cold tones.")
                    appendLine("PRESENTATION: Full-bleed edge-to-edge artwork. Strictly no borders, frames, or gutters.")
                }
            }

            HEROES -> {
                buildString {
                    appendLine("MEDIUM: GRITTY VINTAGE NOIR COMIC (Heavy Brush & Raw Ink).")
                    appendLine(
                        "TECHNIQUE: Massive 'Spot Blacks' (Mignola/Miller). Shadows are solid black shapes, not gradients. Line work is heavy, organic, and imperfect. BANNED: Thin digital vectors or smooth gradients.",
                    )
                    appendLine(
                        "LIGHTING: Hard-Edged Chiaroscuro. Light is a physical shape, not a diffuse glow. No 'soft' lighting.",
                    )
                    appendLine(
                        "TEXTURE: VISIBLE PAPER GRAIN & INK BLEED. High-density Ben-Day dots for shading. The image should feel like physical paper.")
                    appendLine(
                        "PALETTE_BEHAVIOR: FLAT COLOR CUTS. Colors must be solid fills, not digital paintings. Skin/Hair/Eyes must be natural. BANNED: Digital airbrushing, lens flares, or 'glow' effects.",
                    )
                    appendLine("PRESENTATION: Full-bleed edge-to-edge artwork. Strictly no borders, frames, or gutters.")
                }
            }

            CRIME -> {
                buildString {
                    appendLine("MEDIUM: Polished Art Deco Oil Illustration.")
                    appendLine("TECHNIQUE: Seamless gradients. 'Ligne Claire' influence but with painterly depth.")
                    appendLine("LIGHTING: Golden Hour luxury. High-specular gloss on skin and gold.")
                    appendLine("TEXTURE: Smooth marble, silk, polished metal. Zero grit.")
                    appendLine("PALETTE_BEHAVIOR: Rich, expensive tones (Navy, Champagne). Hot Pink neon accents.")
                    appendLine("PRESENTATION: Full-bleed edge-to-edge artwork. Strictly no borders, frames, or gutters.")
                }
            }

            SHINOBI -> {
                buildString {
                    appendLine("MEDIUM: Sumi-e Ink Wash on Washi Paper.")
                    appendLine("TECHNIQUE: 'Flying White' (dry brush). Aggressive, spontaneous strokes. Ink splatters.")
                    appendLine("LIGHTING: Negative Space is light. Black Ink is shadow. High Contrast.")
                    appendLine("TEXTURE: Raw paper fibers. Wet ink bleeding into paper. Rough, organic.")
                    appendLine("PALETTE_BEHAVIOR: Strictly Monochrome Black & White. Single Crimson Red drop.")
                    appendLine("PRESENTATION: Full-bleed edge-to-edge artwork. Strictly no borders, frames, or gutters.")
                }
            }

            SPACE_OPERA -> {
                buildString {
                    appendLine("MEDIUM: Mid-Century Gouache Illustration (Pulp Cover).")
                    appendLine("TECHNIQUE: Board painting. Visible brush directions but blended.")
                    appendLine("LIGHTING: Technicolor studio lighting. Soft, romantic glow.")
                    appendLine("TEXTURE: Matte finish. Slightly chalky gouache texture.")
                    appendLine("PALETTE_BEHAVIOR: Vibrant, saturated Cosmic Indigos and Rocket Oranges.")
                    appendLine("PRESENTATION: Full-bleed edge-to-edge artwork. Strictly no borders, frames, or gutters.")
                }
            }

            COWBOY -> {
                buildString {
                    appendLine("MEDIUM: Alla Prima Oil (Plein Air style).")
                    appendLine("TECHNIQUE: Thick, confident brush strokes (Impasto). Painting 'wet-on-wet'.")
                    appendLine("LIGHTING: Harsh, direct sunlight. Sharp cast shadows.")
                    appendLine("TEXTURE: Weathered, cracked, dust-coated. Thick paint texture.")
                    appendLine("PALETTE_BEHAVIOR: Earthy ochres, burnt siennas, baked by the sun.")
                    appendLine("PRESENTATION: Full-bleed edge-to-edge artwork. Strictly no borders, frames, or gutters.")
                }
            }

            PUNK_ROCK -> {
                buildString {
                    appendLine("MEDIUM: Underground 2D Zine / Xerox Art.")
                    appendLine("TECHNIQUE: Bold, sketchy ink outlines. Messy, unrefined.")
                    appendLine("LIGHTING: Flat, uniform lighting. No complex gradients.")
                    appendLine("TEXTURE: Halftone dot patterns (Offset printing). Photocopy degradation.")
                    appendLine("PALETTE_BEHAVIOR: Flat color blocks. Toxic Green vs. Urban Gays.")
                    appendLine("PRESENTATION: Full-bleed edge-to-edge artwork. Strictly no borders, frames, or gutters.")
                }
            }
        }

    fun artStyle(genre: Genre) =
        when (genre) {
            FANTASY -> "A world of divine grandeur and tragic beauty. It captures the spiritual weight of myths, where light fights purely against darkness."
            CYBERPUNK -> "A 'High Tech, Low Life' analog dystopia. The beauty of decay, where humanity is slowly replaced by machinery under the hum of neon."
            HORROR -> "Unknown psychological dread. The uncanny valley of early digital eras, where low-fidelity implies hidden terrors."
            HEROES -> "A world of high stakes and sharp edges. Bold, confident ink-work and cinematic staging, capturing the dynamic tension of a modern noir graphic novel."
            CRIME -> "Untouchable wealth and superficial perfection. A facade of serene luxury hiding moral decay. The humidity of a tropical criminal paradise."
            SHINOBI -> "The discipline of silence. A brutal, minimalist world where life and death are decided in a single stroke. Nature and violence intertwined."
            SPACE_OPERA -> "Optimistic exploration. The wonder of the atomic age, looking up at the stars with hope and colorful imagination."
            COWBOY -> "The raw struggle of man against nature. A stoic, weathered existence defined by dust, sun, and silence."
            PUNK_ROCK -> "Loud, defiant, and ugly-cute. A rejection of perfection. The visual noise of a garage band poster stapled to a wall."
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
        buildString {
            val palette = getHexPalette(genre)
            appendLine("**${genre.name} GENRE SOUL (Reviewer Mandate):**")
            appendLine("- Narrative Essence: ${artStyle(genre)}")
            appendLine("- Mandatory Palette: $palette")
            appendLine()
            appendLine("**CRITICAL FOCUS AREAS:**")
            appendLine(
                "1. SCENE NATURALITY: Ensure lighting and atmosphere are organic to the narrative context. Ban hard-injected digital overlays or unnatural light bars on faces.",
            )
            appendLine("2. ENVIRONMENT INTEGRITY: The setting must be vivid and match the story context. No vague or empty backgrounds.")
            appendLine("3. CHARACTER FIDELITY: Ensure all physical traits (skin, hair, eyes) match the '#### SUBJECTS DETAILS' perfectly.")
            appendLine()
            appendLine("**TECHNICAL RENDERING NOTICE:**")
            appendLine("- The technical medium (ink, oil, pixels) is handled by the direct rendering pipeline.")
            appendLine(
                "- DO NOT FLAG missing technical keywords. Only flag if the Artist explicitly describes something that breaks the narrative soul.")
        }
}
