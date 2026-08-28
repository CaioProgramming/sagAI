package com.ilustris.sagai.ui.genre.crime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import kotlin.random.Random

/** How many cork-grain flecks to scatter — enough to read as texture, not as visible dots up close. */
private const val GRAIN_COUNT = 260

/**
 * The board's surface behind every pinned photo: the theme's own background (so it still adapts
 * to light/dark, like [CrimeBackground]), the genre's accent as a corner wash, and a scatter of
 * small flecks standing in for cork grain — enough texture to read as a board, not a blank canvas.
 * The fleck positions are cached per-size via [drawWithCache] rather than recomputed on every
 * draw, since the board sits behind an animating camera that redraws constantly.
 */
@Composable
fun CorkboardBackground(modifier: Modifier = Modifier) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val accent = LocalSagaGenre.current?.compiledColorPalette()?.firstOrNull() ?: MaterialTheme.colorScheme.primary
    val grainColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier
            .fillMaxSize()
            .background(backgroundColor)
            .drawWithCache {
                val rng = Random(size.width.toInt() * 31 + size.height.toInt())
                val grain =
                    List(GRAIN_COUNT) {
                        Offset(rng.nextFloat() * size.width, rng.nextFloat() * size.height) to
                            (rng.nextFloat() * 1.6f + 0.5f)
                    }
                val wash =
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.12f), Color.Transparent),
                        radius = size.maxDimension * 0.65f,
                    )
                onDrawBehind {
                    drawRect(brush = wash)
                    grain.forEach { (offset, radius) ->
                        drawCircle(color = grainColor.copy(alpha = 0.05f), radius = radius, center = offset)
                    }
                }
            },
    )
}
