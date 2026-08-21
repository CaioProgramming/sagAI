package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.abs

/** Blocks land in a stable pseudo-random order — same grid, same sequence, every run. */
private fun blockThreshold(
    column: Int,
    row: Int,
    seed: Int,
): Float {
    val hash = (column * 73856093) xor (row * 19349663) xor (seed * 83492791)
    return (abs(hash) % 1000) / 1000f
}

/**
 * Covers its content in opaque blocks that clear away as [progress] runs 0..1, so the image
 * arrives in pieces rather than being wiped in.
 *
 * A modifier rather than a Canvas laid over the content: as an overlay it had to be given the same
 * size as the thing it hid and kept in sync with it by hand, which is why both the boot screen and
 * the decode page carried their own copy of the same forty lines. Here the effect travels with
 * whatever it is applied to.
 *
 * Each cell clears at its own threshold, drawn from a hash of its coordinates, so the order looks
 * scattered while staying identical across recompositions. A single sweeping edge reads as a
 * window shade; blocks resolving out of order read as data arriving.
 *
 * [edgeColor] tints the cells that are on the verge of clearing, which is what keeps the frontier
 * legible instead of letting blocks blink out silently.
 */
fun Modifier.blockDecode(
    progress: Float,
    blockSize: Dp = 18.dp,
    cover: Color = Color.Black,
    edgeColor: Color? = null,
    seed: Int = 0,
) = drawWithContent {
    drawContent()

    if (progress >= 1f) return@drawWithContent

    val block = blockSize.toPx().coerceAtLeast(1f)
    val columns = ceil(size.width / block).toInt()
    val rows = ceil(size.height / block).toInt()

    for (column in 0 until columns) {
        for (row in 0 until rows) {
            val threshold = blockThreshold(column, row, seed)
            if (threshold <= progress) continue

            val topLeft = Offset(column * block, row * block)
            val cellSize =
                Size(
                    width = minOf(block, size.width - topLeft.x),
                    height = minOf(block, size.height - topLeft.y),
                )

            drawRect(color = cover, topLeft = topLeft, size = cellSize)

            // The band of cells about to go: a hint of the frontier moving through the grid.
            if (edgeColor != null && threshold - progress < 0.08f) {
                drawRect(color = edgeColor, topLeft = topLeft, size = cellSize)
            }
        }
    }
}
