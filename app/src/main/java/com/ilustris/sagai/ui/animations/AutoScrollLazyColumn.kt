package com.ilustris.sagai.ui.animations

import android.view.MotionEvent
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A [LazyColumn] that reads itself: it drifts forward on its own, like scrolling a newspaper
 * hands-free, instead of waiting for swipes. Grabbing it (any touch-down) pauses the drift
 * immediately — [MotionEvent.ACTION_DOWN]/`ACTION_UP`/`ACTION_CANCEL` via [pointerInteropFilter],
 * the same pause-on-touch mechanic [com.ilustris.sagai.features.saga.detail.ui.SagaReview]'s
 * `DefaultReviewContainer` already uses for story auto-advance — and after the finger lifts, it
 * waits [resumeDelay] before drifting again, so a reader who paused to actually read isn't yanked
 * forward mid-sentence. The very first auto-scroll (before any touch ever happened) starts with
 * no such delay.
 *
 * Drift is driven frame-by-frame via [withFrameNanos] + [scrollBy] rather than one long
 * `animateScrollBy` (the trick [AnimatedChapterGridBackground] uses for its background loop) —
 * a per-frame delta responds instantly to list content changes and stops cleanly the moment
 * [LazyListState.canScrollForward] goes false, no bounce-back needed since this is a
 * read-once article, not a looping background.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AutoScrollLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    scrollSpeed: Dp = 36.dp,
    resumeDelay: Duration = 20.seconds,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: LazyListScope.() -> Unit,
) {
    val density = LocalDensity.current
    val pixelsPerSecond = with(density) { scrollSpeed.toPx() }

    var paused by remember { mutableStateOf(false) }
    var hasInteracted by remember { mutableStateOf(false) }

    LaunchedEffect(state, paused) {
        if (paused) return@LaunchedEffect
        if (hasInteracted) delay(resumeDelay)

        var lastFrameNanos = withFrameNanos { it }
        while (state.canScrollForward) {
            val frameNanos = withFrameNanos { it }
            val deltaSeconds = (frameNanos - lastFrameNanos) / 1_000_000_000f
            lastFrameNanos = frameNanos
            state.scrollBy(pixelsPerSecond * deltaSeconds)
        }
    }

    LazyColumn(
        state = state,
        modifier =
            modifier.pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        paused = true
                        hasInteracted = true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> paused = false
                }
                false
            },
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}
