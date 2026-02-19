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
import kotlin.math.max

class ShinobiChatBubbleShape(
    private val cornerRadius: Dp = 12.dp,
    private val tailWidth: Dp = 12.dp,
    private val tailHeight: Dp = 12.dp,
    private val tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val tailWidthPx = with(density) { tailWidth.toPx() }
        val tailHeightPx = with(density) { tailHeight.toPx() }
        val bumpDepthPx = with(density) { 8.dp.toPx() }
        val minBumpSize = with(density) { 30.dp.toPx() }

        val path = Path()
        val w = size.width
        val h = size.height
        val hasTail = tailWidthPx > 0f

        val isTailRight = tailAlignment == BubbleTailAlignment.BottomRight
        val isTailLeft = tailAlignment == BubbleTailAlignment.BottomLeft

        // Define Box Bounds (Tail indents the box)
        // With Side Bumps, we need horizontal inset on BOTH sides.
        val leftX = bumpDepthPx + if (hasTail && isTailLeft) tailWidthPx else 0f
        val rightX = w - (bumpDepthPx + if (hasTail && isTailRight) tailWidthPx else 0f)

        val topY = bumpDepthPx
        val bottomY = h - max(bumpDepthPx, if (hasTail) tailHeightPx else 0f)

        // Helper to draw bumps along a line segment
        fun drawBumps(
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
            numBumps: Int,
            bulgeX: Float,
            bulgeY: Float, // Direction to bulge (-1, 0, 1)
        ) {
            val dx = (x2 - x1) / numBumps
            val dy = (y2 - y1) / numBumps

            for (i in 0 until numBumps) {
                val startX = x1 + i * dx
                val startY = y1 + i * dy
                val endX = startX + dx
                val endY = startY + dy

                // Control points for round bump
                // Shift perpendicular to the line segment
                // Perpendicular vector for (dx, dy) is (-dy, dx) or (dy, -dx)
                // We utilize simple bulgeX/Y direction logic for axis-aligned segments

                val ctrlOffX = bulgeX * bumpDepthPx * 1.5f
                val ctrlOffY = bulgeY * bumpDepthPx * 1.5f

                path.cubicTo(
                    startX + dx * 0.2f + ctrlOffX,
                    startY + dy * 0.2f + ctrlOffY,
                    endX - dx * 0.2f + ctrlOffX,
                    endY - dy * 0.2f + ctrlOffY,
                    endX,
                    endY,
                )
            }
        }

        // Start Top-Left
        path.moveTo(leftX, topY + cornerRadiusPx)

        // Top-Left Corner
        path.arcTo(
            Rect(leftX, topY, leftX + 2 * cornerRadiusPx, topY + 2 * cornerRadiusPx),
            180f,
            90f,
            false,
        )

        // Top Edge (Bulge UP)
        drawBumps(
            leftX + cornerRadiusPx,
            topY,
            rightX - cornerRadiusPx,
            topY,
            3,
            0f,
            -1f,
        )

        // Top-Right Corner
        path.arcTo(
            Rect(rightX - 2 * cornerRadiusPx, topY, rightX, topY + 2 * cornerRadiusPx),
            270f,
            90f,
            false,
        )

        // Right Edge (Bulge RIGHT)
        // Calculate dynamic bumps based on height
        val sideH = (bottomY - cornerRadiusPx) - (topY + cornerRadiusPx)
        val sideBumpsFn = max(1, (sideH / minBumpSize).toInt())

        if (sideH > 0) {
            drawBumps(
                rightX,
                topY + cornerRadiusPx,
                rightX,
                bottomY - cornerRadiusPx,
                sideBumpsFn,
                1f,
                0f,
            )
        } else {
            path.lineTo(rightX, bottomY - cornerRadiusPx)
        }

        // Bottom-Right Corner / Tail
        if (hasTail && isTailRight) {
            // Tail Logic
            // Draw connecting line if needed
            path.lineTo(rightX, bottomY)

            // Akatsuki Tail
            path.cubicTo(
                rightX + tailWidthPx * 0.8f,
                bottomY + tailHeightPx * 0.2f,
                w,
                bottomY + tailHeightPx * 0.5f,
                w,
                h,
            )
            path.cubicTo(
                w - tailWidthPx * 0.5f,
                h,
                rightX - tailWidthPx * 0.2f,
                h - tailHeightPx * 0.2f,
                rightX - tailWidthPx,
                bottomY,
            )
        } else {
            path.arcTo(
                Rect(rightX - 2 * cornerRadiusPx, bottomY - 2 * cornerRadiusPx, rightX, bottomY),
                0f,
                90f,
                false,
            )
        }

        // Bottom Edge (Bulge DOWN)
        val bottomStartX =
            if (hasTail && isTailRight) rightX - tailWidthPx else rightX - cornerRadiusPx
        val bottomEndX = if (hasTail && isTailLeft) leftX + tailWidthPx else leftX + cornerRadiusPx

        drawBumps(
            bottomStartX,
            bottomY,
            bottomEndX,
            bottomY,
            3,
            0f,
            1f,
        )

        // Bottom-Left Corner / Tail
        if (hasTail && isTailLeft) {
            // Tail Logic (Left)
            path.lineTo(leftX + tailWidthPx, bottomY)

            path.cubicTo(
                leftX + tailWidthPx * 0.2f,
                h - tailHeightPx * 0.2f,
                leftX + tailWidthPx * 0.5f,
                h,
                0f,
                h,
            )
            path.cubicTo(
                0f,
                bottomY + tailHeightPx * 0.5f,
                leftX - tailWidthPx * 0.8f,
                bottomY + tailHeightPx * 0.2f,
                leftX,
                bottomY,
            )
            path.lineTo(leftX, bottomY - cornerRadiusPx)
        } else {
            path.arcTo(
                Rect(leftX, bottomY - 2 * cornerRadiusPx, leftX + 2 * cornerRadiusPx, bottomY),
                90f,
                90f,
                false,
            )
        }

        // Left Edge (Bulge LEFT)
        if (sideH > 0) {
            drawBumps(
                leftX,
                bottomY - cornerRadiusPx,
                leftX,
                topY + cornerRadiusPx,
                sideBumpsFn,
                -1f,
                0f,
            )
        } else {
            path.lineTo(leftX, topY + cornerRadiusPx)
        }

        path.close()

        return Outline.Generic(path)
    }
}
