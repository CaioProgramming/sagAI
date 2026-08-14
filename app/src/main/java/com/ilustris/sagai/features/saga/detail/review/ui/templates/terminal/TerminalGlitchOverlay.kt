package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * A brief signal-glitch burst every few seconds — a lightweight, self-contained approximation
 * (colored fringe lines + a translucent shifted band), not a real per-pixel channel split, to
 * stay consistent with [TerminalBackground]'s no-shader philosophy. Meant to sit as the topmost
 * layer over a whole page (not just its background), so it reads as "on top of everything".
 */
@Composable
fun TerminalGlitchOverlay(modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    var glitchActive by remember { mutableStateOf(false) }
    var seed by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(3500, 7500))
            seed++
            glitchActive = true
            delay(180)
            glitchActive = false
        }
    }

    if (!glitchActive) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val random = Random(seed)
        repeat(3) {
            val y = random.nextFloat() * size.height
            val bandHeight = random.nextFloat() * 14f + 4f
            val shiftX = (random.nextFloat() - 0.5f) * 32f

            drawRect(
                color = accent.copy(alpha = 0.12f),
                topLeft = Offset(shiftX, y),
                size = Size(size.width, bandHeight),
            )
            drawLine(
                color = Color.Cyan.copy(alpha = 0.45f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.5f,
            )
            drawLine(
                color = Color.Magenta.copy(alpha = 0.45f),
                start = Offset(0f, y + bandHeight),
                end = Offset(size.width, y + bandHeight),
                strokeWidth = 1.5f,
            )
        }
    }
}
