package com.ilustris.sagai.ui.components.taskshell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val CompactFallbackHeight = 56.dp
private val ExpandedRevealThresholdPx = 24f

private val ShellHeightTween = tween<Dp>(420, easing = FastOutSlowInEasing)
private val ShellSizeTween = tween<IntSize>(400, easing = FastOutSlowInEasing)
private val ShellMotionTween = tween<Float>(400, easing = FastOutSlowInEasing)
private val ShellFadeInTween = tween<Float>(340, delayMillis = 80, easing = FastOutSlowInEasing)
private val ShellFadeOutTween = tween<Float>(260, easing = FastOutSlowInEasing)

@Composable
private fun TaskShellExpandedSection(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter =
            expandVertically(
                animationSpec = ShellSizeTween,
                expandFrom = Alignment.Top,
            ) + fadeIn(ShellFadeInTween),
        exit =
            shrinkVertically(
                animationSpec = ShellSizeTween,
                shrinkTowards = Alignment.Top,
            ) + fadeOut(ShellFadeOutTween),
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskShellLayout(
    modifier: Modifier = Modifier,
    horizontalInset: Dp = 8.dp,
    topSlot: TaskShellSlotState? = null,
    bottomSlot: TaskShellSlotState? = null,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val screenHeightPx =
        with(density) {
            LocalConfiguration.current.screenHeightDp.dp
                .toPx()
        }

    var topOccupiedPx by remember { mutableFloatStateOf(0f) }

    val topPadding by animateDpAsState(
        if (topSlot?.expansion == TaskShellExpansion.Expanded) 4.dp else 0.dp,
    )

    val bottomPadding by animateDpAsState(
        if (bottomSlot?.expansion == TaskShellExpansion.Expanded) 4.dp else 0.dp,
    )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .animateContentSize(),
    ) {
        topSlot?.let { slot ->
            TaskShellTopRegion(
                slot = slot,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onSizeChanged { topOccupiedPx = it.height.toFloat() },
            )
        }

        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                    .padding(top = topPadding, bottom = bottomPadding),
        ) {
            content()
        }

        bottomSlot?.let { slot ->
            if (slot.content.isDraggable) {
                TaskShellBottomDraggableRegion(
                    slot = slot,
                    topOccupiedPx = topOccupiedPx,
                    screenHeightPx = screenHeightPx,
                )
            } else {
                TaskShellBottomStateRegion(
                    slot = slot,
                    topOccupiedPx = topOccupiedPx,
                    screenHeightPx = screenHeightPx,
                )
            }
        }
    }
}

