package com.ilustris.sagai.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class HeroesChatBubbleShape(
    private val tailWidth: Dp = 32.dp, // Increased width for better connection
    private val tailHeight: Dp = 20.dp,
    private val skew: Dp = 16.dp,
    private val tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val tailWidthPx = with(density) { tailWidth.toPx() }
        val tailHeightPx = with(density) { tailHeight.toPx() }
        val skewPx = with(density) { skew.toPx() }
        val path = Path()

        val bodyWidth = size.width
        val bodyHeight = size.height - tailHeightPx

        when (tailAlignment) {
            BubbleTailAlignment.BottomRight -> {
                // User Bubble (Right aligned)

                // --- BODY ---
                path.moveTo(0f, 0f)
                path.lineTo(bodyWidth, 0f)
                path.lineTo(bodyWidth, bodyHeight)

                // --- TAIL (Vertical Lightning Bolt) ---

                // Start on bottom edge, left of corner
                // We move left to the start of the tail
                val tailStartX = bodyWidth - tailWidthPx * 0.5f
                path.lineTo(tailStartX, bodyHeight)

                // 1. Bolt 1 (Right & Down)
                path.lineTo(bodyWidth + tailWidthPx * 0.2f, bodyHeight + tailHeightPx * 0.4f)

                // 2. Notch (Left & Down) - Deep cut
                path.lineTo(bodyWidth - tailWidthPx * 0.3f, bodyHeight + tailHeightPx * 0.5f)

                // 3. Bolt 2 / Tip (Right & Down)
                path.lineTo(bodyWidth + tailWidthPx * 0.1f, size.height)

                // 4. Return (Left & Up) - Back to body
                // We connect further left to give the tail a wide, sturdy base
                path.lineTo(bodyWidth - tailWidthPx * 1.2f, bodyHeight)

                // Continue Body Bottom Edge
                path.lineTo(skewPx, bodyHeight)

                path.close()
            }

            BubbleTailAlignment.BottomLeft -> {
                // NPC Bubble (Left aligned)

                // --- BODY ---
                path.moveTo(0f, 0f)
                path.lineTo(bodyWidth, 0f)
                path.lineTo(bodyWidth - skewPx, bodyHeight)

                // Bottom Edge to Tail Return point
                // Tail is on the left.
                // We connect at 1.2 * tailWidth
                path.lineTo(tailWidthPx * 1.2f, bodyHeight)

                // --- TAIL (Mirrored Lightning Bolt) ---

                // 1. Tip (Left & Down)
                path.lineTo(-tailWidthPx * 0.1f, size.height)

                // 2. Notch (Right & Up)
                path.lineTo(tailWidthPx * 0.3f, bodyHeight + tailHeightPx * 0.5f)

                // 3. Bolt 1 (Left & Up)
                path.lineTo(-tailWidthPx * 0.2f, bodyHeight + tailHeightPx * 0.4f)

                // 4. Back to Body (Right & Up)
                path.lineTo(tailWidthPx * 0.5f, bodyHeight)

                // Finish Bottom Edge
                path.lineTo(0f, bodyHeight)

                // Left Edge
                path.lineTo(0f, 0f)

                path.close()
            }
        }
        return Outline.Generic(path)
    }
}
