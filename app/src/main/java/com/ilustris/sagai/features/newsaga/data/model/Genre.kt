package com.ilustris.sagai.features.newsaga.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.filters.SelectiveColorParams

enum class
Genre(
    @StringRes
    val title: Int,
    val color: Color,
    val iconColor: Color,
    @DrawableRes
    val background: Int,
) {
    FANTASY(
        title = R.string.genre_fantasy,
        color = Color(0xFF8B2635), // Pantone 208 C - Deep Ruby Red
        iconColor = Color.White,
        background = R.drawable.fantasy,
    ),
    CYBERPUNK(
        title = R.string.genre_scifi,
        color = Color(0xFF2E294E), // Pantone 5255 C - Dark Purple
        iconColor = Color.White,
        background = R.drawable.scifi,
    ),

    HORROR(
        title = R.string.genre_horror,
        color = Color(0xFF1C2541), // Pantone 533 C - Dark Navy
        iconColor = Color.White,
        background = R.drawable.horror,
    ),

    HEROES(
        title = R.string.genre_heroes,
        color = Color(0xFF003F88), // Pantone 286 C - Classic Hero Blue
        iconColor = Color.White,
        background = R.drawable.hero,
    ),
    CRIME(
        title = R.string.genre_crime,
        color = Color(0xFFE91E63), // Pantone 213 C - Hot Pink
        iconColor = Color.White,
        background = R.drawable.crime,
    ),

    SHINOBI(
        title = R.string.genre_shinobi,
        color = Color(0xFF5C2751), // Pantone 518 C - Deep Plum
        iconColor = Color.White,
        background = R.drawable.shinobi_background,
    ),

    SPACE_OPERA(
        title = R.string.genre_space_opera,
        color = Color(0xFF0081A7), // Pantone 3145 C - Space Teal
        iconColor = Color.White,
        background = R.drawable.space_opera,
    ),

    COWBOY(
        title = R.string.genre_cowboys,
        color = Color(0xFF8B4513), // Pantone 4695 C - Saddle Brown
        iconColor = Color.White,
        background = R.drawable.cowboys,
    ),

    PUNK_ROCK(
        title = R.string.genre_punk_rock,
        color = Color(0xFF00B050), // Pantone 375 C - Vibrant Green
        iconColor = Color.White,
        background = R.drawable.punk_rock,
    ),
    ;

    val ambientMusicConfigKey: String = "${this.name}_ambient_music_url".lowercase()
}

fun Genre.selectiveHighlight(): SelectiveColorParams =
    when (this) {
        Genre.FANTASY -> {
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .85f,
                saturationThreshold = .6f,
                lightnessThreshold = .25f,
                highlightSaturationBoost = 2f,
                desaturationFactorNonTarget = .5f,
            )
        }

        Genre.CYBERPUNK -> {
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .11f,
                saturationThreshold = .15f,
                lightnessThreshold = .47f,
                highlightSaturationBoost = 2f,
                desaturationFactorNonTarget = .5f,
            )
        }

        Genre.HORROR -> {
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .3f,
                saturationThreshold = .3f,
                highlightSaturationBoost = 1.3f,
                desaturationFactorNonTarget = .7f,
            )
        }

        Genre.HEROES -> {
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .3f,
                saturationThreshold = .45f,
                lightnessThreshold = .25f,
                highlightSaturationBoost = 1.4f,
                desaturationFactorNonTarget = .5f,
            )
        }

        Genre.CRIME -> {
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .05f,
                saturationThreshold = .13f,
                lightnessThreshold = .10f,
                highlightSaturationBoost = 1.4f,
                desaturationFactorNonTarget = .4f,
            )
        }

        Genre.SHINOBI -> {
            SelectiveColorParams(
                targetColor = color,
                // tight hue tolerance to preserve that specific wine red accent
                hueTolerance = .02f,
                saturationThreshold = .18f,
                lightnessThreshold = .12f,
                highlightSaturationBoost = 1.8f,
                desaturationFactorNonTarget = .45f,
            )
        }

        Genre.SPACE_OPERA -> {
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = 1f,
                saturationThreshold = .5f,
                lightnessThreshold = .5f,
                highlightSaturationBoost = 2f,
                desaturationFactorNonTarget = .4f,
            )
        }

        Genre.COWBOY -> {
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .1f,
                saturationThreshold = .2f,
                lightnessThreshold = .2f,
                highlightSaturationBoost = 1.5f,
                desaturationFactorNonTarget = .5f,
            )
        }

        Genre.PUNK_ROCK -> {
            SelectiveColorParams(
                targetColor = color,
                hueTolerance = .15f,
                saturationThreshold = .4f,
                lightnessThreshold = .3f,
                highlightSaturationBoost = 2.0f,
                desaturationFactorNonTarget = .5f,
            )
        }
    }

