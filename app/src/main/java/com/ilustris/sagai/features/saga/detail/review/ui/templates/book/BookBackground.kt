package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import kotlin.random.Random

/**
 * The page surface — the theme's own `surfaceContainer` (adapts to light/dark automatically,
 * unlike a fixed parchment gradient) with a faint procedural texture + vignette layered on top.
 * The texture itself is per-genre ([LocalSagaGenre]: an even dot grain for most genres sharing
 * this template, thin washi-style fiber strands for Shinobi) but always tinted with `onSurface`,
 * never a hardcoded ink color, so it reads correctly in both themes.
 */
@Composable
fun BookBackground(modifier: Modifier = Modifier) {
    val genre = LocalSagaGenre.current
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
    val grainColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = surfaceColor)

        if (genre == Genre.SHINOBI) {
            drawRicePaperFibers(grainColor)
        } else {
            drawParchmentGrain(grainColor)
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

private fun DrawScope.drawParchmentGrain(grainColor: Color) {
    val random = Random(size.width.toInt() * 31 + size.height.toInt())
    repeat(400) {
        drawCircle(
            color = grainColor.copy(alpha = random.nextFloat() * 0.035f),
            radius = random.nextFloat() * 1.5f + 0.3f,
            center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height),
        )
    }
}

/** Washi-style scattered fiber strands — long thin flecks instead of an even dot grain. */
private fun DrawScope.drawRicePaperFibers(grainColor: Color) {
    val random = Random(size.width.toInt() * 37 + size.height.toInt())
    repeat(220) {
        val startX = random.nextFloat() * size.width
        val startY = random.nextFloat() * size.height
        val length = random.nextFloat() * 36f + 8f
        val dx = (random.nextFloat() - 0.5f) * length
        val dy = (random.nextFloat() - 0.5f) * length
        drawLine(
            color = grainColor.copy(alpha = random.nextFloat() * 0.06f),
            start = Offset(startX, startY),
            end = Offset(startX + dx, startY + dy),
            strokeWidth = random.nextFloat() * 0.6f + 0.25f,
        )
    }
}
