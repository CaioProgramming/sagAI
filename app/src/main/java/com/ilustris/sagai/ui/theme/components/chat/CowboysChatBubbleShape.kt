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

/**
 * Cowboys-themed chat bubble shape inspired by a ticket stub.
 * Features:
 * - Rounded corners
 * - A "ticket clip" (semicircle cutout) on the side of the tail alignment
 */
class CowboysChatBubbleShape(
    private val cornerNotch: Dp = 8.dp,
    private val tailWidth: Dp = 12.dp,
    @Suppress("UNUSED_PARAMETER") private val tailHeight: Dp = 12.dp,
    private val tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
    private val isNarrator: Boolean = false,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val radius = with(density) { cornerNotch.toPx() }
        val clipSize = with(density) { tailWidth.toPx() }
        val path = Path()

        if (isNarrator) {
            // Double clip (Ticket style) for narrator
            path.moveTo(radius, 0f)
            path.lineTo(size.width - radius, 0f)
            path.quadraticTo(size.width, 0f, size.width, radius)

            val centerY = size.height / 2f

            // Right side clip
            path.lineTo(size.width, centerY - clipSize)
            path.arcTo(
                rect =
                    Rect(
                        left = size.width - clipSize,
                        top = centerY - clipSize,
                        right = size.width + clipSize,
                        bottom = centerY + clipSize,
                    ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false,
            )
            path.lineTo(size.width, size.height - radius)

            path.quadraticTo(size.width, size.height, size.width - radius, size.height)
            path.lineTo(radius, size.height)
            path.quadraticTo(0f, size.height, 0f, size.height - radius)

            // Left side clip
            path.lineTo(0f, centerY + clipSize)
            path.arcTo(
                rect =
                    Rect(
                        left = -clipSize,
                        top = centerY - clipSize,
                        right = clipSize,
                        bottom = centerY + clipSize,
                    ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false,
            )

            path.lineTo(0f, radius)
            path.quadraticTo(0f, 0f, radius, 0f)
            path.close()

            return Outline.Generic(path)
        }

        when (tailAlignment) {
            BubbleTailAlignment.BottomRight -> {
                // Clip on the Right side
                path.moveTo(radius, 0f)
                path.lineTo(size.width - radius, 0f)
                path.quadraticTo(size.width, 0f, size.width, radius)

                val centerY = size.height / 2f
                path.lineTo(size.width, centerY - clipSize)

                path.arcTo(
                    rect =
                        Rect(
                            left = size.width - clipSize,
                            top = centerY - clipSize,
                            right = size.width + clipSize,
                            bottom = centerY + clipSize,
                        ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false,
                )

                path.lineTo(size.width, size.height - radius)
                path.quadraticTo(size.width, size.height, size.width - radius, size.height)
                path.lineTo(radius, size.height)
                path.quadraticTo(0f, size.height, 0f, size.height - radius)
                path.lineTo(0f, radius)
                path.quadraticTo(0f, 0f, radius, 0f)
                path.close()
            }

            BubbleTailAlignment.BottomLeft -> {
                // Clip on the Left side
                path.moveTo(radius, 0f)
                path.lineTo(size.width - radius, 0f)
                path.quadraticTo(size.width, 0f, size.width, radius)
                path.lineTo(size.width, size.height - radius)
                path.quadraticTo(size.width, size.height, size.width - radius, size.height)
                path.lineTo(radius, size.height)
                path.quadraticTo(0f, size.height, 0f, size.height - radius)

                val centerY = size.height / 2f
                path.lineTo(0f, centerY + clipSize)

                path.arcTo(
                    rect =
                        Rect(
                            left = -clipSize,
                            top = centerY - clipSize,
                            right = clipSize,
                            bottom = centerY + clipSize,
                        ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false,
                )

                path.lineTo(0f, radius)
                path.quadraticTo(0f, 0f, radius, 0f)
                path.close()
            }
        }
        return Outline.Generic(path)
    }
}
