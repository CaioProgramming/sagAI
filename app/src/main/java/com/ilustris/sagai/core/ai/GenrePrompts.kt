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
                    Pose: Close-up portrait, a slightly angled 3/4 view, with her looking intensely towards the upper left. The focus is tightly on her face and the details of her cybernetics.
                    Lighting: Dramatic lighting with a strong neon cyan glow originating from the upper left, casting bright highlights on her forehead, cheekbone, and cybernetic implants, with deep shadows on the right side of her face and neck. Subtle hints of a deep blue ambient light.
                    Art Style: Anime/manga-inspired style with clean, defined lines and meticulous detail on the cybernetics. Increased contrast and reduced overall saturation, with a color palette dominated by cool blues, grays, and accented by the bright cyan light.
                    Background: A blurred and out-of-focus cyberpunk cityscape at night, with muted blues and purples suggesting distant neon lights and towering structures."
                    """
        }

    fun detail(genre: Genre) = "theme: ${genre.name}"
}
