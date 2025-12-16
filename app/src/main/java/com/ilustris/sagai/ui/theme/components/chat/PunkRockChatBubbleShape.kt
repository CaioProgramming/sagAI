package com.ilustris.sagai.ui.theme.components.chat

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Punk Rock themed chat bubble shape.
 * Features a "rough cut" aesthetic inspired by grunge and DIY zines (like Life is Strange UI).
 * The edges are slightly irregular and the tail is sharp and angular.
 */
class PunkRockChatBubbleShape(
    private val tailWidth: Dp = 12.dp,
    private val tailHeight: Dp = 12.dp,
    private val tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val tailWidthPx = with(density) { tailWidth.toPx() }
        val tailHeightPx = with(density) { tailHeight.toPx() }
        val path = Path()

        val bodyHeight = size.height - tailHeightPx

        // Jitter values to create the "rough cut" look
        val jitterSmall = with(density) { 2.dp.toPx() }
        val jitterMedium = with(density) { 4.dp.toPx() }

        when (tailAlignment) {
            BubbleTailAlignment.BottomRight -> {
                // Start Top-Left
                path.moveTo(0f, jitterSmall)

                // Top Edge (Rough)
                path.lineTo(size.width * 0.3f, 0f)
                path.lineTo(size.width * 0.7f, jitterSmall)
                path.lineTo(size.width - jitterMedium, 0f)

                // Top-Right Corner
                path.lineTo(size.width, jitterMedium)

                // Right Edge
                path.lineTo(size.width - jitterSmall, bodyHeight * 0.5f)
                path.lineTo(size.width, bodyHeight - jitterMedium)

                // Tail (Sharp and angular)
                path.lineTo(size.width, size.height) // Tip
                path.lineTo(size.width - tailWidthPx, bodyHeight) // Return

                // Bottom Edge
                path.lineTo(size.width * 0.6f, bodyHeight + jitterSmall)
                path.lineTo(size.width * 0.2f, bodyHeight - jitterSmall)
                path.lineTo(jitterMedium, bodyHeight)

                // Left Edge
                path.lineTo(0f, bodyHeight - jitterMedium)
                path.lineTo(jitterSmall, bodyHeight * 0.4f)
                path.lineTo(0f, jitterSmall)
            }

            BubbleTailAlignment.BottomLeft -> {
                // Start Top-Right
                path.moveTo(size.width, jitterSmall)

                // Top Edge
                path.lineTo(size.width * 0.7f, 0f)
                path.lineTo(size.width * 0.3f, jitterSmall)
                path.lineTo(jitterMedium, 0f)

                // Top-Left Corner
                path.lineTo(0f, jitterMedium)

                // Left Edge
                path.lineTo(jitterSmall, bodyHeight * 0.5f)
                path.lineTo(0f, bodyHeight - jitterMedium)

                // Tail
                path.lineTo(0f, size.height) // Tip
                path.lineTo(tailWidthPx, bodyHeight) // Return

                // Bottom Edge
                path.lineTo(size.width * 0.4f, bodyHeight + jitterSmall)
                path.lineTo(size.width * 0.8f, bodyHeight - jitterSmall)
                path.lineTo(size.width - jitterMedium, bodyHeight)

                // Right Edge
                path.lineTo(size.width, bodyHeight - jitterMedium)
                path.lineTo(size.width - jitterSmall, bodyHeight * 0.4f)
                path.lineTo(size.width, jitterSmall)
            }
        }

        path.close()
        return Outline.Generic(path)
    }
}
