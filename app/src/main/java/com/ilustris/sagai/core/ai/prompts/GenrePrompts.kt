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
                Medieval Themed minimalist background.
                no borders.
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
    ) = when (genre) {
        FANTASY -> {
            """
                ${artStyle(genre)}
                ${CharacterFraming.PORTRAIT.description} with focus on face with prominent fantasy elements.
                ${PortraitPose.random().description}
                close-up of a ${mainCharacter.details.race} ${mainCharacter.details.gender}, ${mainCharacter.details.ethnicity}.
                ${mainCharacter.details.facialDetails}
                The character wears: ${mainCharacter.details.clothing}
                Lighting: ${LightningPreset.DRAMATIC.key}
                with red details on the clothing and weapons.
                background in blended tones of deep red,
                suggesting depth and atmosphere, no sharp gradients,
                focus on the character's emotional intensity,
                focus on the character's depth and mystique.
                """
        }

        SCI_FI -> """"
                ${artStyle(genre)}
                ${CharacterFraming.PORTRAIT.description} with focus on face with prominent cybernetic implants.
                ${PortraitPose.random().description}
                close-up of a ${mainCharacter.details.race} ${mainCharacter.details.gender}, ${mainCharacter.details.ethnicity}
                simple facial features,
                cynical and thoughtful expression,
                as if contemplating something while looking towards the horizon,
                natural skin tones,
                cyborg humanoid with a partially visible mechanical face,
                suggesting a subtle fusion of flesh and technology, maintaining a humanoid appearance,
                with intricate details and visible internal mechanisms only on the face,
                with subtle cybernetic lines integrated into the skin of her face,
                fusion of organic and synthetic elements integrated with skin and bone.
                dramatic pose, 3/4 angle, camera positioned slightly below eye level,
                with the character looking off into the distance (to the left of the viewer),
                conveying determination and focus,
                large, simple, minimalist anime eyes, **completely purple with a subtle glow**.
                solid deep purple background,
                with large, bold, prominent,
                white Japanese Kanji symbols with a subtle,
                focus on the character's depth and mystique.
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
            Dark Fantasy Landscape with ethereal aesthetic.
            """

            SCI_FI ->
                """
                Cold tones minimalist background with a melancholic city aesthetic.               
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
            "phoenix",
            "griffin",
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
