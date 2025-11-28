package com.ilustris.sagai.ui.theme.components.chat

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class ThoughtBubbleShape(
    private val cornerRadius: Dp,
    private val tailAlignment: BubbleTailAlignment,
    private val tailWidth: Dp = 10.dp,
    private val tailHeight: Dp = 10.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val tailWidthPx = with(density) { tailWidth.toPx() }
        val tailHeightPx = with(density) { tailHeight.toPx() }

        val path = Path().apply {
            val roundRect = RoundRect(
                rect = Rect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height - tailHeightPx
                ),
                cornerRadius = CornerRadius(cornerRadiusPx)
            )
            addRoundRect(roundRect)

            val tailStartX = when (tailAlignment) {
                BubbleTailAlignment.BottomLeft -> roundRect.left + cornerRadiusPx
                BubbleTailAlignment.BottomRight -> roundRect.right - cornerRadiusPx - tailWidthPx
            }

            moveTo(tailStartX, roundRect.bottom)
            lineTo(tailStartX + tailWidthPx / 2, roundRect.bottom + tailHeightPx)
            lineTo(tailStartX + tailWidthPx, roundRect.bottom)
            close()
        }
        return Outline.Generic(path)
    }
}
