package com.ilustris.sagai.features.newsaga.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
    val background: Int,
) {
    FANTASY(
        title = R.string.genre_fantasy,
        color = Color(0xFF8B2635),
        iconColor = Color.White,
        background = R.drawable.fantasy,
    ),
    CYBERPUNK(
        title = R.string.genre_scifi,
        color = Color(0xFF8B00FF),
        iconColor = Color.White,
        background = R.drawable.scifi,
    ),

    HORROR(
        title = R.string.genre_horror,
        color = Color(0xFF1C2541),
        iconColor = Color.White,
        background = R.drawable.horror,
    ),

    HEROES(
        title = R.string.genre_heroes,
        color = Color(0xFF003F88),
        iconColor = Color.White,
        background = R.drawable.hero,
    ),
    CRIME(
        title = R.string.genre_crime,
        color = Color(0xFFE91E63),
        iconColor = Color.White,
        background = R.drawable.crime,
    ),

    SHINOBI(
        title = R.string.genre_shinobi,
        color = Color(0xFF5C2751),
        iconColor = Color.White,
        background = R.drawable.shinobi_background,
    ),

    SPACE_OPERA(
        title = R.string.genre_space_opera,
        color = Color(0xFF0081A7),
        iconColor = Color.White,
        background = R.drawable.space_opera,
    ),

    COWBOY(
        title = R.string.genre_cowboys,
        color = Color(0xFF8B4513),
        iconColor = Color.White,
        background = R.drawable.cowboys,
    ),

    PUNK_ROCK(
        title = R.string.genre_punk_rock,
        color = Color(0xFF00B050),
        iconColor = Color.White,
        background = R.drawable.punk_rock,
    ),
    ;

    val ambientMusicConfigKey: String = "${this.name}_ambient_music_url".lowercase()
    val configKey: String = "${this.name.lowercase()}_config"
}

// ── Remote-only visual extensions ────────────────────────────────────────
// All visual properties come from GenreVisualConfig (Remote Config).
// If the config is null or missing the relevant data, the effect is NOT applied.

fun Genre.selectiveHighlight(visualConfig: GenreVisualConfig?): SelectiveColorParams? {
    if (visualConfig == null) return null
    val remote = visualConfig.selectiveHighlight ?: return null
    val targetColor = resolveColor(visualConfig) ?: return null
    if (remote.hueTolerance < 0f) return null
    return SelectiveColorParams(
        targetColor = targetColor,
        hueTolerance = remote.hueTolerance,
        saturationThreshold = remote.saturationThreshold.takeIf { it >= 0f } ?: 0.02f,
        lightnessThreshold = remote.lightnessThreshold.takeIf { it >= 0f } ?: 0.05f,
        highlightSaturationBoost = remote.highlightSaturationBoost.takeIf { it >= 0f } ?: 1f,
        highlightLightnessBoost = remote.highlightLightnessBoost.takeIf { it >= 0f } ?: 0.05f,
        desaturationFactorNonTarget = remote.desaturationFactorNonTarget.takeIf { it >= 0f } ?: 0f,
    )
}

@Composable
fun Genre.selectiveHighlight(): SelectiveColorParams? = selectiveHighlight(LocalGenreVisualConfig.current)

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

fun Genre.shimmerColors(visualConfig: GenreVisualConfig?): List<Color> {
    val palette = colorPalette(visualConfig)
    if (palette.isEmpty()) return emptyList()
    val primary = resolveColor(visualConfig) ?: return emptyList()
    return listOf(
        Color.Transparent,
        primary.copy(alpha = .2f),
    ).plus(palette)
        .plus(Color.Transparent)
}

@Composable
fun Genre.shimmerColors(): List<Color> = shimmerColors(LocalGenreVisualConfig.current)

fun Genre.colorPalette(visualConfig: GenreVisualConfig?): List<Color> {
    if (visualConfig == null || visualConfig.colorPalette.isEmpty()) return emptyList()
    return visualConfig.colorPalette.mapNotNull { it.parseColor() }
}

@Composable
fun Genre.colorPalette(): List<Color> = colorPalette(LocalGenreVisualConfig.current)

fun Genre.vibrationPattern(visualConfig: GenreVisualConfig? = null): LongArray? {
    if (visualConfig == null || visualConfig.vibrationPattern.isEmpty()) return null
    return visualConfig.vibrationPattern.toLongArray()
}

// ── Utility ──────────────────────────────────────────────────────────────

/** Resolve primary color from remote config. Returns null if config is missing or color is invalid. */
fun Genre.resolveColor(visualConfig: GenreVisualConfig?): Color? = visualConfig?.primaryColor?.parseColor()

@Composable
fun Genre.resolveColor(): Color? = resolveColor(LocalGenreVisualConfig.current)

/** Resolve icon color from remote config. Returns null if config is missing or color is invalid. */
fun Genre.resolveIconColor(visualConfig: GenreVisualConfig?): Color? = visualConfig?.iconColor?.parseColor()

@Composable
fun Genre.resolveIconColor(): Color? = resolveIconColor(LocalGenreVisualConfig.current)

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
