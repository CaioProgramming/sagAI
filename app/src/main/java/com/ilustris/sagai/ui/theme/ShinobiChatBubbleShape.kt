package com.ilustris.sagai.ui.theme

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
    private val cornerRadius: Dp = 12.dp,
    private val tailWidth: Dp = 12.dp,
    private val tailHeight: Dp = 8.dp, // Increased slightly from 6dp to avoid crushing, but kept small
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
        val bumpHeightPx = with(density) { 6.dp.toPx() }

        val path = Path()
        val w = size.width
        val h = size.height

        val topBodyY = bumpHeightPx
        val bottomBodyY = h - tailHeightPx

        when (tailAlignment) {
            BubbleTailAlignment.BottomRight -> {
                // User Bubble (Right aligned)

                path.moveTo(0f, topBodyY + cornerRadiusPx)

                // Top-Left
                path.arcTo(
                    rect = Rect(0f, topBodyY, 2 * cornerRadiusPx, topBodyY + 2 * cornerRadiusPx),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                // Top Bumps
                val topEdgeWidth = w - 2 * cornerRadiusPx
                val bumpWidth = topEdgeWidth / 3f
                for (i in 0 until 3) {
                    val startX = cornerRadiusPx + i * bumpWidth
                    val endX = cornerRadiusPx + (i + 1) * bumpWidth
                    val midX = (startX + endX) / 2f
                    path.quadraticBezierTo(midX, topBodyY - bumpHeightPx, endX, topBodyY)
                }

                // Top-Right
                path.arcTo(
                    rect = Rect(w - 2 * cornerRadiusPx, topBodyY, w, topBodyY + 2 * cornerRadiusPx),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                // Right Edge
                // Start tail higher up to create a smoother, less crushed transition
                val tailStartY = bottomBodyY - tailHeightPx
                path.lineTo(w, tailStartY)

                // --- INTEGRATED TAIL ---
                // Curves from the side (tailStartY) down and out to the tip.
                path.cubicTo(
                    w,
                    bottomBodyY, // Control 1: Continue down the side
                    w + tailWidthPx * 0.4f,
                    h - tailHeightPx * 0.2f, // Control 2: Curve out gently
                    w + tailWidthPx,
                    h, // Tip
                )

                // Return to bottom
                path.quadraticBezierTo(
                    w + tailWidthPx * 0.2f,
                    h,
                    w - tailWidthPx * 0.5f,
                    bottomBodyY,
                )

                // Bottom Bumps
                val bottomEdgeStart = w - tailWidthPx * 0.5f
                val bottomEdgeEnd = cornerRadiusPx
                val bottomEdgeWidth = bottomEdgeStart - bottomEdgeEnd

                if (bottomEdgeWidth > 0) {
                    val bottomBumpWidth = bottomEdgeWidth / 3f
                    for (i in 0 until 3) {
                        val startX = bottomEdgeStart - i * bottomBumpWidth
                        val endX = bottomEdgeStart - (i + 1) * bottomBumpWidth
                        val midX = (startX + endX) / 2f
                        path.quadraticBezierTo(midX, bottomBodyY + bumpHeightPx, endX, bottomBodyY)
                    }
                } else {
                    path.lineTo(cornerRadiusPx, bottomBodyY)
                }

                // Bottom-Left
                path.arcTo(
                    rect =
                        Rect(
                            0f,
                            bottomBodyY - 2 * cornerRadiusPx,
                            2 * cornerRadiusPx,
                            bottomBodyY,
                        ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                // Left Edge
                path.lineTo(0f, topBodyY + cornerRadiusPx)

                path.close()
            }

            BubbleTailAlignment.BottomLeft -> {
                // NPC Bubble (Left aligned)

                path.moveTo(0f, topBodyY + cornerRadiusPx)

                // Top-Left
                path.arcTo(
                    rect = Rect(0f, topBodyY, 2 * cornerRadiusPx, topBodyY + 2 * cornerRadiusPx),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                // Top Bumps
                val topEdgeWidth = w - 2 * cornerRadiusPx
                val bumpWidth = topEdgeWidth / 3f
                for (i in 0 until 3) {
                    val startX = cornerRadiusPx + i * bumpWidth
                    val endX = cornerRadiusPx + (i + 1) * bumpWidth
                    val midX = (startX + endX) / 2f
                    path.quadraticBezierTo(midX, topBodyY - bumpHeightPx, endX, topBodyY)
                }

                // Top-Right
                path.arcTo(
                    rect = Rect(w - 2 * cornerRadiusPx, topBodyY, w, topBodyY + 2 * cornerRadiusPx),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                // Right Edge
                path.lineTo(w, bottomBodyY - cornerRadiusPx)

                // Bottom-Right
                path.arcTo(
                    rect =
                        Rect(
                            w - 2 * cornerRadiusPx,
                            bottomBodyY - 2 * cornerRadiusPx,
                            w,
                            bottomBodyY,
                        ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                // Bottom Bumps
                val bottomEdgeStart = w - cornerRadiusPx
                val bottomEdgeEnd = tailWidthPx
                val bottomEdgeWidth = bottomEdgeStart - bottomEdgeEnd

                if (bottomEdgeWidth > 0) {
                    val bottomBumpWidth = bottomEdgeWidth / 3f
                    for (i in 0 until 3) {
                        val startX = bottomEdgeStart - i * bottomBumpWidth
                        val endX = bottomEdgeStart - (i + 1) * bottomBumpWidth
                        val midX = (startX + endX) / 2f
                        path.quadraticBezierTo(midX, bottomBodyY + bumpHeightPx, endX, bottomBodyY)
                    }
                } else {
                    path.lineTo(tailWidthPx, bottomBodyY)
                }

                // --- INTEGRATED TAIL (Left) ---
                // Return (Bottom of tail)
                path.quadraticBezierTo(
                    -tailWidthPx * 0.2f,
                    h,
                    -tailWidthPx,
                    h,
                )

                // Top of tail (Tip to Side)
                val tailStartY = bottomBodyY - tailHeightPx
                path.cubicTo(
                    -tailWidthPx * 0.4f,
                    h - tailHeightPx * 0.2f, // Control 2
                    0f,
                    bottomBodyY, // Control 1
                    0f,
                    tailStartY, // End at side (higher up)
                )

                // Left Edge
                path.lineTo(0f, topBodyY + cornerRadiusPx)

                path.close()
            }
        }
        return Outline.Generic(path)
    }
}
