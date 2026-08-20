package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Board width as a multiple of the viewport. Wide enough that a third-width panel still frames at
 * roughly 1:1 (so its text stays crisp), but not so wide that a full-bleed panel has to be shrunk
 * into illegibility to fit.
 */
private const val BOARD_WIDTH_FACTOR = 1.75f

// Comic pages run their frames nearly edge to edge — the gutter is a hairline, not a margin. Wide
// gutters were costing the panels the room their content actually needed.
private val GUTTER = 5.dp
private val BOARD_MARGIN = 10.dp

/** The opening band's frames sit tighter still, so neither loses width to the space between them. */
private val SPLASH_GUTTER = 4.dp

private const val DEFAULT_DWELL_MS = 5200L
private const val FOCUS_ANIM_MS = 1100
private const val OVERVIEW_ANIM_MS = 1400

/** How much of the viewport a focused panel fills. Below 1 so the neighbouring frames stay just in shot. */
private const val FOCUS_FILL = 0.88f

/** Framing is clamped so an unusually small or wide panel can't push its content to blurry-upscaled or unreadably tiny. */
private const val MIN_FOCUS_SCALE = 0.42f
private const val MAX_FOCUS_SCALE = 1.35f


/**
 * One row of the page: how the width splits between its frames, and how tall the row is relative to
 * the board's width. Comic pages are built this way — rows of varying height, each broken into
 * unequal frames — which is what stops a grid from reading as a table.
 */
private data class RowTemplate(
    val widths: List<Float>,
    val heightFactor: Float,
)

private val ROW_TEMPLATES =
    listOf(
        RowTemplate(listOf(0.58f, 0.42f), 0.46f),
        RowTemplate(listOf(0.34f, 0.33f, 0.33f), 0.38f),
        RowTemplate(listOf(1f), 0.40f),
        RowTemplate(listOf(0.4f, 0.6f), 0.52f),
        RowTemplate(listOf(0.5f, 0.5f), 0.44f),
        RowTemplate(listOf(1f), 0.55f),
    )

/**
 * The opening band's aspect. Tall on purpose: these frames carry both a full-bleed image and the
 * balloons over it, and at a shorter aspect the balloons ended up covering the art entirely.
 */
private const val COVER_ASPECT = 1.62f

/** Height of a [PanelSpan.FULL] row, relative to the board's width. */
private const val FULL_ROW_ASPECT = 0.62f

/** Grid cells are portrait — they mostly hold faces and cover art. */
private const val GRID_CELL_ASPECT = 1.12f

/** A [PanelSpan.BAND] is a strip, not a frame: only as tall as a few lines of narration need. */
private const val BAND_ASPECT = 0.3f

internal data class PanelPlacement(
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val height: Dp,
) {
    val centerX: Dp get() = x + width / 2
    val centerY: Dp get() = y + height / 2
}

internal data class BoardLayout(
    val placements: List<PanelPlacement>,
    val width: Dp,
    val height: Dp,
)

/** What a page asks of the layout — its span, plus the group it grids with when it grids at all. */
internal data class PanelRequest(
    val span: PanelSpan,
    val groupKey: String? = null,
)

/**
 * Lays panels out as comic rows rather than a uniform grid: [ROW_TEMPLATES] supplies the width
 * splits and row heights, cycling through them so consecutive rows don't repeat a shape. Panel 0 is
 * always the cover splash — full bleed and taller than anything else, so the page opens on it the
 * way an issue opens on its cover before you start reading frames.
 *
 * A panel that asks for [PanelSpan.FULL] is pulled out of the row packing and given its own band.
 * Those are the frames carrying a real plate of art, which a third-of-a-row slot would shrink to
 * nothing — the page still varies, but not at the cost of the panels that need the space.
 */
