package com.ilustris.sagai.core.ai

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

    fun iconStyle(genre: Genre): String =
        when (genre) {
            FANTASY ->
                """
                    Expression: Determined and resolute, with a strong gaze.
                    Pose: Upper body portrait, in 4/5 angle looking to the viewer.
                    Lighting: Dramatic lighting casting strong shadows and reflecting red light at face side.
                    Art Style: Clean, vector-like illustration with bold colors,
                    sharp lines, simple but effective shading, and anime/manga-inspired character design.
                    Background: Red contrasting with the character.
                    """
            SCI_FI ->
                """
                    Lighting: Dramatic lighting with a strong neon cyan glow originating from the upper left, casting bright highlights on her forehead, cheekbone, and cybernetic implants, with deep shadows on the right side of her face and neck. Subtle hints of a deep blue ambient light.
                    Art Style: Anime/manga-inspired style with clean, defined lines and meticulous detail on the cybernetics. Increased contrast and reduced overall saturation, with a color palette dominated by cool blues, grays, and accented by the bright cyan light.
                    Background: A blurred and out-of-focus cyberpunk cityscape at night, with muted blues and purples suggesting distant neon lights and towering structures."
                    """
        }

    fun detail(genre: Genre) = "theme: ${genre.name}"

    fun artStyle(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                Highly detailed,photorealistic painting,
                Renaissance-inspired reflections,
                evoking the styles of Eric Zener, Rachel Walpole,
                Chris Moore,Michael Hoppen,Stephen Hickman,and Mary Jane Ansell.
                Luminous natural lighting, with profound volumetric light and atmospheric depth,
                Grand color palette of deep, harmonious, and subtly desaturated tones, evoking a timeless grandeur,
                strong focal point on the character, immersed in a majestic and atmospheric background,
                evoking a sense of ancient lore, epic sagas, and enduring legend, with a soft, cinematic film grain.
                """
            SCI_FI ->
                """
                anime illustration, inspired by Ghost in the Shell 1995 film style,
                sharp and angular lines aesthetic,
                crisp and contrasting shading for dramatic depth,
                predominantly cool and desaturated colors,
                dark and immersive atmosphere,
                highly detailed cybernetic and futuristic elements,
                sleek character design,
                single, strong light source casting defined shadows,
                bright specular highlights,
                atmospheric lighting with subtle colored rim lighting,
                meticulous attention to technical details,
                evoking a sense of melancholic futurism, cyberpunk mystery and oppressive atmosphere.
                """
        }

    fun negativePrompt(genre: Genre) =
        when (genre) {
            FANTASY ->
                """
                masterpiece fantasy illustration, in the style of classical realism painting and vintage fantasy art,
                exquisite painterly technique with delicate brushstrokes and subtle impasto for texture,
                rich and nuanced textures reminiscent of fine art canvas and traditional pigments,
                soft yet dramatic natural lighting, with profound volumetric light and a gentle atmospheric haze,
                a refined color palette of deep, harmonious, and subtly desaturated natural tones, evoking a sense of grounded reality,
                strong focal point on the character, realistically rendered within a detailed and atmospheric background,
                evoking a sense of history, depth of character, and a believable world, with a subtle film grain.
                """
            SCI_FI ->
                """
                blurry, messy, ugly, low resolution, childish, cartoon, comic book, medieval,
                fantasy elements, organic textures, soft focus, painterly brushstrokes,
                hand-drawn sketch, watercolor, oil painting, excessive historical details,
                **overly saturated neon, flat digital rendering
                 generic digital illustration, hyper-stylized modern digital art,
                 vibrant primary colors as dominant, sterile white backgrounds.**
                """
        }.uppercase()
}
