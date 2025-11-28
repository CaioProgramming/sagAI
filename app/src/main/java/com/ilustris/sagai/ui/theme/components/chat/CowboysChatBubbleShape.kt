package com.ilustris.sagai.ui.theme.components.chat

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment

/**
 * Cowboys-themed chat bubble shape inspired by Western wooden signs and planks.
 * Features:
 * - Notched/beveled corners (like rough-cut wood)
 * - Angular tail (like a wooden support bracket)
 * - Slightly irregular edges for that frontier aesthetic
 */
class CowboysChatBubbleShape(
    private val cornerNotch: Dp = 8.dp,
    private val tailWidth: Dp = 16.dp,
    private val tailHeight: Dp = 14.dp,
    private val tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val notchPx = with(density) { cornerNotch.toPx() }
        val tailWidthPx = with(density) { tailWidth.toPx() }
        val tailHeightPx = with(density) { tailHeight.toPx() }
        val path = Path()

        val bodyHeight = size.height - tailHeightPx

        when (tailAlignment) {
            BubbleTailAlignment.BottomRight -> {
                // User Bubble (Right aligned) - like a wooden sign pointing right
                path.moveTo(notchPx, 0f)

                // Top edge with notched top-right corner
                path.lineTo(size.width - notchPx, 0f)
                path.lineTo(size.width, notchPx)

                // Right edge down to tail connection
                path.lineTo(size.width, bodyHeight - notchPx)

                // Notch before tail
                path.lineTo(size.width - notchPx, bodyHeight)

                // Tail - angular wooden bracket style
                // Top of tail bracket
                path.lineTo(size.width - tailWidthPx + notchPx, bodyHeight)
                
                // Diagonal down to tail tip (like a support beam)
                path.lineTo(size.width - tailWidthPx, bodyHeight + tailHeightPx * 0.6f)
                path.lineTo(size.width - tailWidthPx - notchPx, size.height)
                
                // Back up along tail
                path.lineTo(size.width - tailWidthPx - notchPx * 2, bodyHeight + tailHeightPx * 0.4f)
                path.lineTo(size.width - tailWidthPx - notchPx * 2, bodyHeight)

                // Bottom edge
                path.lineTo(notchPx, bodyHeight)

                // Bottom-left notched corner
                path.lineTo(0f, bodyHeight - notchPx)

                // Left edge
                path.lineTo(0f, notchPx)

                // Top-left notched corner
                path.lineTo(notchPx, 0f)

                path.close()
            }

            BubbleTailAlignment.BottomLeft -> {
                // NPC Bubble (Left aligned) - like a wooden sign pointing left
                // Top-left notched corner
                path.moveTo(0f, notchPx)
                path.lineTo(notchPx, 0f)

                // Top edge
                path.lineTo(size.width - notchPx, 0f)

                // Top-right notched corner
                path.lineTo(size.width, notchPx)

                // Right edge
                path.lineTo(size.width, bodyHeight - notchPx)

                // Bottom-right notched corner
                path.lineTo(size.width - notchPx, bodyHeight)

                // Bottom edge to tail
                path.lineTo(tailWidthPx + notchPx * 2, bodyHeight)

                // Tail - angular wooden bracket style (mirrored)
                // Top of tail bracket
                path.lineTo(tailWidthPx + notchPx * 2, bodyHeight + tailHeightPx * 0.4f)
                path.lineTo(tailWidthPx + notchPx, size.height)
                
                // Diagonal to tail tip
                path.lineTo(tailWidthPx, bodyHeight + tailHeightPx * 0.6f)
                path.lineTo(tailWidthPx - notchPx, bodyHeight)

                // Back to bottom edge
                path.lineTo(notchPx, bodyHeight)

                // Bottom-left notched corner
                path.lineTo(0f, bodyHeight - notchPx)

                // Left edge back to start
                path.lineTo(0f, notchPx)

                path.close()
            }
        }
        return Outline.Generic(path)
    }
}
