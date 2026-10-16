package com.ilustris.sagai.features.share.ui.card

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private const val FALLBACK_STOPS = 5

/**
 * The holographic finish for devices without AGSL (below API 33): a palette gradient that slides with
 * the tilt and a soft glare, both drawn with [BlendMode.Screen], which the GPU does in fixed
 * function. The same colours and motion as [holoSurface], without the dodge, the grain or the fringe.
 *
 * Draw it over an offscreen layer; it only brightens.
 */
@Composable
fun HoloFoil(
    colors: List<Color>,
    tilt: () -> Offset,
    modifier: Modifier = Modifier,
) {
    val stops = remember(colors) { colors.sampleEvenly(FALLBACK_STOPS) }
    Box(
        modifier.drawBehind {
            if (size.minDimension <= 0f) return@drawBehind
            val t = tilt()
            val sweep = size.width * (1.2f * t.x)
            drawRect(
                brush =
                    Brush.linearGradient(
                        colors = stops.map { it.copy(alpha = 0.45f) },
                        start = Offset(sweep - size.width * 0.4f, 0f),
                        end = Offset(sweep + size.width * 1.4f, size.height),
                    ),
                blendMode = BlendMode.Screen,
            )
            drawRect(
                brush =
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(size.width * (0.5f + t.x * 0.5f), size.height * (0.5f + t.y * 0.5f)),
                        radius = size.maxDimension * 0.6f,
                    ),
                blendMode = BlendMode.Screen,
            )
        },
    )
}

/** [count] colours picked evenly across the list, keeping its order and both ends. */
internal fun List<Color>.sampleEvenly(count: Int): List<Color> {
    if (isEmpty()) return List(count) { Color.White }
    return List(count) { i -> this[((i * (size - 1)) / (count - 1).coerceAtLeast(1)).coerceIn(0, size - 1)] }
}
