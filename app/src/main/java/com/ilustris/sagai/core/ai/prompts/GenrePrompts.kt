package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.network.body.ColorPreset
import com.ilustris.sagai.core.network.body.Effects
import com.ilustris.sagai.core.network.body.FramingPreset
import com.ilustris.sagai.core.network.body.ImageStyling
import com.ilustris.sagai.core.network.body.LightningPreset
import com.ilustris.sagai.core.network.body.StylePreset
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.PortraitPose
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.FANTASY
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
                Harmonious colors, authentic painterly grain.
                Full image, no borders.
                """

            SCI_FI ->
                """
                masterpiece retro 80s 90s anime illustration,
                classic anime style, distinct cel shading,
                limited color palette,
                vintage grainy texture,
                iconic anime character design,
                classic anime facial proportions,
                large,
                simplified expressive anime eyes
                with minimal detail (focus on shape and vibrant color, not individual lashes or multiple reflections).
                dystopian aesthetic, gritty and dark atmosphere,
                """
        }

    fun iconPrompt(
        genre: Genre,
        mainCharacter: Character,
        description: String,
    ) = when (genre) {
        FANTASY -> {
            """
                ${artStyle(genre)}
                ${CharacterFraming.PORTRAIT.description} with focus on face with prominent fantasy elements.
                ${PortraitPose.random().description}
                **Dramatic lighting, intense red light creating strong highlights and casting deep, pronounced shadows, focusing attention on the character. The background is dimly lit with subtle, dark tones, providing a sense of depth and separation, allowing the character to stand out vividly. The lighting emphasizes the character's form and creates a moody, intense, and atmospheric visual.**

                $description
                
                focus on the character's emotional intensity.
                ** NO BORDERS, FILL THE IMAGE.**
                --ar 3:2
                """
        }

        SCI_FI -> """"
                ${artStyle(genre)}
                ${CharacterFraming.PORTRAIT.description} with focus on face with prominent cybernetic implants.
                ${PortraitPose.random().description}
                **Dramatic lighting, intense neon purple creating strong highlights and casting deep, pronounced shadows, focusing attention on the character.
                The lighting emphasizes the character's form and creates a moody, intense, and atmospheric visual.**

                $description
                
                focus on the character's emotional intensity.
                ** NO BORDERS, FILL THE IMAGE.**
                --ar 3:2
                """
    }.trimIndent()

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
        }.trimIndent()

    fun negativePrompt(genre: Genre) =
        when (genre) {
            FANTASY -> StylePreset.entries.filter { it != StylePreset.FANTASY }.joinToString()
            SCI_FI ->
                StylePreset.entries
                    .filter { it != StylePreset.CYBERPUNK && it != StylePreset.ANIME }
                    .joinToString()
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

            FANTASY ->
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

            SCI_FI ->
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

    fun thematicVisuals(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                wearing period-appropriate attire (e.g., worn leather, chainmail, simple tunic, flowing robes),
                adorned with subtle fantasy elements (e.g., elven ear tips, runic tattoos, a magical glow in the eyes),
                """

            SCI_FI ->
                """
                prominent cybernetic implants,
                intricate details of wires, circuits,
                and metallic components integrated with skin and bone,
                subtle neon accents on implants, cybernetic details primarily on cheeks and below,
                minimal to no cybernetics in hair.
                """
        }.trimIndent()

    fun nameDirectives(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                // Aim for names that evoke a sense of magic, ancient lore, epic adventure, or mystical origins.
                // Consider influences from high fantasy (e.g., Tolkien-esque, D&D), classical mythology (Greek, Norse, Celtic), medieval European, or unique, melodious sounds.
                // Names can be majestic, archaic, rustic, tribal, or subtly magical.
                // AVOID overly modern, generic, or overtly tech-sounding names (e.g., John, Mary, Smith, unit numbers, cyber-names).
                """
            SCI_FI ->
                """
                // - Aim for names that blend futuristic, cyberpunk, or slightly exotic sounds.
                // - Consider influences from Japanese, tech-inspired, or gritty Western phonetics.
            """
        }.trimIndent()

    fun conversationDirective(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
            // This directive defines the specific linguistic style for the Fantasy genre.
            // NPCs and narrative voice should evoke a sense of ancient lore, heroism, magic, and medieval or mythical settings.
            
            1.  **Language & Vocabulary:**
                * **Terminology:** Incorporate terms related to magic, mythical creatures, ancient kingdoms, weaponry, and fantastical concepts (e.g., "enchantment," "arcane," "grimoire," "blade," "realm," "wyrm," "fey," "druid," "lord/lady").
                * **Formality:** Dialogue can range from formal and archaic (for nobles, mages, ancient beings) to more rustic (for common folk, villagers). Avoid overly modern slang.
                * **Archaic Phrasing:** Use subtly archaic phrasing or vocabulary where appropriate to enhance the fantasy feel (e.g., "hark," "perchance," "methinks," "hither," "thou/thee" - use sparingly to avoid being cumbersome, perhaps for specific character types).
                * **Profanity (Conditional):** Profanity should be rare and, if used, should reflect historical/fantasy-appropriate expletives rather than modern ones.
            
            2.  **Tone & Delivery:**
                * **Epic & Heroic:** The tone can often be grand, epic, or heroic, especially in moments of adventure or conflict.
                * **Mystical & Respectful:** Characters might speak with reverence towards magic, gods, or ancient powers.
                * **Wisdom & Lore:** Older or learned characters might speak in riddles, proverbs, or with deep knowledge of lore.
                * **Pacing:** Dialogue can be more measured, allowing for descriptions and dramatic pauses.
            
            3.  **Narrative Voice:**
                * Descriptions should be rich, evocative, focusing on landscapes, magical effects, detailed attire, and historical/mythical elements.
                * Maintain a sense of wonder, mystery, or impending doom as appropriate for the scene.
                """
            SCI_FI ->
                """
             // This directive defines the specific linguistic style for the Cyberpunk/Dystopian Sci-Fi genre.
            // NPCs and narrative voice should reflect a gritty, tech-infused, and often cynical tone.
            
            1.  **Language & Vocabulary:**
                * **Terminology:** Freely use tech jargon, hacking terms, corporate slang, and futuristic street argot (e.g., "net-runner," "chrome," "synth-skin," "data-jack," "augment," "glitch," "gig").
                * **Formality:** Conversations can range from casual to aggressively direct. Formal language is rare, often reserved for corporate figures or those trying to exert power.
                * **Slang & Idioms:** Incorporate contemporary or invented cyberpunk-specific slang and idioms.
                * **Profanity (Conditional):** If appropriate for the character's personality and the grim nature of the setting, moderate use of mild to strong profanity is acceptable to enhance realism and grit. Use it sparingly for impact, not gratuitously.
            
            2.  **Tone & Delivery:**
                * **Cynicism & Weariness:** Many characters should reflect a sense of disillusionment, world-weariness, or cynicism towards authority and the system.
                * **Directness:** Dialogues can be blunt, terse, and to the point.
                * **Suspicion:** Characters might often be guarded, suspicious, or secretive in their speech.
                * **Pacing:** Dialogue can be fast-paced, reflecting the urgency and high-stakes environment.
            
            3.  **Narrative Voice:**
                * Descriptions should be sharp, often highlighting the decay, neon glow, advanced tech, and disparity of the dystopian future.
                * Maintain an edgy, sometimes detached, perspective.
                """
        }.trimIndent()
}