internal fun layoutBoard(
    requests: List<PanelRequest>,
    boardWidth: Dp,
): BoardLayout {
    if (requests.isEmpty()) return BoardLayout(emptyList(), boardWidth, boardWidth)

    val spans = requests.map { it.span }

    val contentWidth = boardWidth.value - BOARD_MARGIN.value * 2
    val placements = mutableListOf<PanelPlacement>()
    var y = BOARD_MARGIN.value

    // The opening band. Every leading SPLASH panel shares it side by side rather than stacking, so
    // a second cover-scale frame costs the page width instead of another screen of height — the
    // board stays a page you can take in, not a column. Cells keep the cover's portrait aspect, so
    // adding one to the band shortens the band rather than stretching it.
    val splashRun = spans.takeWhile { it == PanelSpan.SPLASH }.size.coerceAtLeast(1)
    val splashGutters = SPLASH_GUTTER.value * (splashRun - 1)
    val splashWidth = (contentWidth - splashGutters) / splashRun
    val splashHeight = splashWidth * COVER_ASPECT
    var splashX = BOARD_MARGIN.value
    repeat(splashRun) {
        placements += PanelPlacement(splashX.dp, y.dp, splashWidth.dp, splashHeight.dp)
        splashX += splashWidth + SPLASH_GUTTER.value
    }
    // Deeper gutter than the rows get: the cover's caption hangs past its bottom edge (see
    // ComicBalloonSpec) and needs air before the first row of frames.
    y += splashHeight + GUTTER.value * 6

    var index = splashRun
    var templateIndex = 0
    while (index < spans.size) {
        if (spans[index] == PanelSpan.FULL) {
            val rowHeight = boardWidth.value * FULL_ROW_ASPECT
            placements += PanelPlacement(BOARD_MARGIN.value.dp, y.dp, contentWidth.dp, rowHeight.dp)
            y += rowHeight + GUTTER.value
            index++
            continue
        }

        if (spans[index] == PanelSpan.BAND) {
            val rowHeight = boardWidth.value * BAND_ASPECT
            placements += PanelPlacement(BOARD_MARGIN.value.dp, y.dp, contentWidth.dp, rowHeight.dp)
            y += rowHeight + GUTTER.value
            index++
            continue
        }

        if (spans[index] == PanelSpan.MOSAIC) {
            // Packed with the same uneven row templates the loose panels use, but scoped to the
            // group so it can't absorb whatever follows it. The variety is the point here: a run
            // of chapter art in equal cells reads as a contact sheet rather than as a page.
            val key = requests[index].groupKey
            var remaining =
                requests
                    .drop(index)
                    .takeWhile { it.span == PanelSpan.MOSAIC && it.groupKey == key }
                    .size
            index += remaining

            while (remaining > 0) {
                val template = ROW_TEMPLATES[templateIndex % ROW_TEMPLATES.size]
                templateIndex++

                val taken = template.widths.take(minOf(template.widths.size, remaining))
                val total = taken.sum()
                val widths = taken.map { it / total }

                val rowHeight = boardWidth.value * template.heightFactor
                val gutterTotal = GUTTER.value * (widths.size - 1)
                var x = BOARD_MARGIN.value

                widths.forEach { fraction ->
                    val panelWidth = (contentWidth - gutterTotal) * fraction
                    placements += PanelPlacement(x.dp, y.dp, panelWidth.dp, rowHeight.dp)
                    x += panelWidth + GUTTER.value
                }

                y += rowHeight + GUTTER.value
                remaining -= widths.size
            }
            continue
        }

        if (spans[index] == PanelSpan.GRID) {
            // The whole run of peers is laid out at once, in equal cells. Sizing every cell from
            // the run's total is the point: it is what stops one member of a set — a fourth
            // farewell, a stray chapter cover — being handed a row of its own by the packer.
            val key = requests[index].groupKey
            val run =
                requests
                    .drop(index)
                    .takeWhile { it.span == PanelSpan.GRID && it.groupKey == key }
                    .size
            // Wider grids for bigger sets, so a large cast spreads across the page instead of
            // adding rows: the group is uncapped, and at a fixed column count a talkative saga
            // would run the board on for screens.
            val columns =
                when {
                    run <= 4 -> 2
                    run <= 9 -> 3
                    else -> 4
                }
            val cellWidth = (contentWidth - GUTTER.value * (columns - 1)) / columns
            val cellHeight = cellWidth * GRID_CELL_ASPECT

            repeat(run) { cell ->
                val column = cell % columns
                val row = cell / columns
                placements +=
                    PanelPlacement(
                        x = (BOARD_MARGIN.value + column * (cellWidth + GUTTER.value)).dp,
                        y = (y + row * (cellHeight + GUTTER.value)).dp,
                        width = cellWidth.dp,
                        height = cellHeight.dp,
                    )
            }

            val rows = (run + columns - 1) / columns
            y += rows * (cellHeight + GUTTER.value)
            index += run
            continue
        }

        val template = ROW_TEMPLATES[templateIndex % ROW_TEMPLATES.size]
        templateIndex++

        // A row only draws from the run of normal panels ahead of it — a full-span panel ends the
        // run rather than being folded into a split. When the run is short, the remaining widths
        // are renormalised so the row still spans the full page.
        val run = spans.drop(index).takeWhile { it == PanelSpan.NORMAL }.size.coerceAtLeast(1)
        val taken = template.widths.take(minOf(template.widths.size, run))
        val total = taken.sum()
        val widths = taken.map { it / total }

        val rowHeight = boardWidth.value * template.heightFactor
        val gutterTotal = GUTTER.value * (widths.size - 1)
        var x = BOARD_MARGIN.value

        widths.forEach { fraction ->
            val panelWidth = (contentWidth - gutterTotal) * fraction
            placements += PanelPlacement(x.dp, y.dp, panelWidth.dp, rowHeight.dp)
            x += panelWidth + GUTTER.value
        }

        y += rowHeight + GUTTER.value
        index += widths.size
    }

    val height = y - GUTTER.value + BOARD_MARGIN.value
    return BoardLayout(placements, boardWidth, max(height, 1f).dp)
}

