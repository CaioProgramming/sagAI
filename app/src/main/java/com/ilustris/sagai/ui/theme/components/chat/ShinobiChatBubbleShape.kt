package com.ilustris.sagai.ui.theme.components.chat

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class ShinobiChatBubbleShape(
    private val cornerRadius: Dp = 16.dp,
    private val tailWidth: Dp = 12.dp,
    private val tailHeight: Dp = 12.dp,
    private val tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        val w = size.width
        val h = size.height

        // Define radius. For a pill look, we aim for h/2, capped to avoid overly massive arcs on tall bubbles.
        val maxR = with(density) { 32.dp.toPx() }
        val r = (h / 2f).coerceAtMost(maxR).coerceAtMost(w / 2f)

        val tw = with(density) { tailWidth.toPx() }
        val th = with(density) { tailHeight.toPx() }
        val hasTail = tw > 0f

        if (tailAlignment == BubbleTailAlignment.BottomRight) {
            // Semicircle (or large curve) on the LEFT
            path.moveTo(r, 0f)

            // Top Edge
            path.lineTo(w - r, 0f)

            // Top-Right Corner
            path.arcTo(Rect(w - 2 * r, 0f, w, 2 * r), 270f, 90f, false)

            // Right Side and/or Tail
            if (hasTail) {
                // Line down to start of flick
                path.lineTo(w, h - th)
                // Flick out to tip
                // Control points to create the "beak" from the image
                path.cubicTo(
                    w + tw * 0.2f,
                    h - th * 0.5f,
                    w + tw,
                    h - th * 0.2f,
                    w + tw,
                    h,
                )
                // Return to bottom edge
                path.quadraticBezierTo(w, h, w - r, h)
            } else {
                path.arcTo(Rect(w - 2 * r, h - 2 * r, w, h), 0f, 90f, false)
            }

            // Bottom Edge
            path.lineTo(r, h)

            // Left Side Semicircle (spanning top to bottom)
            path.arcTo(Rect(0f, 0f, 2 * r, h), 90f, 180f, false)

            path.close()
        } else {
            // Semicircle on the RIGHT
            path.moveTo(w - r, h)

            // Bottom Edge
            path.lineTo(r, h)

            // Left Side and/or Tail
            if (hasTail) {
                // Return to side (flick tip at -tw, h)
                path.quadraticBezierTo(0f, h, -tw, h)
                path.cubicTo(
                    -tw,
                    h - th * 0.2f,
                    -tw * 0.2f,
                    h - th * 0.5f,
                    0f,
                    h - th,
                )
                // Line up to top-left corner start
                path.lineTo(0f, r)
            } else {
                path.arcTo(Rect(0f, h - 2 * r, 2 * r, h), 90f, 90f, false)
                path.lineTo(0f, r)
            }

            // Top-Left Corner
            path.arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)

            // Top Edge
            path.lineTo(w - r, 0f)

            // Right Side Semicircle
            path.arcTo(Rect(w - 2 * r, 0f, w, h), 270f, 180f, false)

            path.close()
        }

        return Outline.Generic(path)
    }
}
