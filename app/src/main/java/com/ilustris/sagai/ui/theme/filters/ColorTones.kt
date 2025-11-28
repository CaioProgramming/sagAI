package com.ilustris.sagai.ui.theme.filters

object FantasyColorTones {
    val ETHEREAL_LAVENDER_MOONLIGHT =
        ColorTonePalette(
            name = "Ethereal Lavender Moonlight",
            highlightTint = Triple(0.92f, 0.88f, 1.0f), // Pale Cool Lavender/Lilac
            shadowTint = Triple(0.75f, 0.8f, 0.95f), // Deep Cool Blue/Indigo
            defaultTintStrength = 0.28f,
        )

    val ETHEREAL_CYAN_STARLIGHT =
        ColorTonePalette(
            name = "Ethereal Cyan Starlight",
            highlightTint = Triple(0.85f, 0.95f, 1.0f), // Soft Cyan/Aqua Blue
            shadowTint = Triple(0.7f, 0.75f, 0.9f), // Desaturated Deep Blue
            defaultTintStrength = 0.3f,
        )

    val ETHEREAL_WHITE_GOLD_DIVINITY =
        ColorTonePalette(
            name = "Ethereal White Gold Divinity",
            highlightTint = Triple(1.0f, 0.97f, 0.9f), // Bright, Pale "White Gold"
            shadowTint = Triple(0.85f, 0.9f, 1.0f), // Cool Desaturated Blue (classic contrast)
            defaultTintStrength = 0.25f,
        )

    val CLASSIC_WARM_SUNLIT_FANTASY =
        ColorTonePalette(
            name = "Classic Warm Sunlit Fantasy",
            highlightTint = Triple(1.0f, 0.92f, 0.8f), // Warm (Slightly orangey-yellow)
            shadowTint = Triple(0.85f, 0.9f, 1.0f), // Cool (Slightly desaturated blue/cyan)
            defaultTintStrength = 0.25f,
        )

    val MYSTICAL_FOREST_TWILIGHT =
        ColorTonePalette(
            name = "Mystical Forest Twilight",
            highlightTint = Triple(0.9f, 0.95f, 0.85f), // Pale, slightly mossy green-gold
            shadowTint = Triple(0.8f, 0.82f, 0.9f), // Muted, earthy violet/deep blue
            defaultTintStrength = 0.3f,
        )

    val DREAM_LIKE_HAZE =
        ColorTonePalette(
            name = "Dream-Like Haze",
            // Highlights are so blown out they are almost white, but let's give a hint of warmth
            highlightTint = Triple(1.0f, 0.98f, 0.92f), // Very pale warm white/gold
            // Shadows might be slightly lifted and take on ambient light color
            shadowTint = Triple(0.88f, 0.92f, 0.85f), // Muted, slightly warm green/cyan (from ambient light)
            defaultTintStrength = 0.35f, // Needs to be fairly strong to color the haze
        )

    // Add more palettes as you experiment and define them!
    // For example:
    // val GRITTY_DARK_FANTASY = ColorTonePalette(...)
    // val ENCHANTED_EMBER_GLOW = ColorTonePalette(...)

    val allTones =
        listOf(
            ETHEREAL_LAVENDER_MOONLIGHT,
            ETHEREAL_CYAN_STARLIGHT,
            ETHEREAL_WHITE_GOLD_DIVINITY,
            CLASSIC_WARM_SUNLIT_FANTASY,
            MYSTICAL_FOREST_TWILIGHT,
            DREAM_LIKE_HAZE,
            // Add other predefined palettes here
        )
}

object SciFiColorTones { // Or add to your existing tones object

    val CYBERPUNK_NEON_NIGHT =
        ColorTonePalette(
            name = "Cyberpunk Neon Night",
            highlightTint = Triple(0.85f, 0.9f, 1.0f), // Pale, slightly cyan/cool white
            shadowTint = Triple(0.2f, 0.25f, 0.4f), // Deep, desaturated teal-blue or indigo
            defaultTintStrength = 0.4f,
        )

    // Optional: A variation if you want more prominent colored highlights by default
    val CYBERPUNK_MAGENTA_GLOW =
        ColorTonePalette(
            name = "Cyberpunk Magenta Glow",
            highlightTint = Triple(1.0f, 0.75f, 0.9f), // Magenta/Pinkish for key lights
            shadowTint = Triple(0.1f, 0.1f, 0.2f), // Very dark, almost black desaturated blue
            defaultTintStrength = 0.35f,
        )

    // ... any other variations ...
}

object HorrorColorTones {
    val MOONLIGHT_MYSTIQUE =
        ColorTonePalette(
            name = "Moonlight Mystique",
            highlightTint = Triple(0.85f, 0.92f, 1.0f), // Pale, cool, slightly cyan/blueish white
            shadowTint = Triple(0.05f, 0.08f, 0.15f), // Near-black, desaturated indigo/deep blue
            defaultTintStrength = 0.4f,
        )

    val allTones =
        listOf(
            MOONLIGHT_MYSTIQUE,
        )
}

object HeroColorTones {
    val URBAN_COMIC_VIBRANCY =
        ColorTonePalette(
            name = "Urban Comic Vibrancy",
            highlightTint = Triple(0.5f, 0.6f, .7f), // Bright, very subtly cool white (hint of cyan)
            shadowTint = Triple(0.05f, 0.1f, 0.15f), // Very deep, almost black cool blue/indigo
            defaultTintStrength = 0.4f, // Moderate strength for punchy colors
        )

    // You can add more hero-themed palettes here in the future
    val allTones =
        listOf(
            URBAN_COMIC_VIBRANCY,
        )
}

object CrimeColorTones {
    val MIAMI_NEON_SUNSET =
        ColorTonePalette(
            name = "Miami Neon Sunset",
            highlightTint = Triple(1.0f, 0.55f, 0.85f), // neon pink/magenta
            shadowTint = Triple(0.1f, 0.05f, 0.15f), // Deep, dark purple to contrast the pink
            defaultTintStrength = 0.35f,
        )

    val allTones = listOf(MIAMI_NEON_SUNSET)
}

object SpaceOperaTones {
    val COSMIC_TWILIGHT =
        ColorTonePalette(
            name = "Cosmic Twilight",
            highlightTint = Triple(0.9f, 0.85f, 1.0f),
            shadowTint = Triple(0.1f, 0.12f, 0.2f), // Deeper, near-black blue
            defaultTintStrength = 0.3f,
        )

    val allTones = listOf(COSMIC_TWILIGHT)
}

object ShinobiColorTones {
    val BLOOD_MOON_ASSASSIN =
        ColorTonePalette(
            name = "Blood Moon Assassin",
            highlightTint = Triple(0.95f, 0.92f, 0.9f),
            shadowTint = Triple(0.1f, 0.0f, 0.05f), // Very dark, near-black with a hint of red
            defaultTintStrength = 0.35f,
        )

    val allTones =
        listOf(
            BLOOD_MOON_ASSASSIN,
        )
}
