package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.currentLanguage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.CYBERPUNK
import com.ilustris.sagai.features.newsaga.data.model.Genre.FANTASY
import com.ilustris.sagai.features.newsaga.data.model.Genre.HORROR

object GenrePrompts {
    fun detail(genre: Genre) = "theme: ${genre.name}"

    fun artStyle(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                **Grand Classical oil painting, High Classicism style (17th Century Master)**,
                rich impasto texture, visible brushstrokes,
                **Epic Chiaroscuro**, heroic lighting, **natural/varied ambient light.**
                **Ethereal and Majestic aesthetic, Vintage color grading**,
                Harmonious colors, authentic painterly grain, Crisp atmosphere.
                """

            CYBERPUNK ->
                """
               retro anime, 80s anime, 90s anime, cel shading,
               cel art, anime cel, bold outlines, varied line weight,
               limited color palette, Akira, Ghost in the Shell (1995),
               Bubblegum Crisis, theatrical anime, OVA, film grain, animation cel artifacting,
               stylized backgrounds, industrial aesthetic, melancholic atmosphere
               """

            HORROR ->
                """
                pixel art,
                32-bit,
                retro game art,
                pixelated,
                blocky shading.
                Haunted and Mystique dark aesthetic.
                Mystique haunted pale blue background.
                """

            Genre.HEROES -> {
                """
                Comic book art style, characterized by clean and detailed line work,
                anatomically accurate figures, and dynamic poses.
                The shading is subtle, utilizing cross-hatching and texture to create depth.
                Lighting is dramatic, with strong highlights and shadows emphasizing form.
                The color palette is primarily teal and white, accented with darker tones to create a sense of depth and atmosphere.
                The overall aesthetic evokes the superhero comics of the 1990s, with a slightly painterly quality.  
                """
            }

            Genre.CRIME ->
                """
                
                **CRITICAL: RAW, UNREFINED, FUNCTIONAL Comic Panel Illustration. The style must be AUSTERE and GRITTY, strongly avoiding any idealized, beautiful, or overly polished aesthetic commonly found on comic covers.**
                Linework must be **FAST, IMPERFECT, and AUSTERE**, with a **raw, gestural sketch quality**, emphasizing **functional narrative clarity** over aesthetic polish. Avoid all forms of smooth, clean digital rendering.
                
                **Anti-Idealization and Simplicity (BRUTAL SIMPLIFICATION):**
                Facial features and anatomical details must be rendered with **austere simplification** and **minimalist precision**, deliberately resembling a **quickly-drawn internal comic page panel, NOT an idealized or beautiful cover portrait.**
                **Hair must use brutal simplification**, rendered in **flat, contiguous ink blocks**, **AVOIDING** individual strand lines, volume definition, or any internal soft light/shadow rendering.
                **Eyes must use a simplified, two-tone rendering: a flat, colored iris and a solid, black pupil.** CRITICALLY, they must **LACK ALL** highlights, catchlights, or complex reflective details, maintaining a mature, non-idealized appearance.
                **CRITICAL:** The final image must possess a **deliberate lack of polish**. Avoid any excessive luminosity, high-gloss shine, or idealized beauty filters on the skin, eyes, or hair.
                
                **Coloring and Shadow (BRUTALIST & FLAT):**
                Coloring must use **flat, highly saturated color blocks** with **ZERO smooth gradients**.
                Shadows must be **SOLID, black/dark ink-wash blocks** that are **graphic, high-contrast, and angular**, explicitly **avoiding soft light or any form of photorealistic rendering**.
                
                **Lighting and Texture:**
                Lighting must be **extremely dramatic and high-contrast** (Chiaroscuro), prioritizing **strong light and deep, ink-block shadows** over mid-tones.
                The final image must have the visual texture of **physical comic book printing and hand-inking**, not a clean digital vector.
                
                **Setting:**
                The setting is a **tropical urban environment** at dusk or night; **subtle hints of palm trees and distant city lights** are sufficient. The background should be minimalist and stylized, allowing the character to dominate. The color palette of the setting should be vibrant and clean.
                
                **Composition Rules:**
                The composition must look like a **standard, functional panel or close-up from an interior comic book page**, prioritizing **narrative focus** and **expressive emotion** over idealized composition.
                DO NOT PLACE ANY TEXTUAL ELEMENT OR GRAPHICAL ELEMENT, RENDER ONLY THE REQUIRED IMAGE.
                
                """
            Genre.SPACE_OPERA ->
                """
                **1960s POP ART, inspired by Roy Lichtenstein's dramatic comic book illustrations, rendered with the aesthetic of VINTAGE OFFSET PRINTING**.
                Characterized by **heavy black inking, flat solid colors, coarse Ben-Day dots APPLIED TO THE CHARACTER'S FACE AND CLOTHING**, and a limited color palette.
                Emphasize **extreme chiaroscuro** lighting, **with shadows rendered in solid black shapes**, high contrast, and stylized features.
                The character's face and figure display stronger, more **defined features with realistic proportions**, **evocando a youthful, determined appearance** while **avoiding any childlike rendering**.
                
                CRITICAL STYLIZATION DIRECTIVE: Since the style is 1960s POP ART, the translated expression MUST be dramatic, stylized, and slightly exaggerated to match the aesthetic of classic comic book panels, ensuring the emotional state is visually explicit.
                Example Translation: "Calma, observadora, mas sobrecarregada" → Intense, reserved gaze, slight frown of profound weariness emphasized by comic book exaggeration of eyebrows and mouth curve.
                """
        }

    fun getColorEmphasisDescription(genre: Genre): String =
        when (genre) {
            FANTASY ->
                """
                **CRITICAL: These warm colors MUST be incorporated as design elements or local light sources. Select one or more options for this warm accentuation:**
                - `The character's sword hilt/blade glows with a subtle orange-red light`
                - `Intricate, gold/red enameling on the character's armor plating`
                - `A small, localized fire, torch, or glowing magical sigil in the scene`
                - `Deep crimson or orange-red fabric elements (e.g., scarf, cloak lining, flag)`
                - `Scattered ground elements in warm colors (e.g., red/orange flowers, specific colored gems, cracked lava/stone)`
                **CRUCIAL:** The warm tones are for **specific accents and localized light reflection**, not the overall ambient light or background color.
                The lighting should be predominantly natural (daylight, torchlight, moonlight) as appropriate for the scene, with only small, focused areas reflecting the warm accent color (e.g., rim light from the glowing sword).
             
                    2.1. **Background and Ambient Context (ATMOSPHERIC PRIORITY):**
                    The Agent MUST describe the environment to set a strong, evocative mood (the "Ambient Context"), but this description **must not dictate the composition's framing or depth of field.** The final prompt must clearly state that the background serves as the setting for the character.
                    * **Mandatory Adaptation:** The image generation model is internally instructed to **apply a depth of field that supports the chosen Framing (e.g., a shallow depth of field for Macro Shots, or a clear focus for Wider Shots),** ensuring the primary subject remains sharp and dominant.
                    * **Contextual Elements (Fantasy):** Use elements like `ancient ruins`, `crumbled stone`, `stormy sky`, `swirling mist`, or `fantasy forest` to define the setting. The Agent should describe these elements in a way that provides **texture and atmosphere**, not necessarily vast, clear detail.
                    
                    **CRITICAL:** The Agent must ensure the Ambient Context description is concise and supports the visual mood (Epic, Somber, etc.) without contradicting the framing term (e.g., 'MACRO SHOT').
                """

            CYBERPUNK ->
                """
                **Specific Color Application Instructions:**
                *The following rules dictate how the genre's key color (purple) is applied.  Imagine a striking visual effect where purple is used as a focused accent, similar to how a brightly colored detail might stand out against a muted background – think a vibrant neon sign in a dark alley.*
                

                **Background & Ambient Lighting:**
                Establish a subtle cyberpunk atmosphere with **ambient purple neon lighting** and atmospheric haze.
                The background should be suggestive of a city at dusk, but remain *subdued and out of focus* to ensure the character remains the dominant element. 
                **The primary focus of the image MUST be the character's face and upper body.**
                
                **Character Purple Highlights:** Integrate **pronounced purple light details and highlights** to create a visually arresting effect. Think of purple as a "pop" of color that draws the eye:
                *   **Focused Accents:** Use purple for key accent elements – glowing cybernetic implants, illuminated details on clothing, or a striking purple light source casting shadows.
                *   **Rim Lighting:** Apply subtle purple rim lighting to define the character's silhouette.
                *   **Emissive Details:** Enhance details like cybernetic eyes or circuit patterns with a purple glow.
                
                **CRUCIAL:** Maintain the character's natural skin tone, hair color, and clothing colors. **Do NOT globally tint the character purple.** Purple should be used for *focused, impactful highlights* – not as an overall color wash.  The goal is to create a visually striking contrast, where the purple elements immediately capture the viewer's attention. **Do not add any new accessories or items to the character that are not already described in the Character Context.**
                                
                """

            HORROR ->
                """
                The **background should be a dark, desaturated blue-gray or near-black**, establishing a grim, oppressive, and moody setting.
                Character accents, if any, must be **extremely minimal, desaturated, and applied to very small details**: for instance, a faint, chilling blue reflection in the eyes, or a tiny, barely perceptible grey pattern on dark clothing.
                **CRUCIAL:** The character should appear largely devoid of vibrant color, blending with the bleak, desaturated environment. Avoid any noticeable color accents that would break the monochromatic feel. The character's skin should appear pale or shadowed, not tinted by any accent color.
                """
            Genre.HEROES ->
                """
                Urban environment.
                The color palette is dominated by vivid blues and teals, accented with black, white, and yellow.
                The background is a detailed cityscape at night, with glowing streetlights, silhouetted buildings, and a sense of urban grit,
                bathed in a cool blue light.
                """

            Genre.CRIME ->
                """
                Tropical environment.
                The color palette is dominated by vivid pink and orange, accented with teal blues.
                The background is a detailed beach at a sunset, with a sense of calm and tranquility.
                In a  Miami Vice aesthetic with pink highlights at the sky and dramatic cinematic lights.
                """

            Genre.SPACE_OPERA ->
                """
                **Background & Ambient Lighting:**
                Establish a subtle, dark, and atmospheric starry sky background.
                The background should be:
                ** minimalist, out of focus, and stylized, resembling a simple painted backdrop with soft, ethereal cosmic dust and small, sparkling stars**.
                Use soft gradients and subtle textures to create a sense of depth without being overly detailed.
                The primary focus of the image MUST be the character's face and upper body.
                """
        }.trimIndent()

    fun cinematographyComposition(genre: Genre) =
        buildString {
            when (genre) {
                Genre.CRIME -> {
                    appendLine(
                        "*The composition MUST emphasize **intense psychological tension and dramatic observation**. Use **cinematic staging** to create emotional distance or scrutiny, such as **observing a character from behind a pane of glass (like a luxury car window) or through heavy shadow/neon reflection**. Focus on aesthetic of **decadent tropical luxury** and urban grit.",
                    )
                    appendLine(
                        "*  The framing should prioritize **emotional reaction and narrative stage setting** (e.g., dual portrait with visual barrier) over direct physical confrontation.",
                    )
                }

                Genre.CYBERPUNK -> {
                    appendLine(
                        "The composition MUST emphasize tension and dramatic melancholy. With high focus on characters emotions and world building.",
                    )
                    appendLine("Focus on **cinematic staging** to create emotional distance or scrutiny")
                    appendLine("Paying attention to futuristic details and intense emotions.")
                }

                else -> {
                    appendLine("Focus on characters involved")
                    appendLine("Creating angles that focus on their conflicts and emotions")
                }
            }
        }

    fun colorAccent(genre: Genre) =
        when (genre) {
            FANTASY -> "Crimson/Red"
            CYBERPUNK -> "Luminous purple/Neon magenta\""
            HORROR -> "Ash gray/Faded cerulean blue"
            Genre.HEROES -> "Midnight blue/Electric blue"
            Genre.CRIME -> "Hot pink/Electric magenta"
            Genre.SPACE_OPERA -> "Cyan/Deep cerulean blue"
        }

    fun nameDirectives(genre: Genre) =

        when (genre) {
            FANTASY ->
                """
                // Aim for names that evoke a sense of magic, ancient lore, epic adventure, or mystical origins.
                // Consider influences from high fantasy (e.g., Tolkien-esque, D&D),
                classical mythology (Greek, Norse, Celtic), medieval European, or unique, melodious sounds.
                // Names can be majestic, archaic, rustic, tribal, or subtly magical.
                // AVOID overly modern, generic, or overtly tech-sounding names (e.g., John, Mary, Smith, unit numbers, cyber-names).
                """

            CYBERPUNK ->
                """
                // - Aim for names that blend futuristic, cyberpunk, or slightly exotic sounds.
                // - Consider influences from Japanese, tech-inspired, or gritty Western phonetics.
                Avoid names that are overtly heroic or melodramatic.
                Try to create names that is common in the language ${currentLanguage()} .
            """

            HORROR ->
                """
                // - Aim for names that evoke a sense of unease and dread, fitting a grim, dark, or mysterious setting.
                // - For human characters, use common, simple, and contemporary names from ${currentLanguage()} language.
                The horror comes from the mundane.
                // - For creatures, entities, or local myths, use names that are descriptive (e.g., "O Vulto," "A Dama de Preto," "O Sussurro"), guttural, or have a more complex, unsettling feel.
                // - Avoid names that are overtly heroic, futuristic, or melodramatic.
               """

            Genre.HEROES ->
                """
                // - Aim for names that feel grounded, contemporary, and reflect a diverse urban environment.
                // - Consider influences from street culture, hip-hop, graffiti art, and modern city life.
                // - Names should be cool, edgy, and slightly mysterious, hinting at a hidden identity.
                // - Blend common names with unique nicknames or shortened versions.
                // - Avoid overly fantastical, archaic, or overtly heroic names.
                // - Try to create names that are common in the language ${currentLanguage()},
                but with a modern twist or a unique nickname.
                // - Consider names that evoke a sense of agility, speed, or resourcefulness.
                // - Think about names that could easily become a street tag or a whispered legend.
                """

            Genre.CRIME ->
                """
                // - Aim for names fitting a crime drama set in a stylized neon city.
                // - Blend gritty street nicknames with classic, timeless first names.
                // - Consider influences from 80s Miami/LA crime fiction, Latin and Anglo names common in ${currentLanguage()} locales.
                // - Short, punchy monikers or evocative aliases work well (e.g., "Vega", "Neon", "Santos", "Roxie").
                // - Avoid overtly sci-fi or fantasy elements.
                """
            Genre.SPACE_OPERA ->
                """
                Concept: Evoke exploration, cosmic significance, advanced scientific concepts, or ancient, wise origins.
                Influences: Classical astronomy, mythological figures (adapted for space), scientific terms, melodious and ethereal sounds, names suggesting vastness.
                Avoid: Overtly aggressive or militaristic names, overly "hard" sci-fi jargon (unless for specific tech), modern slang.
                Try: Names with soft vowels and unique consonant combinations (e.g., Lyra, Orion, Xylos, Aetheria).    
                """
        }.plus("Try common names in ${currentLanguage()}").trimIndent()

    fun conversationDirective(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
            // This directive defines the specific linguistic style for the Fantasy genre.
            // NPCs and narrative voice should evoke a sense of ancient lore, heroism, magic, and medieval or mythical settings.
            
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

            CYBERPUNK ->
                """
             // This directive defines the specific linguistic style for the Cyberpunk/Dystopian Sci-Fi genre.
            // NPCs and narrative voice should reflect a gritty, tech-infused, and often cynical tone.
            
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

            HORROR ->
                """
               // This directive defines the specific linguistic style for the Horror genre, blending cosmic dread with grounded, psychological terror.
                // The tone should evoke a sense of unease, psychological tension, and the creeping dread of the unknown.
                
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
            Genre.HEROES ->
                """
                // This directive defines the specific linguistic style for the Urban Hero genre.
                // NPCs and narrative voice should reflect a contemporary, street-smart, and often gritty tone, blending realism with a sense of hidden potential.
                
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

            Genre.CRIME ->
                """
                // This directive defines the specific linguistic style for the Crime City genre.
                // NPCs and narration should evoke 80s crime drama with a neon-soaked, Miami Vice mood.
                
                1. Language & Vocabulary:
                    * Terminology: Use crime and street terms (e.g., "stakeout", "heat", "hustle", "dirty money", "dealer", "detective", "vice squad").
                    * Formality: Conversational and direct. Cops may be clipped and procedural; criminals can be slick, terse, or menacing.
                    * Slang & Idioms: Period-appropriate 80s flavor where possible; avoid modern internet slang.
                    * Profanity (Conditional): Moderate and contextual—used for grit, not excess.
                
                2. Tone & Delivery:
                    * Cool, tense, and stylish. Understated bravado with subtext; terse exchanges and loaded pauses.
                    * Noir sensibility meets pop neon. Melancholic glamour and danger.
                    * Pacing: Snappy during action or interrogation; laconic and moody between beats.
                
                3. Narrative Voice:
                    * Visual metaphors that evoke neon nights, ocean breeze, and rumbling engines.
                    * Emphasize rim lighting, silhouettes against pink-yellow dusk, and reflective wet streets.
                    * Keep descriptions cinematic but slightly imperfect and gritty.
                """
            Genre.SPACE_OPERA ->
                """
                Vocabulary: Galactic exploration, profound discoveries, cosmic phenomena,
                ancient alien civilizations, advanced technology, philosophical ponderings about existence.
                Formality: Varies from adventurous and eloquent (explorers, scientists) to mysterious and ancient (alien entities).
                Phrasing: Evocative and grand, with a sense of wonder and epic scope.
                Tone: Aspirational, mysterious, awe-inspiring, adventurous, contemplative.
                """
        }.trimIndent()
}
