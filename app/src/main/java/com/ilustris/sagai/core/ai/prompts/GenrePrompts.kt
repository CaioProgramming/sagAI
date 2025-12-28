package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.currentLanguage
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

object GenrePrompts {
    fun artStyle(genre: Genre) =
        when (genre) {
            FANTASY -> {
                buildString {
                    appendLine("Art Technique: ROMANTIC CLASSICAL OIL PAINTING with Luminous Glazing & Ethereal Light.")
                    appendLine("Emphasize TRANSLUCENT LAYERED GLAZES over tonal underpaintings, creating luminous atmospheric effects.")
                    appendLine(
                        "MUST replicate: Visible warm canvas texture, sfumato edge transitions (NO sharp digital lines), organic color bleeding between warm/cool tones, traditional glazing luminosity.",
                    )
                    appendLine()
                    appendLine(
                        "Rendering Style: CLASSICAL ROMANTIC FANTASY PAINTING (inspired by 18th-19th century academic painters and Pre-Raphaelites).",
                    )
                    appendLine("- Light sources appear to glow from within the composition (divine/ethereal radiance)")
                    appendLine("- Soft golden-hour ambiance with warm atmospheric perspective")
                    appendLine("- Color harmony through glazing: warm reds/golds layered over cool earth tones for depth")
                    appendLine(
                        "- Figures rendered with the noble grace and refined presence characteristic of classical painting, applied to a diverse range of characters.")
                    appendLine("- Brushwork: VISIBLE but CONTROLLED - not loose, not hyper-detailed digital")
                    appendLine()
                    appendLine(
                        "**CLASSICAL ANATOMY & FIGURE RENDERING (CRITICAL):**",
                    )
                    appendLine(
                        "- Anatomical proportions: Balanced weight distribution and purposeful gestures inherited from classical masters, accommodating diverse body types (stout, lanky, regal, powerful).",
                    )
                    appendLine(
                        "- Refined features: High Renaissance-inspired treatment of facial structure (refined features, almond-shaped eyes) applied across all ethnicities (e.g., Black, Asian, Indigenous, etc.) to create unique 'Classical Deities'.",
                    )
                    appendLine(
                        "- Musculature: Subtle muscle definition suggested through warm/cool tones and light play - NOT exaggerated or hyperrealistic",
                    )
                    appendLine(
                        "- Flesh rendering: Warm flesh tones with cool shadows; skin appears luminous and translucent, built through glazes not digital blending",
                    )
                    appendLine(
                        "- Drapery: Flowing, elegant fabrics with realistic cloth physics; folds cast soft shadows and catch luminous highlights",
                    )
                    appendLine(
                        "- Gesture: Graceful, purposeful poses suggesting movement or contemplation; avoid stiff or awkward positioning",
                    )
                    appendLine()
                    appendLine(
                        "Key Lighting Style: LUMINOUS SOFT CHIAROSCURO with WARM GOLDEN GLOW. Light transitions are gradual and atmospheric, creating an ethereal, dreamlike quality.",
                    )
                    appendLine(
                        "NO sharp digital effects: no airbrushed smoothness, no lens bloom, no chromatic aberration, no concept art rendering.",
                    )
                    appendLine()
                    appendLine(
                        "Texture / Materiality: FINE CANVAS OIL PAINTING FINISH. Visible paint texture from glazing layers. Subtle directional brushstrokes, soft impasto in warm highlights. Traditional pigment color mixing visible (not digital color).",
                    )
                    appendLine("- Surface appears hand-painted with visible brushwork, not digitally smooth")
                    appendLine("- Paint application shows depth through layering - underpainting visible through glazes in shadow areas")
                    appendLine("- Highlights built with thick impasto; shadows with thin transparent glazes")
                    appendLine()
                    appendLine(
                        "Aesthetic Era / Influence: Classical Academic Painting + Romantic Movement (18th-19th century). Masters: Frederic Leighton, John William Waterhouse, Alexandre Cabanel, Lawrence Alma-Tadema.",
                    )
                    appendLine(
                        "Visual Reference: Pre-Raphaelite warmth, academic figure painting precision, classical romantic mythology settings, Renaissance anatomical studies.",
                    )
                    appendLine()
                    appendLine("**STRICTLY FORBIDDEN (No Digital Art Rendering):**")
                    appendLine(" - Digital painting aesthetics, concept art rendering, video game illustration")
                    appendLine(" - Modern fantasy illustration, MTG card art, D&D book art, anime-influenced styles")
                    appendLine(" - Hard digital edges, digital brush artifacts, airbrushed smoothness, photoshop gradients")
                    appendLine(" - Hyperrealistic anatomy, photorealistic skin texture, modern anatomical rendering")
                    appendLine(" - Digital lens effects (bloom, chromatic aberration, lens flare, volumetric lighting)")
                    appendLine(" - Sharp digital highlights, plastic-looking surfaces, CG/3D rendering appearance")
                    appendLine(" - Flat digital coloring without glazing depth, oversaturated digital colors, neon tones")
                    appendLine(" - Contemporary realistic style mixing with fantasy (modern photography blending)")
                    appendLine(" - Exaggerated musculature, comic book anatomy, superhero proportions")
                    appendLine()
                    appendLine("**COLOR PALETTE & ACCENT:**")
                    appendLine(
                        " - Base: WARM CRIMSON RED DOMINANCE. Rich reds, deep carmines, rose madder, vermillion, burnt sienna, and warm ochres. Harmonious warm earth tones with red as the primary color.",
                    )
                    appendLine(" - Mandatory Accent: RADIANT GOLD / CELESTIAL GILT paired with CRIMSON RED.")
                    appendLine(
                        " - Application: RED must dominate the composition - use for dramatic crimson skies (dawn/dusk), red-tinted clouds, rose gardens, red flowers, crimson fabrics and drapery, red gemstones, ruby light reflections, warm red ambient light, red architectural elements (terracotta, red marble). Gold accents for divine halos, magical auras, gilded details, and mystical symbols. Apply with Renaissance restraint and elegance. Forbid cool blues, greens, or desaturated tones.",
                    )
                    appendLine(
                        " - Color mixing: Use traditional oil painting color harmonies - no oversaturated digital colors, no neon, no RGB-pure colors.",
                    )
                    appendLine()
                    appendLine(
                        "Mood: Ethereal, passionate, and majestic — a sense of divine warmth and spiritual reverence bathed in crimson light. Emphasize beauty and grace over conflict.",
                    )
                    appendLine(
                        "Ambience: Warm, diffused red-golden light (sunset/sunrise), crimson skies, red-tinted atmospheric perspective, rose-colored clouds or heavenly rays, classical landscape elements with red environmental features.",
                    )
                    appendLine()
                    appendLine("**MANDATORY BACKGROUND & ENVIRONMENT:**")
                    appendLine(
                        "EVERY image MUST have a detailed environmental context with Renaissance painting composition. NO plain or empty backgrounds allowed.",
                    )
                    appendLine("CRIMSON RED must be present in EVERY background through sky, light, or environmental elements.")
                    appendLine("Choose appropriate setting and describe 3+ specific elements in classical style with RED integration:")
                    appendLine(
                        " - Enchanted Garden: Climbing red roses on classical columns, rose bushes, red flowering trees, crimson sunset sky, red marble fountains, distant red-tinted mountains",
                    )
                    appendLine(
                        " - Palace Hall: Renaissance architecture with red tapestries, crimson velvet drapes, red marble floors, terracotta accents, red-tinted frescoes, warm red light through stained glass",
                    )
                    appendLine(
                        " - Sacred Grove: Ancient trees under crimson twilight, red wildflowers, autumn red leaves, red stone altars, warm red-golden light filtering through canopy",
                    )
                    appendLine(
                        " - Celestial Realm: Crimson-gold cloud formations, red-tinted divine architecture, ruby light rays, red crystal formations, sunset-colored ethereal glow",
                    )
                    appendLine(
                        " - Classical Library: Red leather-bound tomes, crimson velvet cushions, red oriental rugs, warm candlelight with red-golden glow, red curtains, burgundy wood",
                    )
                    appendLine(
                        " - Pastoral Landscape: Rolling hills under crimson sunset, red poppy fields, terracotta-roofed villages, red autumn foliage, dramatic red-orange sky, red clay paths",
                    )
                    appendLine()
                    appendLine(
                        "BANNED: 'dark dungeons', 'gritty battlefields', 'plain background', 'gradient background', 'solid color', 'isolated figure', 'empty space', 'cool blues', 'cold greens'",
                    )
                    appendLine(
                        "REQUIRED: Classical Renaissance composition with RED-DOMINANT environmental details, warm atmospheric perspective, and crimson-golden luminosity.",
                    )
                    appendLine()
                    appendLine("**FINAL STYLE CHECK:**")
                    appendLine(
                        "Ask yourself: \"Would this description produce an image that looks like a 15th-century Italian oil painting, or modern digital fantasy art?\"",
                    )
                    appendLine(
                        "If the answer is the latter, REVISE to emphasize classical painting techniques, soft edges, traditional color harmonies, and ethereal beauty.",
                    )
                    appendLine()
                    appendLine("**AUTONOMOUS FIX PATTERNS:**")

                    appendLine(
                        "If prompt contains: modern anime, digital aesthetics, hard edges → REPLACE with oil painting glazing, soft sfumato transitions, classical academic style",
                    )
                    appendLine(
                        "If prompt contains: tech elements, cybernetic, mechanical → REMOVE entirely, replace with classical/mystical elements",
                    )
                    appendLine(
                        "If background is empty/plain/gradient → ADD detailed classical environment with 3+ specific red-integrated elements from the MANDATORY BACKGROUND list",
                    )
                    appendLine(
                        "If missing red tones → INTEGRATE crimson into sky, lighting, fabrics, environmental features to achieve red dominance",
                    )
                }
            }

            CYBERPUNK -> {
                buildString {
                    appendLine(
                        "Art Technique: Vintage 1980s Anime OVA Cel Animation. FLAT SHADING (Cel Shading) is mandatory. Shadows are hard-edged blocks of color; forbid soft gradients, ambient occlusion, and subsurface scattering.",
                    )
                    appendLine(
                        "Line Work: Delicate, sketchy ink lines with varying weight. Organic cloth folds and realistic mechanical detailing.",
                    )
                    appendLine()
                    appendLine("**COLOR PALETTE & ACCENT:**")
                    appendLine(" - Base: Muted Blue Tones. Dominant tones of Slate Blue, Cold Steel, and Deep Cool Greys.")
                    appendLine("    The image should feel desaturated, moody and atmospheric.")
                    appendLine(" - Mandatory Accent: DEEP PURPLE.")
                    appendLine(
                        " - Application: Use this accent subtly for deep shadow casting, low-light environmental glow, or muted interface lights. STRICTLY FORBID bright, fluorescent neon pinks or bright purples. The purple must feel \"bruised\" or metallic.",
                    )
                    appendLine()
                    appendLine(
                        "Detail: Hair rendered as \"clumps\" or shapes with simple highlights, not individual strands. Skin must be matte; reduce micro-detail.",
                    )
                    appendLine(
                        "Lighting: High-contrast \"Rembrandt\" lighting with hard-edged, cast shadows. Strong Hard Rim Light is essential to separate characters. No Global Illumination.",
                    )
                    appendLine(
                        "Texture & Artifacts: Analog Noise, Chromatic Aberration, and subtle Film Grain to simulate vintage broadcast quality.",
                    )
                    appendLine()
                    appendLine(
                        "Facial Features: Classic 1990s anime proportions. Large, expressive eyes with prominent specular highlights, sharp noses, and high-contrast hair sheen.",
                    )
                    appendLine()
                    appendLine("Mood: Dystopian, melancholic, and vast. A sense of high-tech isolation amidst towering structures.")
                    appendLine(
                        "Ambience: Dense vertical metropolis, looming mega-skylines, muted city lights. A feeling of oppressive scale and future decay.",
                    )
                    appendLine()
                    appendLine("**MANDATORY BACKGROUND & ENVIRONMENT:**")
                    appendLine("EVERY image MUST have a detailed cyberpunk environmental context. NO plain or empty backgrounds allowed.")
                    appendLine("Choose appropriate setting and describe 3+ specific elements:")
                    appendLine(" - Cyberpunk Street: Neon signs, holographic ads, rain-slicked pavement, vending machines, cables overhead")
                    appendLine(
                        " - Corporate Interior: Glass walls, data screens, minimalist furniture, cold lighting, geometric architecture",
                    )
                    appendLine(" - Undercity/Slums: Exposed pipes, flickering lights, stacked housing units, steam vents, makeshift tech")
                    appendLine(" - Tech Lab: Computer banks, wiring, screens displaying data, robotic arms, industrial equipment")
                    appendLine(" - Rooftop: Antenna arrays, AC units, cityscape below, neon glow from streets, distant mega-buildings")
                    appendLine(" - Transit Hub: Platform edges, holographic departures, crowds (if wide), ceiling infrastructure")
                    appendLine()
                    appendLine("BANNED: 'plain background', 'gradient background', 'solid color', 'isolated figure', 'empty void'")
                    appendLine(
                        "REQUIRED: Cyberpunk environmental storytelling with muted, atmospheric details matching 1980s anime OVA aesthetic.",
                    )
                    appendLine()
                    appendLine(
                        "Rendering Constraints: Strictly forbid 3D CGI, photorealism, modern digital smoothing/blending techniques, modern anime, digital painting, 3d render, unreal engine, volumetric lighting, smooth shading. The final image must look like a high-quality frame from a vintage laserdisc or a production cel.",
                    )
                    appendLine()
                    appendLine("**AUTONOMOUS FIX PATTERNS:**")
                    appendLine(
                        "If prompt contains: modern anime, bright/fluorescent colors, smooth gradients → REPLACE with 1980s OVA cel style, muted blues, hard cel shading",
                    )
                    appendLine(
                        "If prompt contains: photorealism, smooth blending, 3D rendering → REPLACE with flat cel shading, hard shadows, analog film grain",
                    )
                    appendLine(
                        "If prompt contains: bright pink/neon colors (not purple) → REPLACE with muted purples, bruised tones, metallic purple",
                    )
                    appendLine(
                        "If background is empty/plain → ADD detailed cyberpunk environment with 3+ specific elements from MANDATORY BACKGROUND list",
                    )
                    appendLine(
                        "If eyes lack expression → ENSURE large anime eyes with prominent specular highlights as per Facial Features specs",
                    )
                }
            }

            HORROR -> {
                buildString {
                    appendLine("Art Technique: 32-BIT PIXEL ART with BLOCKY SHADING")
                    appendLine("Texture / Materiality: RETRO GAME ART PIXELATION")
                    appendLine("Aesthetic Era / Influence: PS1/SEGA SATURN HORROR GAME AESTHETIC")
                    appendLine("Vibe / Mood Aesthetic: HAUNTED, MYSTIQUE DARK AESTHETIC")
                    appendLine()
                    appendLine("**COLOR PALETTE & ACCENT:**")
                    appendLine(" - Base: LIMITED DARK AND PALE BLUE PALETTE.")
                    appendLine(" - Mandatory Accent: Ash gray/Faded cerulean blue.")
                    appendLine(
                        " - Application: Use this for subtle highlights, reflections, or to create a chilling atmosphere. Forbid other vibrant colors.",
                    )
                    appendLine()
                    appendLine("Mood: Oppressive, uncanny, and creeping dread — prioritize psychological unease over explicit gore.")
                    appendLine(
                        "Ambience: Low-key, desaturated environment with heavy shadows and subtle volumetric fog; minimal and muted highlights only where needed to draw attention to small, eerie details.",
                    )
                    appendLine()
                    appendLine("**MANDATORY BACKGROUND & ENVIRONMENT:**")
                    appendLine("EVERY image MUST have a detailed eerie environmental context. NO plain or empty backgrounds allowed.")
                    appendLine("Choose appropriate horror setting and describe 3+ specific elements:")
                    appendLine(
                        " - Abandoned Hospital: Flickering fluorescent lights, peeling walls, overturned gurneys, scattered medical equipment, dark doorways",
                    )
                    appendLine(" - Decrepit House: Cracked windows, rotting floorboards, torn wallpaper, dusty furniture, ominous shadows")
                    appendLine(" - Foggy Street: Distant streetlamps, silhouetted buildings, swirling mist, abandoned vehicles, eerie glow")
                    appendLine(
                        " - Dark Forest: Twisted trees, thick undergrowth, moonlight filtering through branches, unsettling sounds suggested",
                    )
                    appendLine(
                        " - Underground Tunnel: Dripping pipes, moldy walls, dim emergency lighting, distant echoes, claustrophobic atmosphere",
                    )
                    appendLine(" - Forgotten Graveyard: Weathered tombstones, creeping ivy, iron gates, ground fog, spectral ambience")
                    appendLine()
                    appendLine("BANNED: 'plain background', 'gradient background', 'solid color', 'empty void', 'isolated figure'")
                    appendLine(
                        "REQUIRED: Eerie environmental storytelling with PS1/Saturn era pixel art aesthetic maintaining oppressive mood.",
                    )
                    appendLine()
                    appendLine("**AUTONOMOUS FIX PATTERNS:**")
                    appendLine(
                        "If prompt contains: modern graphics, smooth shading, high-definition → REPLACE with 32-bit pixelated aesthetic, blocky shading, limited palette",
                    )
                    appendLine("If prompt contains: bright colors, vibrant tones → REPLACE with dark blues, pale blues, ash greys only")
                    appendLine(
                        "If prompt contains: gore, explicit violence → SHIFT focus to psychological horror, subtle unease, atmosphere over explicit detail",
                    )
                    appendLine(
                        "If background is empty/plain → ADD detailed eerie environment with 3+ specific elements from MANDATORY BACKGROUND list",
                    )
                    appendLine("If missing oppressive mood → ADD heavy shadows, volumetric fog, low-key lighting, claustrophobic framing")
                }
            }

            HEROES -> {
                buildString {
                    appendLine("Art Technique: MODERN COMIC BOOK ART STYLE (Dynamic, Clean, and Detailed).")
                    appendLine("Line Detail: BOLD INK LINES, ANATOMICALLY ACCURATE FIGURES, DYNAMIC FORESHORTENING.")
                    appendLine("Key Lighting Style: CINEMATIC NATURAL LIGHTING (Golden Hour or Overcast) with DRAMATIC SHADOWS.")
                    appendLine("Texture / Materiality: HIGH-QUALITY DIGITAL COLORING, SMOOTH GRADIENTS, DETAILED URBAN TEXTURES.")
                    appendLine(
                        "Aesthetic Era / Influence: MODERN SUPERHERO COMICS (2010s-Present) - Think \"Into the Spider-Verse\" meets \"Arkham City\" concept art.",
                    )
                    appendLine()
                    appendLine("**COLOR PALETTE & ACCENT:**")
                    appendLine(" - Base: DYNAMIC URBAN PALETTE. Concrete greys, brick reds, glass reflections, and atmospheric sky tones.")
                    appendLine(" - Mandatory Accent: SUBTLE ELECTRIC BLUE.")
                    appendLine(
                        " - Application: Use this accent for SKY DETAILS, REFLECTIONS on glass/water, subtle RIM LIGHTING, or small details on accessories/tech. It should NOT be a dominant wash, but a unifying atmospheric element.",
                    )
                    appendLine()
                    appendLine("Mood: HEROIC, VERTICAL, and EXPANSIVE. A sense of scale and freedom mixed with urban melancholy.")
                    appendLine(
                        "Ambience: VAST OPEN CITYSCAPES. Towering skyscrapers, dizzying perspectives (looking down or up), busy streets far below. The city feels alive but slightly dystopian. Avoid forced night scenes; prefer dynamic daytime, sunset, or moody overcast skies that allow for depth and scale.",
                    )
                    appendLine()
                    appendLine("**AUTONOMOUS FIX PATTERNS:**")
                    appendLine(
                        "If prompt contains: photorealism, hyper-detailed rendering → REPLACE with comic book stylization, bold ink lines, illustrated aesthetics",
                    )
                    appendLine(
                        "If prompt contains: fantasy elements, medieval setting → REPLACE with modern urban/futuristic architecture, contemporary tech, cityscape",
                    )
                    appendLine(
                        "If accent color is missing or weak → INTEGRATE subtle electric blue in sky details, reflections on glass/water, rim lighting on edges",
                    )
                    appendLine(
                        "If background is empty/plain → ADD detailed urban environment with tall buildings, perspective, street elements, atmospheric details",
                    )
                    appendLine(
                        "If lacking heroic verticality → REFRAME to emphasize scale: looking up at buildings, high vantage points, vast cityscapes",
                    )
                }
            }

            CRIME -> {
                buildString {
                    appendLine("Art Technique: HYPER-REALISTIC CLASSICAL OIL PAINTING (Academic Realism).")
                    appendLine("Key Lighting Style: DIVINE & ETHEREAL. Soft, diffused \"Heavenly\" light that wraps around the subject.")
                    appendLine(
                        "Texture / Materiality: SMOOTH GLAZING technique. No rough brushstrokes. The surface should look like flawless porcelain or silk.",
                    )
                    appendLine(
                        "Aesthetic Era / Influence: Classical Academic Art (e.g., Bouguereau, Godward) meets Modern High-Fashion Photography.",
                    )
                    appendLine()
                    appendLine("**COLOR PALETTE & ACCENT:**")
                    appendLine(" - Base: PURE ELEGANCE. Creamy alabaster, champagne gold, soft pearl grey, and deep, expensive blacks.")
                    appendLine(" - Mandatory Accent: HOT PINK (#E91E63 - Pantone 213 C).")
                    appendLine(
                        " - Application: Weave this Hot Pink accent subtly throughout the composition as sophisticated environmental or character details. Examples include:",
                    )
                    appendLine(
                        "   * ENVIRONMENTAL: Neon light glow reflecting on ocean waves and wet sand, beachfront palm fronds swaying, pink sunset sky bleeding across endless coastline, flamingo sculptures at beachside venues, luxury convertibles on coastal highways, art deco beach hotels, boardwalk neon reflections, champagne glasses against ocean backdrop, silk fabrics flowing in sea breeze.",
                    )
                    appendLine(
                        "   * CHARACTER OUTFIT: Hawaiian shirts with pink tones, designer accessories with pink accents, lipstick or nail polish, fabric trim on luxury clothing, a silk scarf, beach resort attire.",
                    )
                    appendLine(
                        "   * AMBIENT LIGHT: Soft hot pink glow from ocean sunset, neon beachfront signs reflecting on wet sand and skin, golden hour light filtering through palm trees, ocean waves catching twilight and neon glow, beachside fire pits with warm pink undertones.",
                    )
                    appendLine(
                        "   The accent should feel organic and effortless, never jarring—it's a signature touch of vice, glamour, and 80s Miami vibes subtly integrated into luxury aesthetics. Maintain an atmosphere of absolute divine elegance and sophistication throughout.",
                    )
                    appendLine()
                    appendLine(
                        "Figure & Facial Treatment: DIVINE SOPHISTICATION. Skin must be rendered with hyper-realistic subsurface scattering. Features should reflect the diversity of a global elite (e.g., striking Black models, elegant East Asian socialites, or distinguished Hispanic moguls), all looking like \"Modern Deities\": serene, symmetrical, and breathtaking in their unique beauty.",
                    )
                    appendLine(
                        "Rendering: THE \"PERFECT PICTURE\" ILLUSION. At first glance, it appears to be a stunning high-resolution photograph. On closer inspection, it is a masterpiece painting with incredibly fine, deliberate details. Sharp focus on the subject, dreamy/soft background.",
                    )
                    appendLine()
                    appendLine("Mood: A Divine Piece of Art. A frozen moment of absolute luxury and beauty.")
                    appendLine(
                        "Ambience: An enchanted 80s Miami beach city paradise—endless golden beaches, palm-lined oceanfront, neon-lit beachfront promenades, and that thrilling excitement of coastal nightlife. Think Miami Vice, GTA Vice City, and Scarface: the dreamlike adoration of 80s Miami as the ultimate tropical fantasy destination, where ocean meets neon glamour.",
                    )
                    appendLine()
                    appendLine("**AUTONOMOUS FIX PATTERNS:**")
                    appendLine(
                        "If prompt contains: grimdark, violence, harsh realism → REPLACE with luxury, elegance, sophistication, divine beauty",
                    )
                    appendLine(
                        "If prompt contains: cool blues, greens, desaturated colors → REPLACE with warm golds, champagne, alabaster, hot pink accents",
                    )
                    appendLine(
                        "If accent color missing → INTEGRATE hot pink subtly in lighting, environment, or character details (neon reflection, clothing, sunset glow)",
                    )
                    appendLine(
                        "If background is empty/plain → ADD detailed Miami ambience with 3+ beach/80s elements: palm trees, ocean, neon signs, architecture",
                    )
                    appendLine("If missing ethereal quality → SOFTEN edges, add divine lighting, dreamlike atmosphere, luxury details")
                }
            }

            SHINOBI -> {
                buildString {
                    appendLine(
                        "Art Technique: JAPANESE SUMI-E INK WASH PAINTING (Suiboku-ga), EMPHASIS ON ECONOMY OF BRUSHSTROKES AND NEGATIVE SPACE.",
                    )
                    appendLine(
                        "CRITICAL NOTE: THE STYLE IS BLACK AND WHITE. NO COLOR OR TINTS, NOT EVEN OFF-WHITE TONES FOR SKIN OR PAPER.",
                    )
                    appendLine(
                        "Brushwork: BOLD, IMPERFECT, AND HIGHLY TEXTURAL BRUSH STROKES (Hone-gaki/dry-brush) that clearly convey energy and form.",
                    )
                    appendLine(
                        "Use controlled ink bleeds, water/wash artifacts, and visible spontaneity to avoid a \"clean digital\" look.",
                    )
                    appendLine("Prioritize the weight and flow of the ink.")
                    appendLine(
                        "Texture / Materiality: AUTHENTIC RICE-PAPER (WASHI) TEXTURE — Visible paper grain and natural ink absorption/blotches. THE PAPER IS PURE WHITE, NOT TINTED.",
                    )
                    appendLine(
                        "Add subtle ink spatters/splatters for dynamism. Aesthetic Influence: Master Sumi-e (Sesshū Tōyō), Gekiga (raw, expressive comic style), and Zen philosophy (simplicity, spontaneity).",
                    )
                    appendLine()
                    appendLine(
                        "Figure & Facial Treatment: MINIMALIST AND EXPRESSIVE — Features often simplified or obscured by shadow/mist/hair. Focus on silhouette and gestural energy (Sei).",
                    )
                    appendLine(
                        "Avoid smooth skin or polished features. The character's form is suggested more by the surrounding ink wash than by hard outlines.",
                    )
                    appendLine("Rendering: HIGH-CONTRAST MONOCHROME (Pure Black to White Paper) with a SINGLE, VITAL COLOR ACCENT.")
                    appendLine("Form is built using layered, transparent diluted ink washes (Bokashi) and strong areas of solid black.")
                    appendLine()
                    appendLine("**COLOR PALETTE & ACCENT:**")
                    appendLine(" - Base: Monochrome (Pure Black to White Paper).")
                    appendLine(" - Mandatory Accent: VIBRANT CRIMSON RED.")
                    appendLine(" - Application: Use the accent color as a single, vital element. Forbid other vibrant colors.")
                    appendLine()
                    appendLine(
                        "Lighting & Shading: DRAMATIC SHADOWS AND CONTRAST created by DENSITY OF INK WASH versus Pristine Negative Space. Use atmospheric depth (mist/fog) to diffuse light and suggest volume, rather than complex digital rim lights.",
                    )
                    appendLine()
                    appendLine(
                        "Composition & Ambience: FEUDAL JAPANESE HIGH DRAMA — Ambience is suggested (e.g., castle rooftops, bamboo silhouette) rather than fully drawn.",
                    )
                    appendLine("Composition must heavily utilize and celebrate the white/negative space.")
                    appendLine("Focus on the character's immediate action or emotion.")
                    appendLine()
                    appendLine("Rendering: Full-bleed illustration. No borders, frames, or text.")
                    appendLine()
                    appendLine("Mood: Raw, spontaneous, intensely focused, and visually powerful through simplicity.")
                }
            }

            SPACE_OPERA -> {
                buildString {
                    appendLine("Art Technique: 1950s ATOMIC AGE ORIGINAL ILLUSTRATION (Gouache/Oil).")
                    appendLine("Texture / Materiality: SMOOTH ILLUSTRATION BOARD, VISIBLE BRUSHSTROKES, VIBRANT PAINTERLY FINISH.")
                    appendLine(
                        "Aesthetic Era / Influence: RETRO-FUTURISM, RAYGUN GOTHIC, 1950s PIN-UP & PULP COVER ART (The original painting, not the printed poster).",
                    )
                    appendLine()
                    appendLine("**COLOR PALETTE & ACCENT:**")
                    appendLine(" - Base: Deep Space Palette. Rich cosmic blues, purple nebula tones, starlight silver, and void black.")
                    appendLine(" - Mandatory Accent: CHERRY RED / ROCKET ORANGE.")
                    appendLine(
                        " - Application: Use vivid red/orange for key elements (advanced spacecraft engines, energy beams, stellar phenomena) to contrast dramatically against the cosmic backdrop.",
                    )
                    appendLine()
                    appendLine(
                        "Figure Style: RADIANT 50s COMMERCIAL ILLUSTRATION. Expressive, cheerful, and confident archetypes across all ethnicities and body types (e.g., retro-futuristic Black heroines, stout space captains, or lanky cosmic explorers, all rendered with 50s charm).",
                    )
                    appendLine(
                        "Line Work: Soft expressive painting style (like Gil Elvgren or Norman Rockwell). Smooth blending, soft edges.",
                    )
                    appendLine()
                    appendLine("**TECHNOLOGY & VEHICLES (When Applicable):**")
                    appendLine(
                        " - IF spacecraft or technology appear in the scene, they should be: elegant, streamlined starships with flowing curves, chrome accents, and sophisticated aerodynamics. Think advanced space cruisers, not primitive rockets.",
                    )
                    appendLine(
                        " - IF individual vehicles are shown: design them like futuristic motorcycles or sleek fighter jets adapted for space - cool, fast, and technologically advanced.",
                    )
                    appendLine(
                        " - IF propulsion is visible: show sophisticated engine designs, energy trails, plasma drives, and gravitational field effects rather than simple rocket flames.",
                    )
                    appendLine(
                        " - IF technology is included: all tech should look genuinely advanced and purposeful for deep space exploration - orbital stations, research vessels, interstellar transports with elegant, functional designs.",
                    )
                    appendLine(
                        " - AVOID: Simple tube rockets, primitive spacecraft, or overly basic vehicle designs that don't match the scope of cosmic exploration.",
                    )
                    appendLine(
                        " - NOTE: Technology and vehicles are NOT mandatory - the cosmic ambience and 1950s art style are the primary focus.",
                    )
                    appendLine()
                    appendLine("Mood: \"The Infinite Cosmos Awaits!\" — Awe-inspiring, optimistic, and filled with cosmic wonder.")
                    appendLine(
                        "Ambience: VAST COSMIC ENVIRONMENTS. Emphasize the true majesty of space: swirling nebulae in brilliant colors, dense star fields stretching to infinity, the event horizon of black holes, meteor showers blazing across the void, distant galaxies spiraling in the background, cosmic dust clouds illuminated by stellar radiation.",
                    )
                    appendLine(
                        "Focus on the overwhelming scale and beauty of the universe itself—the infinite expanse, the play of cosmic light and shadow, the sense of floating in the boundless void. Avoid terrestrial-looking planets;",
                    )
                    appendLine("instead show the raw cosmic phenomena that make space exploration truly magnificent.")
                    appendLine()
                    appendLine("Rendering: Clean illustration. No text, logos, or borders.")
                }
            }

            COWBOY -> {
                buildString {
                    appendLine(
                        "Art Technique: EXPRESSIVE WESTERN OIL PAINTING (Remington/Russell style). EMPHASIS ON PAINTERLY QUALITY AND VISIBLE BRUSHWORK.",
                    )
                    appendLine("Key Lighting Style: WARM GOLDEN HOUR / DESERT SUNSET with DRAMATIC CHIAROSCURO.")
                    appendLine(
                        "Texture / Materiality: BOLD, VISIBLE BRUSHSTROKES with THICK IMPASTO. Canvas texture must be apparent. Emphasize the HAND-PAINTED quality.",
                    )
                    appendLine("Aesthetic Era / Influence: American Frontier, late 19th Century romantic Western art.")
                    appendLine()
                    appendLine("**CRITICAL RENDERING NOTE:**")
                    appendLine(" - AVOID photorealism, smooth digital blending, or overly refined details.")
                    appendLine(
                        " - PRIORITIZE expressive brushwork, loose gestural strokes, and painterly interpretation over precise detail.",
                    )
                    appendLine(" - Forms should be suggested through confident brush marks, not meticulously rendered.")
                    appendLine(
                        " - Embrace the SOUL and EMOTION of oil painting—rough edges, bold color blocks, and artistic interpretation.",
                    )
                    appendLine(" - Think \"painted with passion\" not \"photographed and polished.\"")
                    appendLine()
                    appendLine("**COLOR PALETTE & ACCENT:**")
                    appendLine(
                        " - Base: Rich earthy tones (Burnt Sienna, Raw Ochre, Desert Sand), weathered leather browns, Desert Sky Blue.",
                    )
                    appendLine(" - Mandatory Accent: BURNT ORANGE / SUNSET GOLD.")
                    appendLine(
                        " - Application: Use the accent boldly for sunsets, campfire glow, dust catching light, or metallic glints. Apply with expressive brush marks.",
                    )
                    appendLine()
                    appendLine("Mood: Rugged, isolated, and stoic. A sense of vastness and freedom, captured with artistic emotion.")
                    appendLine(
                        "Ambience: Wide open spaces, heat haze, long shadows, dust motes dancing in the light. All rendered with EXPRESSIVE BRUSHWORK that captures the FEELING of the frontier, not a literal photograph.",
                    )
                }
            }

            PUNK_ROCK -> {
                buildString {
                    appendLine("**=== CRITICAL RULES - READ FIRST ===**")
                    appendLine()
                    appendLine("**STOP. BEFORE YOU WRITE ANYTHING, THESE RULES ARE ABSOLUTE:**")
                    appendLine()
                    appendLine(
                        "1. **CARTOON STYLE:** This is a CARTOON illustration like Gorillaz",
                    )
                    appendLine("   Characters must look like CARTOON CHARACTERS, not realistic humans with stylized touches.")
                    appendLine("   Think 2D animated music video, NOT concept art or portrait photography.")
                    appendLine()
                    appendLine(
                        "2. **EXPRESSIVE EYES (3 CREATIVE OPTIONS):** Eyes are the emotional anchor and MUST complement character expression.",
                    )
                    appendLine(
                        "   OPTION 1: SIMPLIFIED STYLIZED EYE - unique cartoon interpretation (e.g., almond-shaped with a line, X-eyes, single curved line)",
                    )
                    appendLine("   OPTION 2: FULL WHITE EYE WITH NO PUPIL - blank, haunting, or detached expression")
                    appendLine("   OPTION 3: BLACK VOID EYE - classic Gorillaz style, hollow and intense")
                    appendLine(
                        "   BANNED: Realistic eye colors ('brown eyes', 'blue eyes', 'cast [color] eyes', 'piercing eyes'). NO natural eye colors allowed.",
                    )
                    appendLine(
                        "   CRITICAL: Chosen eye style MUST enhance character personality and emotion. Eyes are NOT generic - they must express attitude.",
                    )
                    appendLine()
                    appendLine("3. **MANDATORY BACKGROUND:** Every image MUST have a DETAILED ENVIRONMENT.")
                    appendLine(
                        "   BANNED: \"plain background\", \"gradient background\", \"white background\", \"solid color background\".",
                    )
                    appendLine("   You MUST describe: walls, posters, graffiti, amps, neon signs, brick, concrete, etc.")
                    appendLine(
                        "   **NOTE:** For vertical/lock-screen compositions, the top area can be less cluttered (e.g., just the brick wall or sky) to allow for UI elements, but it MUST NOT be a plain solid color.",
                    )
                    appendLine()
                    appendLine("4. **NO SOFT/REALISTIC RENDERING:**")
                    appendLine("   BANNED: \"soft lighting\", \"soft and diffused\", \"gentle gradient\", \"subtle glow on skin\".")
                    appendLine("   USE: \"harsh neon\", \"hard cel-shading\", \"flat color blocks\", \"stark shadows\".")
                    appendLine()
                    appendLine("**NOW PROCEED WITH THE ART STYLE:**")
                    appendLine()
                    appendLine("---")
                    appendLine()
                    appendLine("Art Technique: Jamie Hewlett / Gorillaz")
                    appendLine(
                        "This is a 2D CARTOON with flat colors, bold outlines, and stylized proportions—like a frame from an animated music video.",
                    )
                    appendLine()
                    appendLine("**CARTOON AESTHETIC (THIS IS NOT REALISTIC ART):**")
                    appendLine(" - This looks like it belongs in a Gorillaz music video or Codename: Kids Next Door episode.")
                    appendLine(" - Characters are CARTOON CHARACTERS with exaggerated, simplified features.")
                    appendLine(" - The style is closer to ANIMATION than illustration—flat, bold, graphic.")
                    appendLine(" - Reference: 2D, Murdoc, Noodle from Gorillaz;")
                    appendLine()
                    appendLine("**LINE WORK:**")
                    appendLine(" - Bold, thick BLACK outlines—confident and slightly imperfect.")
                    appendLine(" - Variable line weight: thick to thin, with organic wobble.")
                    appendLine(" - Lines can overshoot corners or not quite connect—this is STYLISTIC, not sloppy.")
                    appendLine(" - Think: Brush pen or marker, NOT clean vector art.")
                    appendLine()
                    appendLine("**COLOR PALETTE:**")
                    appendLine(" - Base: Gritty Urban Tones—Olive Drab, Slate Grey, Faded Denim, Dirty Concrete.")
                    appendLine(" - Accent: ACID GREEN / TOXIC YELLOW (in environment only—neon signs, graffiti, amp lights).")
                    appendLine(" - Colors are FLAT with hard edges. Minimal gradients. Cel-shaded shadows.")
                    appendLine()
                    appendLine("**CHARACTER DESIGN (CARTOON PROPORTIONS - CRITICAL):**")
                    appendLine(" - **FACE:** Simplified, angular, CARTOON features. NOT realistic human faces.")
                    appendLine("   * Eyes: EXPRESSIVE AND CREATIVE. Choose ONE of 3 options that matches character emotion:")
                    appendLine(
                        "     - STYLIZED CARTOON EYE: Unique interpretation (almond-shaped, curved lines, X-eyes, etc.) - conveys personality",
                    )
                    appendLine(
                        "     - FULL WHITE EYE (NO PUPIL): Blank, detached, mysterious, haunting - perfect for melancholic or aloof characters",
                    )
                    appendLine("     - BLACK VOID EYE: Classic Gorillaz hollow intensity - defiant, cool, rebellious")
                    appendLine(
                        "   * Eye Selection Rule: Pick the eye style that BEST COMPLEMENTS the character's mood and attitude. Not generic - EXPRESS personality through eyes.",
                    )
                    appendLine("   * BANNED: Any realistic eye colors, color casting, iris detail, pupil gradients.")
                    appendLine("   * Nose: Simple line, angular shape, or small triangle. NOT realistic nostrils.")
                    appendLine("   * Mouth: Bold line, can be crooked or asymmetrical. Expressive but simple.")
                    appendLine("   * Jaw: Angular, exaggerated—sharp or blocky, NOT smooth realistic contours.")
                    appendLine(" - **BODY (IF VISIBLE):** Exaggerated cartoon proportions.")
                    appendLine("   * Limbs can be noodle-thin or chunky—NOT realistic human proportions.")
                    appendLine("   * Hands can be oversized, simplified (4-5 fingers suggested, not detailed).")
                    appendLine("   * Posture is exaggerated: hunched, slouched, angular poses.")
                    appendLine(
                        " - **OVERALL:** Characters should look like they stepped out of a Gorillaz video—stylized, cool, slightly grotesque in a \"beautiful ugly\" way.",
                    )
                    appendLine()
                    appendLine("**SHADING:**")
                    appendLine(" - Hard CEL-SHADING only. Solid blocks of shadow with sharp edges.")
                    appendLine(" - NO soft gradients, NO realistic light falloff, NO ambient occlusion.")
                    appendLine(" - Shadows are graphic shapes, not realistic light simulation.")
                    appendLine()
                    appendLine("**BACKGROUND & ENVIRONMENT (YOU MUST INCLUDE THIS):**")
                    appendLine(" - **EVERY IMAGE NEEDS A REAL BACKGROUND.** This is mandatory.")
                    appendLine(" - Pick ONE of these environments and DESCRIBE IT IN DETAIL:")
                    appendLine("   * **GARAGE:** Brick walls, band posters, amps, cables, drum kit, bare lightbulb, stickers everywhere")
                    appendLine("   * **ALLEY:** Graffiti walls, dumpster, fire escape, chain-link fence, wet pavement, neon signs")
                    appendLine("   * **STAGE:** Mic stands, monitors, harsh stage lights, haze, crowd silhouettes, banner")
                    appendLine("   * **ROOFTOP:** Water towers, antennas, city skyline, smoggy sky, AC units")
                    appendLine("   * **RECORD SHOP:** Vinyl crates, posters, neon beer sign, cluttered shelves, sticky floor")
                    appendLine("   * **STREET CORNER:** Bus stop, payphone, convenience store, street art, flickering signs")
                    appendLine(
                        " - The background should be GRAPHIC and slightly FLAT (to match the cartoon style) but DETAILED and PRESENT.",
                    )
                    appendLine(" - Characters exist IN a world, not floating in empty space.")
                    appendLine()
                    appendLine("**MOOD & ENERGY:**")
                    appendLine(" - Anarchic, rebellious, cool. The energy of a garage band or animated music video.")
                    appendLine(" - Characters have ATTITUDE—bored, manic, defiant, or \"too cool to care.\"")
                    appendLine()
                    appendLine("**MUSIC CULTURE INTEGRATION:**")
                    appendLine(" - Characters interact with musical elements: guitars, headphones, vinyl, amps, drumsticks.")
                    appendLine(" - Music gear shows wear: stickers, scratches, duct tape, sharpie marks.")
                    appendLine(" - Even non-musicians wear band tees, have gig flyers, ticket stubs.")
                    appendLine()
                    appendLine("**COMPOSITION:**")
                    appendLine(
                        " - LAYERED: Foreground objects (amp corner, mic stand), character positioned according to framing, background environment.",
                    )
                    appendLine(" - Elements can OVERLAP and BREAK THE FRAME—hair, smoke, instruments extending past edges.")
                    appendLine(" - Dynamic angles encouraged—avoid flat, static framing.")
                    appendLine(
                        " - **FRAMING FLEXIBILITY:** If the input is a PORTRAIT/HEADSHOT, maintain that framing. Do not force a full-body shot. The background should still be present but adapted to the tighter frame (e.g., a wall behind the head, a mic stand in the foreground).",
                    )
                    appendLine()
                    appendLine("**CANDID MOMENTS:**")
                    appendLine(" - Characters look CAUGHT in a moment, not posing.")
                    appendLine(" - Mid-action: lighting cigarette, checking phone, adjusting strap, mid-conversation.")
                    appendLine(" - Avoid: Direct camera stare, symmetrical poses, hands at sides.")
                    appendLine()
                    appendLine("**CHARACTER ARCHETYPE ENERGY (INSPIRATION ONLY):**")
                    appendLine(" - Adapt these vibes to each character's personality:")
                    appendLine("   * **Melancholic Dreamer:** Distant gaze, slouched, detached.")
                    appendLine("   * **Chaotic Provocateur:** Aggressive stance, sneering, confrontational.")
                    appendLine("   * **Cool Prodigy:** Effortless confidence, minimal expression.")
                    appendLine("   * **Grounded Anchor:** Solid stance, watchful, protective.")
                    appendLine("   * **Manic Energy:** Unpredictable poses, wide-eyed or grinning.")
                    appendLine("   * **Jaded Veteran:** Tired, cigarette present, cynical smirk.")
                    appendLine()
                    appendLine("**=== BANNED WORDS & PHRASES (IF YOU USE THESE, YOU FAILED) ===**")
                    appendLine()
                    appendLine(
                        "**BANNED EYE TERMS:** 'brown eyes', 'blue eyes', 'green eyes', 'cast brown eyes', 'cast [any color] eyes', 'piercing eyes', 'iris', 'pupils', 'eye color', 'natural eyes', 'realistic eyes'",
                    )
                    appendLine(
                        "→ USE: Choose ONE eye style: 'stylized cartoon eyes' (describe creatively), 'full white eyes with no pupil', or 'black void eyes' - MUST match character emotion",
                    )
                    appendLine()
                    appendLine(
                        "**BANNED ANATOMY:** 'realistic proportions', 'soft skin', 'porcelain skin', 'smooth skin', 'flawless', 'photorealistic', 'lifelike'",
                    )
                    appendLine("→ USE: 'cartoon proportions', 'angular features', 'exaggerated limbs', 'stylized'")
                    appendLine()
                    appendLine(
                        "**BANNED FACE TERMS:** 'painted with lipstick', 'glossy lips', 'symmetrical face', 'conventionally attractive', 'defined jawline' (too realistic)",
                    )
                    appendLine("→ USE: 'crooked features', 'angular cartoon face', 'simplified features', 'blocky jaw'")
                    appendLine()
                    appendLine(
                        "**BANNED LIGHTING:** 'soft lighting', 'soft and diffused', 'gentle gradient', 'subtle glow on skin', 'volumetric', 'ambient'",
                    )
                    appendLine("→ USE: 'harsh neon', 'hard cel-shading', 'flat shadows', 'stark contrast'")
                    appendLine()
                    appendLine(
                        "**BANNED BACKGROUND:** 'plain background', 'gradient background', 'isolated portrait', 'floating', 'white background'",
                    )
                    appendLine("→ USE: Describe ACTUAL OBJECTS: 'graffiti-covered brick wall', 'amp stack', 'neon sign', 'band posters'")
                    appendLine()
                    appendLine("**FINAL CHECK - ASK YOURSELF:**")
                    appendLine(" 1. Did I use realistic eye colors (brown/blue/green/cast)? → DELETE IT NOW")
                    appendLine(" 2. Did I pick ONE creative eye style that matches character emotion? → STYLIZED, WHITE-NO-PUPIL, or VOID")
                    appendLine(" 3. Do the eyes EXPRESS character personality and attitude? → If NO, choose a more expressive option")
                    appendLine(" 4. Did I describe a background with 3+ specific objects? → If NO, ADD THEM")
                    appendLine(" 5. Does this sound like a Gorillaz? → If NO, make it MORE STYLIZED")
                    appendLine()
                    appendLine(
                        "Rendering: 2D CARTOON style. Forbid: 3D, photorealism, realistic anatomy, soft lighting, empty backgrounds. Must look like a frame from an animated music video with COMPLETE backgrounds.",
                    )
                }
            }
        }

