package com.ilustris.sagai.ui.theme.components.chat

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class SpaceChatBubbleShape(
    private val cutSize: Dp = 8.dp,
    private val largeCutSize: Dp = 24.dp,
    private val tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val cutPx = with(density) { cutSize.toPx() }
        val largeCutPx = with(density) { largeCutSize.toPx() }
        val path = Path()

        val w = size.width
        val h = size.height

        // "HUD / Stats Card" Design
        // A clean technical shape with chamfered (cut) corners.
        // We use a larger cut on the bottom corner to indicate direction/speaker,
        // replacing the traditional tail.

        when (tailAlignment) {
            BubbleTailAlignment.BottomRight -> {
                // User Bubble (Right aligned)

                // Top-Left: Small Cut
                path.moveTo(0f, cutPx)
                path.lineTo(cutPx, 0f)

                // Top Edge
                path.lineTo(w - cutPx, 0f)

                // Top-Right: Small Cut
                path.lineTo(w, cutPx)

                // Right Edge
                path.lineTo(w, h - largeCutPx)

                // Bottom-Right: LARGE Cut (The "Direction" indicator)
                path.lineTo(w - largeCutPx, h)

                // Bottom Edge
                path.lineTo(cutPx, h)

                // Bottom-Left: Small Cut
                path.lineTo(0f, h - cutPx)

                // Left Edge
                path.lineTo(0f, cutPx)

                path.close()
            }

            BubbleTailAlignment.BottomLeft -> {
                // NPC Bubble (Left aligned)

                // Top-Left: Small Cut
                path.moveTo(0f, cutPx)
                path.lineTo(cutPx, 0f)

                // Top Edge
                path.lineTo(w - cutPx, 0f)

                // Top-Right: Small Cut
                path.lineTo(w, cutPx)

                // Right Edge
                path.lineTo(w, h - cutPx)

                // Bottom-Right: Small Cut
                path.lineTo(w - cutPx, h)

                // Bottom Edge
                path.lineTo(largeCutPx, h)

                // Bottom-Left: LARGE Cut (The "Direction" indicator)
                path.lineTo(0f, h - largeCutPx)

                // Left Edge
                path.lineTo(0f, cutPx)

                path.close()
            }
        }
        return Outline.Generic(path)
    }
}
