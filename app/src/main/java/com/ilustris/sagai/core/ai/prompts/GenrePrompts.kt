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
        }

    fun getColorEmphasisDescription(genre: Genre): String =
        when (genre) {
            Genre.FANTASY ->
                """
                The **background should be a strong, evocative red color scheme** (e.g., deep crimson, fiery orange-red), possibly with subtle textures fitting the fantasy theme.
                On the character, apply **very specific and limited rich red accents ONLY to small details**: examples include the glint in an eye, a single gemstone on a piece of jewelry, fine embroidery on clothing.
                **CRUCIAL:** The character's overall skin tone, hair color (apart from tiny accents), and primary clothing colors MUST retain their natural, distinct hues and NOT be tinted red.
                The red accents should be isolated and clearly defined.
                """
            Genre.SCI_FI ->
                """
                The **background is a bold, vibrant neon purple**, possibly solid or with minimalist graphic elements, creating an unmistakably cyberpunk atmosphere.
                On the character, **apply discrete neon purple accents to small, specific features ONLY**: examples include glowing cybernetic eye details (just the iris or a small implant), thin luminous circuit patterns on clothing (not coloring the whole garment), subtle streaks in hair, or highlights on individual small pieces of tech gear.
                **CRUCIAL:** The character's overall skin tone, hair color (apart from tiny accents), and primary clothing colors MUST remain their natural, distinct hues and NOT be tinted purple. These purple accents should be isolated, clearly defined, and not affect the character's general coloration.
                """
            Genre.HORROR ->
                """
                The **background should be a dark, desaturated blue-gray or near-black**, establishing a grim, oppressive, and moody setting.
                Character accents, if any, must be **extremely minimal, desaturated, and applied to very small details**: for instance, a faint, chilling blue reflection in the eyes, or a tiny, barely perceptible grey pattern on dark clothing.
                **CRUCIAL:** The character should appear largely devoid of vibrant color, blending with the bleak, desaturated environment. Avoid any noticeable color accents that would break the monochromatic feel. The character's skin should appear pale or shadowed, not tinted by any accent color.
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
        }.trimIndent()

    fun chapterCoverGuideline(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                1.  Translate Accurately: Translate all Portuguese values from the input fields into precise English.
                2.  Infer Visuals from Summary: This is critical. From the `Chapter Summary/Description`, infer and elaborate on:
                    * Primary Setting/Environment: Describe the main location(s) with vivid detail (e.g., "dense, ancient forest with gnarled trees and ethereal mist," "swampy terrain with eerie bioluminescent flora," "crumbling stone altar overgrown with vines").
                    * Dominant Mood/Atmosphere & Lighting Theme: Translate the chapter's tone into visual cues, explicitly incorporating a dramatic red and black color palette with high contrast lighting, emphasizing stark silhouettes. (e.g., "ominous and mysterious, bathed in a deep red glow with stark black silhouettes," "tense and adventurous, illuminated by crimson light, casting long, dark shadows," "peaceful yet ancient, with unsettling hints of dark red light outlining figures").
                    * Key Actions/Moments: Identify the most visually impactful actions or climactic moments described and suggest how they could be represented (e.g., "Mila navigating through murky water, a dark silhouette against a crimson sky," "a standoff with the guardian, figures reduced to stark black shapes against a blood-red light," "light emanating from an ancient artifact, casting long, dark red shadows that engulf characters").
                    * Important Objects/Elements: Include any significant items, creatures, or symbols mentioned that would enhance the cover's narrative (e.g., "glowing elven artifact pulsing with dark red energy," "shadowy figures among the trees illuminated by a sinister red backlight," "ancient runes on the altar glowing with an infernal red hue").
                3.  Integrate Main Characters (Central Focus & Silhouette): If characters are listed, integrate them visually into the scene. The main character(s) MUST be prominently in focus and centrally positioned, drawing the viewer's eye, rendered as bold, dark silhouettes against the dramatic lighting. Their appearance (if previously established) and their role/action in the chapter should be powerfully displayed through their form and pose, with minimal internal detail. Use terms to emphasize their powerful, silhouetted presence.
                4.  Composition for Cover/Banner (Dramatic & Wide - Silhouette Focus): Formulate the prompt to suggest a dynamic, wide-angle or cinematic composition, suitable for a book cover or poster. The scene should be visually striking with a powerful narrative, dominated by strong silhouettes. Think of elements that draw the eye, with the main character(s) as the primary focal point, rendered as impactful dark shapes.
                    * Suggested terms to use: "wide shot," "cinematic perspective," "epic scale," "dynamic composition," "foreground, midground, background elements," "strong visual narrative," "dramatic red and black color scheme," "intense crimson lighting," "deep, contrasting shadows," "stark silhouettes," "minimal detail on figures," "graphic novel style," "stylized illustration."
                """

            SCI_FI ->
                """
                    
                **Guidelines for Conversion and Expansion:**
                
                1. Infer Visuals from Summary:
                   From the Chapter Summary/Description, infer and elaborate on:
                   - Dominant Mood/Atmosphere & Lighting Theme: ${moodDescription(genre)}.
                   - Key Actions/Moments: Identify the most visually impactful actions or climactic moments described and suggest how they could be represented by the character's pose and expression.
                   - Important Objects/Elements: Include any significant items, cybernetic implants, or symbols, ensuring their visual description aligns with the retro/analog aesthetic. Avoid terms like "glowing," "shifting colors," or "levitating." Instead, focus on mechanical, wired, or older technological concepts.
                
                3. Integrate Main Characters:
                   If characters are listed, integrate them visually into the scene.
                   The main character(s) MUST be the absolute central and dominant focus,
                   framed tightly as a close up bust shot or head-and-shoulders portrait, with their face fully visible and expressive.
                
                4. Composition for Cover/Banner:
                   Formulate the prompt to suggest a clean, dynamic, tight-shot composition, similar to a manga volume cover.
                 - Suggested terms to use: dynamic character posing, bust shot, head and shoulders portrait,
                   character prominently centered.
                
                """

            HORROR ->
                """
                1.  **Infer Visuals from Summary:** This is critical. From the `Chapter Summary/Description`, infer and elaborate on:
                    * **Primary Setting/Environment:** Describe the main locations with details that evoke mystery and terror (e.g., "dense, ancient forest under cold moonlight with elongated shadows and sinister shapes," "isolated, decrepit cabin in the heart of a fog-shrouded swamp," "imposing gothic ruins against a pale night sky").
                    * **Dominant Mood/Atmosphere & Lighting Theme:** Translate the chapter's tone into visual cues, explicitly incorporating a color palette dominated by cold blues and dark grays, with soft, spectral illumination from the midnight moon. Emphasize deep shadows and the suggestion of hidden dangers (e.g., "oppressive and mysterious atmosphere, bathed in a bluish moonlight with shadows that conceal threats," "tense and full of foreboding, with moonlight barely penetrating the darkness, creating areas of unfathomable mystery").
                    * **Key Actions/Moments:** Identify the most visually impactful moments that suggest terror and mystery, focusing on the atmosphere and setting (e.g., "a solitary figure standing on the edge of a moonlit abyss," "shadows moving among the trees under the moonlight, suggesting an unseen presence").
                    * **Important Objects/Elements:** Include any significant items, creatures, or symbols that intensify the terror and mysticism of the setting (e.g., "the faint glow of the moon reflecting off an ancient, cursed object," "silhouettes of crows perched on dry branches under the moonlight," "bluish fog crawling on the forest floor, concealing what may be lurking").
                3.  **Integrate Main Characters (Subtle Presence & Integrated into the Setting):** If characters are listed, integrate them visually into the scene, but with a smaller focus. 
                The character MAY be present, but should be an integral part of the mystical and terrifying setting, often on a smaller scale or as a solitary silhouette. The primary focus must remain on the atmosphere and the terror suggested by the environment and moonlight.
                4.  **Composition for Cover/Banner (Wide & Atmospheric - Terror at Bay):** Formulate the prompt to suggest a wide and atmospheric composition, ideal for a book cover. The scene should prioritize creating a sense of terror and mystery through the setting and midnight moonlight. The character, if present, should be on a scale that emphasizes their vulnerability or loneliness within this oppressive environment.
                    * **Suggested terms to use:** "wide shot," "cinematic perspective," "eerie atmosphere," "mysterious setting," "foreboding mood," "strong sense of place," "terror at bay," "moonlit scene," "pale blue moonlight," "deep, cold shadows," "subtle character presence," "lonely figure," "vulnerable silhouette," "gothic horror aesthetic," "Lovecraftian atmosphere," "stylized illustration."
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
        }.trimIndent()
}
