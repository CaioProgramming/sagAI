package com.ilustris.sagai.ui.genre.crime

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.ui.theme.LocalSagaGenre

/**
 * The corkboard's own colors, deliberately *not* taken from `colorScheme.background`/`onBackground`.
 *
 * A pinned photo is a physical object: the paper is light stock in a lit room and light stock at
 * night, so its ink has to be dark in both. Reading the theme's `on` color for that ink is what made
 * the first pass illegible — in dark mode it resolved to near-white and every caption vanished into
 * the cream paper it was written on. So [paper] and [ink] are always a self-contained, high-contrast
 * pair, and dark mode changes how *lit* the board is (dimmer, aged paper; deeper cork) rather than
 * inverting it.
 *
 * Only [pin] follows the saga's own accent — it's the one part of a board that is genre-colored
 * rather than physical.
 */
@Immutable
data class CorkboardPalette(
    /** The cork surface behind everything. */
    val board: Color,
    /** Photo/index-card stock. Always light — see the class note. */
    val paper: Color,
    /** Primary handwriting on [paper]. Always dark. */
    val ink: Color,
    /** Secondary handwriting — captions, counts, anything subordinate. */
    val inkSoft: Color,
    /** The red string strung between pins. */
    val thread: Color,
    /** Pushpin heads, in the saga's accent. */
    val pin: Color,
)

/** Cork in a lit room. */
private val LIGHT_BOARD = Color(0xFFB98D5F)
private val LIGHT_PAPER = Color(0xFFFFFDF6)

/** Cork at night — the same board under a desk lamp, not an inverted one. */
private val DARK_BOARD = Color(0xFF3B2E24)

/** Aged stock rather than bright white, so a lit pin doesn't glare against a dark room. */
private val DARK_PAPER = Color(0xFFE7DFCC)

/** Pencil/graphite, warmed slightly toward the paper it sits on. */
private val INK = Color(0xFF2A2520)

/** The detective-board red. Lifted in dark mode so the string still reads against deep cork. */
private val LIGHT_THREAD = Color(0xFFA82D24)
private val DARK_THREAD = Color(0xFFD4463A)

@Composable
fun rememberCorkboardPalette(): CorkboardPalette {
    val dark = isSystemInDarkTheme()
    val fallbackAccent = MaterialTheme.colorScheme.primary
    val genre = LocalSagaGenre.current
    val accent = genre?.compiledColorPalette()?.firstOrNull() ?: fallbackAccent

    return remember(dark, accent) {
        CorkboardPalette(
            board = if (dark) DARK_BOARD else LIGHT_BOARD,
            paper = if (dark) DARK_PAPER else LIGHT_PAPER,
            ink = INK,
            inkSoft = INK.copy(alpha = 0.62f),
            thread = if (dark) DARK_THREAD else LIGHT_THREAD,
            pin = accent,
        )
    }
}
