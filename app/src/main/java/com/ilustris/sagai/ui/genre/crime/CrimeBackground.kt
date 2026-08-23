package com.ilustris.sagai.ui.genre.crime

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.ui.theme.LocalSagaGenre

/**
 * The chat surface — theme's own `background`, so it adapts to light/dark like every other
 * messaging app, with a faint corner wash in the genre's own accent (not a hardcoded noir palette)
 * for a little atmosphere behind the bubbles.
 */
@Composable
fun CrimeBackground(modifier: Modifier = Modifier) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val accent = LocalSagaGenre.current?.compiledColorPalette()?.firstOrNull() ?: MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = backgroundColor)
        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.1f), Color.Transparent),
                    radius = size.maxDimension * 0.6f,
                ),
        )
    }
}
