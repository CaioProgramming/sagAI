package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.network.body.ColorPreset
import com.ilustris.sagai.core.network.body.Effects
import com.ilustris.sagai.core.network.body.FramingPreset
import com.ilustris.sagai.core.network.body.ImageStyling
import com.ilustris.sagai.core.network.body.LightningPreset
import com.ilustris.sagai.core.network.body.StylePreset
import com.ilustris.sagai.core.utils.currentLanguage
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.FANTASY
import com.ilustris.sagai.features.newsaga.data.model.Genre.HORROR
import com.ilustris.sagai.features.newsaga.data.model.Genre.SCI_FI

object GenrePrompts {
    fun detail(genre: Genre) = "theme: ${genre.name}"

    fun artStyle(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                Classical oil painting,
                rich impasto texture, visible brushstrokes,
                strong chiaroscuro.
                natural lighting.
                ethereal aesthetic.
                Harmonious colors, authentic painterly grain.
                """

            SCI_FI ->
                """
                **True 80s-90s Retro Anime Cel Art Style.**
                Think classic theatrical anime features and OVAs: bold, clean inked outlines with varied line weight.
                **Distinct Cel Shading:** Hard-edged shadows, flat color fills, and a limited but impactful color palette. Avoid overly smooth gradients or photorealistic textures.
                Subtle film grain or animation cel artifacting is desirable.
                Inspired by the visual aesthetics of works like Akira, Ghost in the Shell (1995), and Bubblegum Crisis.
                The character rendering should strictly adhere to this classic cel animation look.
                Cyberpunk elements should be integrated within this specific retro anime framework.
                Backgrounds can be detailed yet stylized, often with a melancholic or industrial feel, fitting the era. **For character icons, however, the background inspired by these elements should be significantly simplified, perhaps to bold graphic shapes, a minimalist abstract texture, or a tastefully blurred representation, ensuring the character remains the dominant focus and the icon is uncluttered.**
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
        }

    fun getColorEmphasisDescription(genre: Genre): String =
        when (genre) {
            FANTASY ->
                """
                The background Dominated by a strong, evocative red color scheme, featuring deep scarlet, crimson, fiery orange-red, and burgundy tones.
                The background is composed of voluminous, dramatic cloud formations intensely lit with these red and orange hues, creating a powerful, ambient glow that permeates the entire scene.
                The lighting is an intense, almost apocalyptic, reddish-orange glow, suggesting either an epic, burning sunset or the aftermath of a fiery event, casting everything in a warm yet foreboding light with deep, rich shadows.
                **CRUCIAL:** The character's overall skin tone, hair color (apart from tiny accents), and primary clothing colors MUST retain their natural, distinct hues and NOT be tinted red.
                The red accents should be isolated and clearly defined.
                """

            SCI_FI ->
                """
                The overall scene should capture a moody, retro-anime cyberpunk aesthetic, with a strong emphasis on purple, drawing inspiration from visuals like a character in a neon-lit city at dusk.

                **Background & Ambient Lighting:**
                The background should evoke a cyberpunk city at dusk or night, featuring sources of **subtle ambient purple neon lighting** that cast a moody glow. This could manifest as reflections off wet surfaces, distant city lights, or atmospheric haze, creating an environment rich with purple hues without being uniformly purple. For character icons, this background should be significantly simplified to bold graphic shapes, a minimalist abstract texture, or a tastefully blurred representation, still retaining the purple atmospheric feel and ensuring the character remains the dominant focus.

                **Character Purple Highlights & Details:**
                While the character's primary clothing, skin, and hair colors must remain distinct and true to their base design (as per the CRUCIAL rule below), integrate **more pronounced and thoughtfully placed purple light details and highlights** in the following ways:
                *   **Reflective Surfaces:** Catch subtle but clear purple reflections on metallic parts of clothing (zippers, buckles), cybernetics, weapons, or accessories.
                *   **Edge Lighting/Rim Lighting:** Use sharp purple rim lighting to define parts of the character's silhouette against the darker background, suggesting they are being hit by distinct purple light sources from the environment.
                *   **Emissive Details:** Enhance or add elements like glowing purple cybernetic eye details (irises, small implants), thin luminous purple circuit patterns on clothing (not coloring the whole garment), subtle purple light strips on gear, or faint highlights on individual small pieces of tech. These elements should feel like they are emitting a soft purple light.
                *   **Subtle Cast Light:** Allow a *very subtle* and localized cast of purple light onto areas of the character immediately adjacent to strong purple light sources or their own emissive details. Avoid tinting large surfaces or the character's overall complexion.

                **CRUCIAL:** The character's overall skin tone, hair color (apart from specific, small glowing accents like streaks or cybernetic parts), and primary clothing colors MUST retain their natural, distinct hues and NOT be globally tinted purple. The purple elements should be targeted highlights, reflections, and light sources, enhancing the cyberpunk feel without altering the character's core color palette. The goal is a sophisticated interplay of light, not a purple wash.
                """

            HORROR ->
                """
                The **background should be a dark, desaturated blue-gray or near-black**, establishing a grim, oppressive, and moody setting.
                Character accents, if any, must be **extremely minimal, desaturated, and applied to very small details**: for instance, a faint, chilling blue reflection in the eyes, or a tiny, barely perceptible grey pattern on dark clothing.
                **CRUCIAL:** The character should appear largely devoid of vibrant color, blending with the bleak, desaturated environment. Avoid any noticeable color accents that would break the monochromatic feel. The character's skin should appear pale or shadowed, not tinted by any accent color.
                """
            Genre.HEROES ->
                """
                Urban environment. The color palette is dominated by vivid blues and teals, accented with black, white, and yellow.
                The background is a detailed cityscape at night, with glowing streetlights, silhouetted buildings, and a sense of urban grit,
                bathed in a cool blue light.
               
                """
        }.trimIndent()

