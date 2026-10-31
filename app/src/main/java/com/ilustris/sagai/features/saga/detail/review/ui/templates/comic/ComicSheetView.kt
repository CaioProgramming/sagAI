package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.ui.genre.comic.ComicPanel
import com.ilustris.sagai.ui.genre.comic.LocalComicFrameDelay
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * A page is laid out on a canvas this many times the screen and scaled back down. The panels'
 * type and balloons were sized for a board wider than the screen, seen from a distance; at 1:1
 * on a phone-width page the same type would swamp the frames it sits in. One number to tune the
 * density of the whole template.
 */
private const val SHEET_CANVAS_FACTOR = 1.3f

private val SHEET_GUTTER = 5.dp
private val SHEET_MARGIN = 10.dp

/** Caption strips take this share of the page each. */
private const val STRIP_FRACTION = 0.19f

/** A cast lead takes this share of its page; its companions share the rest. */
private const val LEAD_FRACTION = 0.52f

/** Grid cells are portrait, but not stretched past this when a page holds few of them. */
private const val GRID_CELL_MAX_ASPECT = 1.5f

/** Frames on one page fill in reading order, this far apart. */
private const val FRAME_STAGGER_MS = 350

internal data class SheetRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
)

/**
 * Where each of a sheet's panels sits, in the sheet's own pixels: caption strips first, then the
 * body, in the same order as `sheet.strips + sheet.panels`. Pure, so the geometry can be checked
 * without drawing anything.
 */
internal fun sheetRects(
    sheet: ComicSheet,
    width: Int,
    height: Int,
    gutter: Int,
    margin: Int,
    topInset: Int,
    bottomInset: Int,
): List<SheetRect> {
    if (sheet.body == SheetBody.COVER) {
        return List(sheet.panels.size) { SheetRect(0, 0, width, height) }
    }

    val left = margin
    val areaWidth = width - margin * 2
    var top = topInset
    val areaHeight = height - topInset - bottomInset
    val rects = mutableListOf<SheetRect>()

    val stripCount = sheet.strips.size
    if (sheet.body == SheetBody.TEXT) {
        val rowHeight = (areaHeight - gutter * (stripCount - 1)) / stripCount.coerceAtLeast(1)
        repeat(stripCount) {
            rects += SheetRect(left, top, areaWidth, rowHeight)
            top += rowHeight + gutter
        }
        return rects
    }

    val stripHeight = (areaHeight * STRIP_FRACTION).roundToInt()
    repeat(stripCount) {
        rects += SheetRect(left, top, areaWidth, stripHeight)
        top += stripHeight + gutter
    }
    val bodyHeight = height - bottomInset - top
    val count = sheet.panels.size

    /** One row of frames splitting [areaWidth] by [fractions]. */
    fun row(
        rowTop: Int,
        rowHeight: Int,
        fractions: List<Float>,
    ): List<SheetRect> {
        val usable = areaWidth - gutter * (fractions.size - 1)
        var x = left
        return fractions.map { fraction ->
            val w = (usable * fraction).roundToInt()
            SheetRect(x, rowTop, w, rowHeight).also { x += w + gutter }
        }
    }

    when (sheet.body) {
        SheetBody.SINGLE -> rects += SheetRect(left, top, areaWidth, bodyHeight)

        SheetBody.LEAD -> {
            val leadHeight = (bodyHeight * LEAD_FRACTION).roundToInt()
            rects += SheetRect(left, top, areaWidth, leadHeight)
            val companions = count - 1
            rects +=
                row(
                    top + leadHeight + gutter,
                    bodyHeight - leadHeight - gutter,
                    List(companions) { 1f / companions },
                )
        }

        SheetBody.MOSAIC ->
            when (count) {
                1 -> rects += SheetRect(left, top, areaWidth, bodyHeight)
                2 -> {
                    val h = (bodyHeight - gutter) / 2
                    rects += row(top, h, listOf(1f))
                    rects += row(top + h + gutter, h, listOf(1f))
                }
                3 -> {
                    val topRow = (bodyHeight * 0.42f).roundToInt()
                    rects += row(top, topRow, listOf(1f))
                    rects += row(top + topRow + gutter, bodyHeight - topRow - gutter, listOf(0.55f, 0.45f))
                }
                else -> {
                    val h = (bodyHeight - gutter) / 2
                    rects += row(top, h, listOf(0.58f, 0.42f))
                    rects += row(top + h + gutter, h, listOf(0.42f, 0.58f))
                }
            }

        SheetBody.GRID -> {
            val columns =
                when {
                    count == 1 -> 1
                    count <= 4 -> 2
                    count <= 9 -> 3
                    else -> 4
                }
            val rows = ceil(count / columns.toFloat()).toInt()
            val cellWidth = (areaWidth - gutter * (columns - 1)) / columns
            val cellHeight =
                minOf(
                    (bodyHeight - gutter * (rows - 1)) / rows,
                    (cellWidth * GRID_CELL_MAX_ASPECT).roundToInt(),
                )
            val gridHeight = rows * cellHeight + (rows - 1) * gutter
            val gridTop = top + (bodyHeight - gridHeight) / 2
            repeat(rows) { r ->
                val inRow = minOf(columns, count - r * columns)
                // A short last row sits centred under the full ones rather than hugging the left.
                val rowLeft = left + ((columns - inRow) * (cellWidth + gutter)) / 2
                repeat(inRow) { c ->
                    rects +=
                        SheetRect(
                            rowLeft + c * (cellWidth + gutter),
                            gridTop + r * (cellHeight + gutter),
                            cellWidth,
                            cellHeight,
                        )
                }
            }
        }

        SheetBody.COVER, SheetBody.TEXT -> Unit
    }
    return rects
}

