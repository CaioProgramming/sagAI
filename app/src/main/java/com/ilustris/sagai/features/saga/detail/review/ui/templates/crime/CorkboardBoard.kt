package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.PanelRequest
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.PanelSpan
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.layoutBoard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Same width-to-viewport ratio [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicBoard] uses — wide enough to spread pins out, not so wide a full-bleed cover shrinks away. */
private const val BOARD_WIDTH_FACTOR = 1.8f

private const val DEFAULT_DWELL_MS = 4600L
private const val FOCUS_ANIM_MS = 1100
private const val FOCUS_FILL = 0.86f
private const val MIN_FOCUS_SCALE = 0.42f
private const val MAX_FOCUS_SCALE = 1.35f

/** The route line's default color — a fixed detective-board red rather than the genre's own accent, since the string itself is the one constant across every saga's palette. */
private val DEFAULT_ROUTE_COLOR = Color(0xFFB1332A)

/**
 * The corkboard equivalent of
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicBoard]: every review
 * stage is a pinned photo or note on one big board, and the same camera model flies between them
 * — plays through on its own, a tap flies to a pin or pulls back to the whole board, swipes walk
 * the reading order. The one genuinely new piece is [drawRoute]: a dashed line strung between the
 * pins in visiting order, growing as the camera advances — the travelling red line across a map an
 * Indiana Jones intro opens on, rather than more chat.
 */
@Composable
fun CorkboardBoard(
    pages: List<ReviewPage>,
    modifier: Modifier = Modifier,
    routeColor: Color = DEFAULT_ROUTE_COLOR,
    onFinished: () -> Unit = {},
    onPanelAction: (ReviewAction) -> Unit = {},
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val requests =
            pages.map { page ->
                val pin = page as? CorkboardPinPage
                PanelRequest(pin?.panelSpan ?: PanelSpan.NORMAL, pin?.groupKey)
            }
        val board =
            remember(requests, maxWidth) { layoutBoard(requests, maxWidth * BOARD_WIDTH_FACTOR) }

        val viewportW = with(density) { maxWidth.toPx() }
        val viewportH = with(density) { maxHeight.toPx() }
        val boardW = with(density) { board.width.toPx() }
        val boardH = with(density) { board.height.toPx() }

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

        // Keyed on emptiness rather than size — see ComicBoard for why: the review streams in
        // section by section, so `pages` keeps growing while the reader is mid-board.
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

        /** Fly back to the cover and play the whole board through again. */
        fun replay() {
            target = 0
            autoPlaying = true
        }

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
                // Ends on the whole board, so the reader sees the route they just walked.
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
                .pointerInput(board, target) {
                    detectTapGestures { offset ->
                        val tapped = panelAt(offset.x, offset.y)
                        when {
                            tapped != null && tapped == target -> goTo(null)
                            tapped != null -> goTo(tapped)
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
                        .fillMaxSize()
                        .graphicsLayer {
                            val scale = camScale.value
                            transformOrigin = TransformOrigin(0f, 0f)
                            scaleX = scale
                            scaleY = scale
                            translationX = viewportW / 2f - camFocusX.value * scale
                            translationY = viewportH / 2f - camFocusY.value * scale
                        },
                content = {
                    // Sized to the whole board and placed at its origin, so it draws in the same
                    // board-coordinate space the pins are placed in.
                    Box(Modifier.layoutId(RouteSlot)) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawRoute(panelCenters.take(maxVisited + 1), routeColor)
                        }
                    }

                    visible.forEachIndexed { index, page ->
                        Box(Modifier.layoutId(PanelSlot(index))) {
                            page.Show(
                                modifier = Modifier.fillMaxSize(),
                                canAnimate = true,
                                // Restart means "watch it again" on a board — it rewinds the
                                // camera rather than bubbling up to a container with nothing to
                                // rebuild, same as ComicBoard.
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
                },
            ) { measurables, constraints ->
                val rects =
                    board.placements.map { placement ->
                        PanelRect(
                            left = placement.x.roundToPx(),
                            top = placement.y.roundToPx(),
                            width = placement.width.roundToPx(),
                            height = placement.height.roundToPx(),
                        )
                    }

                val routePlaceable =
                    measurables
                        .firstOrNull { it.layoutId == RouteSlot }
                        ?.measure(Constraints.fixed(board.width.roundToPx(), board.height.roundToPx()))

                val panels =
                    measurables.mapNotNull { measurable ->
                        val slot = measurable.layoutId as? PanelSlot ?: return@mapNotNull null
                        val rect = rects[slot.index]
                        slot.index to measurable.measure(Constraints.fixed(rect.width, rect.height))
                    }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    routePlaceable?.place(0, 0)
                    panels.forEach { (index, placeable) ->
                        val rect = rects[index]
                        placeable.place(rect.left, rect.top)
                    }
                }
            }
        }
    }
}

/** Dashed string connecting each visited pin's center in order, with a dot marking every stop and a slightly bigger one on the latest — the travelling line growing behind the camera. */
private fun DrawScope.drawRoute(
    visitedCenters: List<Pair<Float, Float>>,
    routeColor: Color,
) {
    if (visitedCenters.size >= 2) {
        val path =
            Path().apply {
                moveTo(visitedCenters[0].first, visitedCenters[0].second)
                visitedCenters.drop(1).forEach { (x, y) -> lineTo(x, y) }
            }
        drawPath(
            path,
            color = routeColor.copy(alpha = 0.75f),
            style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f))),
        )
    }
    visitedCenters.forEachIndexed { index, (x, y) ->
        val isLatest = index == visitedCenters.lastIndex
        drawCircle(routeColor, radius = if (isLatest) 9f else 6f, center = Offset(x, y))
    }
}

/** Board children are tagged so the measure policy can tell the route overlay from the pins. */
private data class PanelSlot(
    val index: Int,
)

private data object RouteSlot

private data class PanelRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
)
