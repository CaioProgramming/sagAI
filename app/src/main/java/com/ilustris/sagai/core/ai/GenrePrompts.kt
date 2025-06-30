package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.network.body.ColorPreset
import com.ilustris.sagai.core.network.body.Effects
import com.ilustris.sagai.core.network.body.FramingPreset
import com.ilustris.sagai.core.network.body.ImageStyling
import com.ilustris.sagai.core.network.body.LightningPreset
import com.ilustris.sagai.core.network.body.StylePreset
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterExpression
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.FANTASY
import com.ilustris.sagai.features.newsaga.data.model.Genre.SCI_FI

object GenrePrompts {
    fun bannerStyle(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
            Pose: Standing in a dynamic pose conveying bravery or anger, ready for a fight. 
            This could involve a wider stance, a tighter grip on their weapon (if visible), a raised fist (if weapon isn't the focus), or a forward lean.
            Their gaze should reflect determination or fury, looking towards the right. 
            Subtle sparkles of fire or embers drift around the character, hinting at recent action or a magical element, and now also emphasizing their intense emotion.
            
            Art Style: Clean, vector-like illustration style with bold colors and sharp lines. Simple but effective shading to create depth. Anime/manga-inspired character design (without specific facial feature copying),
            with an emphasis on conveying strong emotion through facial expression and body language.
            Photography: Medium shot, side profile view (or a slightly angled three-quarter view to better showcase the dynamic pose and expression).
            Background: A red minimalist background, starting with a deeper red and fading to a slightly lighter red.
            Color Palette: The overall color palette should be dominated by red hues for the background.
            The warrior's attire and equipment can utilize contrasting colors (e.g., dark browns, blacks, greys, muted golds or silvers) to stand out against the red.
            Include intentional color accents on the warrior to add visual interest and potentially tie into the red theme subtly (e.g., a crimson detail, reddish-brown elements).
            The fire sparkles should also contribute to the red and orange tones, further highlighting the warrior's emotional state.
            """

            SCI_FI ->
                """
            Clothing: Practical and functional cyberwear, reminiscent of clothing seen in Ghost in the Shell; includes tactical elements and sleek lines, with a slightly subdued color palette.
            Art Style: Directly inspired by Ghost in the Shell (both the 1995 film and the Stand Alone Complex series); features clean and precise line art with a sense of realism in anatomy and proportions; detailed rendering of environments and technology; subtle and sophisticated shading to create depth and atmosphere (not sharp cell shading); a slightly muted and often cooler color palette, focusing on blues, grays, and subtle contrasting accents.
            Quality: Best quality, masterpiece, highly detailed, cinematic lighting, smooth animation frames.
            Pose: Confident and awareness pose.
            Palette: Blue and purple colors, with a cold look like, reminiscent of a gothic vibe.
            Background: Detailed and atmospheric cyberpunk environment (e.g., rain-slicked city street, technologically dense interior, or cityscape with subtle holographic elements), feeling tangible and lived-in.
            Action: Holding or subtly interacting with technology relevant to cyberpunk theme.           
            """
        }.trimIndent()

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
                Medieval Themed minimalist background.
                no borders.
                """

            SCI_FI ->
                """
                masterpiece retro anime illustration, 80s 90s anime style,
                sharp ink outlines, cel shading, slightly desaturated colors, 
                im and moody lighting, stylized character features,
                Melancholic city background.
                 """
        }

    fun iconPrompt(
        genre: Genre,
        mainCharacter: Character,
    ) = when (genre) {
        FANTASY -> {
            val symbol = symbolOptions.random()
            val backgroundColor = backgroundColorOptions.random()
            val texture = textures.random()
            """
                ${artStyle(genre)}
                single central $symbol element with a $texture texturization.
                positioned perfectly in the absolute center,
                textured solid $backgroundColor background,
                in a color combo with gold, evoking fantasy vibes.
                ample clear space around the central symbol,
                wallpaper style,
                focus on color and texture.
                """
        }

        SCI_FI -> """"
                Cyberpunk Icon
                ${artStyle(genre)}
                close-up of ${mainCharacter.details.gender}, face with prominent cybernetic implants and exposed mechanical parts,
                ${mainCharacter.details.appearance}, a ${CharacterExpression.entries.random()},
                dramatic pose, conveying emotion through body language (e.g., looking over the shoulder with a defiant gaze, head tilted upwards with a melancholic expression, intense side profile glance),
                fusion of organic and synthetic elements,
                dystopian aesthetic, gritty and dark atmosphere,
                intricate details of wires, circuits, and metallic components integrated with skin and bone,
                vibrant colored eyes (deep purple) that stand out in the painting, subtle neon accents on implants if desired,
                minimalist dark and cold background (deep blues) with subtle, faint undulating mesh lines or abstract circuit patterns, no gradients,
                focus on the contrast between humanity and technology,
                not black and white (allow for color, even if muted),
                icon style
                """
    }.trimIndent()

    fun coverComposition(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                Simple background in celestial tones, with subtle, stylized 
                ancient ruins.
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
            With a clear sky background in any daytime(eg: moonlight, golden hour, dawn, sunset, pink sky).
            """

            SCI_FI ->
                """
            A solid, contrasting background with subtle, futuristic vector lines,
            either as abstract patterns.
            """
        }

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

    fun sagaWallpaperStyling(genre: Genre): ImageStyling =
        when (genre) {
            FANTASY ->
                ImageStyling(
                    style = StylePreset.FANTASY.key,
                    effects =
                        Effects(
                            color = ColorPreset.GOLD_GLOW,
                            lightning = LightningPreset.STUDIO,
                            framing = FramingPreset.CINEMATIC,
                        ),
                )

            SCI_FI ->
                ImageStyling(
                    style = StylePreset.CYBERPUNK.key,
                    effects =
                        Effects(
                            color = ColorPreset.COLD_NEON,
                            lightning = LightningPreset.VOLUMETRIC,
                            framing = FramingPreset.CINEMATIC,
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
                prominent cybernetic implants and exposed mechanical parts,
                intricate details of wires, circuits,
                and metallic components integrated with skin and bone,
                subtle neon accents on implants, cybernetic details primarily on cheeks and below,
                minimal to no cybernetics in hair,
                """
        }

    private val symbolOptions =
        listOf(
            "wolf",
            "dragon",
            "moon",
            "sun",
            "sword",
            "crown",
            "chalice",
            "orb",
            "phoenix",
            "griffin",
            "unicorn",
            "castle",
            "tree",
            "star",
        )

    private val backgroundColorOptions =
        listOf(
            "deep-purple",
            "emerald-green",
            "ruby-red",
            "sapphire-blue",
            "onyx-black",
            "ivory-white",
            "burgundy",
            "forest-green",
        )

    private val textures =
        listOf(
            "glass",
            "nature",
            "fire",
            "cosmic",
            "Wooden",
            "Iron",
            "Diamond",
            "Dreaming",
        )
}
