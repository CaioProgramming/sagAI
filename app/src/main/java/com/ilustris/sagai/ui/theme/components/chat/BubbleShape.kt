package com.ilustris.sagai.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

enum class BubbleTailAlignment {
    BottomLeft, BottomRight,
}

class CurvedChatBubbleShape(
    private val cornerRadius: Dp,
    private val tailWidth: Dp,
    private val tailHeight: Dp,
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

        val bubbleBodyWidth = size.width - tailWidthPx
        val bubbleBodyHeight = size.height - tailHeightPx

        val path = Path().apply {
            when (tailAlignment) {
                BubbleTailAlignment.BottomRight -> {
                    reset()
                    // Top-left corner
                    arcTo(Rect(0f, 0f, 2 * cornerRadiusPx, 2 * cornerRadiusPx), 180f, 90f, false)
                    lineTo(bubbleBodyWidth - cornerRadiusPx, 0f) // Top edge

                    // Top-right corner
                    arcTo(Rect(bubbleBodyWidth - 2 * cornerRadiusPx, 0f, bubbleBodyWidth, 2 * cornerRadiusPx), 270f, 90f, false)
                    // Right edge leading to tail
                    lineTo(bubbleBodyWidth, bubbleBodyHeight - cornerRadiusPx) // End of right edge curve

                    // --- BottomRight Tail ---
                    // Current point: (bubbleBodyWidth, bubbleBodyHeight - cornerRadiusPx)
                    // 1. Curve from right edge towards tail tip area (first part of "S")
                    val control1X = bubbleBodyWidth // Control point aligned with right edge
                    val control1Y = bubbleBodyHeight // Pulls curve down
                    val midPointX = bubbleBodyWidth + tailWidthPx * 0.4f // X projection of curve mid-point
                    val midPointY = bubbleBodyHeight + tailHeightPx * 0.3f // Y projection of curve mid-point (less than full tail height)
                    quadraticBezierTo(control1X, control1Y, midPointX, midPointY)

                    // 2. Curve from mid-point to the tail tip
                    val tipX = size.width
                    val tipY = size.height
                    // Control point for the curve to the tip.
                    // Adjust these to shape the "pointiness" and curve into the tip.
                    val control2X = bubbleBodyWidth + tailWidthPx * 0.7f // Further out towards tip
                    val control2Y = size.height - tailHeightPx * 0.1f // Slightly above tip base, for a gentle curve in
                    quadraticBezierTo(control2X, control2Y, tipX, tipY)

                    // 3. Curve from tail tip back to bottom edge of bubble (second part of "S")
                    val connectToBottomX = bubbleBodyWidth - cornerRadiusPx // Where tail meets bottom edge
                    // Control point for curve from tip to bottom edge.
                    // Adjust to shape how the tail "scoops" back.
                    val control3X = bubbleBodyWidth - tailWidthPx * 0.2f // Pulls curve inwards from right
                    val control3Y = bubbleBodyHeight + tailHeightPx * 0.8f // Lower down, shaping the curve
                    quadraticBezierTo(control3X, control3Y, connectToBottomX, bubbleBodyHeight)

                    lineTo(cornerRadiusPx, bubbleBodyHeight) // Bottom edge

                    // Bottom-left corner
                    arcTo(Rect(0f, bubbleBodyHeight - 2 * cornerRadiusPx, 2 * cornerRadiusPx, bubbleBodyHeight), 90f, 90f, false)
                    close() // Connects to top-left arc start
                }

                BubbleTailAlignment.BottomLeft -> {
                    val bubbleBodyStartX = tailWidthPx // Bubble body starts after tail width
                    reset()

                    // Top-left corner (main body)
                    arcTo(Rect(bubbleBodyStartX, 0f, bubbleBodyStartX + 2 * cornerRadiusPx, 2 * cornerRadiusPx), 180f, 90f, false)
                    lineTo(size.width - cornerRadiusPx, 0f) // Top edge

                    // Top-right corner
                    arcTo(Rect(size.width - 2 * cornerRadiusPx, 0f, size.width, 2 * cornerRadiusPx), 270f, 90f, false)
                    lineTo(size.width, bubbleBodyHeight - cornerRadiusPx) // Right edge

                    // Bottom-right corner
                    arcTo(Rect(size.width - 2 * cornerRadiusPx, bubbleBodyHeight - 2 * cornerRadiusPx, size.width, bubbleBodyHeight), 0f, 90f, false)
                    // Bottom edge leading to tail
                    lineTo(bubbleBodyStartX + cornerRadiusPx, bubbleBodyHeight) // End of bottom edge curve

                    // --- BottomLeft Tail ---
                    // Current point: (bubbleBodyStartX + cornerRadiusPx, bubbleBodyHeight)
                    // 1. Curve from bottom edge towards tail tip area (first part of "S", mirrored)
                    val control1X = bubbleBodyStartX // Control point aligned with bottom edge connection
                    val control1Y = bubbleBodyHeight // Pulls curve left
                    val midPointX = bubbleBodyStartX - tailWidthPx * .4f // X projection (mirrored)
                    val midPointY = bubbleBodyHeight + tailHeightPx * .65f // Y projection (same as right)
                   // quadraticBezierTo(control1X, control1Y, midPointX, midPointY)

                    // 2. Curve from mid-point to the tail tip
                    val tipX = 0f // Tip is at the far left
                    val tipY = size.height
                    val control2X = bubbleBodyStartX - tailWidthPx * 0.7f // Further out towards tip (mirrored)
                    val control2Y = size.height - tailHeightPx * 0.1f // Slightly above tip base (same as right)
                    quadraticBezierTo(control2X, control2Y, tipX, tipY)

                    // 3. Curve from tail tip back to left edge of bubble (second part of "S", mirrored)
                    val connectToLeftY = bubbleBodyHeight - cornerRadiusPx // Where tail meets left edge
                    // Control point for curve from tip to left edge.
                    val control3X = bubbleBodyStartX + tailWidthPx * 0.2f // Pulls curve inwards from left (mirrored from BR's control3X logic)
                    val control3Y = bubbleBodyHeight + tailHeightPx * 0.8f // Lower down (same as right)
                    quadraticBezierTo(control3X, control3Y, bubbleBodyStartX, connectToLeftY)

                    lineTo(bubbleBodyStartX, cornerRadiusPx) // Left edge
                    close() // Connects to top-left arc start
                }
            }
        }
        return Outline.Generic(path)
    }
}