    fun iconPrompt(
        genre: Genre,
        description: String,
    ) = """
        ${CharacterRules.IMAGE_CRITICAL_RULE}

        $description
        """.trimIndent()

    fun coverComposition(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                Simple background in celestial tones, with subtle, stylized 
                fantasy landmark.
                """

            SCI_FI ->
                """
            Minimalist background in cold tones with single Big Stylized kanji symbol behind the characters.
            """

            else -> emptyString()
        }

    fun portraitStyle(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
            Ethereal epic background with a divine mood.
            """

            SCI_FI ->
                """
                Subtle cold and melancholic city background.               
                """

            else -> emptyString()
        }.trimIndent()

    fun negativePrompt(genre: Genre) =
        when (genre) {
            FANTASY -> StylePreset.entries.filter { it != StylePreset.FANTASY }.joinToString()
            SCI_FI ->
                StylePreset.entries
                    .filter { it != StylePreset.CYBERPUNK && it != StylePreset.ANIME }
                    .joinToString()

            else -> emptyString()
        }.uppercase()

    fun characterStyling(genre: Genre): ImageStyling =
        when (genre) {
            SCI_FI ->
                ImageStyling(
                    style = StylePreset.ANIME.key,
                    effects =
                        Effects(
                            color = ColorPreset.COLD_NEON,
                            lightning = LightningPreset.DRAMATIC,
                            framing = FramingPreset.CLOSE_UP,
                        ),
                )

            else ->
                ImageStyling(
                    style = StylePreset.VINTAGE.key,
                    effects =
                        Effects(
                            color = ColorPreset.GOLD_GLOW,
                            lightning = LightningPreset.DRAMATIC,
                            framing = FramingPreset.PORTRAIT,
                        ),
                )
        }

    fun chapterCoverStyling(genre: Genre): ImageStyling =
        when (genre) {
            FANTASY ->
                ImageStyling(
                    style = StylePreset.FANTASY.key,
                    effects =
                        Effects(
                            color = ColorPreset.GOLD_GLOW,
                            lightning = LightningPreset.STUDIO,
                            framing = FramingPreset.entries.random(),
                        ),
                )

            else ->
                ImageStyling(
                    style = StylePreset.CYBERPUNK.key,
                    effects =
                        Effects(
                            color = ColorPreset.COLD_NEON,
                            lightning = LightningPreset.VOLUMETRIC,
                            framing = FramingPreset.entries.random(),
                        ),
                )
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

            SCI_FI ->
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

            SCI_FI ->
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
        }.trimIndent()

    fun moodDescription(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                  Translate the character's mood/situation into visual cues, emphasizing a dark and moody atmosphere with stark,
                  dramatic lighting that strongly highlights the central character and key red details.
                  The lighting should create significant contrast and shadows (e.g., "powerful and dramatic, with strong contrasts and selective crimson highlights,"
                  "mysterious and intense, where red details pierce through deep shadows") 
                   """

            SCI_FI ->
                """
                Translate the character's mood/situation into visual cues,
                explicitly incorporating a limited color palette dominated by artistic shades of neon purple
                and contrasting highlights of a lighter, almost neon purple. Emphasize strong,
                directional lighting creating dramatic shadows and silhouettes
                **Crucially, incorporate subtle cybernetic implants and enhancements as elements of fusion between human and machine.** These details should be visible on the **face, neck, eyes (e.g., glowing pupils or integrated displays), and lips (e.g., metallic sheen or subtle integrated tech)**, adding to the cyberpunk aesthetic without overwhelming the character's humanity, as seen in the provided example.
                """

            HORROR ->
                """
               Translate the story's theme/mystery into a visual scene, emphasizing a haunting and atmospheric mood.
               The main focus should be on the environment and the supernatural elements.
               explicitly incorporating a limited color palette dominated by artistic shades of blue gray.
               and contrasting highlights of a lighter, almost moonlight blue.
               Emphasize strong directional lighting creating dramatic shadows and silhouettes
               Evoke a sense of dread, forbidden knowledge, and impending doom
                (e.g., "haunting and mysterious, with ethereal light and deep shadows," "sinister and foreboding, with a hint of the occult").
               """
            Genre.HEROES ->
                """
                Translate the character's mood/situation into visual cues, emphasizing a dynamic and energetic atmosphere with strong,
                dramatic lighting that highlights the central character and key blue details.
                The lighting should create significant contrast and shadows (e.g., "powerful and dramatic, with strong contrasts and selective electric blue highlights," "dynamic and intense, where blue energy emanates from the character").
            
                **Crucially, incorporate subtle energy effects and tech details as elements of the character's abilities.
                ** These details should be visible around the hands (suggesting energy manipulation), on the suit (subtle glowing lines or panels), or as a faint aura surrounding the character, adding to the urban hero aesthetic without overwhelming the design.
            
                Emphasize a sense of movement, agility, and determination. 
                The character should appear poised for action, ready to take on any challenge.
                The background should be a blurred cityscape, suggesting speed and momentum.
            
                Focus on capturing a heroic pose and a confident expression, conveying a sense of hope and resilience in the face of adversity.
                """
        }.trimIndent()
}
