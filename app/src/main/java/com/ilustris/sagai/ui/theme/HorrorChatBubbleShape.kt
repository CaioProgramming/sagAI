package com.ilustris.sagai.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class HorrorChatBubbleShape(
    private val pixelSize: Dp = 4.dp,
    private val tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
    private val drawTail: Boolean = true,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val p = with(density) { pixelSize.toPx() }
        val path = Path()

        val w = size.width
        val h = size.height

        // If no tail, body uses full height. If tail, reserve space.
        val bodyH = if (drawTail) h - 2 * p else h

        // Top Edge
        path.moveTo(p, 0f)
        path.lineTo(w - p, 0f)

        // Top-Right Corner (Step down)
        path.lineTo(w - p, p)
        path.lineTo(w, p)

        // Right Edge
        path.lineTo(w, bodyH - p)

        // Bottom-Right Corner (Step in)
        path.lineTo(w - p, bodyH - p)
        path.lineTo(w - p, bodyH)

        if (drawTail && tailAlignment == BubbleTailAlignment.BottomRight) {
            // Tail (Stepped "Lightning" down-right)
            path.lineTo(w - p, bodyH + p) // Down 1
            path.lineTo(w, bodyH + p) // Right 1
            path.lineTo(w, bodyH + 2 * p) // Down 1 (Tip)
            path.lineTo(w - p, bodyH + 2 * p) // Left 1
            path.lineTo(w - p, bodyH + p) // Up 1
            path.lineTo(w - 2 * p, bodyH + p) // Left 1
            path.lineTo(w - 2 * p, bodyH) // Up 1 (Back to body bottom)
        }

        // Bottom Edge
        if (drawTail && tailAlignment == BubbleTailAlignment.BottomLeft) {
            path.lineTo(2 * p, bodyH)
            // Tail (Stepped "Lightning" down-left)
            path.lineTo(2 * p, bodyH + p) // Down 1
            path.lineTo(p, bodyH + p) // Left 1
            path.lineTo(p, bodyH + 2 * p) // Down 1 (Tip)
            path.lineTo(0f, bodyH + 2 * p) // Left 1
            path.lineTo(0f, bodyH + p) // Up 1
            path.lineTo(p, bodyH + p) // Right 1
            path.lineTo(p, bodyH) // Up 1 (Back to body)
        } else {
            path.lineTo(p, bodyH)
        }

        // Bottom-Left Corner (Step up)
        path.lineTo(p, bodyH - p)
        path.lineTo(0f, bodyH - p)

        // Left Edge
        path.lineTo(0f, p)

        // Top-Left Corner (Step right)
        path.lineTo(p, p)
        path.lineTo(p, 0f)

        path.close()

        return Outline.Generic(path)
    }
}
