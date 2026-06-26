package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.utils.vibrate
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DRAG_MULTIPLIER = 0.5f
private val ADVANCE_TRIGGER_DISTANCE = 96.dp

/** Bottom of a reverse-layout chat: newest content is near index 0. */
private fun LazyListState.isNearChatBottom(): Boolean {
    if (!canScrollForward) return true
    return firstVisibleItemIndex <= 1 &&
        (firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset < 120)
}

@Composable
fun AdvancePullContainer(
    listState: LazyListState,
    enabled: Boolean,
    isProcessing: Boolean,
    onAdvance: () -> Unit,
    onPullProgress: (Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val triggerPx = with(LocalDensity.current) { ADVANCE_TRIGGER_DISTANCE.toPx() }
    var offset by remember { mutableFloatStateOf(0f) }
    var thresholdHapticFired by remember { mutableStateOf(false) }

    val isNearBottom by remember {
        derivedStateOf { listState.isNearChatBottom() }
    }

    val pullProgress = (offset / triggerPx).coerceIn(0f, 1f)

    LaunchedEffect(pullProgress) {
        onPullProgress(pullProgress)
    }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            offset = 0f
            thresholdHapticFired = false
            onPullProgress(0f)
        }
    }

    val nestedScrollConnection =
        remember(enabled, isProcessing, isNearBottom, triggerPx) {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (
                        source != NestedScrollSource.UserInput ||
                        !enabled ||
                        isProcessing ||
                        !isNearBottom ||
                        available.y == 0f
                    ) {
                        return Offset.Zero
                    }

                    val dragDelta = abs(available.y)
                    val newOffset = offset + dragDelta * DRAG_MULTIPLIER
                    offset = newOffset.coerceAtMost(triggerPx * 1.5f)

                    if (offset >= triggerPx && !thresholdHapticFired) {
                        thresholdHapticFired = true
                        context.vibrate(longArrayOf(0, 20))
                    } else if (offset < triggerPx) {
                        thresholdHapticFired = false
                    }

                    return available
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (offset >= triggerPx && enabled && !isProcessing && isNearBottom) {
                        context.vibrate(longArrayOf(0, 400))
                        onAdvance()
                    }

                    thresholdHapticFired = false
                    coroutineScope.launch {
                        animate(initialValue = offset, targetValue = 0f) { value, _ ->
                            offset = value
                        }
                    }

                    return Velocity.Zero
                }
            }
        }

    Box(modifier = modifier.nestedScroll(nestedScrollConnection)) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, offset.roundToInt()) },
        ) {
            content()
        }
    }
}
