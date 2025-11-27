package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val DRAG_MULTIPLIER = 0.5f
private val REFRESH_TRIGGER_DISTANCE = 80.dp

/**
 * A container that adds pull-to-refresh functionality to its content.
 * This is designed for lists where new content is loaded at the top (e.g., chat history).
 *
 * @param listState The LazyListState of the scrollable content within this container.
 * @param isLoading Whether a refresh is currently in progress.
 * @param onRefresh Lambda to be called when a refresh is triggered.
 * @param modifier Modifier to be applied to the container.
 * @param content The content to be displayed inside the container, typically a LazyColumn.
 */
@Composable
fun SwipeUpToRefreshContainer(
    listState: LazyListState,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val refreshTriggerPx = with(LocalDensity.current) { REFRESH_TRIGGER_DISTANCE.toPx() }
    var offset by remember { mutableFloatStateOf(0f) }

    // Check if the list is at the very top (first item visible and fully scrolled)
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    // Animate the offset back to zero when loading is finished.
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            coroutineScope.launch {
                animate(initialValue = offset, targetValue = 0f) { value, _ ->
                    offset = value
                }
            }
        }
    }

    val nestedScrollConnection = remember(isLoading, isAtTop) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource,
            ): androidx.compose.ui.geometry.Offset {
                // We only care about drag gestures, when not loading, and when the list is at the top.
                if (source != NestedScrollSource.Drag || isLoading || !isAtTop) return androidx.compose.ui.geometry.Offset.Zero

                // When the user pulls down at the top of the list, `available.y` will be positive.
                if (available.y > 0) {
                    val newOffset = (offset + available.y * DRAG_MULTIPLIER)
                    offset = newOffset.coerceAtMost(refreshTriggerPx * 1.5f)
                    // Consume the drag distance.
                    return available
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // If the user releases the drag with enough offset, trigger a refresh.
                // Also, ensure the list is at the top before triggering.
                if (offset >= refreshTriggerPx && !isLoading && isAtTop) {
                    onRefresh()
                }

                // Animate the offset back to zero.
                coroutineScope.launch {
                    animate(initialValue = offset, targetValue = 0f) { value, _ ->
                        offset = value
                    }
                }

                // Do not consume the fling.
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier.nestedScroll(nestedScrollConnection),
        contentAlignment = Alignment.TopCenter,
    ) {
        // Offset the content based on the drag amount.
        Box(modifier = Modifier.offset { IntOffset(0, offset.roundToInt()) }) {
            content()
        }

        // Show the loading indicator if refreshing or if the user is pulling.
        if (isLoading || offset > 0f) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .offset {
                        // Adjust the indicator position as the user pulls.
                        IntOffset(0, (offset.coerceAtMost(refreshTriggerPx) / 2).roundToInt() - (refreshTriggerPx / 2).roundToInt())
                    },
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
        }
    }
}