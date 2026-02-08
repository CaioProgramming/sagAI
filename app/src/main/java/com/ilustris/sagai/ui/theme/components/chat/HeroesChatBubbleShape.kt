package com.ilustris.sagai.ui.theme.components.chat

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class HeroesChatBubbleShape(
    private val tailWidth: Dp = 16.dp,
    private val tailHeight: Dp = 16.dp,
    private val skew: Dp = 12.dp,
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
                // User Bubble (Right)
                // Skew: \  (Leaning Left towards center)
                // Top-Left: (0, 0)
                // Top-Right: (bodyWidth - skewPx, 0)
                // Bottom-Right: (bodyWidth, bodyHeight)
                // Bottom-Left: (skewPx, bodyHeight)

                path.moveTo(0f, 0f)
                // Top Edge
                path.lineTo(bodyWidth - skewPx, 0f)

                // Right Edge (Vertical-ish but skewed? No, logic above is:
                // (bodyWidth - skewPx, 0) -> (bodyWidth, bodyHeight)
                // This is a slant /  wait.
                // x goes from (W-skew) to W. Increases.
                // y goes from 0 to H. Increases.
                // This is \ slant? No, \ is TopLeft to BottomRight.
                // This slants Right. /
                // Wait.
                // Top: x = W - skew. Bottom: x = W.
                // Bottom is further Right than Top. So it leans Right. /

                // Let's re-verify simple shapes.
                // | |  Normal
                // / /  Leans Right (Top is Left of Bottom)
                // \ \  Leans Left (Top is Right of Bottom)

                // My coordinates:
                // TopRight x: W - skew
                // BottomRight x: W
                // TopRight < BottomRight. Top is Left of Bottom.
                // So this is / slant.

                // Left Edge:
                // TopLeft x: 0
                // BottomLeft x: skew
                // Top < Bottom. Top is Left of Bottom.
                // This is / slant.

                // So this is / /  (Forward slant).
                // This implies motion to the Right.
                // If User is on Right, sending message? Maybe fine.

                path.lineTo(bodyWidth, bodyHeight)

                // TAIL - Lightning Bolt style connected to Bottom Right

                // 1. First jag down-right
                path.lineTo(bodyWidth + (tailWidthPx * 0.3f), bodyHeight + (tailHeightPx * 0.4f))

                // 2. Zag back left
                path.lineTo(bodyWidth - (tailWidthPx * 0.1f), bodyHeight + (tailHeightPx * 0.6f))

                // 3. Tip (Long jagged point)
                path.lineTo(bodyWidth + (tailWidthPx * 0.2f), size.height)

                // 4. Return back up to body
                // Connect back along the bottom edge, inwards
                path.lineTo(bodyWidth - tailWidthPx, bodyHeight)

                // Bottom Edge to Bottom Left
                path.lineTo(skewPx, bodyHeight)

                // Left Edge back to Top Left
                path.close()
            }

            BubbleTailAlignment.BottomLeft -> {
                // Character Bubble (Left)
                // Skew: \ \ (Backward slant?)
                // Let's try to make them symmetric.
                // If User was / /, Character should be \ \.
                // \ \ means Top is Right of Bottom.

                // Top-Left: (skewPx, 0)
                // Bottom-Left: (0, bodyHeight)
                // Top is Right of Bottom. \ slant.

                // Top-Right: (bodyWidth, 0)
                // Bottom-Right: (bodyWidth - skewPx, bodyHeight)
                // Top is Right of Bottom. \ slant.

                path.moveTo(skewPx, 0f)

                // Top Edge
                path.lineTo(bodyWidth, 0f)

                // Right Edge
                path.lineTo(bodyWidth - skewPx, bodyHeight)

                // Bottom Edge to Tail Start
                // We connect near the Left corner (0, bodyHeight)
                path.lineTo(tailWidthPx, bodyHeight)

                // TAIL - Lightning Bolt style connected to Bottom Left

                // 1. First jag down-left
                path.lineTo(tailWidthPx * 0.1f, bodyHeight + (tailHeightPx * 0.4f)) // Slightly out

                // 2. Zag back right
                path.lineTo(tailWidthPx * 0.4f, bodyHeight + (tailHeightPx * 0.6f))

                // 3. Tip
                path.lineTo(-tailWidthPx * 0.2f, size.height)

                // 4. Return back up to corner
                path.lineTo(0f, bodyHeight)

                // Left Edge back to Top Left
                path.close()
            }
        }
        return Outline.Generic(path)
    }
}