@Composable
private fun TaskShellBottomStateRegion(
    slot: TaskShellSlotState,
    topOccupiedPx: Float,
    screenHeightPx: Float,
) {
    val density = LocalDensity.current
    var compactHeightPx by remember { mutableFloatStateOf(with(density) { CompactFallbackHeight.toPx() }) }
    val isExpanded = slot.expansion != TaskShellExpansion.Collapsed
    val fullHeightPx = (screenHeightPx - topOccupiedPx).coerceAtLeast(compactHeightPx)

    val shellScope =
        slot.rememberScope(
            onMinimize = { slot.onExpansionChange(TaskShellExpansion.Collapsed) },
            onToggle = {
                val next =
                    if (slot.expansion == TaskShellExpansion.Collapsed) {
                        TaskShellExpansion.Expanded
                    } else {
                        TaskShellExpansion.Collapsed
                    }
                slot.onExpansionChange(next)
            },
            onRequestFull = { slot.onExpansionChange(TaskShellExpansion.Full) },
        )

    val targetHeightDp =
        with(density) {
            if (isExpanded) fullHeightPx.toDp() else compactHeightPx.toDp()
        }
    val animatedHeight by animateDpAsState(
        targetValue = targetHeightDp,
        animationSpec = ShellHeightTween,
        label = "bottomShellHeight",
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(animatedHeight)
                .clip(TaskShellBottomShape),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .onSizeChanged { compactHeightPx = it.height.toFloat() },
        ) {
            slot.content.Compact(shellScope)
        }

        if (slot.content.isExpandable) {
            TaskShellExpandedSection(
                visible = isExpanded,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                Box(Modifier.fillMaxSize()) {
                    slot.content.Expanded(shellScope)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskShellBottomDraggableRegion(
    slot: TaskShellSlotState,
    topOccupiedPx: Float,
    screenHeightPx: Float,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var bottomCompactPx by remember { mutableFloatStateOf(with(density) { CompactFallbackHeight.toPx() }) }
    val fullHeightPx = (screenHeightPx - topOccupiedPx).coerceAtLeast(bottomCompactPx)
    val expandedHeightPx =
        remember(bottomCompactPx, fullHeightPx) {
            bottomCompactPx + (fullHeightPx - bottomCompactPx) * 0.4f
        }

    val anchors =
        remember(bottomCompactPx, expandedHeightPx, fullHeightPx) {
            DraggableAnchors {
                TaskShellExpansion.Collapsed at bottomCompactPx
                TaskShellExpansion.Expanded at expandedHeightPx
                TaskShellExpansion.Full at fullHeightPx
            }
        }

    val draggableState =
        remember(anchors, slot.expansion) {
            AnchoredDraggableState(
                initialValue = slot.expansion,
                anchors = anchors,
            )
        }

    val sheetHeightPx = draggableState.requireOffset()
    val revealExpanded =
        draggableState.currentValue != TaskShellExpansion.Collapsed ||
            sheetHeightPx > bottomCompactPx + ExpandedRevealThresholdPx

    val dragSnapThresholdPx = with(density) { 40.dp.toPx() }

    androidx.compose.runtime.LaunchedEffect(draggableState.offset, bottomCompactPx) {
        if (
            draggableState.currentValue == TaskShellExpansion.Collapsed &&
            sheetHeightPx > bottomCompactPx + dragSnapThresholdPx
        ) {
            draggableState.snapTo(TaskShellExpansion.Full)
        }
    }

    androidx.compose.runtime.LaunchedEffect(slot.expansion) {
        if (draggableState.currentValue != slot.expansion) {
            draggableState.animateTo(
                targetValue = slot.expansion,
                animationSpec = ShellMotionTween,
            )
        }
    }

    androidx.compose.runtime.LaunchedEffect(draggableState.currentValue) {
        if (draggableState.currentValue != slot.expansion) {
            slot.onExpansionChange(draggableState.currentValue)
        }
    }

    val shellScope =
        slot.rememberScope(
            onMinimize = {
                scope.launch {
                    draggableState.animateTo(
                        targetValue = TaskShellExpansion.Collapsed,
                        animationSpec = ShellMotionTween,
                    )
                }
            },
            onToggle = {
                scope.launch {
                    val target =
                        if (slot.expansion == TaskShellExpansion.Collapsed) {
                            TaskShellExpansion.Expanded
                        } else {
                            TaskShellExpansion.Collapsed
                        }
                    draggableState.animateTo(
                        targetValue = target,
                        animationSpec = ShellMotionTween,
                    )
                }
            },
            onRequestFull = {
                scope.launch {
                    draggableState.animateTo(
                        targetValue = TaskShellExpansion.Full,
                        animationSpec = ShellMotionTween,
                    )
                }
            },
        )

    val expansionProgress =
        remember(sheetHeightPx, bottomCompactPx, fullHeightPx) {
            ((sheetHeightPx - bottomCompactPx) / (fullHeightPx - bottomCompactPx).coerceAtLeast(1f))
                .coerceIn(0f, 1f)
        }
    val expandedAlpha by animateFloatAsState(
        targetValue = if (revealExpanded) expansionProgress.coerceAtLeast(0.08f) else 0f,
        animationSpec = ShellFadeInTween,
        label = "bottomDraggableExpandedAlpha",
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(with(density) { sheetHeightPx.toDp() })
                .clip(TaskShellBottomShape),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .anchoredDraggable(
                        state = draggableState,
                        orientation = Orientation.Vertical,
                        reverseDirection = true,
                    ).onSizeChanged { bottomCompactPx = it.height.toFloat() },
        ) {
            slot.content.Compact(shellScope)
        }

        if (revealExpanded && slot.content.isExpandable) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .alpha(expandedAlpha),
            ) {
                slot.content.Expanded(shellScope)
            }
        }
    }
}

@Composable
private fun TaskShellTopRegion(
    slot: TaskShellSlotState,
    modifier: Modifier = Modifier,
) {
    val isExpanded = slot.expansion != TaskShellExpansion.Collapsed

    val shellScope =
        slot.rememberScope(
            onMinimize = { slot.onExpansionChange(TaskShellExpansion.Collapsed) },
            onToggle = {
                val next =
                    if (slot.expansion == TaskShellExpansion.Collapsed) {
                        TaskShellExpansion.Expanded
                    } else {
                        TaskShellExpansion.Collapsed
                    }
                slot.onExpansionChange(next)
            },
            onRequestFull = { slot.onExpansionChange(TaskShellExpansion.Full) },
        )

    if (!slot.content.isExpandable) {
        Box(modifier = modifier.wrapContentHeight()) {
            slot.content.Compact(shellScope)
        }
        return
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize(ShellSizeTween),
    ) {
        Box(Modifier.fillMaxWidth()) {
            slot.content.Compact(shellScope)
        }

        TaskShellExpandedSection(visible = isExpanded) {
            slot.content.Expanded(shellScope)
        }
    }
}
