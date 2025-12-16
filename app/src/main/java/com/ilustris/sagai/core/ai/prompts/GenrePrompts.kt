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
                """
                Art Technique: Gritty Dark Fantasy Oil Painting. Emphasize earthy tones, mud, steel, and stone.
                Key Lighting Style: SUBTLE CHIAROSCURO / ATMOSPHERIC RENDERING.
                Texture / Materiality: CANVAS OIL with VISIBLE BRUSHSTROKES and LOOSE IMPASTO.
                Aesthetic Era / Influence: Romanticism; Pre-Raphaelite.
                
                **COLOR PALETTE & ACCENT:**
                 - Base: A palette of earthy tones, mud, steel, and stone. Desaturated and dark.
                 - Mandatory Accent: EMBER GOLD / FIERY ORANGE.
                 - Application: Use the accent color for magical runes, torchlight reflections on armor, glowing eyes, or spell effects. It must pop against the earthy background. Forbid other vibrant colors.

                Mood: Epic, wistful, and mysterious — a sense of ancient fate and quiet reverence. Emphasize wonder over action.
                Ambience: Soft, volumetric natural light (dawn/dusk), drifting mist or smoke, distant weather elements (storm clouds or light rain).
                """
            }

            CYBERPUNK -> {
                """
                Art Technique: Vintage 1980s Anime OVA Cel Animation. FLAT SHADING (Cel Shading) is mandatory. Shadows are hard-edged blocks of color; forbid soft gradients, ambient occlusion, and subsurface scattering.
                Line Work: Delicate, sketchy ink lines with varying weight. Organic cloth folds and realistic mechanical detailing.
                
                **COLOR PALETTE & ACCENT:**
                 - Base: Muted Blue Tones. Dominant tones of Slate Blue, Cold Steel, and Deep Cool Greys. 
                    The image should feel desaturated, moody and atmospheric.
                 - Mandatory Accent: DEEP PURPLE. 
                 - Application: Use this accent subtly for deep shadow casting, low-light environmental glow, or muted interface lights. STRICTLY FORBID bright, fluorescent neon pinks or bright purples. The purple must feel "bruised" or metallic.
                
                Detail: Hair rendered as "clumps" or shapes with simple highlights, not individual strands. Skin must be matte; reduce micro-detail.
                Lighting: High-contrast "Rembrandt" lighting with hard-edged, cast shadows. Strong Hard Rim Light is essential to separate characters. No Global Illumination.
                Texture & Artifacts: Analog Noise, Chromatic Aberration, and subtle Film Grain to simulate vintage broadcast quality. 
                
                Facial Features: Classic 1990s anime proportions. Large, expressive eyes with prominent specular highlights, sharp noses, and high-contrast hair sheen.
                
                Mood: Dystopian, melancholic, and vast. A sense of high-tech isolation amidst towering structures.
                Ambience: Dense vertical metropolis, looming mega-skylines, muted city lights. A feeling of oppressive scale and future decay.
                
                Rendering Constraints: Strictly forbid 3D CGI, photorealism, modern digital smoothing/blending techniques, modern anime, digital painting, 3d render, unreal engine, volumetric lighting, smooth shading. The final image must look like a high-quality frame from a vintage laserdisc or a production cel.
                """
            }

            HORROR -> {
                """
        Art Technique: 32-BIT PIXEL ART with BLOCKY SHADING
        Texture / Materiality: RETRO GAME ART PIXELATION
        Aesthetic Era / Influence: PS1/SEGA SATURN HORROR GAME AESTHETIC
        Vibe / Mood Aesthetic: HAUNTED, MYSTIQUE DARK AESTHETIC

        **COLOR PALETTE & ACCENT:**
         - Base: LIMITED DARK AND PALE BLUE PALETTE.
         - Mandatory Accent: Ash gray/Faded cerulean blue.
         - Application: Use this for subtle highlights, reflections, or to create a chilling atmosphere. Forbid other vibrant colors.

        Mood: Oppressive, uncanny, and creeping dread — prioritize psychological unease over explicit gore.
        Ambience: Low-key, desaturated environment with heavy shadows and subtle volumetric fog; minimal and muted highlights only where needed to draw attention to small, eerie details.
        """
            }

            HEROES -> {
                """
        Art Technique: MODERN COMIC BOOK ART STYLE (Dynamic, Clean, and Detailed).
        Line Detail: BOLD INK LINES, ANATOMICALLY ACCURATE FIGURES, DYNAMIC FORESHORTENING.
        Key Lighting Style: CINEMATIC NATURAL LIGHTING (Golden Hour or Overcast) with DRAMATIC SHADOWS.
        Texture / Materiality: HIGH-QUALITY DIGITAL COLORING, SMOOTH GRADIENTS, DETAILED URBAN TEXTURES.
        Aesthetic Era / Influence: MODERN SUPERHERO COMICS (2010s-Present) - Think "Into the Spider-Verse" meets "Arkham City" concept art.

        **COLOR PALETTE & ACCENT:**
         - Base: DYNAMIC URBAN PALETTE. Concrete greys, brick reds, glass reflections, and atmospheric sky tones.
         - Mandatory Accent: SUBTLE ELECTRIC BLUE.
         - Application: Use this accent for SKY DETAILS, REFLECTIONS on glass/water, subtle RIM LIGHTING, or small details on accessories/tech. It should NOT be a dominant wash, but a unifying atmospheric element.

        Mood: HEROIC, VERTICAL, and EXPANSIVE. A sense of scale and freedom mixed with urban melancholy.
        Ambience: VAST OPEN CITYSCAPES. Towering skyscrapers, dizzying perspectives (looking down or up), busy streets far below. The city feels alive but slightly dystopian. Avoid forced night scenes; prefer dynamic daytime, sunset, or moody overcast skies that allow for depth and scale.
        """
            }

            CRIME -> {
                """
                Art Technique: HYPER-REALISTIC CLASSICAL OIL PAINTING (Academic Realism).
                Key Lighting Style: DIVINE & ETHEREAL. Soft, diffused "Heavenly" light that wraps around the subject.
                Texture / Materiality: SMOOTH GLAZING technique. No rough brushstrokes. The surface should look like flawless porcelain or silk.
                Aesthetic Era / Influence: Classical Academic Art (e.g., Bouguereau, Godward) meets Modern High-Fashion Photography.

                **COLOR PALETTE & ACCENT:**
                 - Base: PURE ELEGANCE. Creamy alabaster, champagne gold, soft pearl grey, and deep, expensive blacks.
                 - Mandatory Accent: HOT PINK (#E91E63 - Pantone 213 C).
                 - Application: Weave this Hot Pink accent subtly throughout the composition as sophisticated environmental or character details. Examples include:
                   * ENVIRONMENTAL: Neon light glow reflecting on ocean waves and wet sand, beachfront palm fronds swaying, pink sunset sky bleeding across endless coastline, flamingo sculptures at beachside venues, luxury convertibles on coastal highways, art deco beach hotels, boardwalk neon reflections, champagne glasses against ocean backdrop, silk fabrics flowing in sea breeze.
                   * CHARACTER OUTFIT: Hawaiian shirts with pink tones, designer accessories with pink accents, lipstick or nail polish, fabric trim on luxury clothing, a silk scarf, beach resort attire.
                   * AMBIENT LIGHT: Soft hot pink glow from ocean sunset, neon beachfront signs reflecting on wet sand and skin, golden hour light filtering through palm trees, ocean waves catching twilight and neon glow, beachside fire pits with warm pink undertones.
                   The accent should feel organic and effortless, never jarring—it's a signature touch of vice, glamour, and 80s Miami vibes subtly integrated into luxury aesthetics. Maintain an atmosphere of absolute divine elegance and sophistication throughout.

                Figure & Facial Treatment: FLAWLESS PERFECTION. Skin must be rendered with hyper-realistic subsurface scattering—absolutely no imperfections. Faces should look like "Modern Deities": serene, symmetrical, and breathtaking.
                Rendering: THE "PERFECT PICTURE" ILLUSION. At first glance, it appears to be a stunning high-resolution photograph. On closer inspection, it is a masterpiece painting with incredibly fine, deliberate details. Sharp focus on the subject, dreamy/soft background.

                Mood: A Divine Piece of Art. A frozen moment of absolute luxury and beauty.
                Ambience: An enchanted 80s Miami beach city paradise—endless golden beaches, palm-lined oceanfront, neon-lit beachfront promenades, and that thrilling excitement of coastal nightlife. Think Miami Vice, GTA Vice City, and Scarface: the dreamlike adoration of 80s Miami as the ultimate tropical fantasy destination, where ocean meets neon glamour.
                """
            }

            SHINOBI -> {
                """
                Art Technique: JAPANESE SUMI-E INK WASH PAINTING (Suiboku-ga), EMPHASIS ON ECONOMY OF BRUSHSTROKES AND NEGATIVE SPACE.
                CRITICAL NOTE: THE STYLE IS BLACK AND WHITE. NO COLOR OR TINTS, NOT EVEN OFF-WHITE TONES FOR SKIN OR PAPER.
                Brushwork: BOLD, IMPERFECT, AND HIGHLY TEXTURAL BRUSH STROKES (Hone-gaki/dry-brush) that clearly convey energy and form.
                Use controlled ink bleeds, water/wash artifacts, and visible spontaneity to avoid a "clean digital" look.
                Prioritize the weight and flow of the ink.
                Texture / Materiality: AUTHENTIC RICE-PAPER (WASHI) TEXTURE — Visible paper grain and natural ink absorption/blotches. THE PAPER IS PURE WHITE, NOT TINTED.
                Add subtle ink spatters/splatters for dynamism. Aesthetic Influence: Master Sumi-e (Sesshū Tōyō), Gekiga (raw, expressive comic style), and Zen philosophy (simplicity, spontaneity).

                Figure & Facial Treatment: MINIMALIST AND EXPRESSIVE — Features often simplified or obscured by shadow/mist/hair. Focus on silhouette and gestural energy (Sei).
                Avoid smooth skin or polished features. The character's form is suggested more by the surrounding ink wash than by hard outlines.
                Rendering: HIGH-CONTRAST MONOCHROME (Pure Black to White Paper) with a SINGLE, VITAL COLOR ACCENT.
                Form is built using layered, transparent diluted ink washes (Bokashi) and strong areas of solid black.
                
                **COLOR PALETTE & ACCENT:**
                 - Base: Monochrome (Pure Black to White Paper).
                 - Mandatory Accent: VIBRANT CRIMSON RED.
                 - Application: Use the accent color as a single, vital element. Forbid other vibrant colors.

                Lighting & Shading: DRAMATIC SHADOWS AND CONTRAST created by DENSITY OF INK WASH versus Pristine Negative Space. Use atmospheric depth (mist/fog) to diffuse light and suggest volume, rather than complex digital rim lights.
                
                Composition & Ambience: FEUDAL JAPANESE HIGH DRAMA — Ambience is suggested (e.g., castle rooftops, bamboo silhouette) rather than fully drawn.
                Composition must heavily utilize and celebrate the white/negative space.
                Focus on the character's immediate action or emotion.
                
                Rendering: Full-bleed illustration. No borders, frames, or text.
                
                Mood: Raw, spontaneous, intensely focused, and visually powerful through simplicity.
                """
            }

            SPACE_OPERA -> {
                """
                Art Technique: 1950s ATOMIC AGE ORIGINAL ILLUSTRATION (Gouache/Oil).
                Texture / Materiality: SMOOTH ILLUSTRATION BOARD, VISIBLE BRUSHSTROKES, VIBRANT PAINTERLY FINISH.
                Aesthetic Era / Influence: RETRO-FUTURISM, RAYGUN GOTHIC, 1950s PIN-UP & PULP COVER ART (The original painting, not the printed poster).

                **COLOR PALETTE & ACCENT:**
                 - Base: Deep Space Palette. Rich cosmic blues, purple nebula tones, starlight silver, and void black.
                 - Mandatory Accent: CHERRY RED / ROCKET ORANGE.
                 - Application: Use vivid red/orange for key elements (advanced spacecraft engines, energy beams, stellar phenomena) to contrast dramatically against the cosmic backdrop.

                Figure Style: IDEALIZED 50s COMMERCIAL ILLUSTRATION. Expressive, cheerful, and confident "Pin-up" or "Action Hero" archetypes.
                Line Work: Soft expressive painting style (like Gil Elvgren or Norman Rockwell). Smooth blending, soft edges.

                **TECHNOLOGY & VEHICLES (When Applicable):**
                 - IF spacecraft or technology appear in the scene, they should be: elegant, streamlined starships with flowing curves, chrome accents, and sophisticated aerodynamics. Think advanced space cruisers, not primitive rockets.
                 - IF individual vehicles are shown: design them like futuristic motorcycles or sleek fighter jets adapted for space - cool, fast, and technologically advanced.
                 - IF propulsion is visible: show sophisticated engine designs, energy trails, plasma drives, and gravitational field effects rather than simple rocket flames.
                 - IF technology is included: all tech should look genuinely advanced and purposeful for deep space exploration - orbital stations, research vessels, interstellar transports with elegant, functional designs.
                 - AVOID: Simple tube rockets, primitive spacecraft, or overly basic vehicle designs that don't match the scope of cosmic exploration.
                 - NOTE: Technology and vehicles are NOT mandatory - the cosmic ambience and 1950s art style are the primary focus.

                Mood: "The Infinite Cosmos Awaits!" — Awe-inspiring, optimistic, and filled with cosmic wonder.
                Ambience: VAST COSMIC ENVIRONMENTS. Emphasize the true majesty of space: swirling nebulae in brilliant colors, dense star fields stretching to infinity, the event horizon of black holes, meteor showers blazing across the void, distant galaxies spiraling in the background, cosmic dust clouds illuminated by stellar radiation.
                Focus on the overwhelming scale and beauty of the universe itself—the infinite expanse, the play of cosmic light and shadow, the sense of floating in the boundless void. Avoid terrestrial-looking planets;
                instead show the raw cosmic phenomena that make space exploration truly magnificent.
                
                Rendering: Clean illustration. No text, logos, or borders.
                """
            }

            COWBOY -> {
                """
                Art Technique: EXPRESSIVE WESTERN OIL PAINTING (Remington/Russell style). EMPHASIS ON PAINTERLY QUALITY AND VISIBLE BRUSHWORK.
                Key Lighting Style: WARM GOLDEN HOUR / DESERT SUNSET with DRAMATIC CHIAROSCURO.
                Texture / Materiality: BOLD, VISIBLE BRUSHSTROKES with THICK IMPASTO. Canvas texture must be apparent. Emphasize the HAND-PAINTED quality.
                Aesthetic Era / Influence: American Frontier, late 19th Century romantic Western art.

                **CRITICAL RENDERING NOTE:**
                 - AVOID photorealism, smooth digital blending, or overly refined details.
                 - PRIORITIZE expressive brushwork, loose gestural strokes, and painterly interpretation over precise detail.
                 - Forms should be suggested through confident brush marks, not meticulously rendered.
                 - Embrace the SOUL and EMOTION of oil painting—rough edges, bold color blocks, and artistic interpretation.
                 - Think "painted with passion" not "photographed and polished."

                **COLOR PALETTE & ACCENT:**
                 - Base: Rich earthy tones (Burnt Sienna, Raw Ochre, Desert Sand), weathered leather browns, Desert Sky Blue.
                 - Mandatory Accent: BURNT ORANGE / SUNSET GOLD.
                 - Application: Use the accent boldly for sunsets, campfire glow, dust catching light, or metallic glints. Apply with expressive brush marks.
                
                Mood: Rugged, isolated, and stoic. A sense of vastness and freedom, captured with artistic emotion.
                Ambience: Wide open spaces, heat haze, long shadows, dust motes dancing in the light. All rendered with EXPRESSIVE BRUSHWORK that captures the FEELING of the frontier, not a literal photograph.
                """
            }

            PUNK_ROCK -> {
                """
                **=== CRITICAL RULES - READ FIRST ===**
                
                **STOP. BEFORE YOU WRITE ANYTHING, THESE RULES ARE ABSOLUTE:**
                
                1. **CARTOON STYLE:** This is a CARTOON illustration like Gorillaz, Codename: Kids Next Door (KND), or Tank Girl. 
                   Characters must look like CARTOON CHARACTERS, not realistic humans with stylized touches.
                   Think 2D animated music video, NOT concept art or portrait photography.
                
                2. **NO EYE COLORS:** Eyes are SIMPLE BLACK DOTS or WHITE CIRCLES. Period.
                   BANNED: "brown eyes", "blue eyes", "cast [color] eyes", "piercing eyes", iris, pupils.
                   If you write ANY eye color, you have FAILED.
                
                3. **MANDATORY BACKGROUND:** Every image MUST have a DETAILED ENVIRONMENT.
                   BANNED: "plain background", "gradient background", "white background", "solid color background".
                   You MUST describe: walls, posters, graffiti, amps, neon signs, brick, concrete, etc.
                   **NOTE:** For vertical/lock-screen compositions, the top area can be less cluttered (e.g., just the brick wall or sky) to allow for UI elements, but it MUST NOT be a plain solid color.

                4. **NO SOFT/REALISTIC RENDERING:** 
                   BANNED: "soft lighting", "soft and diffused", "gentle gradient", "subtle glow on skin".
                   USE: "harsh neon", "hard cel-shading", "flat color blocks", "stark shadows".
                
                **NOW PROCEED WITH THE ART STYLE:**
                
                ---
                
                Art Technique: Jamie Hewlett / Gorillaz / KND Style CARTOON Illustration.
                This is a 2D CARTOON with flat colors, bold outlines, and stylized proportions—like a frame from an animated music video.
                
                **CARTOON AESTHETIC (THIS IS NOT REALISTIC ART):**
                 - This looks like it belongs in a Gorillaz music video or Codename: Kids Next Door episode.
                 - Characters are CARTOON CHARACTERS with exaggerated, simplified features.
                 - The style is closer to ANIMATION than illustration—flat, bold, graphic.
                 - Reference: 2D, Murdoc, Noodle from Gorillaz; the angular, exaggerated style of KND.
                
                **LINE WORK:**
                 - Bold, thick BLACK outlines—confident and slightly imperfect.
                 - Variable line weight: thick to thin, with organic wobble.
                 - Lines can overshoot corners or not quite connect—this is STYLISTIC, not sloppy.
                 - Think: Brush pen or marker, NOT clean vector art.
                
                **COLOR PALETTE:**
                 - Base: Gritty Urban Tones—Olive Drab, Slate Grey, Faded Denim, Dirty Concrete.
                 - Accent: ACID GREEN / TOXIC YELLOW (in environment only—neon signs, graffiti, amp lights).
                 - Colors are FLAT with hard edges. Minimal gradients. Cel-shaded shadows.
                
                **CHARACTER DESIGN (CARTOON PROPORTIONS - CRITICAL):**
                 - **FACE:** Simplified, angular, CARTOON features. NOT realistic human faces.
                   * Eyes: SIMPLE BLACK DOTS or WHITE CIRCLES. NO iris, NO pupil detail, NO eye color.
                   * Nose: Simple line, angular shape, or small triangle. NOT realistic nostrils.
                   * Mouth: Bold line, can be crooked or asymmetrical. Expressive but simple.
                   * Jaw: Angular, exaggerated—sharp or blocky, NOT smooth realistic contours.
                 - **BODY (IF VISIBLE):** Exaggerated cartoon proportions.
                   * Limbs can be noodle-thin or chunky—NOT realistic human proportions.
                   * Hands can be oversized, simplified (4-5 fingers suggested, not detailed).
                   * Posture is exaggerated: hunched, slouched, angular poses.
                 - **OVERALL:** Characters should look like they stepped out of a Gorillaz video—stylized, cool, slightly grotesque in a "beautiful ugly" way.
                
                **SHADING:**
                 - Hard CEL-SHADING only. Solid blocks of shadow with sharp edges.
                 - NO soft gradients, NO realistic light falloff, NO ambient occlusion.
                 - Shadows are graphic shapes, not realistic light simulation.
                
                **BACKGROUND & ENVIRONMENT (YOU MUST INCLUDE THIS):**
                 - **EVERY IMAGE NEEDS A REAL BACKGROUND.** This is mandatory.
                 - Pick ONE of these environments and DESCRIBE IT IN DETAIL:
                   * **GARAGE:** Brick walls, band posters, amps, cables, drum kit, bare lightbulb, stickers everywhere
                   * **ALLEY:** Graffiti walls, dumpster, fire escape, chain-link fence, wet pavement, neon signs
                   * **STAGE:** Mic stands, monitors, harsh stage lights, haze, crowd silhouettes, banner
                   * **ROOFTOP:** Water towers, antennas, city skyline, smoggy sky, AC units
                   * **RECORD SHOP:** Vinyl crates, posters, neon beer sign, cluttered shelves, sticky floor
                   * **STREET CORNER:** Bus stop, payphone, convenience store, street art, flickering signs
                 - The background should be GRAPHIC and slightly FLAT (to match the cartoon style) but DETAILED and PRESENT.
                 - Characters exist IN a world, not floating in empty space.

                **MOOD & ENERGY:**
                 - Anarchic, rebellious, cool. The energy of a garage band or animated music video.
                 - Characters have ATTITUDE—bored, manic, defiant, or "too cool to care."

                **MUSIC CULTURE INTEGRATION:**
                 - Characters interact with musical elements: guitars, headphones, vinyl, amps, drumsticks.
                 - Music gear shows wear: stickers, scratches, duct tape, sharpie marks.
                 - Even non-musicians wear band tees, have gig flyers, ticket stubs.

                **COMPOSITION:**
                 - LAYERED: Foreground objects (amp corner, mic stand), character positioned according to framing, background environment.
                 - Elements can OVERLAP and BREAK THE FRAME—hair, smoke, instruments extending past edges.
                 - Dynamic angles encouraged—avoid flat, static framing.
                 - **FRAMING FLEXIBILITY:** If the input is a PORTRAIT/HEADSHOT, maintain that framing. Do not force a full-body shot. The background should still be present but adapted to the tighter frame (e.g., a wall behind the head, a mic stand in the foreground).

                **CANDID MOMENTS:**
                 - Characters look CAUGHT in a moment, not posing.
                 - Mid-action: lighting cigarette, checking phone, adjusting strap, mid-conversation.
                 - Avoid: Direct camera stare, symmetrical poses, hands at sides.

                **CHARACTER ARCHETYPE ENERGY (INSPIRATION ONLY):**
                 - Adapt these vibes to each character's personality:
                   * **Melancholic Dreamer:** Distant gaze, slouched, detached.
                   * **Chaotic Provocateur:** Aggressive stance, sneering, confrontational.
                   * **Cool Prodigy:** Effortless confidence, minimal expression.
                   * **Grounded Anchor:** Solid stance, watchful, protective.
                   * **Manic Energy:** Unpredictable poses, wide-eyed or grinning.
                   * **Jaded Veteran:** Tired, cigarette present, cynical smirk.

                **=== BANNED WORDS & PHRASES (IF YOU USE THESE, YOU FAILED) ===**
                
                **BANNED EYE TERMS:** 'brown eyes', 'blue eyes', 'green eyes', 'cast brown eyes', 'cast [any color] eyes', 'piercing eyes', 'iris', 'pupils', 'eye color', 'eyes hold [emotion]', 'gaze reflects'
                → USE: 'simple black dot eyes', 'hollow circular eyes', 'void-like eyes'
                
                **BANNED ANATOMY:** 'realistic proportions', 'soft skin', 'porcelain skin', 'smooth skin', 'flawless', 'photorealistic', 'lifelike'
                → USE: 'cartoon proportions', 'angular features', 'exaggerated limbs', 'stylized'
                
                **BANNED FACE TERMS:** 'painted with lipstick', 'glossy lips', 'symmetrical face', 'conventionally attractive', 'defined jawline' (too realistic)
                → USE: 'crooked features', 'angular cartoon face', 'simplified features', 'blocky jaw'
                
                **BANNED LIGHTING:** 'soft lighting', 'soft and diffused', 'gentle gradient', 'subtle glow on skin', 'volumetric', 'ambient'
                → USE: 'harsh neon', 'hard cel-shading', 'flat shadows', 'stark contrast'
                
                **BANNED BACKGROUND:** 'plain background', 'gradient background', 'isolated portrait', 'floating', 'white background'
                → USE: Describe ACTUAL OBJECTS: 'graffiti-covered brick wall', 'amp stack', 'neon sign', 'band posters'

                **FINAL CHECK - ASK YOURSELF:**
                 1. Did I write ANY eye color? → DELETE IT NOW
                 2. Did I describe a background with 3+ specific objects? → If NO, ADD THEM
                 3. Does this sound like a Gorillaz/KND cartoon frame? → If NO, make it MORE STYLIZED
                 4. Did I use "plain background"? → DELETE IT, add environment instead

                Rendering: 2D CARTOON style. Forbid: 3D, photorealism, realistic anatomy, soft lighting, empty backgrounds. Must look like a frame from an animated music video with COMPLETE backgrounds.
                """
            }
        }

    fun appearanceGuidelines(genre: Genre): String =
        when (genre) {
            HEROES -> {
                """
                Heroes must wear vibrant and colorful supersuits, complete with dynamic masks, iconic emblems, and functional accessories.
                Emphasize a powerful, heroic silhouette and clean, distinct lines. The outfit should reflect their powers or origin.
                """.trimIndent()
            }

            CYBERPUNK -> {
                """
                Cyberpunk characters should feature futuristic, asymmetrical clothing, unique high-tech accessories (e.g., data readers, holographic communicators),
                and distinctive cybernetic modifications on their face or limbs (e.g., glowing implants, metallic prosthetics, intricate circuitry patterns).
                Their appearance should convey a blend of cutting-edge technology and gritty street style.
                """.trimIndent()
            }

            COWBOY -> {
                """
                Cowboy characters should wear classic western outfits: wide-brimmed hats, bandanas, rugged boots, leather vests, and denim or canvas clothing.
                Accessories like spurs, holsters, and sheriff badges are common. Their look should evoke the spirit of the frontier and rural Americana.
                """.trimIndent()
            }

            SHINOBI -> {
                """
                Shinobi characters should wear traditional ninja or samurai-inspired attire: dark, layered robes, hakama pants, arm guards, tabi boots, and headbands or masks.
                Outfits may include subtle armor pieces and sashes. Their appearance should be stealthy, agile, and rooted in feudal Japanese aesthetics.
                """.trimIndent()
            }

            CRIME -> {
                """
                Crime city characters should dress in High-End Luxury Resort Wear: tailored linen suits, flowing silk dresses, designer swimwear, and expensive accessories.
                Think 'Old Money' aesthetic, yacht parties, and exclusive beach clubs. Outfits should feature premium fabrics, elegant cuts, and statement pieces like oversized sunglasses, gold jewelry, and designer watches.
                Their appearance must radiate wealth, sophistication, and effortless glamour.
                """.trimIndent()
            }

            HORROR -> {
                """
                Horror characters should wear worn, distressed clothing in muted or dark tones. Outfits may include tattered coats, faded uniforms, or vintage garments.
                Their appearance should evoke unease, mystery, and psychological tension, fitting the haunted and uncanny mood.
                """.trimIndent()
            }

            FANTASY -> {
                """
                Fantasy characters should wear medieval or ancient-inspired outfits: cloaks, tunics, armor, robes, and boots.
                Accessories like belts, pouches, magical amulets, and weapons are common. Their look should evoke epic adventure, mysticism, and a connection to legendary worlds.
                """.trimIndent()
            }

            SPACE_OPERA -> {
                """
                Space opera characters should wear retro-futuristic suits, sleek uniforms, and bold accessories. Outfits may include metallic fabrics, capes, visors, and utility belts.
                Their appearance should evoke classic sci-fi adventure, interstellar travel, and atomic age optimism.
                """.trimIndent()
            }

            PUNK_ROCK -> {
                """
                Punk rock characters must be stylized and caricatured, NOT realistic. Use exaggerated proportions (lanky or stocky), simple dot eyes, and expressive, jagged features.
                Clothing: leather jackets, ripped tees, combat boots, and vibrant dyed hair (acid green, toxic yellow). The look is 'ugly-cute', rebellious, and distinct.
                """.trimIndent()
            }
        }

    fun nameDirectives(genre: Genre) =
        (
            when (genre) {
                FANTASY -> {
                    """
                    Aim for names that evoke a sense of magic, ancient lore, epic adventure, or mystical origins.
                    Consider influences from high fantasy (e.g., Tolkien-esque, D&D),
                    classical mythology (Greek, Norse, Celtic), medieval European, or unique, melodious sounds.
                    Names can be majestic, archaic, rustic, tribal, or subtly magical.
                    AVOID overly modern, generic, or overtly tech-sounding names (e.g., John, Mary, Smith, unit numbers, cyber-names).
                    """
                }

                CYBERPUNK -> {
                    """
                     - Aim for names that blend futuristic, cyberpunk, or slightly exotic sounds.
                     - Consider influences from Japanese, tech-inspired, or gritty Western phonetics.
                    Avoid names that are overtly heroic or melodramatic.
                    Try to create names that is common in the language ${currentLanguage()} .
                """
                }

                HORROR -> {
                    """
                     - Aim for names that evoke a sense of unease and dread, fitting a grim, dark, or mysterious setting.
                     - For human characters, use common, simple, and contemporary names from ${currentLanguage()} language.
                    The horror comes from the mundane.
                     - For creatures, entities, or local myths, use names that are descriptive (e.g., "O Vulto," "A Dama de Preto," "O Sussurro"), guttural, or have a more complex, unsettling feel.
                     - Avoid names that are overtly heroic, futuristic, or melodramatic.
                   """
                }

                HEROES -> {
                    """
                     - Aim for names that feel grounded, contemporary, and reflect a diverse urban environment.
                     - Consider influences from street culture, hip-hop, graffiti art, and modern city life.
                     - Names should be cool, edgy, and slightly mysterious, hinting at a hidden identity.
                     - Blend common names with unique nicknames or shortened versions.
                     - Avoid overly fantastical, archaic, or overtly heroic names.
                     - Try to create names that are common in the language ${currentLanguage()},
                    but with a modern twist or a unique nickname.
                     - Consider names that evoke a sense of agility, speed, or resourcefulness.
                     - Think about names that could easily become a street tag or a whispered legend.
                    """
                }

                CRIME -> {
                    """
                     - Aim for names fitting a crime drama set in a stylized neon city.
                     - Blend gritty street nicknames with classic, timeless first names.
                     - Consider influences from 80s Miami/LA crime fiction, Latin and Anglo names common in ${currentLanguage()} locales.
                     - Short, punchy monikers or evocative aliases work well (e.g., "Vega", "Neon", "Santos", "Roxie").
                     - Avoid overtly sci-fi or fantasy elements.
                    """
                }

                SHINOBI -> {
                    """
                     - Aim for names rooted in feudal Japan or stylized adaptations that fit the setting.
                     - Consider short, evocative names or clan-like monikers (e.g., "Aka-ryu", "Kage", "Hanae", "Shirogane").
                     - Blend historical Japanese-sounding names with terse nicknames suitable for operatives and covert figures.
                     - Avoid overtly modern slang or sci-fi terminology.
                    """
                }

                SPACE_OPERA -> {
                    """
                    Concept: Evoke exploration, cosmic significance, advanced scientific concepts, or ancient, wise origins.
                    Influences: Classical astronomy, mythological figures (adapted for space), scientific terms, melodious and ethereal sounds, names suggesting vastness.
                    Avoid: Overtly aggressive or militaristic names, overly "hard" sci-fi jargon (unless for specific tech), modern slang.
                    Try: Names with soft vowels and unique consonant combinations (e.g., Lyra, Orion, Xylos, Aetheria).    
                    """
                }

                COWBOY -> {
                    """
                    Aim for names that sound rugged, biblical, or have a nickname quality.
                    Examples: Jed, Silas, "Tex", "Slim", Ezekiel, Clementine.
                    Avoid modern or overly fancy names.
                    """
                }

                PUNK_ROCK -> {
                    """
                    Aim for names influenced by music culture, street-smart, and edgy.
                    Consider: Music-culture nicknames, street-smart names, modern edgy sounds, names that reference music terms or artist-like qualities.
                    Examples: Echo, Vinyl, Riff, Chord, Neon, Sage, Blaze, Sonic, Rebel, Riot, or street nicknames reflecting personality or musical style.
                    Avoid: Overly heroic names, fantasy-sounding names, corporate-sounding names.
                    Embrace: Short, punchy names that feel authentic to youth and music culture.
                    """
                }
            }
        ).plus("Try common names in ${currentLanguage()}").trimIndent()

    fun conversationDirective(genre: Genre) =
        when (genre) {
            FANTASY -> {
                """
            This directive defines the specific linguistic style for the Fantasy genre.
            NPCs and narrative voice should evoke a sense of ancient lore, heroism, magic, and medieval or mythical settings.
            
            1.  Language & Vocabulary:
                * Terminology: Incorporate terms related to magic, mythical creatures, ancient kingdoms, weaponry, and fantastical concepts (e.g., "enchantment," "arcane," "grimoire," "blade," "realm," "wyrm," "fey," "druid," "lord/lady").
                * Formality: Dialogue can range from formal and archaic (for nobles, mages, ancient beings) to more rustic (for common folk, villagers). Avoid overly modern slang.
                * Archaic Phrasing: Use subtly archaic phrasing or vocabulary where appropriate to enhance the fantasy feel (e.g., "hark," "perchance," "methinks," "hither," "thou/thee" - use sparingly to avoid being cumbersome, perhaps for specific character types).
                * Profanity (Conditional): Profanity should be rare and, if used, should reflect historical/fantasy-appropriate expletives rather than modern ones.
            
            2.  Tone & Delivery:
                * Epic & Heroic: The tone can often be grand, epic, or heroic, especially in moments of adventure or conflict.
                * Mystical & Respectful: Characters might speak with reverence towards magic, gods, or ancient powers.
                * Wisdom & Lore: Older or learned characters might speak in riddles, proverbs, or with deep knowledge of lore.
                * Pacing: Dialogue can be more measured, allowing for descriptions and dramatic pauses.
            
            3.  Narrative Voice:
                * Descriptions should be rich, evocative, focusing on landscapes, magical effects, detailed attire, and historical/mythical elements.
                * Maintain a sense of wonder, mystery, or impending doom as appropriate for the scene.
                """
            }

            CYBERPUNK -> {
                """
             This directive defines the specific linguistic style for the Cyberpunk/Dystopian Sci-Fi genre.
            NPCs and narrative voice should reflect a gritty, tech-infused, and often cynical tone.
            
            1.  Language & Vocabulary:
                * Terminology: Freely use tech jargon, hacking terms, corporate slang, and futuristic street argot (e.g., "net-runner," "chrome," "synth-skin," "data-jack," "augment," "glitch," "gig").
                * Formality: Conversations can range from casual to aggressively direct. Formal language is rare, often reserved for corporate figures or those trying to exert power.
                * Slang & Idioms: Incorporate contemporary or invented cyberpunk-specific slang and idioms.
                * Profanity (Conditional): If appropriate for the character's personality and the grim nature of the setting, moderate use of mild to strong profanity is acceptable to enhance realism and grit. Use it sparingly for impact, not gratuitously.
            
            2.  Tone & Delivery:
                * Cynicism & Weariness: Many characters should reflect a sense of disillusionment, world-weariness, or cynicism towards authority and the system.
                * Directness: Dialogues can be blunt, terse, and to the point.
                * Suspicion: Characters might often be guarded, suspicious, or secretive in their speech.
                * Pacing: Dialogue can be fast-paced, reflecting the urgency and high-stakes environment.
            
            3.  Narrative Voice:
                * Descriptions should be sharp, often highlighting the decay, neon glow, advanced tech, and disparity of the dystopian future.
                * Maintain an edgy, sometimes detached, perspective.
                """
            }

            HORROR -> {
                """
               This directive defines the specific linguistic style for the Horror genre, blending cosmic dread with grounded, psychological terror.
                The tone should evoke a sense of unease, psychological tension, and the creeping dread of the unknown.
                
                1.  Language & Vocabulary:
                    * Terminology: Use language that ranges from the mundane to terms that suggest the occult, the inexplicable, or a descent into madness (e.g., "whisper," "ritual," "cyclopean," "non-Euclidean," "anomaly," "sanity erodes").
                    * Formality: Dialogue can be casual and realistic (like everyday people), but the narration and tone can become more formal or clinical when describing the horror, creating a chilling contrast.
                    * Phrasing: Use phrases that allude to, but do not explicitly describe, the horror, focusing on how characters perceive the threat.
                    * Profanity (Conditional): Profanity should be used realistically, sparingly, and contextually to reflect a character's stress and terror.
                
                2.  Tone & Delivery:
                    * Psychological Dread: The tone should build tension and paranoia. Characters should express fear, suspicion, and a gradual decline in their mental state.
                    * Mundane vs. Sinister: The tone should highlight the contrast between a seemingly normal environment and the subtle, growing threat lurking beneath the surface.
                    * Desperation: Dialogue should, over time, reflect a sense of urgency, desperation, and a growing helplessness against the unknown.
                    * Pacing: The pace should be slow and deliberate at first to build suspense, accelerating during moments of climax or revelation.
                
                3.  Narrative Voice:
                    * Descriptions should be detailed but focus on small, everyday elements that become sinister (e.g., a creak in the floor, a shadow in the corner).
                    * The narrative should maintain a sense that reality is distorting and the threat is something the human mind can barely comprehend, drawing from cosmic horror.
                    * Avoid explicit and graphic descriptions of the horror, opting instead to hint at what is indescribable to heighten the reader's fear.
                     
               """
            }

            HEROES -> {
                """
                This directive defines the specific linguistic style for the Urban Hero genre.
                NPCs and narrative voice should reflect a contemporary, street-smart, and often gritty tone, blending realism with a sense of hidden potential.
                
                1.  Language & Vocabulary:
                    * Terminology: Incorporate contemporary slang, street jargon, and terms related to urban life, parkour, technology (but not overly futuristic), and local landmarks (e.g., "spot," "crew," "grind," "flow," "tag," "wire," "glitch," "the block").
                    * Formality: Dialogue should generally be informal and conversational, reflecting the way people actually speak in a city. Vary formality based on character age, background, and social standing.
                    * Slang & Idioms: Use contemporary slang and idioms authentically, but avoid overly trendy terms that might quickly date the dialogue.
                    * Profanity (Conditional): Moderate use of profanity is acceptable to enhance realism and character authenticity, but avoid gratuitous or excessive swearing. Use it strategically for impact.
                
                2.  Tone & Delivery:
                    * Street-Smart & Resourceful: Characters should sound quick-witted, adaptable, and capable of navigating the urban landscape.
                    * Cynicism & Hope: A blend of cynicism about the system and a glimmer of hope for making a difference.
                    * Directness & Authenticity: Dialogue should be direct and honest, avoiding overly dramatic or flowery language.
                    * Pacing: Dialogue can be fast-paced and energetic, reflecting the rhythm of city life.
                
                3.  Narrative Voice:
                    * Descriptions should be vivid and detailed, focusing on the sights, sounds, and smells of the city. Highlight the contrast between beauty and decay, opportunity and danger.
                    * Maintain a sense of realism and groundedness, even when describing extraordinary events.
                    * Focus on the human element – the struggles, dreams, and resilience of the people who live in the city.
                    * The narrative should subtly hint at the hidden potential and extraordinary abilities that exist beneath the surface of everyday life.
                """
            }

            CRIME -> {
                """
                This directive defines the specific linguistic style for the Crime City genre.
                NPCs and narration should evoke 80s crime drama with a neon-soaked, Miami Vice mood.
                
                1. Language & Vocabulary:
                    * Terminology: Use crime and street terms (e.g., "stakeout", "heat", "hustle", "dirty money", "dealer", "detective", "vice squad").
                    * Formality: Conversational and direct. Cops may be clipped and procedural; criminals can be slick, terse, or menacing.
                    * Slang & Idioms: Period-appropriate 80s flavor where possible; avoid modern internet slang.
                    * Profanity (Conditional): Moderate and contextual—used for grit, not excess.
                
                2. Tone & Delivery:
                    * Cool, tense, and stylish. Understated bravado with subtext; terse exchanges and loaded pauses.
                    * Noir sensibility meets pop neon. Melancholic glamour and danger.
                    * Pacing: Snappy during action or interrogation; laconic and moody between beats.
                """
            }

            SPACE_OPERA -> {
                """
                Vocabulary: Galactic exploration, profound discoveries, cosmic phenomena,
                ancient alien civilizations, advanced technology, philosophical ponderings about existence.
                Formality: Varies from adventurous and eloquent (explorers, scientists) to mysterious and ancient (alien entities).
                Phrasing: Evocative and grand, with a sense of wonder and epic scope.
                Tone: Aspirational, mysterious, awe-inspiring, adventurous, contemplative.
                """
            }

            SHINOBI -> {
                """
                This directive defines the specific linguistic style for the Shinobi (Mythical Feudal Japan) genre.
                NPCs and narrative voice should evoke a sense of discipline, tradition, and underlying tension.
                
                1.  Language & Vocabulary:
                    * Terminology: Use terms related to feudal Japan, martial arts, espionage, and honor (e.g., "shogun," "daimyo," "samurai," "ronin," "kunoichi," "jutsu," "katana," "oni," "yokai").
                    * Formality: Dialogue should be respectful and often formal, reflecting the hierarchical society. Use honorifics where appropriate (e.g., "-san," "-sama").
                    * Phrasing: Sentences are often concise and deliberate. Avoid unnecessary words.
                    * Profanity (Conditional): Extremely rare. Insults are more about dishonor than vulgarity.
                
                2.  Tone & Delivery:
                    * Reserved & Disciplined: Characters speak with restraint and precision. Emotion is shown through subtext, not overt displays.
                    * Tense & Mysterious: A constant undercurrent of suspicion, hidden motives, and political intrigue.
                    * Respectful & Traditional: Speech reflects a deep respect for tradition, duty, and honor.
                    * Pacing: Dialogue can be slow and measured, with meaningful pauses.
                
                3.  Narrative Voice:
                    * Descriptions should focus on atmosphere—the rustle of bamboo, the glint of a blade in moonlight, the quiet tension of a room.
                    * Maintain a sense of quiet grace and lethal potential.
                """
            }

            COWBOY -> {
                """
                This directive defines the linguistic style for the Cowboys genre.
                NPCs and narrative voice should be laconic, stoic, and flavored with Western slang.

                1. Language & Vocabulary:
                    * Terminology: Use Western slang (e.g., "reckon", "howdy", "yonder", "fixin' to", "varmint").
                    * Formality: Casual but respectful (e.g., "Ma'am", "Sir").
                    * Phrasing: Simple, direct, and often colorful idioms.

                2. Tone & Delivery:
                    * Laconic & Stoic: Characters speak only when necessary. "Strong silent type".
                    * Drawl: Implied slow, deliberate speech pattern.
                    * Grit: A sense of toughness and resilience.

                3. Narrative Voice:
                    * Descriptions should emphasize the harshness and beauty of the frontier.
                    * Focus on sensory details: heat, dust, the smell of leather and horses.
                """
            }

            PUNK_ROCK -> {
                """
                This directive defines the specific linguistic style for the Punk Rock genre.
                NPCs and narrative voice should reflect confident, rebellious, and energetic youth culture with music as a central theme.

                1. Language & Vocabulary:
                    * Terminology: Music culture terminology (gig, jam, beat, riff, distortion, reverb, amplifier, stage, crowd, vibe).
                    * Contemporary Street Slang: Use modern, casual slang authentically (avoiding overly trendy terms that will date quickly).
                    * Band/Music References: Characters may reference bands, songs, concerts, or music genres naturally in dialogue.
                    * Youth Culture Terms: Terms reflecting teenage/young adult experience, independence, creativity.
                    * Formality: Minimal to none. Dialogue is informal, conversational, direct. No "sir/ma'am" unless ironic.
                    * Profanity (Conditional): Moderate profanity is acceptable to reflect authenticity of youth culture and rebellious spirit. Use strategically, not gratuitously.

                2. Tone & Delivery:
                    * Confident & Rebellious: Characters speak with conviction and a defiant edge. They question authority and embrace individuality.
                    * Irreverent & Playful: Humor is sarcastic, witty, self-deprecating. Characters don't take themselves too seriously.
                    * Passionate: When discussing music, art, or causes they care about, characters become animated and intense.
                    * Fast-Paced & Energetic: Dialogue is quick, dynamic, reflecting the energy of live music and youth culture.
                    * Authentic & Real: Dialogue should feel genuine to how teenagers/young adults actually speak—casual, stream-of-consciousness, with interruptions and tangents.

                3. Narrative Voice:
                    * Descriptions should be vivid and energetic, focusing on sensory details: the roar of crowd, the crunch of amplifiers, the smell of a crowded venue, sweat and electricity.
                    * Emphasize movement, dynamism, and visual energy—people dancing, musicians performing, creative expression in action.
                    * Maintain a sense of youthful optimism mixed with edgy rebellion—idealism tempered with street-smart attitude.
                    * The narrative should capture the exhilaration and freedom of music and creative self-expression.
                """
            }
        }.trimIndent()

    /**
     * Get the reviewer strictness level for a specific genre.
     * Based on how rigid the art style requirements are.
     */
    fun reviewerStrictness(genre: Genre): com.ilustris.sagai.core.ai.models.ReviewerStrictness =
        when (genre) {
            // STRICT: Cartoon styles with specific anatomical requirements
            PUNK_ROCK -> com.ilustris.sagai.core.ai.models.ReviewerStrictness.STRICT

            HORROR -> com.ilustris.sagai.core.ai.models.ReviewerStrictness.STRICT

            // CONSERVATIVE: Stylized but with some flexibility
            CYBERPUNK -> com.ilustris.sagai.core.ai.models.ReviewerStrictness.CONSERVATIVE

            HEROES -> com.ilustris.sagai.core.ai.models.ReviewerStrictness.CONSERVATIVE

            SPACE_OPERA -> com.ilustris.sagai.core.ai.models.ReviewerStrictness.CONSERVATIVE

            SHINOBI -> com.ilustris.sagai.core.ai.models.ReviewerStrictness.CONSERVATIVE

            // LENIENT: Traditional art with organic flexibility
            FANTASY -> com.ilustris.sagai.core.ai.models.ReviewerStrictness.LENIENT

            CRIME -> com.ilustris.sagai.core.ai.models.ReviewerStrictness.LENIENT

            COWBOY -> com.ilustris.sagai.core.ai.models.ReviewerStrictness.LENIENT
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
                - Eye colors: 'brown eyes', 'blue eyes', 'green eyes', 'cast [color] eyes', 'piercing eyes', 'iris', 'pupils'
                - Realistic anatomy: 'realistic proportions', 'soft skin', 'porcelain skin', 'photorealistic', 'lifelike'
                - Soft lighting: 'soft lighting', 'soft and diffused', 'gentle gradient', 'subtle glow', 'volumetric'
                - Empty backgrounds: 'plain background', 'gradient background', 'solid color background', 'white background', 'isolated portrait'
                
                REQUIRED ELEMENTS:
                - Eyes MUST be: 'simple black dot eyes', 'hollow circular eyes', 'void-like eyes', 'white circle eyes'
                - Background MUST include 3+ specific objects: 'graffiti wall', 'amp stack', 'neon sign', 'band posters', 'brick wall', etc.
                - Proportions MUST be: 'cartoon proportions', 'exaggerated limbs', 'angular features', 'stylized'
                - Shading MUST be: 'hard cel-shading', 'flat shadows', 'stark contrast', 'graphic blocks'
                
                FRAMING ADAPTATION:
                - Portrait/Close-up: Background still required but adapted (wall behind head, neon sign glow, posters visible)
                - NO 'plain' or 'solid color' backgrounds even for tight framing
                """.trimIndent()
            }

            HORROR -> {
                """
                **CRITICAL VALIDATION RULES FOR HORROR:**
                
                BANNED TERMS:
                - Modern rendering: 'photorealistic', '3D render', 'smooth gradients', 'high-res textures'
                - Vibrant colors: avoid bright, saturated colors outside the dark/pale blue palette
                
                REQUIRED ELEMENTS:
                - Art style MUST mention: '32-BIT PIXEL ART', 'BLOCKY SHADING', 'PS1/SEGA SATURN aesthetic'
                - Texture MUST be: 'pixelated', 'retro game art', 'low-resolution'
                - Palette MUST be: 'limited dark and pale blue', 'ash gray', 'faded cerulean'
                
                FRAMING ADAPTATION:
                - All framings work with pixel art style
                - Maintain blocky, pixelated appearance regardless of shot distance
                """.trimIndent()
            }

            CYBERPUNK -> {
                """
                **CRITICAL VALIDATION RULES FOR CYBERPUNK:**
                
                BANNED TERMS:
                - Modern techniques: '3D CGI', 'photorealism', 'digital smoothing', 'Unreal Engine', 'volumetric lighting'
                - Bright neon: 'bright fluorescent pink', 'bright purple' (only DEEP purple allowed)
                - Soft rendering: 'soft gradients', 'ambient occlusion', 'subsurface scattering'
                
                REQUIRED ELEMENTS:
                - Art style MUST mention: '1980s Anime OVA', 'Cel Animation', 'FLAT SHADING'
                - Shadows MUST be: 'hard-edged blocks', 'cel-shaded', 'no soft gradients'
                - Colors MUST be: 'muted blue tones', 'slate blue', 'cold steel', 'DEEP purple accent'
                - Lighting MUST be: 'high-contrast Rembrandt', 'hard rim light', 'cast shadows'
                
                FRAMING ADAPTATION:
                - Maintain flat shading and hard edges regardless of framing
                - Hair as 'clumps' not strands, even in close-ups
                """.trimIndent()
            }

            HEROES -> {
                """
                **VALIDATION RULES FOR HEROES:**
                
                BANNED TERMS:
                - Avoid: forced night scenes (prefer dynamic daytime/sunset)
                
                REQUIRED ELEMENTS:
                - Art style: 'Modern comic book art', 'bold ink lines', 'dynamic foreshortening'
                - Environment: 'vast cityscapes', 'towering skyscrapers', 'dizzying perspectives'
                - Colors: 'dynamic urban palette' with 'subtle electric blue accent'
                
                FRAMING ADAPTATION:
                - Emphasize vertical scale and urban environment
                - Maintain comic book line work quality
                """.trimIndent()
            }

            SPACE_OPERA -> {
                """
                **VALIDATION RULES FOR SPACE_OPERA:**
                
                BANNED TERMS:
                - Avoid: 'primitive rockets', 'simple tube rockets', 'basic spacecraft'
                
                REQUIRED ELEMENTS:
                - Art style: '1950s Atomic Age illustration', 'gouache/oil', 'painterly finish'
                - Environment: 'vast cosmic environments', 'swirling nebulae', 'star fields', 'cosmic phenomena'
                - Technology (if present): 'streamlined starships', 'sophisticated engines', 'advanced design'
                - Figures: 'idealized 50s illustration', 'pin-up or action hero archetypes'
                
                FRAMING ADAPTATION:
                - Focus on cosmic ambience over technology details
                - Maintain retro-futuristic optimism
                """.trimIndent()
            }

            FANTASY -> {
                """
                **VALIDATION RULES FOR FANTASY:**
                
                REQUIRED ELEMENTS:
                - Art style: 'gritty dark fantasy oil painting', 'visible brushstrokes', 'loose impasto'
                - Palette: 'earthy tones', 'mud, steel, stone' with 'ember gold/fiery orange accent'
                - Texture: 'canvas oil', 'painterly quality'
                
                FRAMING ADAPTATION:
                - Allow artistic interpretation within oil painting tradition
                - Maintain brushstroke visibility and earthy palette
                """.trimIndent()
            }

            CRIME -> {
                """
                **VALIDATION RULES FOR CRIME:**
                
                REQUIRED ELEMENTS:
                - Art style: 'hyper-realistic classical oil painting', 'academic realism'
                - Technique: 'smooth glazing', 'no rough brushstrokes', 'flawless surface'
                - Lighting: 'divine & ethereal', 'soft diffused heavenly light'
                
                FRAMING ADAPTATION:
                - Maintain smooth, flawless rendering quality
                - High-fashion luxury aesthetic required
                """.trimIndent()
            }

            COWBOY -> {
                """
                **VALIDATION RULES FOR COWBOY:**
                
                BANNED TERMS:
                - Avoid: 'photorealism', 'smooth digital blending', 'refined details'
                
                REQUIRED ELEMENTS:
                - Art style: 'expressive western oil painting', 'Remington/Russell style'
                - Technique: 'bold visible brushstrokes', 'thick impasto', 'painterly quality'
                - Mood: 'rough edges', 'bold color blocks', 'artistic interpretation'
                
                FRAMING ADAPTATION:
                - Maintain expressive brushwork at all scales
                - Emphasize painted quality over photographic detail
                """.trimIndent()
            }

            SHINOBI -> {
                """
                **VALIDATION RULES FOR SHINOBI:**
                
                REQUIRED ELEMENTS:
                - Traditional Japanese aesthetic
                - Stealth and agility emphasis
                - Feudal Japan styling
                
                FRAMING ADAPTATION:
                - Maintain cultural authenticity
                """.trimIndent()
            }
        }
}
