package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.animations.spaceVoyage
import com.ilustris.sagai.ui.theme.LocalSagaGenre

private val TerminalBlack = Color(0xFF050208)

/**
 * A self-contained CRT-monitor look: near-black background, a radial vignette, and (for most
 * genres sharing this template) a hand-drawn horizontal scanline grid — deliberately independent
 * of the remote-config-driven shader pipeline (Filters.kt) so it renders identically on every
 * device/API level without needing Remote Config to be populated.
 *
 * Space Opera is the one exception: rather than reusing Cyberpunk's plain hacker-terminal
 * scanlines, it layers [spaceVoyage] on top — the genre's own existing CRT/VHS VFX (phosphor
 * glow, segment jitter, scanlines, interference), already used everywhere else `genreVfx(genre)`
 * applies for Space Opera — so this reads as a ship console panel, not a borrowed terminal.
 * [spaceVoyage] draws its own scanlines, so the manual grid below is skipped for that genre to
 * avoid doubling up.
 */
@Composable
fun TerminalBackground(modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    val isSpaceOperaPanel = LocalSagaGenre.current == Genre.SPACE_OPERA

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .let { if (isSpaceOperaPanel) it.spaceVoyage(true) else it },
    ) {
        drawRect(color = TerminalBlack)

        if (!isSpaceOperaPanel) {
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = accent.copy(alpha = 0.05f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
                y += 4f
            }
        }

        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                    radius = size.maxDimension * 0.75f,
                ),
        )
    }
}
