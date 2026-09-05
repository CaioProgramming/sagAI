package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import android.view.MotionEvent
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.ui.genre.crime.CorkboardPalette
import com.ilustris.sagai.ui.genre.crime.rememberCorkboardPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * How fast the table drifts past. Slow on purpose — this is a camera travelling over a map, not a
 * carousel, and the reader is meant to be able to finish a caption before it leaves the screen.
 */
private val DRIFT_SPEED = 15.dp

/** After the reader lets go, how long before the drift picks up again. */
private val RESUME_DELAY: Duration = 5.seconds

/** Base spacing between pins; varied per gap so the spread doesn't read as a filmstrip. */
private val PIN_GAP = 26.dp

/** Blank table before the first pin and after the last, so neither is jammed against a screen edge. */
private val STRIP_MARGIN = 32.dp

/** How far a pin may sit off the table's centre line, as a fraction of the viewport height. */
private const val STAGGER_FRACTION = 0.11f

/** Vertical room kept clear at the top and bottom of the table for pushpins and shadows. */
private val BAND_INSET = 28.dp

/**
 * Crime's SagaReview: the whole saga spread out as photos on a table, panning steadily past.
 *
 * The first pass flew a camera from pin to pin, reusing Heroes' comic-board model — which worked,
 * but made two very different genres move the same way. This is the Indiana Jones travelling shot
 * instead: one long horizontal table, and the view slides across it at a constant speed while the
 * red string threads from photo to photo. Nothing zooms and nothing snaps; the reader can grab the
 * table to scrub it and let go to resume, the way you'd push a stack of photos along to see the
 * next one.
 *
 * Pins are measured against their own content rather than dropped into fixed rects (what the board
 * layout did), so a long note grows its card instead of spilling over the pin beside it.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CorkboardStrip(
    pages: List<ReviewPage>,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
    onPinAction: (ReviewAction) -> Unit = {},
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val palette = rememberCorkboardPalette()
        val scrollState = rememberScrollState()
        val scope = rememberCoroutineScope()

        val viewportWidthPx = with(density) { maxWidth.toPx() }
        val driftPxPerSecond = with(density) { DRIFT_SPEED.toPx() }

        var paused by remember { mutableStateOf(false) }
        var hasInteracted by remember { mutableStateOf(false) }

        /**
         * Written while the strip measures and read again when it draws, so the string can be
         * strung between the pins' real resting places. Measure always runs before draw in the same
         * frame, so the two never disagree — and keeping the centres here rather than deriving them
         * up front is what lets pins size themselves to their content.
         */
        var pinCenters by remember { mutableStateOf(emptyList<Offset>()) }

        // Keyed on emptiness rather than size — the review streams in section by section, so `pages`
        // keeps growing while the reader is partway along the table.
        val hasPages = pages.isNotEmpty()

        LaunchedEffect(hasPages, paused, pages.size) {
            if (!hasPages || paused) return@LaunchedEffect
            if (hasInteracted) delay(RESUME_DELAY)

            var lastFrameNanos = withFrameNanos { it }
            while (scrollState.canScrollForward) {
                val frameNanos = withFrameNanos { it }
                val deltaSeconds = (frameNanos - lastFrameNanos) / 1_000_000_000f
                lastFrameNanos = frameNanos
                scrollState.scrollBy(driftPxPerSecond * deltaSeconds)
            }

            // maxValue is 0 before the first layout pass, which is not the same as having reached
            // the end of the table.
            if (scrollState.maxValue > 0) onFinished()
        }

        Box(
            Modifier
                .fillMaxSize()
                // Outside the scroll modifier, like AutoScrollLazyColumn's: it only wants to see
                // the touch go down and come up, and returning false lets the drag itself carry on
                // to the scroll underneath.
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            paused = true
                            hasInteracted = true
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> paused = false
                    }
                    false
                }.horizontalScroll(scrollState),
        ) {
            Layout(
                modifier =
                    Modifier.drawBehind {
                        drawThread(pinCenters, palette)
                    },
                content = {
                    pages.forEach { page ->
                        page.Show(
                            modifier = Modifier,
                            canAnimate = true,
                            onAction = { action ->
                                if (action == ReviewAction.Restart) {
                                    scope.launch {
                                        scrollState.animateScrollTo(0)
                                        paused = false
                                    }
                                } else {
                                    onPinAction(action)
                                }
                            },
                        )
                    }
                },
            ) { measurables, constraints ->
                val bandHeight = constraints.maxHeight
                val inset = BAND_INSET.roundToPx()
                val margin = STRIP_MARGIN.roundToPx()
                val baseGap = PIN_GAP.roundToPx()
                val staggerRange = bandHeight * STAGGER_FRACTION

                val placeables =
                    measurables.mapIndexed { index, measurable ->
                        val fraction =
                            (pages.getOrNull(index) as? CorkboardPinPage)?.pinSize?.widthFraction
                                ?: CorkPinSize.PHOTO.widthFraction
                        val width = (viewportWidthPx * fraction).roundToInt()
                        measurable.measure(
                            Constraints(
                                minWidth = width,
                                maxWidth = width,
                                minHeight = 0,
                                maxHeight = (bandHeight - inset * 2).coerceAtLeast(0),
                            ),
                        )
                    }

                var cursor = margin
                val positions =
                    placeables.mapIndexed { index, placeable ->
                        // Deterministic per index so a pin doesn't hop as the strip recomposes.
                        val rng = Random(index * 977 + 13)
                        val stagger = ((rng.nextFloat() - 0.5f) * 2f * staggerRange).roundToInt()
                        val y =
                            ((bandHeight - placeable.height) / 2 + stagger)
                                .coerceIn(inset, (bandHeight - placeable.height - inset).coerceAtLeast(inset))

                        val x = cursor
                        cursor += placeable.width + baseGap + (rng.nextFloat() * baseGap).roundToInt()
                        x to y
                    }

                val totalWidth = cursor - baseGap + margin

                val centers =
                    positions.mapIndexed { index, (x, y) ->
                        val placeable = placeables[index]
                        Offset(x + placeable.width / 2f, y + placeable.height / 2f)
                    }
                // Guarded so a re-measure that lands the pins in the same places doesn't invalidate
                // the string's draw for nothing.
                if (centers != pinCenters) pinCenters = centers

                layout(totalWidth.coerceAtLeast(constraints.minWidth), bandHeight) {
                    placeables.forEachIndexed { index, placeable ->
                        val (x, y) = positions[index]
                        placeable.place(x, y)
                    }
                }
            }
        }
    }
}

/**
 * The red string between pins, drawn as a slack line rather than a straight segment: a real thread
 * pinned across a board sags between its two tacks, and the dip is what separates it from the ruled
 * connector lines a diagram would use. A dot marks each pin it passes through.
 */
private fun DrawScope.drawThread(
    centers: List<Offset>,
    palette: CorkboardPalette,
) {
    if (centers.size >= 2) {
        val path =
            Path().apply {
                moveTo(centers.first().x, centers.first().y)
                centers.zipWithNext { from, to ->
                    val sag = (to.x - from.x) * 0.16f
                    quadraticTo(
                        (from.x + to.x) / 2f,
                        maxOf(from.y, to.y) + sag,
                        to.x,
                        to.y,
                    )
                }
            }
        drawPath(
            path,
            color = palette.thread.copy(alpha = 0.85f),
            style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f))),
        )
    }

    centers.forEach { center ->
        drawCircle(palette.thread, radius = 6f, center = center)
        drawCircle(Color.White.copy(alpha = 0.35f), radius = 2.5f, center = center)
    }
}
