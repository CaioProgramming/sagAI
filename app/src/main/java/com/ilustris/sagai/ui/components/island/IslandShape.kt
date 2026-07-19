package com.ilustris.sagai.ui.components.island

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private val IslandCompactCorner = 28.dp
private val IslandExpandedCorner = 32.dp

/**
 * The island's shape, deliberately independent of [androidx.compose.material3.MaterialTheme]'s
 * genre-driven corner radius (system chrome shouldn't change shape per saga, unlike story-content
 * UI). [RoundedCornerShape] auto-clamps a corner larger than half the box's shorter dimension, so
 * this single corner value renders as a full pill on any short/narrow compact row for free — no
 * per-content measurement needed.
 */
@Composable
fun rememberIslandShape(expanded: Boolean): Shape {
    val corner by
        animateDpAsState(
            targetValue = if (expanded) IslandExpandedCorner else IslandCompactCorner,
            label = "islandCorner",
        )
    return RoundedCornerShape(corner)
}