fun Genre.defaultHeaderImage() =
    when (this) {
        Genre.FANTASY -> R.drawable.fantasy_card
        Genre.CYBERPUNK -> R.drawable.scifi_card
        Genre.HORROR -> R.drawable.horror_card
        Genre.HEROES -> R.drawable.hero_card
        Genre.CRIME -> R.drawable.crime_card
        Genre.SHINOBI -> R.drawable.shinobi_card
        Genre.SPACE_OPERA -> R.drawable.space_opera_card
        Genre.COWBOY -> R.drawable.cowboys_card
        Genre.PUNK_ROCK -> R.drawable.punk_rock_card
    }

fun Genre.shimmerColors() =
    listOf(
        Color.Transparent,
        color.copy(alpha = .5f),
    ).plus(colorPalette())
        .plus(Color.Transparent)

fun Genre.colorPalette() =
    when (this) {
        Genre.FANTASY -> {
            listOf(
                color,
                Color(0xFFD4AF37), // Pantone 871 C - Mystical Gold
                Color(0xFF8B0000), // Pantone 18-1664 TPX - Dark Red
                Color(0xFFFF6B35), // Pantone 17-1462 TPX - Flame Orange
            )
        }

        Genre.CYBERPUNK -> {
            listOf(
                color,
                Color(0xFF00F5FF), // Pantone Neon Blue
                Color(0xFFFF1493), // Pantone Neon Pink
                Color(0xFF39FF14), // Pantone Neon Green
            )
        }

        Genre.HORROR -> {
            listOf(
                color,
                Color(0xFF2F2F2F), // Pantone Black 7 C
                Color(0xFF708090), // Pantone Cool Gray 9 C
                Color(0xFF4B0082), // Pantone 268 C - Deep Violet
            )
        }

        Genre.HEROES -> {
            listOf(
                color,
                Color(0xFFDC143C), // Pantone 18-1664 TPX - Hero Red
                Color(0xFFFFD700), // Pantone 116 C - Hero Gold
                Color(0xFF00BFFF), // Pantone Process Blue C
            )
        }

        Genre.CRIME -> {
            listOf(
                color,
                Color(0xFF00CED1), // Pantone 319 C - Miami Turquoise
                Color(0xFFFF69B4), // Pantone 812 C - Vice Pink
                Color(0xFFFFA500), // Pantone 144 C - Sunset Orange
            )
        }

        Genre.SHINOBI -> {
            listOf(
                color,
                Color(0xFF8B0000), // Pantone 18-1664 TPX - Blood Red
                Color(0xFF2F4F4F), // Pantone 5467 C - Shadow Gray
                Color(0xFFB8860B), // Pantone 7562 C - Ancient Gold
            )
        }

        Genre.SPACE_OPERA -> {
            listOf(
                color,
                Color(0xFF4169E1), // Pantone 286 C - Royal Blue
                Color(0xFF00FA9A), // Pantone 354 C - Cosmic Green
                Color(0xFFFF4500), // Pantone 17-1463 TPX - Rocket Orange
            )
        }

        Genre.COWBOY -> {
            listOf(
                color,
                Color(0xFFD2691E), // Pantone 4695 C - Desert Sand
                Color(0xFFCD853F), // Pantone 4665 C - Prairie Tan
                Color(0xFF800000), // Pantone 18-1142 TPX - Maroon
            )
        }

        Genre.PUNK_ROCK -> {
            listOf(
                color,
                Color(0xFFFFD700), // Pantone 106 C - Vibrant Yellow
                Color(0xFFFF6B35), // Pantone 16-1546 TPX - Hot Orange
                Color(0xFF8B0000), // Pantone 18-1664 TPX - Deep Red
            )
        }
    }
