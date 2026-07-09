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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
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
    background: (@Composable BoxScope.(topSlot: TaskShellSlotState?, bottomSlot: TaskShellSlotState?) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val screenHeightPx =
        with(density) {
            LocalConfiguration.current.screenHeightDp.dp
                .toPx()
        }

    var topOccupiedPx by remember { mutableFloatStateOf(0f) }

    var lastTopSlot by remember { mutableStateOf(topSlot) }
    androidx.compose.runtime.LaunchedEffect(topSlot) {
        if (topSlot != null) lastTopSlot = topSlot
    }
    val effectiveTopSlot = topSlot ?: lastTopSlot

    val topPadding by animateDpAsState(
        when {
            topSlot == null -> 0.dp
            topSlot.expansion == TaskShellExpansion.Expanded -> 8.dp
            else -> 4.dp
        },
    )

    val bottomPadding by animateDpAsState(
        if (bottomSlot?.expansion == TaskShellExpansion.Expanded) 4.dp else 0.dp,
    )

    Box(modifier = modifier.fillMaxSize()) {
        background?.invoke(this, topSlot, bottomSlot)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .animateContentSize(),
        ) {
            TaskShellExpandedSection(
                visible = topSlot != null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onSizeChanged { topOccupiedPx = it.height.toFloat() },
            ) {
                effectiveTopSlot?.let { slot ->
                    if (slot.content.isDraggable) {
                        TaskShellTopDraggableRegion(
                            slot = slot,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        TaskShellTopRegion(
                            slot = slot,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
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

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(animatedHeight)
                .clip(TaskShellBottomShape),
    ) {
        with(slot.content) { Background(shellScope) }

        Column(modifier = Modifier.fillMaxSize()) {
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
            bottomCompactPx + (fullHeightPx - bottomCompactPx) * 0.24f
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

    val rawSheetHeightPx = draggableState.requireOffset()
    val sheetHeightPx =
        if (draggableState.currentValue == TaskShellExpansion.Collapsed) {
            bottomCompactPx
        } else {
            rawSheetHeightPx
        }

    val showExpanded = slot.expansion != TaskShellExpansion.Collapsed

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
            draggableState.snapTo(slot.expansion)
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
                    draggableState.snapTo(TaskShellExpansion.Collapsed)
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
                    draggableState.snapTo(TaskShellExpansion.Full)
                }
            },
        )

    val expansionProgress =
        remember(sheetHeightPx, bottomCompactPx, fullHeightPx) {
            ((sheetHeightPx - bottomCompactPx) / (fullHeightPx - bottomCompactPx).coerceAtLeast(1f))
                .coerceIn(0f, 1f)
        }
    val expandedAlpha by animateFloatAsState(
        targetValue = if (showExpanded) expansionProgress.coerceAtLeast(0.08f) else 0f,
        animationSpec = ShellFadeInTween,
        label = "bottomDraggableExpandedAlpha",
    )

    val onCompactTap: () -> Unit = {
        when (slot.content.compactClick) {
            TaskShellCompactClick.Toggle -> {
                if (slot.expansion == TaskShellExpansion.Collapsed) {
                    shellScope.onToggle()
                } else {
                    shellScope.onMinimize()
                }
            }

            TaskShellCompactClick.RequestFull -> {
                if (slot.expansion == TaskShellExpansion.Collapsed) {
                    shellScope.onRequestFull()
                } else {
                    shellScope.onMinimize()
                }
            }

            TaskShellCompactClick.None -> {
                Unit
            }
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(with(density) { sheetHeightPx.toDp() })
                .clip(TaskShellBottomShape),
    ) {
        with(slot.content) { Background(shellScope) }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .pointerInput(slot.expansion, slot.content.compactClick) {
                            detectTapGestures { onCompactTap() }
                        }.anchoredDraggable(
                            state = draggableState,
                            orientation = Orientation.Vertical,
                            reverseDirection = true,
                        ).onSizeChanged { bottomCompactPx = it.height.toFloat() },
            ) {
                slot.content.Compact(shellScope)
            }

            if (showExpanded && slot.content.isExpandable) {
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
}

private val TopSwipeTravel = 32.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskShellTopDraggableRegion(
    slot: TaskShellSlotState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val swipeTravelPx = with(density) { TopSwipeTravel.toPx() }

    val anchors =
        remember(swipeTravelPx) {
            DraggableAnchors {
                TaskShellExpansion.Collapsed at 0f
                TaskShellExpansion.Expanded at swipeTravelPx
            }
        }

    val draggableState =
        remember(anchors, slot.expansion) {
            AnchoredDraggableState(
                initialValue = slot.expansion,
                anchors = anchors,
            )
        }

    androidx.compose.runtime.LaunchedEffect(slot.expansion) {
        if (draggableState.currentValue != slot.expansion) {
            draggableState.snapTo(slot.expansion)
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
                scope.launch { draggableState.animateTo(TaskShellExpansion.Collapsed) }
            },
            onToggle = {
                scope.launch {
                    val target =
                        if (slot.expansion == TaskShellExpansion.Collapsed) {
                            TaskShellExpansion.Expanded
                        } else {
                            TaskShellExpansion.Collapsed
                        }
                    draggableState.animateTo(target, animationSpec = ShellMotionTween)
                }
            },
            onRequestFull = {
                scope.launch { draggableState.animateTo(TaskShellExpansion.Expanded) }
            },
        )

    val onCompactTap: () -> Unit = {
        when (slot.content.compactClick) {
            TaskShellCompactClick.Toggle -> {
                if (slot.expansion == TaskShellExpansion.Collapsed) {
                    shellScope.onToggle()
                } else {
                    shellScope.onMinimize()
                }
            }

            TaskShellCompactClick.RequestFull -> {
                if (slot.expansion == TaskShellExpansion.Collapsed) {
                    shellScope.onRequestFull()
                } else {
                    shellScope.onMinimize()
                }
            }

            TaskShellCompactClick.None -> {
                Unit
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        with(slot.content) { Background(shellScope) }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(ShellSizeTween),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .pointerInput(slot.expansion, slot.content.compactClick) {
                            detectTapGestures { onCompactTap() }
                        }.anchoredDraggable(
                            state = draggableState,
                            orientation = Orientation.Vertical,
                        ),
            ) {
                slot.content.Compact(shellScope)
            }

            if (slot.content.isExpandable) {
                TaskShellExpandedSection(visible = slot.expansion != TaskShellExpansion.Collapsed) {
                    slot.content.Expanded(shellScope)
                }
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
            with(slot.content) { Background(shellScope) }
            slot.content.Compact(shellScope)
        }
        return
    }

    Box(modifier = modifier.fillMaxWidth()) {
        with(slot.content) { Background(shellScope) }

        Column(
            modifier =
                Modifier
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
}