    fun appearanceGuidelines(genre: Genre): String =
        when (genre) {
            HEROES -> {
                buildString {
                    appendLine(
                        "Heroes must wear vibrant and colorful supersuits, complete with dynamic masks, iconic emblems, and functional accessories.",
                    )
                    appendLine(
                        "Emphasize a powerful, heroic silhouette and clean, distinct lines. The outfit should reflect their powers or origin.",
                    )
                }.trimIndent()
            }

            CYBERPUNK -> {
                buildString {
                    appendLine(
                        "Cyberpunk characters should feature futuristic, asymmetrical clothing, unique high-tech accessories (e.g., data readers, holographic communicators),",
                    )
                    appendLine(
                        "and distinctive cybernetic modifications on their face or limbs (e.g., glowing implants, metallic prosthetics, intricate circuitry patterns).",
                    )
                    appendLine("Their appearance should convey a blend of cutting-edge technology and gritty street style.")
                }.trimIndent()
            }

            COWBOY -> {
                buildString {
                    appendLine(
                        "Cowboy characters should wear classic western outfits: wide-brimmed hats, bandanas, rugged boots, leather vests, and denim or canvas clothing.",
                    )
                    appendLine(
                        "Accessories like spurs, holsters, and sheriff badges are common. Their look should evoke the spirit of the frontier and rural Americana.",
                    )
                }.trimIndent()
            }

            SHINOBI -> {
                buildString {
                    appendLine(
                        "Shinobi characters should wear traditional ninja or samurai-inspired attire: dark, layered robes, hakama pants, arm guards, tabi boots, and headbands or masks.",
                    )
                    appendLine(
                        "Outfits may include subtle armor pieces and sashes. Their appearance should be stealthy, agile, and rooted in feudal Japanese aesthetics.",
                    )
                }.trimIndent()
            }

            CRIME -> {
                buildString {
                    appendLine(
                        "Crime city characters should dress in High-End Luxury Resort Wear: tailored linen suits, flowing silk dresses, designer swimwear, and expensive accessories.",
                    )
                    appendLine(
                        "Think 'Old Money' aesthetic, yacht parties, and exclusive beach clubs. Outfits should feature premium fabrics, elegant cuts, and statement pieces like oversized sunglasses, gold jewelry, and designer watches.",
                    )
                    appendLine("Their appearance must radiate wealth, sophistication, and effortless glamour.")
                }.trimIndent()
            }

            HORROR -> {
                buildString {
                    appendLine(
                        "Horror characters should wear worn, distressed clothing in muted or dark tones. Outfits may include tattered coats, faded uniforms, or vintage garments.",
                    )
                    appendLine(
                        "Their appearance should evoke unease, mystery, and psychological tension, fitting the haunted and uncanny mood.",
                    )
                }.trimIndent()
            }

            FANTASY -> {
                buildString {
                    appendLine(
                        "Fantasy characters should wear Renaissance-inspired elegant attire: flowing robes with rich fabrics, ornate tunics with detailed embroidery, graceful cloaks, renaissance gowns, classical togas, or ceremonial garments.",
                    )
                    appendLine(
                        "Accessories include golden amulets, jeweled circlets, elegant sashes, ornate belts, mystical pendants, and ceremonial staffs. Fabrics should be luxurious: silk, velvet, brocade in crimson reds, golds, and warm earth tones.",
                    )
                    appendLine(
                        "Their appearance should evoke ethereal beauty, classical elegance, divine grace, and timeless mysticism - like figures from Renaissance paintings by Botticelli or Raphael.",
                    )
                    appendLine(
                        "AVOID: Heavy armor, gritty leather, dark tones, battle-worn gear. PREFER: Flowing fabrics, ornate details, warm colors, elegant silhouettes.",
                    )
                }.trimIndent()
            }

            SPACE_OPERA -> {
                buildString {
                    appendLine(
                        "Space opera characters should wear retro-futuristic suits, sleek uniforms, and bold accessories. Outfits may include metallic fabrics, capes, visors, and utility belts.",
                    )
                    appendLine("Their appearance should evoke classic sci-fi adventure, interstellar travel, and atomic age optimism.")
                }.trimIndent()
            }

            PUNK_ROCK -> {
                buildString {
                    appendLine(
                        "Punk rock characters must be stylized and caricatured, NOT realistic. Use exaggerated proportions (lanky or stocky), simple dot eyes, and expressive, jagged features.",
                    )
                    appendLine(
                        "Clothing: leather jackets, ripped tees, combat boots, and vibrant dyed hair (acid green, toxic yellow). The look is 'ugly-cute', rebellious, and distinct.",
                    )
                }.trimIndent()
            }
        }

