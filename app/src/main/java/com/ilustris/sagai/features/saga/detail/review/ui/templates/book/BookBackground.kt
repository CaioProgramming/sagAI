package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private val ParchmentTop = Color(0xFFF3E6C8)
private val ParchmentBottom = Color(0xFFE1CB9C)
private val Ink = Color(0xFF3B2E1F)

/**
 * A warm parchment page: cream-to-tan gradient with a faint procedural paper
 * grain. Self-contained (no shader/Remote Config dependency) so it always
 * renders the same regardless of device or API level.
 */
@Composable
fun BookBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(listOf(ParchmentTop, ParchmentBottom)))

        val random = Random(size.width.toInt() * 31 + size.height.toInt())
        repeat(400) {
            drawCircle(
                color = Ink.copy(alpha = random.nextFloat() * 0.03f),
                radius = random.nextFloat() * 1.5f + 0.3f,
                center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height),
            )
        }

        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(Color.Transparent, Ink.copy(alpha = 0.12f)),
                    radius = size.maxDimension * 0.8f,
                ),
        )
    }
}
