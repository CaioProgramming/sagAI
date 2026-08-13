package com.ilustris.sagai.ui.theme.components.chat

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * A classic comic-book speech balloon: a rounded-rect body with a straight-edged, pointed tail —
 * not the smooth "S-curve" tail [CurvedChatBubbleShape] uses, and not a scalloped "thought bubble"
 * (deliberately avoided — reads as cartoonish, not "bold Alex Ross / Jim Lee"). Replaces the
 * skewed-parallelogram [HeroesChatBubbleShape] entirely rather than decorating it.
 */
class HeroesSpeechBalloonShape(
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
        val r = with(density) { cornerRadius.toPx() }
        val tailW = with(density) { tailWidth.toPx() }
        val tailH = with(density) { tailHeight.toPx() }
        val w = size.width
        val bodyHeight = size.height - tailH

        val path =
            Path().apply {
                when (tailAlignment) {
                    BubbleTailAlignment.BottomRight -> {
                        moveTo(r, 0f)
                        lineTo(w - r, 0f)
                        arcTo(Rect(w - 2 * r, 0f, w, 2 * r), -90f, 90f, false)
                        lineTo(w, bodyHeight - r)
                        arcTo(Rect(w - 2 * r, bodyHeight - 2 * r, w, bodyHeight), 0f, 90f, false)

                        // Straight-edged pointed tail, offset toward the near corner.
                        lineTo(w - tailW * 0.3f, bodyHeight)
                        lineTo(w - tailW * 0.9f, size.height)
                        lineTo(w - tailW * 1.6f, bodyHeight)

                        lineTo(r, bodyHeight)
                        arcTo(Rect(0f, bodyHeight - 2 * r, 2 * r, bodyHeight), 90f, 90f, false)
                        lineTo(0f, r)
                        arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
                        close()
                    }

                    BubbleTailAlignment.BottomLeft -> {
                        moveTo(r, 0f)
                        lineTo(w - r, 0f)
                        arcTo(Rect(w - 2 * r, 0f, w, 2 * r), -90f, 90f, false)
                        lineTo(w, bodyHeight - r)
                        arcTo(Rect(w - 2 * r, bodyHeight - 2 * r, w, bodyHeight), 0f, 90f, false)
                        lineTo(r, bodyHeight)

                        // Straight-edged pointed tail, mirrored toward the near (left) corner.
                        lineTo(tailW * 1.6f, bodyHeight)
                        lineTo(tailW * 0.9f, size.height)
                        lineTo(tailW * 0.3f, bodyHeight)

                        arcTo(Rect(0f, bodyHeight - 2 * r, 2 * r, bodyHeight), 90f, 90f, false)
                        lineTo(0f, r)
                        arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
                        close()
                    }
                }
            }
        return Outline.Generic(path)
    }
}
