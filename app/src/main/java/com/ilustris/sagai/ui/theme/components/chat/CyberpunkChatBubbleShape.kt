package com.ilustris.sagai.ui.theme.components.chat

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class CyberpunkChatBubbleShape(
    private val cornerRadius: Dp,
    private val tailWidth: Dp = 12.dp,
    private val tailHeight: Dp = 12.dp,
    private val tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val cutSizePx = with(density) { cornerRadius.toPx() }
        val tailWidthPx = with(density) { tailWidth.toPx() }
        val tailHeightPx = with(density) { tailHeight.toPx() }
        val path = Path()

        val bodyHeight = size.height - tailHeightPx
        // A small flat area at the bottom of the tail to avoid a needle-sharp point,
        // giving it a more "manufactured" look.
        val tailTipPx = tailWidthPx * 0.25f

        when (tailAlignment) {
            BubbleTailAlignment.BottomRight -> {
                // User Bubble (Right aligned)
                path.moveTo(0f, 0f)

                // Top-Right: Cut
                path.lineTo(size.width - cutSizePx, 0f)
                path.lineTo(size.width, cutSizePx)

                // Right Edge (Vertical down to bottom of tail)
                path.lineTo(size.width, size.height)

                // Tail Bottom (Small flat tip)
                path.lineTo(size.width - tailTipPx, size.height)

                // Tail Diagonal (Up and Left to body)
                path.lineTo(size.width - tailWidthPx, bodyHeight)

                // Bottom Edge
                path.lineTo(cutSizePx, bodyHeight)

                // Bottom-Left: Cut
                path.lineTo(0f, bodyHeight - cutSizePx)

                // Left Edge
                path.lineTo(0f, 0f)

                path.close()
            }

            BubbleTailAlignment.BottomLeft -> {
                // NPC Bubble (Left aligned)
                // Top-Left: Cut
                path.moveTo(0f, cutSizePx)
                path.lineTo(cutSizePx, 0f)

                // Top-Right: Square
                path.lineTo(size.width, 0f)

                // Right Edge
                path.lineTo(size.width, bodyHeight - cutSizePx)

                // Bottom-Right: Cut
                path.lineTo(size.width - cutSizePx, bodyHeight)

                // Bottom Edge to Tail
                path.lineTo(tailWidthPx, bodyHeight)

                // Tail Diagonal (Down and Left to tip)
                path.lineTo(tailTipPx, size.height)

                // Tail Bottom (Small flat tip)
                path.lineTo(0f, size.height)

                // Left Edge (Vertical up from bottom of tail)
                path.lineTo(0f, cutSizePx)

                path.close()
            }
        }
        return Outline.Generic(path)
    }
}
