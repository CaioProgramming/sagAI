package com.ilustris.sagai.features.newsaga.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.ui.theme.filters.SelectiveColorParams

enum class
Genre(
    @StringRes
    val title: Int,
    val color: Color,
    val iconColor: Color,
    @DrawableRes
    val icon: Int,
) {
    FANTASY(
        title = R.string.genre_fantasy,
        color = Color(0xFF8B2635),
        iconColor = Color.White,
        icon = R.drawable.fantasy,
    ),
    CYBERPUNK(
        title = R.string.genre_scifi,
        color = Color(0xFF8B00FF),
        iconColor = Color.White,
        icon = R.drawable.scifi,
    ),

    HORROR(
        title = R.string.genre_horror,
        color = Color(0xff515e8c),
        iconColor = Color.White,
        icon = R.drawable.horror,
    ),

    HEROES(
        title = R.string.genre_heroes,
        color = Color(0xff0057bd),
        iconColor = Color.White,
        icon = R.drawable.hero,
    ),
    CRIME(
        title = R.string.genre_crime,
        color = Color(0xFFE91E63),
        iconColor = Color.White,
        icon = R.drawable.crime,
    ),

    SHINOBI(
        title = R.string.genre_shinobi,
        color = Color(0xFF5C2751),
        iconColor = Color.White,
        icon = R.drawable.shinobi_background,
    ),

    SPACE_OPERA(
        title = R.string.genre_space_opera,
        color = Color(0xFF0081A7),
        iconColor = Color.White,
        icon = R.drawable.space_opera,
    ),

    COWBOY(
        title = R.string.genre_cowboys,
        color = Color(0xFF8B4513),
        iconColor = Color.White,
        icon = R.drawable.cowboys,
    ),

    PUNK_ROCK(
        title = R.string.genre_punk_rock,
        color = Color(0xFF00B050),
        iconColor = Color.White,
        icon = R.drawable.punk_rock,
    ),
    ;

    val ambientMusicConfigKey: String = "${this.name}_ambient_music_url".lowercase()
    val configKey: String = "${this.name.lowercase()}_config"
}

fun Genre.selectiveHighlight(visualConfig: GenreVisualConfig?): SelectiveColorParams? {
    if (visualConfig == null) return null
    val remote = visualConfig.selectiveHighlight ?: return null
    val targetColor = resolveColor(visualConfig) ?: return null
    if (remote.hueTolerance < 0f) return null
    return SelectiveColorParams(
        targetColor = targetColor,
        hueTolerance = remote.hueTolerance,
        saturationThreshold = remote.saturationThreshold.takeIf { it >= 0f } ?: 0f,
        lightnessThreshold = remote.lightnessThreshold.takeIf { it >= 0f } ?: 0f,
        highlightSaturationBoost = remote.highlightSaturationBoost.takeIf { it >= 0f } ?: 1f,
        highlightLightnessBoost = remote.highlightLightnessBoost.takeIf { it >= 0f } ?: 0f,
        desaturationFactorNonTarget = remote.desaturationFactorNonTarget.takeIf { it >= 0f } ?: 0f,
    )
}

@Composable
fun Genre.selectiveHighlight(): SelectiveColorParams? = selectiveHighlight(LocalGenreVisualConfig.current)

fun Genre.shimmerColors(visualConfig: GenreVisualConfig?): List<Color> {
    val palette = colorPalette(visualConfig)
    val primary = resolveColor(visualConfig)
    return listOf(
        Color.Transparent,
        primary.copy(alpha = .2f),
    ).plus(palette)
        .plus(Color.Transparent)
}

@Composable
fun Genre.shimmerColors(): List<Color> = shimmerColors(LocalGenreVisualConfig.current)

fun Genre.colorPalette(visualConfig: GenreVisualConfig?): List<Color> {
    val remotePalette = visualConfig?.colorPalette?.mapNotNull { it.parseColor() } ?: emptyList()
    return remotePalette.ifEmpty { compiledColorPalette() }
}

fun Genre.subtitle(): String =
    when (this) {
        Genre.FANTASY -> "MAGIC & MYTH"
        Genre.CYBERPUNK -> "NEON & TECH"
        Genre.HORROR -> "SHADOWS & FEAR"
        Genre.HEROES -> "ARTFACTS & POWERS"
        Genre.CRIME -> "BLOOD & NOIR"
        Genre.SHINOBI -> "BLADES & HONOR"
        Genre.SPACE_OPERA -> "STARS & WAR"
        Genre.COWBOY -> "GUNS & SAND"
        Genre.PUNK_ROCK -> "ANARCHY & RIOTS"
    }

