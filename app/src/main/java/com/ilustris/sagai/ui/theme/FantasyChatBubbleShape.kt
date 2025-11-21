package com.ilustris.sagai.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class FantasyChatBubbleShape(
    private val cornerRadius: Dp = 0.dp, // Unused, we want ragged edges
    private val tailWidth: Dp = 20.dp,
    private val tailHeight: Dp = 20.dp,
    private val tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val tailWidthPx = with(density) { tailWidth.toPx() }
        val tailHeightPx = with(density) { tailHeight.toPx() }
        val raggedPx = with(density) { 3.dp.toPx() } // Depth of the "rags"
        val path = Path()

        val w = size.width
        val h = size.height

        // We'll create a "Parchment" shape with irregular edges.
        // We simulate this by adding small random-looking variations to the lines.

        when (tailAlignment) {
            BubbleTailAlignment.BottomRight -> {
                // User Bubble

                // Start Top-Left
                path.moveTo(0f, 0f)

                // Top Edge (Ragged)
                path.lineTo(w * 0.2f, raggedPx)
                path.lineTo(w * 0.4f, -raggedPx * 0.5f)
                path.lineTo(w * 0.7f, raggedPx)
                path.lineTo(w, 0f) // Top-Right

                // Right Edge (Ragged)
                path.lineTo(w - raggedPx, h * 0.3f)
                path.lineTo(w + raggedPx * 0.5f, h * 0.6f)

                // Tail (Bottom-Right Corner)
                // A "torn flap" hanging down
                val tailStart = h - tailHeightPx
                path.lineTo(w, tailStart)

                // Tail Out
                path.lineTo(w + raggedPx, h) // Tip (slightly out)

                // Tail Return (Raggedly back to bottom edge)
                path.lineTo(w - tailWidthPx * 0.5f, h - raggedPx)
                path.lineTo(w - tailWidthPx, h)

                // Bottom Edge (Ragged, moving left)
                path.lineTo(w * 0.6f, h - raggedPx)
                path.lineTo(w * 0.3f, h + raggedPx * 0.5f)
                path.lineTo(0f, h) // Bottom-Left

                // Left Edge (Ragged, moving up)
                path.lineTo(raggedPx, h * 0.7f)
                path.lineTo(-raggedPx * 0.5f, h * 0.3f)
                path.close() // Back to 0,0
            }

            BubbleTailAlignment.BottomLeft -> {
                // NPC Bubble

                // Start Top-Left
                path.moveTo(0f, 0f)

                // Top Edge
                path.lineTo(w * 0.3f, -raggedPx * 0.5f)
                path.lineTo(w * 0.6f, raggedPx)
                path.lineTo(w, 0f) // Top-Right

                // Right Edge
                path.lineTo(w + raggedPx * 0.5f, h * 0.4f)
                path.lineTo(w - raggedPx, h * 0.8f)
                path.lineTo(w, h) // Bottom-Right

                // Bottom Edge
                path.lineTo(w * 0.7f, h - raggedPx)
                path.lineTo(w * 0.4f, h + raggedPx * 0.5f)

                // Tail (Bottom-Left Corner)
                // We are approaching the left side.
                // Tail starts at tailWidthPx
                path.lineTo(tailWidthPx, h)

                // Tail Out (The flap)
                path.lineTo(tailWidthPx * 0.5f, h - raggedPx) // Inner notch
                path.lineTo(-raggedPx, h) // Tip (slightly out left)

                // Tail Return (Up to side)
                path.lineTo(0f, h - tailHeightPx)

                // Left Edge (Ragged, moving up)
                path.lineTo(raggedPx, h * 0.6f)
                path.lineTo(-raggedPx * 0.5f, h * 0.2f)
                path.close()
            }
        }
        return Outline.Generic(path)
    }
}
