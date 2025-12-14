package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.currentLanguage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.COWBOY
import com.ilustris.sagai.features.newsaga.data.model.Genre.CRIME
import com.ilustris.sagai.features.newsaga.data.model.Genre.CYBERPUNK
import com.ilustris.sagai.features.newsaga.data.model.Genre.FANTASY
import com.ilustris.sagai.features.newsaga.data.model.Genre.HEROES
import com.ilustris.sagai.features.newsaga.data.model.Genre.HORROR
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
                
                CRITICAL RENDERING NOTE (ABSOLUTE): Render as a full-bleed, edge-to-edge illustration with NO BORDERS, FRAMES, TEXT, OR UI ARTIFACTS.
                The output must be a clean composition suitable for final printing or display.
                
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
                NO TEXT, NO LOGOS, NO GRAPHIC SHAPES, NO BORDERS.
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
        }

    fun appearanceGuidelines(genre: Genre): String =
        when (genre) {
            HEROES -> "Heroes must wear vibrant and colorful supersuits, complete with dynamic masks, iconic emblems, and functional accessories. Emphasize a powerful, heroic silhouette and clean, distinct lines. The outfit should reflect their powers or origin."
            CYBERPUNK -> "Cyberpunk characters should feature futuristic, asymmetrical clothing, unique high-tech accessories (e.g., data readers, holographic communicators), and distinctive cybernetic modifications on their face or limbs (e.g., glowing implants, metallic prosthetics, intricate circuitry patterns). Their appearance should convey a blend of cutting-edge technology and gritty street style."
            COWBOY -> "Cowboy characters should wear classic western outfits: wide-brimmed hats, bandanas, rugged boots, leather vests, and denim or canvas clothing. Accessories like spurs, holsters, and sheriff badges are common. Their look should evoke the spirit of the frontier and rural Americana."
            SHINOBI -> "Shinobi characters should wear traditional ninja or samurai-inspired attire: dark, layered robes, hakama pants, arm guards, tabi boots, and headbands or masks. Outfits may include subtle armor pieces and sashes. Their appearance should be stealthy, agile, and rooted in feudal Japanese aesthetics."
            CRIME -> "Crime city characters should dress in High-End Luxury Resort Wear: tailored linen suits, flowing silk dresses, designer swimwear, and expensive accessories. Think 'Old Money' aesthetic, yacht parties, and exclusive beach clubs. Outfits should feature premium fabrics, elegant cuts, and statement pieces like oversized sunglasses, gold jewelry, and designer watches. Their appearance must radiate wealth, sophistication, and effortless glamour."
            HORROR -> "Horror characters should wear worn, distressed clothing in muted or dark tones. Outfits may include tattered coats, faded uniforms, or vintage garments. Their appearance should evoke unease, mystery, and psychological tension, fitting the haunted and uncanny mood."
            FANTASY -> "Fantasy characters should wear medieval or ancient-inspired outfits: cloaks, tunics, armor, robes, and boots. Accessories like belts, pouches, magical amulets, and weapons are common. Their look should evoke epic adventure, mysticism, and a connection to legendary worlds."
            SPACE_OPERA -> "Space opera characters should wear retro-futuristic suits, sleek uniforms, and bold accessories. Outfits may include metallic fabrics, capes, visors, and utility belts. Their appearance should evoke classic sci-fi adventure, interstellar travel, and atomic age optimism."
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

                    else -> {
                    """
                    Aim for names that sound rugged, biblical, or have a nickname quality.
                    Examples: Jed, Silas, "Tex", "Slim", Ezekiel, Clementine.
                    Avoid modern or overly fancy names.
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
        }.trimIndent()
}