    fun nameDirectives(genre: Genre) =
        (
            when (genre) {
                FANTASY -> {
                    buildString {
                        appendLine("Aim for names that evoke ethereal beauty, classical elegance, and timeless mysticism.")
                        appendLine("Consider influences from Renaissance literature, classical mythology (Greek, Roman, Italian),")
                        appendLine(
                            "medieval poetry, and melodious Italian/Latin names (e.g., Beatrice, Dante, Seraphina, Lorenzo, Celestina, Alessandro).",
                        )
                        appendLine("Names should be elegant, flowing, poetic, and evoke a sense of divine grace or ancient nobility.")
                        appendLine("Prefer soft consonants and vowel-rich sounds that feel romantic and lyrical.")
                        appendLine("AVOID: Harsh or guttural names, overly modern names, tech-sounding names, dark or menacing names.")
                        appendLine("EMBRACE: Names that could belong to Renaissance nobles, saints, angels, or classical heroes.")
                    }
                }

                CYBERPUNK -> {
                    buildString {
                        appendLine("- Aim for names that blend futuristic, cyberpunk, or slightly exotic sounds.")
                        appendLine("- Consider influences from Japanese, tech-inspired, or gritty Western phonetics.")
                        appendLine("Avoid names that are overtly heroic or melodramatic.")
                        appendLine("Try to create names that is common in the language ${currentLanguage()} .")
                    }
                }

                HORROR -> {
                    buildString {
                        appendLine("- Aim for names that evoke a sense of unease and dread, fitting a grim, dark, or mysterious setting.")
                        appendLine("- For human characters, use common, simple, and contemporary names from ${currentLanguage()} language.")
                        appendLine("The horror comes from the mundane.")
                        appendLine(
                            "- For creatures, entities, or local myths, use names that are descriptive (e.g., \"O Vulto,\" \"A Dama de Preto,\" \"O Sussurro\"), guttural, or have a more complex, unsettling feel.",
                        )
                        appendLine("- Avoid names that are overtly heroic, futuristic, or melodramatic.")
                    }
                }

                HEROES -> {
                    buildString {
                        appendLine("- Aim for names that feel grounded, contemporary, and reflect a diverse urban environment.")
                        appendLine("- Consider influences from street culture, hip-hop, graffiti art, and modern city life.")
                        appendLine("- Names should be cool, edgy, and slightly mysterious, hinting at a hidden identity.")
                        appendLine("- Blend common names with unique nicknames or shortened versions.")
                        appendLine("- Avoid overly fantastical, archaic, or overtly heroic names.")
                        appendLine("- Try to create names that are common in the language ${currentLanguage()},")
                        appendLine("but with a modern twist or a unique nickname.")
                        appendLine("- Consider names that evoke a sense of agility, speed, or resourcefulness.")
                        appendLine("- Think about names that could easily become a street tag or a whispered legend.")
                    }
                }

                CRIME -> {
                    buildString {
                        appendLine("- Aim for names fitting a crime drama set in a stylized neon city.")
                        appendLine("- Blend gritty street nicknames with classic, timeless first names.")
                        appendLine(
                            "- Consider influences from 80s Miami/LA crime fiction, Latin and Anglo names common in ${currentLanguage()} locales.",
                        )
                        appendLine(
                            "- Short, punchy monikers or evocative aliases work well (e.g., \"Vega\", \"Neon\", \"Santos\", \"Roxie\").",
                        )
                        appendLine("- Avoid overtly sci-fi or fantasy elements.")
                    }
                }

                SHINOBI -> {
                    buildString {
                        appendLine("- Aim for names rooted in feudal Japan or stylized adaptations that fit the setting.")
                        appendLine(
                            "- Consider short, evocative names or clan-like monikers (e.g., \"Aka-ryu\", \"Kage\", \"Hanae\", \"Shirogane\").",
                        )
                        appendLine(
                            "- Blend historical Japanese-sounding names with terse nicknames suitable for operatives and covert figures.",
                        )
                        appendLine("- Avoid overtly modern slang or sci-fi terminology.")
                    }
                }

                SPACE_OPERA -> {
                    buildString {
                        appendLine(
                            "Concept: Evoke exploration, cosmic significance, advanced scientific concepts, or ancient, wise origins.",
                        )
                        appendLine(
                            "Influences: Classical astronomy, mythological figures (adapted for space), scientific terms, melodious and ethereal sounds, names suggesting vastness.",
                        )
                        appendLine(
                            "Avoid: Overtly aggressive or militaristic names, overly \"hard\" sci-fi jargon (unless for specific tech), modern slang.",
                        )
                        appendLine("Try: Names with soft vowels and unique consonant combinations (e.g., Lyra, Orion, Xylos, Aetheria).")
                    }
                }

                COWBOY -> {
                    buildString {
                        appendLine("Aim for names that sound rugged, biblical, or have a nickname quality.")
                        appendLine("Examples: Jed, Silas, \"Tex\", \"Slim\", Ezekiel, Clementine.")
                        appendLine("Avoid modern or overly fancy names.")
                    }
                }

                PUNK_ROCK -> {
                    buildString {
                        appendLine("Aim for names influenced by music culture, street-smart, and edgy.")
                        appendLine(
                            "Consider: Music-culture nicknames, street-smart names, modern edgy sounds, names that reference music terms or artist-like qualities.",
                        )
                        appendLine(
                            "Examples: Echo, Vinyl, Riff, Chord, Neon, Sage, Blaze, Sonic, Rebel, Riot, or street nicknames reflecting personality or musical style.",
                        )
                        appendLine("Avoid: Overly heroic names, fantasy-sounding names, corporate-sounding names.")
                        appendLine("Embrace: Short, punchy names that feel authentic to youth and music culture.")
                    }
                }
            }
        ).plus("Try common names in ${currentLanguage()}").trimIndent()

