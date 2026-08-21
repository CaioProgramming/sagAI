package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/** How many cells the bar is drawn with — a real one is measured in characters, not pixels. */
private const val BAR_CELLS = 24

/**
 * Progress as a shell reports it: a bracketed bar of filled and empty cells with a count beside it.
 *
 * A rounded, tweened Material track is the single most modern thing that could sit on this screen —
 * it belongs to a design language that postdates everything the rest of the template is imitating.
 * Drawing the bar out of characters keeps the whole surface inside one idiom, and a terminal really
 * did report progress this way.
 */
@Composable
fun TerminalProgress(
    current: Int,
    total: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (total <= 0) return

    val filled = ((current.toFloat() / total) * BAR_CELLS).toInt().coerceIn(0, BAR_CELLS)
    val bar = "█".repeat(filled) + "░".repeat(BAR_CELLS - filled)

    // No numeric counter beside it: the bar already says how far along this is, and a fraction
    // turns a mood-setting frame into a progress report the reader starts counting down.
    Text(
        text = "[$bar]",
        color = color,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier,
    )
}
