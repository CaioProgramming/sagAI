package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.network.body.ColorPreset
import com.ilustris.sagai.core.network.body.Effects
import com.ilustris.sagai.core.network.body.FramingPreset
import com.ilustris.sagai.core.network.body.ImageStyling
import com.ilustris.sagai.core.network.body.LightningPreset
import com.ilustris.sagai.core.network.body.StylePreset
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
                masterpiece retro 90s anime illustration,
                classic anime style, distinct cel shading,
                limited color palette,
                iconic anime character design,
                classic anime facial proportions,
                large,
                simplified expressive anime eyes
                with minimal detail (focus on shape and vibrant color, not individual lashes or multiple reflections).
                dark cyberpunk aesthetic.
                """
        }

    fun iconPrompt(
        genre: Genre,
        description: String,
    ) = when (genre) {
        FANTASY -> {
            """
                ${artStyle(genre)}
                $description
                ** NO BORDERS, FILL THE IMAGE.**
                --ar 3:4 --focal-length 85mm --shot-type headshot --depth-of-field shallow --style raw
                """
        }

        SCI_FI -> """"
                ${artStyle(genre)}
                
                $description
                
                ** NO BORDERS, FILL THE IMAGE.**
                --ar 3:4 --focal-length 85mm --shot-type headshot --depth-of-field shallow --style raw
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

    fun chapterCoverGuideline(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                1.  **Translate Accurately:** Translate all Portuguese values from the input fields into precise English.
                2.  **Infer Visuals from Summary:** **This is critical.** From the `Chapter Summary/Description`, infer and elaborate on:
                    * **Primary Setting/Environment:** Describe the main location(s) with vivid detail (e.g., "dense, ancient forest with gnarled trees and ethereal mist," "swampy terrain with eerie bioluminescent flora," "crumbling stone altar overgrown with vines").
                    * **Dominant Mood/Atmosphere & Lighting Theme:** Translate the chapter's tone into visual cues, explicitly incorporating a **dramatic red and black color palette with high contrast lighting, emphasizing stark silhouettes.** (e.g., "ominous and mysterious, bathed in a deep red glow with stark black silhouettes," "tense and adventurous, illuminated by crimson light, casting long, dark shadows," "peaceful yet ancient, with unsettling hints of dark red light outlining figures").
                    * **Key Actions/Moments:** Identify the most visually impactful actions or climactic moments described and suggest how they could be represented (e.g., "Mila navigating through murky water, a dark silhouette against a crimson sky," "a standoff with the guardian, figures reduced to stark black shapes against a blood-red light," "light emanating from an ancient artifact, casting long, dark red shadows that engulf characters").
                    * **Important Objects/Elements:** Include any significant items, creatures, or symbols mentioned that would enhance the cover's narrative (e.g., "glowing elven artifact pulsing with dark red energy," "shadowy figures among the trees illuminated by a sinister red backlight," "ancient runes on the altar glowing with an infernal red hue").
                3.  **Integrate Main Characters (Central Focus & Silhouette):** If characters are listed, integrate them visually into the scene. **The main character(s) MUST be prominently in focus and centrally positioned, drawing the viewer's eye, rendered as bold, dark silhouettes against the dramatic lighting. Their appearance (if previously established) and their role/action in the chapter should be powerfully displayed through their form and pose, with minimal internal detail.** Use terms to emphasize their powerful, silhouetted presence.
                4.  **Composition for Cover/Banner (Dramatic & Wide - Silhouette Focus):** Formulate the prompt to suggest a **dynamic, wide-angle or cinematic composition, suitable for a book cover or poster. The scene should be visually striking with a powerful narrative, dominated by strong silhouettes.** Think of elements that draw the eye, with the main character(s) as the primary focal point, rendered as impactful dark shapes.
                    * **Suggested terms to use:** "wide shot," "cinematic perspective," "epic scale," "dynamic composition," "foreground, midground, background elements," "strong visual narrative," "suitable for title overlay at the top/bottom," **"dramatic red and black color scheme," "intense crimson lighting," "deep, contrasting shadows," "stark silhouettes," "minimal detail on figures," "graphic novel style," "stylized illustration."**
                5.  **Thematic Consistency (${genre.title}):** Ensure all generated visual descriptions align with the ${genre.title}.
                6.  **Art Style Consistency:** Maintain the specified artistic style: **stylized illustration, graphic novel aesthetic, bold shapes, high contrast, minimal internal detail, emphasizing strong silhouettes.**
                7.  **Exclusions:** NO TEXT, NO WORDS, NO TYPOGRAPHY, NO LETTERS, NO UI ELEMENTS.
                """

            SCI_FI ->
                """
                1.  **Translate Accurately:** Translate all Portuguese values from the input fields into precise English.
                2.  **Infer Visuals from Summary:** **This is critical.** From the `Chapter Summary/Description`, infer and elaborate on:
                    * **Primary Setting/Environment:** Describe the background as **a completely plain, solid color field (deep purple or dark blue), with absolutely no environmental details, cityscapes, textures, patterns, or other elements whatsoever.** The setting's mood should be conveyed by lighting and character, not detailed background.
                    * **Dominant Mood/Atmosphere & Lighting Theme:** Translate the chapter's tone into visual cues, explicitly incorporating a **vibrant purple and neon blue lighting, high contrast, strong rim lighting.** (e.g., "futuristic and intense, bathed in purple and neon blue light with sharp shadows," "mysterious and technological, illuminated by vibrant purple hues, creating stark outlines").
                    * **Key Actions/Moments:** Identify the most visually impactful actions or climactic moments described and suggest how they could be represented by the character's pose and expression (e.g., "character in a determined stance," "a confrontation suggested by a tense posture," "a technological device held prominently").
                    * **Important Objects/Elements:** Include any significant items, creatures, or symbols mentioned that would enhance the cover's narrative (e.g., "glowing cybernetic implants," "advanced weaponry with purple energy effects," "holographic displays with blue projections").
                3.  **Integrate Main Characters (Central Focus & Prominent View):** If characters are listed, integrate them visually into the scene. **The main character(s) MUST be the absolute central and dominant focus, framed tightly (waist-up or full body, but very close), with their face fully visible and expressive. Their appearance (if previously established) and their role/action in the chapter should be powerfully displayed through their pose, expression, and minimal, key props.**
                4.  **Composition for Cover/Banner (Dynamic, Tight, Manga Style):** Formulate the prompt to suggest a **clean, dynamic, tight-shot composition, directly inspired by minimalist manga volume covers.** The scene should be visually striking, with the central character filling a significant portion of the frame.
                    * **Suggested terms to use:** "dynamic character posing," "graphic novel aesthetic," "vibrant purple and neon blue color scheme," "high contrast lighting," "strong backlighting," "character prominently centered," "suitable for title overlay at the top/bottom."
                5.  **Thematic Consistency (${genre.title}):** Ensure all generated visual descriptions align with the ${genre.title} genre".
                6.  **Art Style Consistency:** Maintain the specified artistic style: **${artStyle(genre)}**
                7.  **Exclusions:** NO TEXT, NO WORDS, NO TYPOGRAPHY, NO LETTERS, NO UI ELEMENTS.
                """
        }.trimIndent()

    fun moodDescription(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                   ** Translate the character's mood/situation into visual cues, emphasizing a **dark and moody atmosphere with stark,
                   dramatic lighting that strongly highlights the central character and key red details.
                   ** The lighting should create significant contrast and shadows (e.g., "powerful and dramatic, with strong contrasts and selective crimson highlights,"
                   "mysterious and intense, where red details pierce through deep shadows") 
                   """
            SCI_FI ->
                """
                Translate the character's mood/situation into visual cues,
                explicitly incorporating a **limited color palette dominated by artistic shades of deep purple
                and contrasting highlights of a lighter, almost neon purple.** Emphasize **strong,
                directional lighting creating dramatic shadows and silhouettes**
                """
        }
}