/**
 * One printed page: its caption strips and framed panels placed by [sheetRects], with every
 * panel's balloons laid over the lot so they can break out past the borders they hang from.
 *
 * Balloons are placed here, in one measure policy, rather than inside their panel — a balloon
 * that is a child of its frame is clipped by it, and breaking the border is the whole point.
 */
@Composable
internal fun ComicSheetView(
    sheet: ComicSheet,
    pages: List<ReviewPage>,
    borderColor: Color,
    panelBackground: Color,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
    onAction: (ReviewAction) -> Unit,
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val realWidth = constraints.maxWidth
        val realHeight = constraints.maxHeight
        val canvasWidth = (realWidth * SHEET_CANVAS_FACTOR).roundToInt()
        val canvasHeight = (realHeight * SHEET_CANVAS_FACTOR).roundToInt()

        val rects =
            remember(sheet, canvasWidth, canvasHeight, density, topInset, bottomInset) {
                with(density) {
                    sheetRects(
                        sheet = sheet,
                        width = canvasWidth,
                        height = canvasHeight,
                        gutter = SHEET_GUTTER.roundToPx(),
                        margin = SHEET_MARGIN.roundToPx(),
                        topInset = (topInset.toPx() * SHEET_CANVAS_FACTOR).roundToInt(),
                        bottomInset = (bottomInset.toPx() * SHEET_CANVAS_FACTOR).roundToInt(),
                    )
                }
            }
        val slots = remember(sheet) { sheet.strips + sheet.panels }

        Box(
            Modifier.layout { measurable, incoming ->
                val placeable = measurable.measure(Constraints.fixed(canvasWidth, canvasHeight))
                layout(incoming.maxWidth, incoming.maxHeight) {
                    placeable.placeWithLayer(0, 0) {
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = 1f / SHEET_CANVAS_FACTOR
                        scaleY = 1f / SHEET_CANVAS_FACTOR
                    }
                }
            },
        ) {
            Layout(
                modifier = Modifier.fillMaxSize(),
                content = {
                    slots.forEachIndexed { slot, pageIndex ->
                        val page = pages.getOrNull(pageIndex) ?: return@forEachIndexed
                        val comicPage = page as? ComicPanelPage
                        CompositionLocalProvider(LocalComicFrameDelay provides slot * FRAME_STAGGER_MS) {
                            Box(Modifier.layoutId(SheetPanelSlot(slot))) {
                                ComicPanel(
                                    modifier = Modifier.fillMaxSize(),
                                    borderColor = borderColor,
                                    background = panelBackground,
                                    framed = comicPage?.hasFrame ?: true,
                                    shape = comicPage?.panelShape ?: RectangleShape,
                                ) {
                                    page.Show(
                                        modifier = Modifier.fillMaxSize(),
                                        canAnimate = true,
                                        onAction = onAction,
                                    )
                                }
                            }
                        }
                    }
                    // Balloons compose after every panel so they draw over the frames they break.
                    slots.forEachIndexed { slot, pageIndex ->
                        val balloonPage = pages.getOrNull(pageIndex) as? ComicPanelPage ?: return@forEachIndexed
                        CompositionLocalProvider(LocalComicFrameDelay provides slot * FRAME_STAGGER_MS) {
                            balloonPage.balloons.forEachIndexed { balloonIndex, spec ->
                                Box(Modifier.layoutId(SheetBalloonSlot(slot, balloonIndex))) {
                                    spec.content()
                                }
                            }
                        }
                    }
                },
            ) { measurables, constraints ->
                // Placed by hand at fixed sizes: a required-size modifier would coerce anything
                // wider than the canvas and re-centre it, nudging full-bleed frames off their mark.
                val panels =
                    measurables.mapNotNull { measurable ->
                        val slot = measurable.layoutId as? SheetPanelSlot ?: return@mapNotNull null
                        val rect = rects.getOrNull(slot.index) ?: return@mapNotNull null
                        rect to measurable.measure(Constraints.fixed(rect.width, rect.height))
                    }
                val balloons =
                    measurables.mapNotNull { measurable ->
                        val slot = measurable.layoutId as? SheetBalloonSlot ?: return@mapNotNull null
                        val rect = rects.getOrNull(slot.index) ?: return@mapNotNull null
                        val spec =
                            (pages.getOrNull(slots[slot.index]) as? ComicPanelPage)
                                ?.balloons
                                ?.getOrNull(slot.balloonIndex)
                                ?: return@mapNotNull null
                        // Loose height: a balloon is as tall as its text needs, and may run past
                        // the space its anchor edge leaves.
                        val maxWidth = (rect.width * spec.widthFraction).roundToInt()
                        Triple(rect, spec, measurable.measure(Constraints(maxWidth = maxWidth)))
                    }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    panels.forEach { (rect, placeable) -> placeable.place(rect.left, rect.top) }
                    balloons.forEach { (rect, spec, placeable) ->
                        val aligned =
                            spec.alignment.align(
                                IntSize(placeable.width, placeable.height),
                                IntSize(rect.width, rect.height),
                                layoutDirection,
                            )
                        placeable.place(
                            x = rect.left + aligned.x + spec.offset.x.roundToPx(),
                            y = rect.top + aligned.y + spec.offset.y.roundToPx(),
                        )
                    }
                }
            }
        }
    }
}

private data class SheetPanelSlot(
    val index: Int,
)

private data class SheetBalloonSlot(
    val index: Int,
    val balloonIndex: Int,
)