/**
 * The comic page itself: all panels composed onto one board, with a camera that always frames
 * something — either a single panel or the whole page.
 *
 * Navigation follows [com.ilustris.sagai.features.brain.ui.SagaBrainView]'s model rather than free
 * pan/zoom: the reader taps a panel to fly to it, taps it again to pull back to the page, and
 * swipes left/right to walk the reading order. Free-form panning was tried first and made the page
 * easy to lose; snapping the camera to a target keeps every gesture landing somewhere legible.
 *
 * A real `HorizontalPager` isn't usable here — panels sit at arbitrary board coordinates rather
 * than in a linear list, and a pager owns its children's layout. Swipe-to-snap gives the same
 * feel because the camera already animates between focus points; the only thing given up is the
 * page tracking your finger mid-drag.
 *
 * Panels are only composed once the camera has reached them, so each one's entrance animation
 * fires on arrival instead of all of them running at once behind the scenes.
 */
@Composable
fun ComicBoard(
    pages: List<ReviewPage>,
    modifier: Modifier = Modifier,
    panelBorderColor: Color = Color.Black,
    panelBackground: Color = Color.White,
    onFinished: () -> Unit = {},
    onPanelAction: (ReviewAction) -> Unit = {},
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val requests =
            pages.map { page ->
                val comicPage = page as? ComicPanelPage
                PanelRequest(comicPage?.panelSpan ?: PanelSpan.NORMAL, comicPage?.groupKey)
            }
        val board =
            remember(requests, maxWidth) { layoutBoard(requests, maxWidth * BOARD_WIDTH_FACTOR) }

        val viewportW = with(density) { maxWidth.toPx() }
        val viewportH = with(density) { maxHeight.toPx() }
        val boardW = with(density) { board.width.toPx() }
        val boardH = with(density) { board.height.toPx() }

        // Each panel is framed by its own dimensions now that they differ — a wide establishing
        // frame and a narrow beat can't share one scale without one of them being wrong.
        val focusScales =
            remember(board, viewportW, viewportH) {
                board.placements.map { placement ->
                    val w = with(density) { placement.width.toPx() }
                    val h = with(density) { placement.height.toPx() }
                    (minOf(viewportW / w, viewportH / h) * FOCUS_FILL)
                        .coerceIn(MIN_FOCUS_SCALE, MAX_FOCUS_SCALE)
                }
            }
        val overviewScale =
            remember(board, viewportW, viewportH) {
                minOf(viewportW / boardW, viewportH / boardH) * 0.9f
            }
        val panelCenters =
            remember(board) {
                board.placements.map { placement ->
                    with(density) { placement.centerX.toPx() } to with(density) { placement.centerY.toPx() }
                }
            }

        // Keyed on emptiness rather than size: the review streams in section by section, so
        // `pages` keeps growing while the reader is mid-board. Keying on size would reset the
        // walkthrough back to the cover on every new section instead of just extending it.
        val hasPages = pages.isNotEmpty()
        var target: Int? by remember(hasPages) { mutableStateOf(if (hasPages) 0 else null) }
        var autoPlaying by remember(hasPages) { mutableStateOf(true) }
        var maxVisited by remember(hasPages) { mutableIntStateOf(0) }

        val camScale = remember { Animatable(focusScales.firstOrNull() ?: 1f) }
        val camFocusX = remember { Animatable(boardW / 2f) }
        val camFocusY = remember { Animatable(boardH / 2f) }

        fun goTo(index: Int?) {
            autoPlaying = false
            target = index
            if (index != null && index > maxVisited) maxVisited = index
        }

        /** Fly back to the cover and play the whole page through again. */
        fun replay() {
            target = 0
            autoPlaying = true
        }

        // The camera follows `target`; while auto-playing it also advances it. Keying the effect on
        // both means any tap or swipe restarts it, animates to the new target, and then stops —
        // taking over is just a state change, not a separate mode.
        //
        // Deliberately NOT keyed on focusScales/overviewScale: those are recomputed as fresh list
        // instances every time a new review section streams in and `board` grows, and keying on
        // them meant every arrival cancelled the in-flight tween toward the current target before
        // it ever settled — the cover would still be mid-flight when the reader looked at it. Their
        // current values are read fresh below regardless, so this only stops spurious restarts.
        LaunchedEffect(target, autoPlaying) {
            if (pages.isEmpty()) return@LaunchedEffect
            val current = target

            val targetScale = if (current == null) overviewScale else focusScales[current]
            val targetX = if (current == null) boardW / 2f else panelCenters[current].first
            val targetY = if (current == null) boardH / 2f else panelCenters[current].second

            listOf(
                launch { camScale.animateTo(targetScale, tween(FOCUS_ANIM_MS, easing = FastOutSlowInEasing)) },
                launch { camFocusX.animateTo(targetX, tween(FOCUS_ANIM_MS, easing = FastOutSlowInEasing)) },
                launch { camFocusY.animateTo(targetY, tween(FOCUS_ANIM_MS, easing = FastOutSlowInEasing)) },
            ).forEach { it.join() }

            if (!autoPlaying || current == null) return@LaunchedEffect

            delay(pages[current].estimatedRevealDurationMs.takeIf { it > 0 } ?: DEFAULT_DWELL_MS)

            if (current < pages.lastIndex) {
                target = current + 1
                maxVisited = maxOf(maxVisited, current + 1)
            } else {
                // Ends on the whole page, so the reader sees what they just walked through.
                target = null
                autoPlaying = false
                onFinished()
            }
        }

        /** Screen point → board point, so taps can be resolved against placements directly. */
        fun panelAt(
            screenX: Float,
            screenY: Float,
        ): Int? {
            // Read the camera at tap time rather than during composition — see the graphicsLayer
            // below for why these must not be composition reads.
            val scale = camScale.value
            val boardX = (screenX - (viewportW / 2f - camFocusX.value * scale)) / scale
            val boardY = (screenY - (viewportH / 2f - camFocusY.value * scale)) / scale
            return board.placements.indexOfFirst { placement ->
                val left = with(density) { placement.x.toPx() }
                val top = with(density) { placement.y.toPx() }
                val right = left + with(density) { placement.width.toPx() }
                val bottom = top + with(density) { placement.height.toPx() }
                boardX in left..right && boardY in top..bottom
            }.takeIf { it >= 0 }
        }

        Box(
            Modifier
                .fillMaxSize()
                // Hit-testing is done here against board coordinates rather than by making each
                // panel clickable: the panels live under a transform, and resolving taps in one
                // place avoids child/parent gesture ordering surprises with the swipe handler.
                .pointerInput(board, target) {
                    detectTapGestures { offset ->
                        val tapped = panelAt(offset.x, offset.y)
                        when {
                            // Tapping the panel already in shot pulls back to the whole page.
                            tapped != null && tapped == target -> goTo(null)
                            tapped != null -> goTo(tapped)
                            // Tapping the gutter during playback skips ahead.
                            autoPlaying -> target?.let { goTo((it + 1).coerceAtMost(pages.lastIndex)) }
                            else -> goTo(null)
                        }
                    }
                }.pointerInput(pages.size, target) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragEnd = {
                            val from = target ?: return@detectHorizontalDragGestures
                            val threshold = size.width * 0.18f
                            when {
                                totalDrag <= -threshold -> goTo((from + 1).coerceAtMost(pages.lastIndex))
                                totalDrag >= threshold -> goTo((from - 1).coerceAtLeast(0))
                            }
                        },
                    ) { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                },
        ) {
            val visible = pages.take((maxVisited + 1).coerceAtMost(pages.size))

            Layout(
                modifier =
                    Modifier
                        // Deliberately viewport-sized rather than board-sized: the measure policy
                        // below places panels at board coordinates, which overflow this node (it
                        // doesn't clip). Since transformOrigin is its top-left, the node's own size
                        // never enters the camera maths — and sizing it to the full board instead
                        // produced a ~1900x6000px render layer, which is where drawing once broke.
                        .fillMaxSize()
                        .graphicsLayer {
                            // Read inside the layer block: these are Animatables, and reading them
                            // in composition would recompose the whole board — every panel with it
                            // — on every camera frame. Here it only reruns the draw.
                            val scale = camScale.value
                            // Origin at the board's top-left keeps the mapping simple: a board
                            // point p lands at p * scale + translation, so centring p means
                            // translation = viewportCentre - p * scale. `panelAt` inverts this.
                            transformOrigin = TransformOrigin(0f, 0f)
                            scaleX = scale
                            scaleY = scale
                            translationX = viewportW / 2f - camFocusX.value * scale
                            translationY = viewportH / 2f - camFocusY.value * scale
                        },
                content = {
                    visible.forEachIndexed { index, page ->
                        val comicPage = page as? ComicPanelPage
                        Box(Modifier.layoutId(PanelSlot(index))) {
                            ComicPanel(
                                modifier = Modifier.fillMaxSize(),
                                borderColor = panelBorderColor,
                                background = panelBackground,
                                framed = comicPage?.hasFrame ?: true,
                                shape = comicPage?.panelShape ?: RectangleShape,
                            ) {
                                page.Show(
                                    modifier = Modifier.fillMaxSize(),
                                    canAnimate = true,
                                    // Restart means "watch it again" on a board — the pages are
                                    // still here, so it rewinds the camera rather than bubbling up
                                    // to a container that would have nothing to rebuild.
                                    onAction = { action ->
                                        if (action == ReviewAction.Restart) {
                                            replay()
                                        } else {
                                            onPanelAction(action)
                                        }
                                    },
                                )
                            }
                        }
                    }
                    // Balloons compose after every panel so they draw over the frames they break.
                    visible.forEachIndexed { index, page ->
                        val balloonPage = page as? ComicPanelPage ?: return@forEachIndexed
                        balloonPage.balloons.forEachIndexed { balloonIndex, spec ->
                            Box(Modifier.layoutId(BalloonSlot(index, balloonIndex))) {
                                spec.content()
                            }
                        }
                    }
                },
            ) { measurables, constraints ->
                // Absolute placement done here rather than with offset + requiredSize. When a
                // required size exceeds the incoming constraints, that modifier reports a coerced
                // size and *centres* the content inside it — so full-bleed panels (wider than the
                // viewport, since the board is BOARD_WIDTH_FACTOR times it) drifted left while
                // narrow ones, which fit, sat exactly where asked. Measuring at fixed constraints
                // and placing by hand has no such coercion.
                val rects =
                    board.placements.map { placement ->
                        PanelRect(
                            left = placement.x.roundToPx(),
                            top = placement.y.roundToPx(),
                            width = placement.width.roundToPx(),
                            height = placement.height.roundToPx(),
                        )
                    }

                val panels =
                    measurables.mapNotNull { measurable ->
                        val slot = measurable.layoutId as? PanelSlot ?: return@mapNotNull null
                        val rect = rects[slot.index]
                        slot.index to measurable.measure(Constraints.fixed(rect.width, rect.height))
                    }

                val balloons =
                    measurables.mapNotNull { measurable ->
                        val slot = measurable.layoutId as? BalloonSlot ?: return@mapNotNull null
                        val rect = rects[slot.index]
                        val spec = (pages[slot.index] as ComicPanelPage).balloons[slot.balloonIndex]
                        // Loose height: a balloon is as tall as its text needs, and it is allowed
                        // to end up taller than the space its anchor edge leaves.
                        val maxWidth = (rect.width * spec.widthFraction).roundToInt()
                        Triple(rect, spec, measurable.measure(Constraints(maxWidth = maxWidth)))
                    }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    panels.forEach { (index, placeable) ->
                        val rect = rects[index]
                        placeable.place(rect.left, rect.top)
                    }
                    balloons.forEach { (rect, spec, placeable) ->
                        // Aligned against its own panel's box, then nudged. Pushing the nudge past
                        // an edge is what makes a balloon straddle the border.
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

/** Board children are tagged so the measure policy can tell frames from the balloons over them. */
private data class PanelSlot(
    val index: Int,
)

private data class BalloonSlot(
    val index: Int,
    val balloonIndex: Int,
)

private data class PanelRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
)


/**
 * A single comic frame: hard border and a flat ground, the gutter around it coming from the
 * board's own spacing. Clips its content — pages still on the default (full-screen) layout are
 * taller than a panel's allotted height, and without clipping they bleed past the border into the
 * row below instead of just running out of room.
 */
@Composable
fun ComicPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Black,
    background: Color = Color.White,
    framed: Boolean = true,
    shape: Shape = RectangleShape,
    content: @Composable () -> Unit,
) {
    // Unframed panels claim their space in the layout but draw nothing of their own — and notably
    // don't clip, so a page made only of balloons can let them spill wherever they land.
    if (!framed) {
        Box(modifier) { content() }
        return
    }

    Box(
        modifier
            .clip(shape)
            .background(background, shape)
            .border(3.dp, borderColor, shape),
    ) {
        content()
    }
}