fun Genre.genreIcon(): Int =
    when (this) {
        Genre.FANTASY -> R.drawable.ic_dragon
        Genre.CYBERPUNK -> R.drawable.scifi_icon
        Genre.HORROR -> R.drawable.horror_icon
        Genre.HEROES -> R.drawable.ic_eye_mask
        Genre.CRIME -> R.drawable.crime
        Genre.SHINOBI -> R.drawable.shinobi_background
        Genre.SPACE_OPERA -> R.drawable.space_opera
        Genre.COWBOY -> R.drawable.cowboys
        Genre.PUNK_ROCK -> R.drawable.punk_rock
    }

@Composable
fun Genre.colorPalette(): List<Color> = colorPalette(LocalGenreVisualConfig.current)

fun Genre.vibrationPattern(visualConfig: GenreVisualConfig? = null): LongArray? {
    if (visualConfig == null || visualConfig.vibrationPattern.isEmpty()) return null
    return visualConfig.vibrationPattern.toLongArray()
}

fun Genre.compiledColorPalette(): List<Color> =
    when (this) {
        Genre.FANTASY -> {
            listOf(Color(0xFF8B2635), Color(0xFF5E1A24), Color(0xFFB33144))
        }

        Genre.CYBERPUNK -> {
            listOf(
                Color(0xFF8B00FF),
                Color(0xFF00FFFF),
                Color(0xFFFF00FF),
                Color(0xFF2D0066),
            )
        }

        Genre.HORROR -> {
            listOf(
                Color(0xFF1C2541),
                Color(0xFF0B132B),
                Color(0xFF3A506B),
                Color(0xFF6FFFE9),
            )
        }

        Genre.HEROES -> {
            listOf(
                Color(0xFF003F88),
                Color(0xFF002952),
                Color(0xFF00509D),
                Color(0xFFFDC500),
            )
        }

        Genre.CRIME -> {
            listOf(
                Color(0xFFE91E63),
                Color(0xFFAD1457),
                Color(0xFFFF4081),
                Color(0xFF000000),
            )
        }

        Genre.SHINOBI -> {
            listOf(
                Color(0xFF5C2751),
                Color(0xFF431C3A),
                Color(0xFF7A336B),
                Color(0xFFF39237),
            )
        }

        Genre.SPACE_OPERA -> {
            listOf(
                Color(0xFF0081A7),
                Color(0xFF005F73),
                Color(0xFF00AFB9),
                Color(0xFFFED9B7),
            )
        }

        Genre.COWBOY -> {
            listOf(
                Color(0xFF8B4513),
                Color(0xFF5D2E0A),
                Color(0xFFA0522D),
                Color(0xFFDEB887),
            )
        }

        Genre.PUNK_ROCK -> {
            listOf(
                Color(0xFF00B050),
                Color(0xFF008037),
                Color(0xFF00E676),
                Color(0xFF000000),
            )
        }
    }

// ── Utility ──────────────────────────────────────────────────────────────

/** Resolve primary color from remote config. Falls back to enum property. */
fun Genre.resolveColor(visualConfig: GenreVisualConfig?): Color = visualConfig?.primaryColor?.parseColor() ?: this.color

@Composable
fun Genre.resolveColor(): Color = resolveColor(LocalGenreVisualConfig.current)

/** Resolve icon color from remote config. Falls back to enum property. */
fun Genre.resolveIconColor(visualConfig: GenreVisualConfig?): Color = visualConfig?.iconColor?.parseColor() ?: this.iconColor

@Composable
fun Genre?.resolveIconColor(): Color =
    if (this == null) {
        MaterialTheme.colorScheme.onBackground
    } else {
        resolveIconColor(LocalGenreVisualConfig.current)
    }

/** Resolve the image for the genre. Falls back to default header image. */
fun Genre.resolveImageUrl(visualConfig: GenreVisualConfig?): String? = visualConfig?.imageUrl

@Composable
fun Genre.resolveImageUrl(): String? = resolveImageUrl(LocalGenreVisualConfig.current)

/** Resolve the background image for the genre. Falls back to enum property. */
fun Genre.resolveBackground(visualConfig: GenreVisualConfig?): Any = this.icon

@Composable
fun Genre.resolveBackground(): Any = resolveBackground(LocalGenreVisualConfig.current)

/** Parse a hex color string like "#8B2635" to a Compose [Color], or null if invalid/empty. */
internal fun String.parseColor(): Color? =
    if (isNotBlank()) {
        try {
            Color(android.graphics.Color.parseColor(this))
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }
