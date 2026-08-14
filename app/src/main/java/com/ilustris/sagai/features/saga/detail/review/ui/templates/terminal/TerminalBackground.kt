package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

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
 * A self-contained CRT-monitor look: near-black background, a radial vignette,
 * and horizontal scanlines. Deliberately independent of the remote-config-driven
 * shader pipeline (Filters.kt) so it renders identically on every device/API
 * level without needing Remote Config to be populated. The scanline tint comes
 * from `MaterialTheme.colorScheme.primary` — already the live, theme-resolved
 * genre color — rather than a second, hand-picked palette.
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
