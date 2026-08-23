package com.ilustris.sagai.ui.genre.terminal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val TerminalBlack = Color(0xFF050208)

/**
 * The ground the terminal sits on: near-black, a faint scanline grid, and a radial vignette.
 *
 * Deliberately static. The screen treatment now comes from the CRT shader wrapping the whole
 * container ([com.ilustris.sagai.ui.theme.filters.crtScreen]), which supplies the phosphor grid,
 * the bloom and the vignette for every genre — so this only has to supply a plausible surface
 * underneath it.
 *
 * Space Opera previously layered `spaceVoyage` here for its own phosphor/jitter treatment. That is
 * now doubling up with the shader, and worse: it animates continuously, and an animating layer
 * *underneath* a full-screen render effect forces that effect to re-run on every frame. The CRT
 * supersedes it, so both genres share this still background.
 */
@Composable
fun TerminalBackground(modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = TerminalBlack)

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

        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                    radius = size.maxDimension * 0.75f,
                ),
        )
    }
}