    fun conversationDirective(genre: Genre) =
        when (genre) {
            FANTASY -> {
                buildString {
                    appendLine("This directive defines the specific linguistic style for the Fantasy genre - Ethereal Renaissance style.")
                    appendLine(
                        "NPCs and narrative voice should evoke classical elegance, divine beauty, mystical wonder, and Renaissance-era sophistication.",
                    )
                    appendLine()
                    appendLine("1.  Language & Vocabulary:")
                    appendLine(
                        "    * Terminology: Incorporate terms related to classical beauty, divine magic, Renaissance arts, celestial realms, and mystical wonders (e.g., \"divine,\" \"celestial,\" \"radiant,\" \"ethereal,\" \"grace,\" \"luminous,\" \"sacred,\" \"enchantment,\" \"mystique,\" \"renaissance\").",
                    )
                    appendLine(
                        "    * Formality: Dialogue should be elegant and refined (inspired by Renaissance literature and classical poetry). Use poetic language, melodious phrasing, and sophisticated vocabulary.",
                    )
                    appendLine(
                        "    * Classical Phrasing: Use elegant, flowing language reminiscent of Renaissance poetry and classical literature (e.g., \"verily,\" \"behold,\" \"most gracious,\" \"fair,\" \"blessed,\" \"wondrous\") - apply with refinement and beauty.",
                    )
                    appendLine(
                        "    * Profanity: Profanity should be AVOIDED entirely. Replace with elegant exclamations or poetic expressions of emotion.",
                    )
                    appendLine()
                    appendLine("2.  Tone & Delivery:")
                    appendLine("    * Ethereal & Graceful: The tone should be serene, majestic, and filled with wonder and reverence.")
                    appendLine(
                        "    * Divine & Inspiring: Characters might speak with awe towards beauty, divine grace, celestial powers, or sacred mysteries.",
                    )
                    appendLine(
                        "    * Wisdom & Poetry: Characters should speak with lyrical beauty, using metaphors drawn from nature, art, and the heavens.",
                    )
                    appendLine("    * Pacing: Dialogue should flow like poetry, with elegant rhythm and measured contemplation.")
                    appendLine()
                    appendLine("3.  Narrative Voice:")
                    appendLine(
                        "    * Descriptions should be luminous and painterly, focusing on crimson skies, golden light, flowing fabrics, Renaissance architecture, gardens of roses, and ethereal beauty.",
                    )
                    appendLine(
                        "    * Emphasize warm colors (crimson reds, radiant golds), soft lighting, and classical elegance in all descriptions.",
                    )
                    appendLine("    * Maintain a sense of divine wonder, timeless beauty, and spiritual serenity throughout the narrative.")
                    appendLine(
                        "    * AVOID: Dark imagery, gritty descriptions, battle scenes, grim settings. EMBRACE: Beauty, grace, harmony, and celestial majesty.",
                    )
                }.trimIndent()
            }

            CYBERPUNK -> {
                buildString {
                    appendLine("This directive defines the specific linguistic style for the Cyberpunk/Dystopian Sci-Fi genre.")
                    appendLine("NPCs and narrative voice should reflect a gritty, tech-infused, and often cynical tone.")
                    appendLine()
                    appendLine("1.  Language & Vocabulary:")
                    appendLine(
                        "    * Terminology: Freely use tech jargon, hacking terms, corporate slang, and futuristic street argot (e.g., \"net-runner,\" \"chrome,\" \"synth-skin,\" \"data-jack,\" \"augment,\" \"glitch,\" \"gig\").",
                    )
                    appendLine(
                        "    * Formality: Conversations can range from casual to aggressively direct. Formal language is rare, often reserved for corporate figures or those trying to exert power.",
                    )
                    appendLine("    * Slang & Idioms: Incorporate contemporary or invented cyberpunk-specific slang and idioms.")
                    appendLine(
                        "    * Profanity (Conditional): If appropriate for the character's personality and the grim nature of the setting, moderate use of mild to strong profanity is acceptable to enhance realism and grit. Use it sparingly for impact, not gratuitously.",
                    )
                    appendLine()
                    appendLine("2.  Tone & Delivery:")
                    appendLine(
                        "    * Cynicism & Weariness: Many characters should reflect a sense of disillusionment, world-weariness, or cynicism towards authority and the system.",
                    )
                    appendLine("    * Directness: Dialogues can be blunt, terse, and to the point.")
                    appendLine("    * Suspicion: Characters might often be guarded, suspicious, or secretive in their speech.")
                    appendLine("    * Pacing: Dialogue can be fast-paced, reflecting the urgency and high-stakes environment.")
                    appendLine()
                    appendLine("3.  Narrative Voice:")
                    appendLine(
                        "    * Descriptions should be sharp, often highlighting the decay, neon glow, advanced tech, and disparity of the dystopian future.",
                    )
                    appendLine("    * Maintain an edgy, sometimes detached, perspective.")
                }.trimIndent()
            }

            HORROR -> {
                buildString {
                    appendLine(
                        "This directive defines the specific linguistic style for the Horror genre, blending cosmic dread with grounded, psychological terror.",
                    )
                    appendLine("The tone should evoke a sense of unease, psychological tension, and the creeping dread of the unknown.")
                    appendLine()
                    appendLine("1.  Language & Vocabulary:")
                    appendLine(
                        "    * Terminology: Use language that ranges from the mundane to terms that suggest the occult, the inexplicable, or a descent into madness (e.g., \"whisper,\" \"ritual,\" \"cyclopean,\" \"non-Euclidean,\" \"anomaly,\" \"sanity erodes\").",
                    )
                    appendLine(
                        "    * Formality: Dialogue can be casual and realistic (like everyday people), but the narration and tone can become more formal or clinical when describing the horror, creating a chilling contrast.",
                    )
                    appendLine(
                        "    * Phrasing: Use phrases that allude to, but do not explicitly describe, the horror, focusing on how characters perceive the threat.",
                    )
                    appendLine(
                        "    * Profanity (Conditional): Profanity should be used realistically, sparingly, and contextually to reflect a character's stress and terror.",
                    )
                    appendLine()
                    appendLine("2.  Tone & Delivery:")
                    appendLine(
                        "    * Psychological Dread: The tone should build tension and paranoia. Characters should express fear, suspicion, and a gradual decline in their mental state.",
                    )
                    appendLine(
                        "    * Mundane vs. Sinister: The tone should highlight the contrast between a seemingly normal environment and the subtle, growing threat lurking beneath the surface.",
                    )
                    appendLine(
                        "    * Desperation: Dialogue should, over time, reflect a sense of urgency, desperation, and a growing helplessness against the unknown.",
                    )
                    appendLine(
                        "    * Pacing: The pace should be slow and deliberate at first to build suspense, accelerating during moments of climax or revelation.",
                    )
                    appendLine()
                    appendLine("3.  Narrative Voice:")
                    appendLine(
                        "    * Descriptions should be detailed but focus on small, everyday elements that become sinister (e.g., a creak in the floor, a shadow in the corner).",
                    )
                    appendLine(
                        "    * The narrative should maintain a sense that reality is distorting and the threat is something the human mind can barely comprehend, drawing from cosmic horror.",
                    )
                    appendLine(
                        "    * Avoid explicit and graphic descriptions of the horror, opting instead to hint at what is indescribable to heighten the reader's fear.",
                    )
                }.trimIndent()
            }

            HEROES -> {
                buildString {
                    appendLine("This directive defines the specific linguistic style for the Urban Hero genre.")
                    appendLine(
                        "NPCs and narrative voice should reflect a contemporary, street-smart, and often gritty tone, blending realism with a sense of hidden potential.",
                    )
                    appendLine()
                    appendLine("1.  Language & Vocabulary:")
                    appendLine(
                        "    * Terminology: Incorporate contemporary slang, street jargon, and terms related to urban life, parkour, technology (but not overly futuristic), and local landmarks (e.g., \"spot,\" \"crew,\" \"grind,\" \"flow,\" \"tag,\" \"wire,\" \"glitch,\" \"the block\").",
                    )
                    appendLine(
                        "    * Formality: Dialogue should generally be informal and conversational, reflecting the way people actually speak in a city. Vary formality based on character age, background, and social standing.",
                    )
                    appendLine(
                        "    * Slang & Idioms: Use contemporary slang and idioms authentically, but avoid overly trendy terms that might quickly date the dialogue.",
                    )
                    appendLine(
                        "    * Profanity (Conditional): Moderate use of profanity is acceptable to enhance realism and character authenticity, but avoid gratuitous or excessive swearing. Use it strategically for impact.",
                    )
                    appendLine()
                    appendLine("2.  Tone & Delivery:")
                    appendLine(
                        "    * Street-Smart & Resourceful: Characters should sound quick-witted, adaptable, and capable of navigating the urban landscape.",
                    )
                    appendLine("    * Cynicism & Hope: A blend of cynicism about the system and a glimmer of hope for making a difference.")
                    appendLine(
                        "    * Directness & Authenticity: Dialogue should be direct and honest, avoiding overly dramatic or flowery language.",
                    )
                    appendLine("    * Pacing: Dialogue can be fast-paced and energetic, reflecting the rhythm of city life.")
                    appendLine()
                    appendLine("3.  Narrative Voice:")
                    appendLine(
                        "    * Descriptions should be vivid and detailed, focusing on the sights, sounds, and smells of the city. Highlight the contrast between beauty and decay, opportunity and danger.",
                    )
                    appendLine("    * Maintain a sense of realism and groundedness, even when describing extraordinary events.")
                    appendLine(
                        "    * Focus on the human element – the struggles, dreams, and resilience of the people who live in the city.",
                    )
                    appendLine(
                        "    * The narrative should subtly hint at the hidden potential and extraordinary abilities that exist beneath the surface of everyday life.",
                    )
                }.trimIndent()
            }

            CRIME -> {
                buildString {
                    appendLine("This directive defines the specific linguistic style for the Crime City genre.")
                    appendLine("NPCs and narration should evoke 80s crime drama with a neon-soaked, Miami Vice mood.")
                    appendLine()
                    appendLine("1. Language & Vocabulary:")
                    appendLine(
                        "    * Terminology: Use crime and street terms (e.g., \"stakeout\", \"heat\", \"hustle\", \"dirty money\", \"dealer\", \"detective\", \"vice squad\").",
                    )
                    appendLine(
                        "    * Formality: Conversational and direct. Cops may be clipped and procedural; criminals can be slick, terse, or menacing.",
                    )
                    appendLine("    * Slang & Idioms: Period-appropriate 80s flavor where possible; avoid modern internet slang.")
                    appendLine("    * Profanity (Conditional): Moderate and contextual—used for grit, not excess.")
                    appendLine()
                    appendLine("2. Tone & Delivery:")
                    appendLine("    * Cool, tense, and stylish. Understated bravado with subtext; terse exchanges and loaded pauses.")
                    appendLine("    * Noir sensibility meets pop neon. Melancholic glamour and danger.")
                    appendLine("    * Pacing: Snappy during action or interrogation; laconic and moody between beats.")
                }.trimIndent()
            }

            SPACE_OPERA -> {
                buildString {
                    appendLine("Vocabulary: Galactic exploration, profound discoveries, cosmic phenomena,")
                    appendLine("ancient alien civilizations, advanced technology, philosophical ponderings about existence.")
                    appendLine(
                        "Formality: Varies from adventurous and eloquent (explorers, scientists) to mysterious and ancient (alien entities).",
                    )
                    appendLine("Phrasing: Evocative and grand, with a sense of wonder and epic scope.")
                    appendLine("Tone: Aspirational, mysterious, awe-inspiring, adventurous, contemplative.")
                }.trimIndent()
            }

            SHINOBI -> {
                buildString {
                    appendLine("This directive defines the specific linguistic style for the Shinobi (Mythical Feudal Japan) genre.")
                    appendLine("NPCs and narrative voice should evoke a sense of discipline, tradition, and underlying tension.")
                    appendLine()
                    appendLine("1.  Language & Vocabulary:")
                    appendLine(
                        "    * Terminology: Use terms related to feudal Japan, martial arts, espionage, and honor (e.g., \"shogun,\" \"daimyo,\" \"samurai,\" \"ronin,\" \"kunoichi,\" \"jutsu,\" \"katana,\" \"oni,\" \"yokai\").",
                    )
                    appendLine(
                        "    * Formality: Dialogue should be respectful and often formal, reflecting the hierarchical society. Use honorifics where appropriate (e.g., \"-san,\" \"-sama\").",
                    )
                    appendLine("    * Phrasing: Sentences are often concise and deliberate. Avoid unnecessary words.")
                    appendLine("    * Profanity (Conditional): Extremely rare. Insults are more about dishonor than vulgarity.")
                    appendLine()
                    appendLine("2.  Tone & Delivery:")
                    appendLine(
                        "    * Reserved & Disciplined: Characters speak with restraint and precision. Emotion is shown through subtext, not overt displays.",
                    )
                    appendLine("    * Tense & Mysterious: A constant undercurrent of suspicion, hidden motives, and political intrigue.")
                    appendLine("    * Respectful & Traditional: Speech reflects a deep respect for tradition, duty, and honor.")
                    appendLine("    * Pacing: Dialogue can be slow and measured, with meaningful pauses.")
                    appendLine()
                    appendLine("3.  Narrative Voice:")
                    appendLine(
                        "    * Descriptions should focus on atmosphere—the rustle of bamboo, the glint of a blade in moonlight, the quiet tension of a room.",
                    )
                    appendLine("    * Maintain a sense of quiet grace and lethal potential.")
                }.trimIndent()
            }

            COWBOY -> {
                buildString {
                    appendLine("This directive defines the linguistic style for the Cowboys genre.")
                    appendLine("NPCs and narrative voice should be laconic, stoic, and flavored with Western slang.")
                    appendLine()
                    appendLine("1. Language & Vocabulary:")
                    appendLine(
                        "    * Terminology: Use Western slang (e.g., \"reckon\", \"howdy\", \"yonder\", \"fixin' to\", \"varmint\").",
                    )
                    appendLine("    * Formality: Casual but respectful (e.g., \"Ma'am\", \"Sir\").")
                    appendLine("    * Phrasing: Simple, direct, and often colorful idioms.")
                    appendLine()
                    appendLine("2. Tone & Delivery:")
                    appendLine("    * Laconic & Stoic: Characters speak only when necessary. \"Strong silent type\".")
                    appendLine("    * Drawl: Implied slow, deliberate speech pattern.")
                    appendLine("    * Grit: A sense of toughness and resilience.")
                    appendLine()
                    appendLine("3. Narrative Voice:")
                    appendLine("    * Descriptions should emphasize the harshness and beauty of the frontier.")
                    appendLine("    * Focus on sensory details: heat, dust, the smell of leather and horses.")
                }.trimIndent()
            }

            PUNK_ROCK -> {
                buildString {
                    appendLine("This directive defines the specific linguistic style for the Punk Rock genre.")
                    appendLine(
                        "NPCs and narrative voice should reflect confident, rebellious, and energetic youth culture with music as a central theme.",
                    )
                    appendLine()
                    appendLine("1. Language & Vocabulary:")
                    appendLine(
                        "    * Terminology: Music culture terminology (gig, jam, beat, riff, distortion, reverb, amplifier, stage, crowd, vibe).",
                    )
                    appendLine(
                        "    * Contemporary Street Slang: Use modern, casual slang authentically (avoiding overly trendy terms that will date quickly).",
                    )
                    appendLine(
                        "    * Band/Music References: Characters may reference bands, songs, concerts, or music genres naturally in dialogue.",
                    )
                    appendLine("    * Youth Culture Terms: Terms reflecting teenage/young adult experience, independence, creativity.")
                    appendLine(
                        "    * Formality: Minimal to none. Dialogue is informal, conversational, direct. No \"sir/ma'am\" unless ironic.",
                    )
                    appendLine(
                        "    * Profanity (Conditional): Moderate profanity is acceptable to reflect authenticity of youth culture and rebellious spirit. Use strategically, not gratuitously.",
                    )
                    appendLine()
                    appendLine("2. Tone & Delivery:")
                    appendLine(
                        "    * Confident & Rebellious: Characters speak with conviction and a defiant edge. They question authority and embrace individuality.",
                    )
                    appendLine(
                        "    * Irreverent & Playful: Humor is sarcastic, witty, self-deprecating. Characters don't take themselves too seriously.",
                    )
                    appendLine(
                        "    * Passionate: When discussing music, art, or causes they care about, characters become animated and intense.",
                    )
                    appendLine(
                        "    * Fast-Paced & Energetic: Dialogue is quick, dynamic, reflecting the energy of live music and youth culture.",
                    )
                    appendLine(
                        "    * Authentic & Real: Dialogue should feel genuine to how teenagers/young adults actually speak—casual, stream-of-consciousness, with interruptions and tangents.",
                    )
                    appendLine()
                    appendLine("3. Narrative Voice:")
                    appendLine(
                        "    * Descriptions should be vivid and energetic, focusing on sensory details: the roar of crowd, the crunch of amplifiers, the smell of a crowded venue, sweat and electricity.",
                    )
                    appendLine(
                        "    * Emphasize movement, dynamism, and visual energy—people dancing, musicians performing, creative expression in action.",
                    )
                    appendLine(
                        "    * Maintain a sense of youthful optimism mixed with edgy rebellion—idealism tempered with street-smart attitude.",
                    )
                    appendLine("    * The narrative should capture the exhilaration and freedom of music and creative self-expression.")
                }.trimIndent()
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

            HEROES -> com.ilustris.sagai.core.ai.model.ReviewerStrictness.CONSERVATIVE

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
    fun validationRules(genre: Genre): String =
        when (genre) {
            PUNK_ROCK -> {
                """
                **CRITICAL VALIDATION RULES FOR PUNK_ROCK:**
                
                BANNED TERMS (ZERO TOLERANCE):
                - Realistic eye colors: 'brown eyes', 'blue eyes', 'green eyes', 'cast [color] eyes', 'piercing eyes', 'iris', 'pupils', 'natural eyes'
                - Realistic anatomy: 'realistic proportions', 'soft skin', 'porcelain skin', 'photorealistic', 'lifelike'
                - Soft lighting: 'soft lighting', 'soft and diffused', 'gentle gradient', 'subtle glow', 'volumetric'
                - Empty backgrounds: 'plain background', 'gradient background', 'solid color background', 'white background', 'isolated portrait'
                
                REQUIRED ELEMENTS:
                - Eyes MUST be ONE of: 'gorillaz like eyes', 'simple black eyes with black pupil with stylized outline' (describe creatively), 'full white eyes with no pupil', or 'black void eyes'
                - Eyes MUST EXPRESS character emotion: melancholic, rebellious, cool, detached, chaotic, etc.
                - Background MUST include 3+ specific objects: 'graffiti wall', 'amp stack', 'neon sign', 'band posters', 'brick wall', etc.
                - Proportions MUST be: 'cartoon proportions', 'exaggerated limbs', 'angular features', 'stylized'
                - Shading MUST be: 'hard cel-shading', 'flat shadows', 'stark contrast', 'graphic blocks'
                
                ACCENT COLOR ENFORCEMENT (MAJOR):
                - Mandatory Accent: ACID GREEN / TOXIC YELLOW
                - Application: Environment ONLY (neon signs, graffiti, amp lights)
                - Prompt MUST mention: 'acid green', 'toxic yellow', 'neon green', 'lime green glow', or similar environmental accent
                - If missing accent → MAJOR VIOLATION: Add environmental green/yellow accent (e.g., 'acid green neon sign', 'toxic yellow graffiti tag')
                
                BACKGROUND ENFORCEMENT (CRITICAL):
                - EVERY image MUST have detailed environment - NO EXCEPTIONS
                - Portrait/Close-up: Background adapted (wall, neon glow, posters visible behind head)
                - If prompt says 'plain', 'gradient', 'solid color', or 'empty' background → CRITICAL VIOLATION
                - Reviewer MUST add specific environment (garage/alley/stage/rooftop/record shop/street corner)
                
                EYE EXPRESSIVENESS ENFORCEMENT (CRITICAL):
                - Eyes are NOT generic design elements - they MUST complement and enhance character emotion
                - If eye description is vague or disconnected from character mood → CORRECTION REQUIRED
                - If choosing between the 3 eye options: Verify that chosen style matches character archetype (stylized for unique personality, white for detached/mysterious, void for rebellious/intense)
                - Eyes should be a KEY VISUAL EXPRESSION of attitude and emotion
                
                AUTONOMOUS FIX PATTERNS:
                - If contains: realistic eyes, natural eye colors → REPLACE with: ONE of three creative options (stylized/white-no-pupil/void) that matches character
                - If eyes are generic/unexpressive → ENHANCE description and ensure emotion is conveyed through eye choice
                - If contains: soft skin, photorealistic → REPLACE with: cartoon style, exaggerated proportions
                - If contains: soft lighting, gentle gradients, realistic rendering → REPLACE with: harsh cel-shading, flat shadows, stark contrast
                - If contains: plain/gradient background, white background → ADD: specific environment with 3+ objects (amp stack, graffiti wall, neon sign, band posters, brick wall, etc.)
                - If contains: no acid green/yellow → INTEGRATE: acid green neon sign OR toxic yellow graffiti tag in environment
                - If animation/video game/anime references appear inconsistently → STANDARDIZE to: Gorillaz/KND cartoon style throughout
                """.trimIndent()
            }

            HORROR -> {
                """
                **CRITICAL VALIDATION RULES FOR HORROR:**
                
                BANNED TERMS:
                - Modern rendering: 'photorealistic', '3D render', 'smooth gradients', 'high-res textures'
                - Vibrant colors: avoid bright, saturated colors outside the dark/pale blue palette
                - Empty backgrounds: 'plain background', 'gradient background', 'empty void', 'isolated figure'
                
                REQUIRED ELEMENTS:
                - Art style MUST mention: '32-BIT PIXEL ART', 'BLOCKY SHADING', 'PS1/SEGA SATURN aesthetic'
                - Texture MUST be: 'pixelated', 'retro game art', 'low-resolution'
                - Palette MUST be: 'limited dark and pale blue', 'ash gray', 'faded cerulean'
                - Background MUST include eerie environment: abandoned hospital, decrepit house, foggy street, dark forest, etc.
                
                ACCENT COLOR ENFORCEMENT (MAJOR):
                - Mandatory Accent: ASH GRAY / FADED CERULEAN BLUE
                - Application: Subtle highlights, reflections, chilling atmosphere
                - Prompt should mention: 'ash gray highlights', 'faded cerulean', 'pale blue glow', 'cold gray tones', or similar eerie accents
                - If missing → MAJOR VIOLATION: Add subtle accent (e.g., 'faded cerulean glow', 'ash gray highlights on edges')
                
                BACKGROUND ENFORCEMENT (CRITICAL):
                - EVERY image MUST have detailed horror environment - NO EXCEPTIONS
                - Describe 3+ specific eerie elements (flickering lights, peeling walls, swirling mist, etc.)
                - If prompt lacks environmental details → CRITICAL VIOLATION, reviewer adds horror setting
                
                AUTONOMOUS FIX PATTERNS:
                - If contains: photorealistic, smooth gradients, high-res textures, 3D render → REPLACE with: 32-bit pixel art, blocky shading, retro game aesthetic
                - If contains: bright vibrant colors → REPLACE with: dark blues, pale blues, ash greys only
                - If contains: gore, explicit violence → SHIFT to: psychological horror, subtle unease, atmosphere over explicit detail
                - If contains: plain background, gradient background, empty void, isolated figure → ADD: detailed eerie environment (abandoned hospital/decrepit house/foggy street/dark forest/tunnel/graveyard)
                - If missing oppressive mood → ADD: heavy shadows, volumetric fog, low-key lighting, claustrophobic framing
                - If missing accent colors → INTEGRATE: ash gray highlights OR faded cerulean glow in shadows/reflections
                """
            }

            CYBERPUNK -> {
                """
                **CRITICAL VALIDATION RULES FOR CYBERPUNK:**
                
                BANNED TERMS:
                - Modern techniques: '3D CGI', 'photorealism', 'digital smoothing', 'Unreal Engine', 'volumetric lighting'
                - Bright neon: 'bright fluorescent pink', 'bright purple' (only DEEP purple allowed)
                - Soft rendering: 'soft gradients', 'ambient occlusion', 'subsurface scattering'
                - Empty backgrounds: 'plain background', 'gradient background', 'empty void'
                
                REQUIRED ELEMENTS:
                - Art style MUST mention: '1980s Anime OVA', 'Cel Animation', 'FLAT SHADING'
                - Shadows MUST be: 'hard-edged blocks', 'cel-shaded', 'no soft gradients'
                - Colors MUST be: 'muted blue tones', 'slate blue', 'cold steel', 'DEEP purple accent'
                - Lighting MUST be: 'high-contrast Rembrandt', 'hard rim light', 'cast shadows'
                - Background MUST include cyberpunk environment: neon signs, holographic ads, tech infrastructure, etc.
                
                ACCENT COLOR ENFORCEMENT (MAJOR):
                - Mandatory Accent: DEEP PURPLE (NOT bright/fluorescent)
                - Application: Deep shadow casting, low-light environmental glow, muted interface lights
                - Prompt MUST mention: 'deep purple', 'bruised purple', 'dark violet', 'metallic purple shadows', or similar muted purple
                - FORBIDDEN: 'bright purple', 'neon purple', 'fluorescent purple', 'hot pink'
                - If missing or wrong → MAJOR VIOLATION: Add/correct to deep muted purple (e.g., 'deep purple shadow casting', 'bruised violet glow')
                
                BACKGROUND ENFORCEMENT (CRITICAL):
                - EVERY image MUST have detailed cyberpunk environment
                - Describe 3+ specific elements (neon signs, data screens, cables, rain-slicked pavement, etc.)
                - If prompt lacks environmental details → MAJOR VIOLATION, reviewer adds cyberpunk setting

                AUTONOMOUS FIX PATTERNS:
                - If contains: modern anime, bright/fluorescent colors, smooth gradients → REPLACE with: 1980s OVA cel style, muted blues, hard cel shading
                - If contains: photorealism, smooth blending, 3D rendering, Unreal Engine → REPLACE with: flat cel shading, hard shadows, analog film grain
                - If contains: bright pink, neon colors (not deep purple) → REPLACE with: muted purples, bruised tones, metallic purple shades
                - If contains: soft lighting, gradient shadows → REPLACE with: high-contrast Rembrandt lighting, hard-edged cast shadows, strong rim light
                - If contains: plain/gradient background, empty void → ADD: detailed cyberpunk environment (neon street/corporate interior/undercity slums/tech lab/rooftop/transit hub)
                - If eyes lack expression or detail → ENSURE: large anime eyes with prominent specular highlights per facial features specs
                """
            }

            HEROES -> {
                """
                **VALIDATION RULES FOR HEROES:**
                
                BANNED TERMS:
                - Avoid: forced night scenes (prefer dynamic daytime/sunset)
                - Empty backgrounds: 'plain background', 'gradient background', 'isolated figure'
                
                REQUIRED ELEMENTS:
                - Art style: 'Modern comic book art', 'bold ink lines', 'dynamic foreshortening'
                - Environment: 'vast cityscapes', 'towering skyscrapers', 'dizzying perspectives'
                - Colors: 'dynamic urban palette' with 'subtle electric blue accent'
                - Background MUST emphasize urban scale and vertical architecture
                
                ACCENT COLOR ENFORCEMENT (MODERATE):
                - Mandatory Accent: SUBTLE ELECTRIC BLUE
                - Application: Sky details, glass/water reflections, subtle RIM LIGHTING, or small details on accessories/tech
                - Should be UNIFYING element, NOT dominant wash
                - Prompt should mention: 'electric blue sky', 'blue glass reflections', 'subtle blue rim light', or similar atmospheric blue
                - If missing → MODERATE VIOLATION: Suggest blue accent in sky or reflections (e.g., 'electric blue sky tones', 'glass catching blue light')
                
                BACKGROUND ENFORCEMENT (MAJOR):
                - EVERY image MUST have detailed urban environment
                - Describe cityscape with towering buildings, dizzying perspectives
                - If prompt lacks environmental context → MAJOR VIOLATION, reviewer adds urban setting
                
                AUTONOMOUS FIX PATTERNS:
                - If contains: photorealism, hyper-detailed realistic rendering → REPLACE with: comic book stylization, bold ink lines, illustrated aesthetics
                - If contains: fantasy elements, medieval setting, castles → REPLACE with: modern urban/futuristic architecture, contemporary tech, cityscape
                - If accent color is missing or weak → INTEGRATE: subtle electric blue in sky details, reflections on glass/water, rim lighting on character edges
                - If contains: plain/gradient background, isolated figure → ADD: detailed urban environment (tall buildings, perspective, street elements, atmospheric depth)
                - If lacking heroic verticality or scale → REFRAME: emphasize looking up at towering buildings, high vantage points, vast cityscapes, dizzying perspectives
                """
            }

            SPACE_OPERA -> {
                buildString {
                    appendLine("**VALIDATION RULES FOR SPACE_OPERA:**")
                    appendLine()
                    appendLine("BANNED TERMS:")
                    appendLine("- Avoid: 'primitive rockets', 'simple tube rockets', 'basic spacecraft'")
                    appendLine("- Empty backgrounds: 'plain background', 'void', 'empty space' (without cosmic detail)")
                    appendLine()
                    appendLine("REQUIRED ELEMENTS:")
                    appendLine("- Art style: '1950s Atomic Age illustration', 'gouache/oil', 'painterly finish'")
                    appendLine("- Environment: 'vast cosmic environments', 'swirling nebulae', 'star fields', 'cosmic phenomena'")
                    appendLine("- Technology (if present): 'streamlined starships', 'sophisticated engines', 'advanced design'")
                    appendLine("- Figures: 'radiant 50s illustration', 'diverse archetypes (pin-up/action hero) across all ethnicities'")
                    appendLine()
                    appendLine("ACCENT COLOR ENFORCEMENT (MAJOR):")
                    appendLine("- Mandatory Accent: CHERRY RED / ROCKET ORANGE")
                    appendLine(
                        "- Application: Spacecraft engines, energy beams, stellar phenomena (dramatic contrast against cosmic backdrop)",
                    )
                    appendLine(
                        "- Prompt MUST mention: 'cherry red engines', 'rocket orange thrust', 'vivid red energy', 'orange stellar glow', or similar vibrant accent",
                    )
                    appendLine(
                        "- If missing → MAJOR VIOLATION: Add red/orange accent to tech or cosmic elements (e.g., 'cherry red engine glow', 'rocket orange energy trail')",
                    )
                    appendLine()
                    appendLine("BACKGROUND ENFORCEMENT (MAJOR):")
                    appendLine("- EVERY image MUST have detailed cosmic environment")
                    appendLine("- Describe cosmic phenomena: nebulae, star fields, galaxies, cosmic dust")
                    appendLine("- If prompt says 'empty space' without detail → MAJOR VIOLATION, reviewer adds cosmic elements")
                    appendLine()
                    appendLine("AUTONOMOUS FIX PATTERNS:")
                    appendLine(
                        "- If contains: primitive rockets, simple tube rockets, basic spacecraft → REPLACE with: streamlined starships, sophisticated engines, advanced futuristic design",
                    )
                    appendLine(
                        "- If contains: photorealism, modern rendering → REPLACE with: 1950s Atomic Age illustration, gouache/oil, painterly finish, vintage aesthetics",
                    )
                    appendLine(
                        "- If contains: plain background, void without cosmic detail → ADD: vast cosmic environment (swirling nebulae, dense star fields, galaxies, cosmic phenomena, stellar radiation)",
                    )
                    appendLine(
                        "- If missing or weak red/orange accent → INTEGRATE: cherry red engines, rocket orange thrust, vivid red energy beams, orange stellar glow on spacecraft/tech",
                    )
                    appendLine(
                        "- If figures lack 50s illustration style → EMPHASIZE: radiant proportions across diverse body types, pin-up or action hero archetypes of all ethnicities, soft expressive painting style",
                    )
                }
            }

            FANTASY -> {
                buildString {
                    appendLine("**VALIDATION RULES FOR FANTASY:**")
                    appendLine()
                    appendLine("BANNED TERMS:")
                    appendLine("- Empty backgrounds: 'plain background', 'gradient background', 'isolated figure'")
                    appendLine("- Cool colors: 'blue skies', 'cold tones', 'desaturated', 'grey'")
                    appendLine(
                        "- Modern digital art: 'digital painting', 'concept art', 'airbrush', 'photoshop', 'digital gradient', 'video game art', 'MTG art', 'D&D illustration'",
                    )
                    appendLine(
                        "- Modern effects: 'lens flare', 'bloom effect', 'chromatic aberration', 'photorealistic', 'CG', '3D rendered'",
                    )
                    appendLine(
                        "- Modern anatomy: 'hyperrealistic anatomy', 'photorealistic skin', 'anatomically perfect', 'realistic musculature', 'detailed muscularity'",
                    )
                    appendLine(
                        "- Comic/stylized anatomy: 'comic book style', 'superhero proportions', 'exaggerated muscles', 'anime anatomy'",
                    )
                    appendLine()
                    appendLine("REQUIRED ELEMENTS:")
                    appendLine(
                        "- Art style: 'ethereal Renaissance oil painting', 'luminous glazes', 'delicate sfumato', 'subtle brushstrokes', 'smooth glazing', 'canvas texture', 'visible brushwork'",
                    )
                    appendLine(
                        "- Oil painting technique: 'traditional oil painting', 'soft blended edges', 'layered glazes', 'organic color transitions', 'matte oil finish', 'glazed luminosity'",
                    )
                    appendLine(
                        "- Era reference: 'High Renaissance', 'Italian Masters', 'Botticelli', 'Raphael', 'Leonardo da Vinci', '15th-16th century', 'classical period', 'academic painting'",
                    )
                    appendLine(
                        "- Palette: 'warm tones throughout', 'rich ochres', 'warm umber shadows', 'golden highlights', 'terracotta', 'sienna', 'burnt sienna', 'warm greens', 'warm golds'",
                    )
                    appendLine(
                        "- Texture: 'fine canvas oil', 'classical oil painting', 'medieval fantasy aesthetic', 'soft edges', 'visible paint texture'",
                    )
                    appendLine("- Mood: 'ethereal', 'majestic', 'graceful', 'divine warmth', 'spiritual', 'beauty', 'timeless'")
                    appendLine(
                        "- Classical anatomy: 'contrapposto stance', 'idealized proportions', 'graceful gestures', 'flowing fabrics', 'classical drapery', 'soft musculature suggested through light'",
                    )
                    appendLine(
                        "- Character design: 'flowing robes', 'elegant fabrics', 'medieval garments', 'silk', 'velvet', 'graceful', 'classical', 'refined facial features', 'contemplative expression'",
                    )
                    appendLine(
                        "- Environment: diverse and creative, choose ONE or blend multiple: open rolling landscapes with distant mountains, ancient weathered castles, dramatic battle ruins with nature reclaiming, enchanted forests with ancient trees, medieval villages, pastoral countryside, mystical groves, riverside valleys, moorlands",
                    )
                    appendLine()
                    appendLine("STYLE ENFORCEMENT (CRITICAL):")
                    appendLine("- Prompt MUST explicitly state \"Renaissance oil painting\" or \"classical oil painting\" style")
                    appendLine("- Prompt MUST NOT describe modern digital art aesthetics")
                    appendLine(
                        "- If prompt says 'digital painting', 'concept art', or 'illustration' → CRITICAL VIOLATION: Replace with 'Renaissance oil painting'",
                    )
                    appendLine(
                        "- If prompt describes hard edges or digital effects → MAJOR VIOLATION: Replace with 'soft blended oil painting edges'",
                    )
                    appendLine(
                        "- Character clothing: flowing medieval/fantasy garments OR heavy armor allowed (epic fantasy warriors with armor are encouraged)",
                    )
                    appendLine()
                    appendLine("**CLASSICAL ANATOMY ENFORCEMENT (CRITICAL):**")
                    appendLine("- Figure proportions MUST follow classical ideals:")
                    appendLine(
                        "  * Head: 1/7 to 1/8 of total body height (not modern photorealistic 1/9)",
                    )
                    appendLine(
                        "  * Facial features: Refined and noble, NOT photorealistic; High Renaissance-inspired treatment applied to diverse facial structures (almond-shaped eyes, contemplative expression) across all ethnicities",
                    )
                    appendLine(
                        "  * Stance: Contrapposto or S-curve elegance, graceful weight distribution, balanced asymmetry",
                    )
                    appendLine(
                        "  * Musculature: Suggested through warm/cool tone transitions and light play - NEVER exaggerated or hyperrealistic bodybuilder style",
                    )
                    appendLine(
                        "  * Flesh: Luminous and translucent quality, warm base tones with cool shadow transitions, built through traditional oil glazing",
                    )
                    appendLine(
                        "  * Limbs: Graceful proportions with refined joints; avoid awkward or stiff positioning",
                    )
                    appendLine(
                        "- If prompt describes: photorealistic anatomy, detailed musculature, hyperrealism → CRITICAL VIOLATION: Replace with 'noble classical proportions across diverse body types, soft anatomical suggestion'",
                    )
                    appendLine(
                        "- If prompt describes: comic/anime anatomy, exaggerated features → CRITICAL VIOLATION: Replace with 'High Renaissance academic figure painting'",
                    )
                    appendLine()
                    appendLine("**PAINTING MATERIALITY ENFORCEMENT (MAJOR):**")
                    appendLine("- Prompt MUST convey OIL PAINTING PHYSICALITY:")
                    appendLine(
                        "  * Visible brushwork: Describe painting approach, NOT digital perfection",
                    )
                    appendLine(
                        "  * Glazing technique: Emphasize translucent layering, warm/cool glazes building luminosity",
                    )
                    appendLine(
                        "  * Surface quality: Canvas texture visible, soft edges from sfumato blending, impasto in highlights",
                    )
                    appendLine(
                        "  * Light integration: Sources glow naturally within composition, not added digitally after",
                    )
                    appendLine(
                        "- If prompt describes: digital smoothness, airbrushed finish, photorealistic rendering → MAJOR VIOLATION: Replace with 'oil glazing technique, visible brushwork, canvas texture'",
                    )
                    appendLine(
                        "- If image appears too smooth/digital → ADD: 'visible subtle brushstrokes', 'soft sfumato transitions', 'glazed luminosity', 'warm impasto highlights'",
                    )
                    appendLine()
                    appendLine("WARM PALETTE ENFORCEMENT (MAJOR):")
                    appendLine("- Entire image MUST maintain warm color temperature throughout - NO COLD TONES")
                    appendLine(
                        "- Primary colors: warm golds, ochres, warm browns, terracottas, sienna, burnt sienna, warm olive greens, sage greens, peachy tones, amber tones",
                    )
                    appendLine(
                        "- Sky options (choose based on environment): golden hour sunset, warm amber light, peachy sunrise, warm overcast, hazy golden afternoon, sunset-tinted clouds",
                    )
                    appendLine(
                        "- Environment color palette: warm earth tones, natural materials (stone with warm hues, wood, warm foliage)",
                    )
                    appendLine("- If missing warm palette → MAJOR VIOLATION: Replace cool/desaturated tones with warm equivalents")
                    appendLine()
                    appendLine("ACCENT COLOR ENFORCEMENT (MODERATE - SUBTLE DETAILS):")
                    appendLine("- Mandatory Accent: RADIANT GOLD / CELESTIAL GILT in SUBTLE DETAILS only")
                    appendLine(
                        "- Application options (choose one or combine subtly): gilded sword/weapon edge, jewelry catching warm light, magical glow on small elements, light reflecting off water/metalwork, luminous nature details (blooming flowers, fireflies, glowing moss), warm light on stone architectural details, character's adornment (belt buckle, brooch, simple halo)",
                    )
                    appendLine(
                        "- Prompt should mention ONE OR TWO of: 'gilded sword edge', 'golden jewelry', 'light catching metal', 'radiant details', 'warm glow on...', 'luminous accent', 'golden highlights on...'",
                    )
                    appendLine("- FORBIDDEN: Bright neon colors, garish tones, overly dominant red, oversaturated RGB colors")
                    appendLine(
                        "- If missing → MODERATE VIOLATION: Add subtle gold detail (weapon edge, jewelry glint, light reflection, nature detail, architectural highlight)",
                    )
                    appendLine()
                    appendLine("BACKGROUND ENFORCEMENT (MAJOR - CREATIVE DIVERSITY):")
                    appendLine("- EVERY image MUST have detailed, diverse medieval/fantasy environment")
                    appendLine(
                        "- Environment choices (encourage variety): rolling hills with distant castles, weathered stone ruins with ivy/nature, enchanted ancient forest, medieval village streets, pastoral farmland, misty moorlands, castle courtyard, riverside valley, mountain pass, woodland stream",
                    )
                    appendLine("- Describe 3+ specific environmental details appropriate to chosen setting")
                    appendLine(
                        "- Creativity encouraged: varying environments prevents visual monotony while unified warm palette maintains cohesion",
                    )
                    appendLine(
                        "- If prompt lacks environmental context → MAJOR VIOLATION, reviewer adds appropriate medieval fantasy setting",
                    )
                    appendLine()
                    appendLine("LIGHTING ENFORCEMENT:")
                    appendLine(
                        "- Required: 'soft chiaroscuro', 'divine illumination', 'warm golden light', 'gentle transitions', 'luminous', 'atmospheric'",
                    )
                    appendLine("- Forbidden: 'harsh shadows', 'dramatic contrast', 'dark lighting', 'cold light', 'rim light' (too modern)")
                    appendLine(
                        "- Lighting should enhance environment: golden hour on landscapes, candlelit ruins, dappled forest light, warm sunset on stone, misty morning glow",
                    )
                    appendLine()
                    appendLine("FINAL STYLE CHECK:")
                    appendLine(
                        "- Ask: \"Does this sound like a description for a 15th-century Italian Renaissance oil painting with medieval/fantasy elements?\"",
                    )
                    appendLine(
                        "- If NO → CRITICAL VIOLATION: Revise to emphasize classical painting technique, soft ethereal beauty, traditional warm pigments",
                    )
                    appendLine(
                        "- Look for: Classical painting references, oil terminology, warm light language, Renaissance aesthetic with fantasy elements",
                    )
                    appendLine("- Reject: Modern digital art terms, gaming terminology, hard-edged descriptions, photorealistic rendering")
                    appendLine()
                    appendLine("AUTONOMOUS FIX PATTERNS:")
                    appendLine(
                        "- If contains: over-saturated red, crimson dominance making image monochromatic red → SOFTEN: distribute warm colors more naturally, maintain warm palette without overwhelming red",
                    )
                    appendLine(
                        "- If contains: grimdark aesthetics or overly dark tones → BRIGHTEN: shift to ethereal beauty, warm atmosphere, add light and spiritual elements",
                    )
                    appendLine(
                        "- If contains: cool blues, desaturated colors, grey tones → REPLACE with: warm golds, ochres, warm greens, peachy/amber lighting",
                    )
                    appendLine(
                        "- If contains: modern anime, digital aesthetics, hard edges, concept art rendering → REPLACE with: oil painting glazing, soft sfumato transitions, classical style",
                    )
                    appendLine(
                        "- If contains: hyperrealistic anatomy, photorealistic skin, detailed musculature → CRITICAL FIX: Replace with 'noble classical proportions across diverse body types', 'soft anatomical suggestion through light', 'ethereal luminous skin tones across all ethnicities'",
                    )
                    appendLine(
                        "- If contains: exaggerated muscles, comic book anatomy, superhero proportions → CRITICAL FIX: Replace with 'contrapposto elegance', 'High Renaissance academic proportions', 'graceful classical figure painting'",
                    )
                    appendLine(
                        "- If image appears too smooth or digital → ADD: 'visible soft brushwork', 'oil glazing technique', 'subtle canvas texture', 'sfumato blending', 'impasto highlights', 'hand-painted appearance'",
                    )
                    appendLine(
                        "- If lacks painting materiality → ADD: 'warm glazed luminosity', 'layered oil translucency', 'soft directional brushstrokes', 'classical underpainting visible through glazes'",
                    )
                    appendLine(
                        "- If figure anatomy appears stiff or awkward → IMPROVE: 'graceful contrapposto stance', 'balanced weight distribution', 'flowing gesture suggesting movement', 'classical S-curve elegance'",
                    )
                    appendLine(
                        "- If facial features too realistic/photographic → SOFTEN: 'refined noble features', 'contemplative expression', 'delicate quality across diverse facial types'",
                    )
                    appendLine(
                        "- If contains: tech elements, cybernetic, mechanical, modern elements → REMOVE entirely, replace with classical/mystical medieval elements",
                    )
                    appendLine(
                        "- If contains: plain/gradient background, isolated figure → ADD: detailed medieval fantasy environment (landscape/castle/ruins/forest/village - choose ONE with 3+ details)",
                    )
                    appendLine(
                        "- If accent color missing → ADD: subtle gold detail (gilded weapon edge, jewelry glint, luminous nature detail, warm light on architecture, character adornment)",
                    )
                    appendLine(
                        "- If environments too repetitive/same → VARY: alternate between landscape types (open fields, ancient ruins, forest depths, medieval structures) while maintaining warm palette",
                    )
                }
            }

            CRIME -> {
                buildString {
                    appendLine("**VALIDATION RULES FOR CRIME:**")
                    appendLine()
                    appendLine("BANNED TERMS:")
                    appendLine("- Empty backgrounds: 'plain background', 'gradient background'")
                    appendLine()
                    appendLine("REQUIRED ELEMENTS:")
                    appendLine("- Art style: 'hyper-realistic classical oil painting', 'academic realism'")
                    appendLine("- Technique: 'smooth glazing', 'no rough brushstrokes', 'flawless surface'")
                    appendLine("- Lighting: 'divine & ethereal', 'soft diffused heavenly light'")
                    appendLine("- Background MUST include 80s Miami beach paradise elements")
                    appendLine()
                    appendLine("ACCENT COLOR ENFORCEMENT (MAJOR):")
                    appendLine("- Mandatory Accent: HOT PINK (#E91E63 - Pantone 213 C)")
                    appendLine(
                        "- Application: Environmental (neon reflections on ocean/sand, sunset, flamingos, neon signs) OR Character (outfit accents, accessories, lipstick)",
                    )
                    appendLine("- Must feel ORGANIC and sophisticated, never jarring")
                    appendLine(
                        "- Prompt MUST mention: 'hot pink', 'neon pink glow', 'pink sunset tones', 'pink neon reflections', or similar Miami Vice pink",
                    )
                    appendLine(
                        "- If missing → MAJOR VIOLATION: Add pink accent to environment or character (e.g., 'hot pink neon glow reflecting on waves', 'pink sunset sky')",
                    )
                    appendLine()
                    appendLine("BACKGROUND ENFORCEMENT (MAJOR):")
                    appendLine("- EVERY image MUST have luxury beach environment details")
                    appendLine("- Describe Miami elements: ocean, palm trees, neon lights, art deco hotels, etc.")
                    appendLine("- If prompt lacks environmental glamour → MAJOR VIOLATION, reviewer adds beach paradise details")
                    appendLine()
                    appendLine("AUTONOMOUS FIX PATTERNS:")
                    appendLine(
                        "- If contains: grimdark, violence, harsh realism, dark tones → REPLACE with: luxury, elegance, sophistication, divine beauty, ethereal light",
                    )
                    appendLine(
                        "- If contains: cool blues, greens, desaturated colors → REPLACE with: warm golds, champagne, alabaster, creamy luxurious tones",
                    )
                    appendLine(
                        "- If missing hot pink accent → INTEGRATE: hot pink neon glow reflecting on ocean/sand, pink sunset tones, flamingo sculptures, pink outfit accents, or neon beachfront signs",
                    )
                    appendLine(
                        "- If contains: plain/gradient background → ADD: detailed Miami beach ambience with 3+ elements (ocean waves, palm trees, neon lights, art deco architecture, boardwalk, golden hour sky)",
                    )
                    appendLine(
                        "- If missing ethereal/divine quality → SOFTEN: edges and lighting, add divine ethereal atmosphere, intensify luxury details, emphasize flawless perfection",
                    )
                }
            }

            COWBOY -> {
                buildString {
                    appendLine("**VALIDATION RULES FOR COWBOY:**")
                    appendLine()
                    appendLine("BANNED TERMS:")
                    appendLine("- Avoid: 'photorealism', 'smooth digital blending', 'refined details'")
                    appendLine("- Empty backgrounds: 'plain background', 'gradient background'")
                    appendLine()
                    appendLine("REQUIRED ELEMENTS:")
                    appendLine("- Art style: 'expressive western oil painting', 'Remington/Russell style'")
                    appendLine("- Technique: 'bold visible brushstrokes', 'thick impasto', 'painterly quality'")
                    appendLine("- Mood: 'rough edges', 'bold color blocks', 'artistic interpretation'")
                    appendLine("- Background MUST include frontier environment")
                    appendLine()
                    appendLine("ACCENT COLOR ENFORCEMENT (MAJOR):")
                    appendLine("- Mandatory Accent: BURNT ORANGE / SUNSET GOLD")
                    appendLine("- Application: Sunsets, campfire glow, dust catching light, metallic glints (with expressive brush marks)")
                    appendLine(
                        "- Prompt MUST mention: 'burnt orange', 'sunset gold', 'golden hour glow', 'orange dust', 'warm amber sunset', or similar warm frontier accent",
                    )
                    appendLine(
                        "- If missing → MAJOR VIOLATION: Add orange/gold accent to lighting or atmosphere (e.g., 'burnt orange sunset', 'golden dust catching light')",
                    )
                    appendLine()
                    appendLine("BACKGROUND ENFORCEMENT (MAJOR):")
                    appendLine("- EVERY image MUST have detailed Western environment")
                    appendLine("- Describe frontier elements: desert landscape, saloon, canyon, prairie, frontier town, etc.")
                    appendLine("- If prompt lacks environmental vastness → MAJOR VIOLATION, reviewer adds Western setting")
                    appendLine()
                    appendLine("AUTONOMOUS FIX PATTERNS:")
                    appendLine(
                        "- If contains: photorealism, smooth digital blending, refined details → REPLACE with: expressive oil painting, bold visible brushstrokes, thick impasto, painterly quality",
                    )
                    appendLine(
                        "- If contains: modern aesthetics, photo-like rendering → REPLACE with: Remington/Russell western oil painting style, gestural strokes, artistic interpretation over precision",
                    )
                    appendLine(
                        "- If missing burnt orange/sunset gold accent → INTEGRATE: burnt orange sunset, golden hour glow, orange dust catching light, golden campfire light, or warm amber tones",
                    )
                    appendLine(
                        "- If contains: plain/gradient background → ADD: detailed frontier environment (desert landscape/saloon/canyon/prairie/frontier town with 3+ specific Western elements)",
                    )
                    appendLine(
                        "- If lacking emotion/soul → EMPHASIZE: expressive brushwork, rough edges, bold color blocks, passionate artistic interpretation of frontier feeling",
                    )
                }
            }

            SHINOBI -> {
                buildString {
                    appendLine("**VALIDATION RULES FOR SHINOBI:**")
                    appendLine()
                    appendLine("BANNED TERMS:")
                    appendLine("- Empty backgrounds violate negative space principle (environment suggested, not plain)")
                    appendLine()
                    appendLine("REQUIRED ELEMENTS:")
                    appendLine("- Art style: Sumi-e ink wash, monochrome, negative space celebrated")
                    appendLine("- Environment: Suggested through minimal brushstrokes (bamboo silhouette, castle rooftops, mist)")
                    appendLine("- Background uses negative space artistically, not emptiness")
                    appendLine()
                    appendLine("ACCENT COLOR ENFORCEMENT (MAJOR):")
                    appendLine("- Mandatory Accent: VIBRANT CRIMSON RED")
                    appendLine("- Application: Single, vital element (blood, sunset, banner, flower) - ONLY ONE accent color allowed")
                    appendLine("- Must be DRAMATIC contrast against black/white")
                    appendLine(
                        "- Prompt MUST mention: 'vibrant crimson', 'blood red', 'scarlet accent', 'deep red', or similar single red element",
                    )
                    appendLine("- FORBIDDEN: Any other colors beyond black/white/red")
                    appendLine(
                        "- If missing → MAJOR VIOLATION: Add single crimson accent element (e.g., 'single crimson red accent on blade', 'vibrant red blood splatter')",
                    )
                    appendLine()
                    appendLine("BACKGROUND ENFORCEMENT (MODERATE):")
                    appendLine("- Environment should be SUGGESTED through minimal ink washes")
                    appendLine("- Negative space is intentional art, NOT lack of background")
                    appendLine(
                        "- If prompt says 'plain background' → Clarify as 'pristine white paper with suggested environment through ink mist'",
                    )
                    appendLine()
                    appendLine("AUTONOMOUS FIX PATTERNS:")
                    appendLine(
                        "- If contains: colors beyond black/white/red, tinted paper, off-white backgrounds → REPLACE with: pure monochrome black ink on white paper, NO tints or colors",
                    )
                    appendLine(
                        "- If contains: photorealistic rendering, digital style, smooth blending → REPLACE with: sumi-e ink wash, bold imperfect brushstrokes, dry-brush texture, visible paper grain",
                    )
                    appendLine(
                        "- If contains: multiple accent colors or weak crimson → REPLACE with: single vibrant crimson red accent (blood/sunset/banner/flower), high contrast against monochrome",
                    )
                    appendLine(
                        "- If contains: plain background or empty void → CLARIFY: transform to 'suggested environment through minimal ink washes' (bamboo silhouette, castle rooflines, mist/fog through ink bleeds)",
                    )
                    appendLine(
                        "- If missing crimson accent → INTEGRATE: single dramatic red element (crimson blade accent, blood splatter, red sunset, scarlet banner)",
                    )
                    appendLine(
                        "- If lacking energy/gestural quality → EMPHASIZE: bold imperfect brushstrokes, visible spontaneity, water/ink artifacts, gestural energy in ink application",
                    )
                }
            }
        }
}