// --- Preview Example (using the new CurvedChatBubbleShape) ---
// The preview Composable ("CurvedChatBubbleItem" and "CurvedChatBubblePreview")
// from your previous message can be reused.

@Composable
fun CurvedChatBubbleItem(
    text: String,
    backgroundColor: Color,
    tailAlignment: BubbleTailAlignment,
    modifier: Modifier = Modifier,
) {
    val tailWidth = 12.dp
    val tailHeight = 12.dp

    Box(
        modifier = modifier
            .wrapContentSize()
            .clip(
                CurvedChatBubbleShape(
                    cornerRadius = 16.dp,
                    tailWidth = tailWidth,
                    tailHeight = tailHeight,
                    tailAlignment = tailAlignment
                )
            )
            .background(backgroundColor)
            .padding( // Inner padding for the text content
                top = 8.dp,
                bottom = 8.dp + tailHeight, // Account for tail height
                start = 12.dp + if (tailAlignment == BubbleTailAlignment.BottomLeft) tailWidth else 0.dp,
                end = 12.dp + if (tailAlignment == BubbleTailAlignment.BottomRight) tailWidth else 0.dp
            )
    ) {
        Text(text = text, color = if (backgroundColor == Color.LightGray) Color.Black else Color.White)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CurvedChatBubblePreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        CurvedChatBubbleItem(
            text = "Hello! This is a right bubble with a curved tail.",
            backgroundColor = Color(0xFF007AFF),
            tailAlignment = BubbleTailAlignment.BottomRight
        )
        Spacer(Modifier.height(10.dp))
        CurvedChatBubbleItem(
            text = "Hi! This is a left bubble. It's longer and also curved.",
            backgroundColor = Color.LightGray,
            tailAlignment = BubbleTailAlignment.BottomLeft
        )
        Spacer(Modifier.height(10.dp))
        CurvedChatBubbleItem(
            text = "Short",
            backgroundColor = Color(0xFF34C759),
            tailAlignment = BubbleTailAlignment.BottomRight
        )
        Spacer(Modifier.height(10.dp))
        CurvedChatBubbleItem(
            text = "Another right one for testing consistency.",
            backgroundColor = Color(0xFF5856D6), // Purple
            tailAlignment = BubbleTailAlignment.BottomRight
        )
    }
}