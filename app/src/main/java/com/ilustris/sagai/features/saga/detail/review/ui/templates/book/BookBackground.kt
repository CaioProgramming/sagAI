package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * The page surface — the theme's own `surfaceContainer` (adapts to light/dark automatically,
 * unlike the old fixed parchment gradient) with a faint procedural grain + vignette layered on
 * top for an aged-page texture. The grain color is `onSurface`, not a hardcoded ink brown, so the
 * grunge reads correctly in both themes instead of only ever looking right against cream.
 */
@Composable
fun BookBackground(modifier: Modifier = Modifier) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
    val grainColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = surfaceColor)

        val random = Random(size.width.toInt() * 31 + size.height.toInt())
        repeat(400) {
            drawCircle(
                color = grainColor.copy(alpha = random.nextFloat() * 0.035f),
                radius = random.nextFloat() * 1.5f + 0.3f,
                center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height),
            )
        }

        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(Color.Transparent, grainColor.copy(alpha = 0.08f)),
                    radius = size.maxDimension * 0.8f,
                ),
        )
    }
}
