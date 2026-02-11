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
        color = Color(0xFF8B00FF), // Pantone 2665 C - Vibrant Electric Purple
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
    val configKey: String = "${this.name.lowercase()}_config"
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
                hueTolerance = .1f,
                saturationThreshold = .1f,
                lightnessThreshold = .3f,
                highlightSaturationBoost = 2f,
                desaturationFactorNonTarget = .0f,
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
        color.copy(alpha = .2f),
    ).plus(colorPalette())
        .plus(Color.Transparent)

fun Genre.colorPalette() =
    when (this) {
        Genre.FANTASY -> {
            listOf(
                color,
                Color(0xFFA52A2A), // Brown
                Color(0xFF800000), // Maroon
                Color(0xFFCD5C5C), // IndianRed
            )
        }

        Genre.CYBERPUNK -> {
            listOf(
                color,
                Color(0xFF9400D3), // Dark Violet
                Color(0xFFBA55D3), // Medium Orchid
                Color(0xFF4B0082), // Indigo
            )
        }

        Genre.HORROR -> {
            listOf(
                color,
                Color(0xFF0B132B), // Rich Black
                Color(0xFF3A506B), // Imperial Blue
                Color(0xFF1B263B), // Oxford Blue
            )
        }

        Genre.HEROES -> {
            listOf(
                color,
                Color(0xFF00509D), // Lighter Blue
                Color(0xFF00296B), // Darker Blue
                Color(0xFF4A90E2), // Cornflower Blue
            )
        }

        Genre.CRIME -> {
            listOf(
                color,
                Color(0xFFC2185B), // Darker Pink
                Color(0xFFF48FB1), // Lighter Pink
                Color(0xFF880E4F), // Very Dark Pink
            )
        }

        Genre.SHINOBI -> {
            listOf(
                color,
                Color(0xFF4A1F41), // Darker Plum
                Color(0xFF8E447E), // Lighter Plum
                Color(0xFF2D1328), // Very Dark Plum
            )
        }

        Genre.SPACE_OPERA -> {
            listOf(
                color,
                Color(0xFF00AFB9), // Lighter Teal
                Color(0xFF264653), // Dark Slate
                Color(0xFF48CAE4), // Cyan
            )
        }

        Genre.COWBOY -> {
            listOf(
                color,
                Color(0xFFA0522D), // Sienna
                Color(0xFF654321), // Dark Brown
                Color(0xFFD2691E), // Chocolate
            )
        }

        Genre.PUNK_ROCK -> {
            listOf(
                color,
                Color(0xFF008000), // Green
                Color(0xFF32CD32), // Lime Green
                Color(0xFF006400), // Dark Green
            )
        }
    }

fun Genre.vibrationPattern(): LongArray =
    when (this) {
        Genre.FANTASY -> longArrayOf(0, 100, 50, 100)
        Genre.CYBERPUNK -> longArrayOf(0, 50, 30, 50, 30, 50)
        Genre.HORROR -> longArrayOf(0, 500)
        Genre.HEROES -> longArrayOf(0, 300)
        Genre.CRIME -> longArrayOf(0, 80, 80, 80)
        Genre.SHINOBI -> longArrayOf(0, 150)
        Genre.SPACE_OPERA -> longArrayOf(0, 200, 100, 200)
        Genre.COWBOY -> longArrayOf(0, 250)
        Genre.PUNK_ROCK -> longArrayOf(0, 50, 50, 100, 50, 50)
    }
