package com.ilustris.sagai.ui.components.taskshell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val CompactFallbackHeight = 56.dp
private val ExpandedRevealThresholdPx = 24f

private val ShellHeightTween = tween<Dp>(400, easing = EaseIn)
private val ShellSizeTween = tween<IntSize>(200, easing = FastOutSlowInEasing)
private val ShellMotionTween = tween<Float>(300, easing = EaseInExpo)
private val ShellFadeInTween = tween<Float>(120, delayMillis = 100, easing = FastOutSlowInEasing)
private val ShellFadeOutTween = tween<Float>(400, easing = FastOutSlowInEasing)

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

/**
 * Coarse swipe-to-collapse gesture for an expanded shell body: a large, forgiving target
 * (the whole visible content, not just the compact header) that dismisses the shell on a
 * committed swipe toward its collapsed edge — [collapseDirection] is `-1f` for a top shell
 * (swipe up retracts) or `1f` for a bottom shell (swipe down retracts).
 *
 * Doesn't consume any pointer movement until the drag has cleared [touchSlopPx] *in the
 * collapse direction*, so a scrollable `Expanded()` body (e.g. long intro text) keeps
 * ordinary scroll gestures — only a movement big enough and pointed the right way gets
 * claimed by this gesture instead.
 */
private fun Modifier.swipeToCollapse(
    active: Boolean,
    touchSlopPx: Float,
    thresholdPx: Float,
    collapseDirection: Float,
    onCollapse: () -> Unit,
): Modifier =
    if (!active) {
        this
    } else {
        this.pointerInput(collapseDirection) {
            var accumulated = 0f
            var committed = false
            detectVerticalDragGestures(
                onDragStart = {
                    accumulated = 0f
                    committed = false
                },
                onVerticalDrag = { change, dragAmount ->
                    accumulated += dragAmount
                    if (!committed && accumulated * collapseDirection > touchSlopPx) {
                        committed = true
                    }
                    if (committed) {
                        change.consume()
                    }
                },
                onDragEnd = {
                    if (accumulated * collapseDirection > thresholdPx) {
                        onCollapse()
                    }
                },
            )
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskShellLayout(
    modifier: Modifier = Modifier,
    horizontalInset: Dp = 0.dp,
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
    var bottomOccupiedPx by remember { mutableFloatStateOf(0f) }

    var lastTopSlot by remember { mutableStateOf(topSlot) }
    LaunchedEffect(topSlot) {
        if (topSlot != null) lastTopSlot = topSlot
    }
    val effectiveTopSlot = topSlot ?: lastTopSlot

    LaunchedEffect(bottomSlot) {
        if (bottomSlot == null) bottomOccupiedPx = 0f
    }

    // Only one slot should ever own the whole screen at a time.
    LaunchedEffect(topSlot?.expansion) {
        if (topSlot?.expansion == TaskShellExpansion.Full &&
            bottomSlot?.expansion != null &&
            bottomSlot.expansion != TaskShellExpansion.Collapsed
        ) {
            bottomSlot.onExpansionChange(TaskShellExpansion.Collapsed)
        }
    }
    LaunchedEffect(bottomSlot?.expansion) {
        if (bottomSlot?.expansion == TaskShellExpansion.Full &&
            topSlot?.expansion != null &&
            topSlot.expansion != TaskShellExpansion.Collapsed
        ) {
            topSlot.onExpansionChange(TaskShellExpansion.Collapsed)
        }
    }

    // A slot's rounded content-card corner (MaterialTheme.shapes.large, rounded on every side)
    // needs *some* gap to read as a deliberate reveal of the background behind it — with zero
    // padding the curve just looks like a stray cut against whatever's on the other side of it.
    // So this stays non-zero any time a slot exists at all, not just while Expanded.
    val topPadding by animateDpAsState(
        when {
            topSlot == null -> 0.dp
            topSlot.expansion == TaskShellExpansion.Expanded -> 8.dp
            else -> 4.dp
        },
    )

    val bottomPadding by animateDpAsState(
        when {
            bottomSlot == null -> 0.dp
            bottomSlot.expansion == TaskShellExpansion.Expanded -> 8.dp
            else -> 4.dp
        },
    )

    Box(modifier = modifier.fillMaxSize()) {
        background?.invoke(this, topSlot, bottomSlot)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .animateContentSize(
                        tween(500, easing = EaseIn),
                    ),
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
                            bottomOccupiedPx = bottomOccupiedPx,
                            screenHeightPx = screenHeightPx,
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
                        .clip(MaterialTheme.shapes.large)
                        .weight(1f)
                        .background(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.shapes.large,
                        ).padding(
                            top = topPadding,
                            bottom = bottomPadding,
                            start = horizontalInset,
                            end = horizontalInset,
                        ),
            ) {
                content()
            }

            bottomSlot?.let {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .onSizeChanged { bottomOccupiedPx = it.height.toFloat() },
                ) {
                    if (it.content.isDraggable) {
                        TaskShellBottomDraggableRegion(
                            slot = it,
                            topOccupiedPx = topOccupiedPx,
                            screenHeightPx = screenHeightPx,
                        )
                    } else {
                        TaskShellBottomStateRegion(
                            slot = it,
                            topOccupiedPx = topOccupiedPx,
                            screenHeightPx = screenHeightPx,
                        )
                    }
                }
            }

            AnimatedContent(bottomSlot, transitionSpec = {
                fadeIn(animationSpec = ShellFadeInTween) togetherWith fadeOut(animationSpec = ShellFadeOutTween)
            }) {
                it?.let { slot ->
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

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                // Only lock the height once actually expanding — while collapsed, this must
                // stay wrap-content so Compact can genuinely self-measure via onSizeChanged
                // below instead of being pre-clipped to whatever compactHeightPx last was.
                .then(if (isExpanded) Modifier.height(animatedHeight) else Modifier)
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

    LaunchedEffect(draggableState.offset, bottomCompactPx) {
        if (
            draggableState.currentValue == TaskShellExpansion.Collapsed &&
            sheetHeightPx > bottomCompactPx + dragSnapThresholdPx
        ) {
            draggableState.snapTo(TaskShellExpansion.Full)
        }
    }

    LaunchedEffect(slot.expansion) {
        if (draggableState.currentValue != slot.expansion) {
            draggableState.snapTo(slot.expansion)
        }
    }

    LaunchedEffect(draggableState.currentValue) {
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

    val collapseSwipeThresholdPx = with(density) { CollapseSwipeThreshold.toPx() }
    val touchSlopPx = LocalViewConfiguration.current.touchSlop
    val expandedBodyModifier =
        Modifier.swipeToCollapse(
            active = showExpanded,
            touchSlopPx = touchSlopPx,
            thresholdPx = collapseSwipeThresholdPx,
            collapseDirection = 1f,
            onCollapse = shellScope.onMinimize,
        )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                // Only lock the height once actually dragged off Collapsed — while collapsed,
                // this must stay wrap-content so Compact can genuinely self-measure via
                // onSizeChanged below instead of being pre-clipped to whatever bottomCompactPx
                // last was.
                .then(
                    if (draggableState.currentValue == TaskShellExpansion.Collapsed) {
                        Modifier
                    } else {
                        Modifier.height(with(density) { sheetHeightPx.toDp() })
                    },
                ).clip(TaskShellBottomShape),
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
                        .alpha(expandedAlpha)
                        .then(expandedBodyModifier),
            ) {
                slot.content.Expanded(shellScope)
            }
        }
    }
}

private val TopSwipeTravel = 32.dp
private val CollapseSwipeThreshold = 56.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskShellTopDraggableRegion(
    slot: TaskShellSlotState,
    bottomOccupiedPx: Float,
    screenHeightPx: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val swipeTravelPx = with(density) { TopSwipeTravel.toPx() }
    val isFull = slot.expansion == TaskShellExpansion.Full

    val anchors =
        remember(swipeTravelPx) {
            DraggableAnchors {
                TaskShellExpansion.Collapsed at 0f
                TaskShellExpansion.Expanded at swipeTravelPx
            }
        }

    // Full isn't one of this region's drag anchors (it's reached only programmatically, via
    // onRequestFull or the swipe-to-collapse gesture below) — map it to Expanded here so the
    // gesture recognizer itself never sees an undefined anchor.
    val draggableState =
        remember(anchors, slot.expansion) {
            AnchoredDraggableState(
                initialValue = if (isFull) TaskShellExpansion.Expanded else slot.expansion,
                anchors = anchors,
            )
        }

    LaunchedEffect(slot.expansion) {
        val draggableTarget = if (isFull) TaskShellExpansion.Expanded else slot.expansion
        if (draggableState.currentValue != draggableTarget) {
            draggableState.snapTo(draggableTarget)
        }
    }

    // Only propagate real user drags back into slot state; while Full, the header's tiny
    // Collapsed/Expanded anchors aren't meaningful and must not downgrade it.
    LaunchedEffect(draggableState.currentValue) {
        if (isFull) return@LaunchedEffect
        if (draggableState.currentValue != slot.expansion) {
            slot.onExpansionChange(draggableState.currentValue)
        }
    }

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

    val fullHeightPx =
        (screenHeightPx - bottomOccupiedPx).coerceAtLeast(with(density) { CompactFallbackHeight.toPx() })
    val animatedFullHeight by animateDpAsState(
        targetValue = with(density) { fullHeightPx.toDp() },
        animationSpec = ShellHeightTween,
        label = "topShellFullHeight",
    )
    val collapseSwipeThresholdPx = with(density) { CollapseSwipeThreshold.toPx() }
    val touchSlopPx = LocalViewConfiguration.current.touchSlop
    val expandedBodyDragModifier =
        Modifier.swipeToCollapse(
            active = slot.expansion != TaskShellExpansion.Collapsed,
            touchSlopPx = touchSlopPx,
            thresholdPx = collapseSwipeThresholdPx,
            collapseDirection = -1f,
            onCollapse = shellScope.onMinimize,
        )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (isFull) Modifier.height(animatedFullHeight) else Modifier)
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
                // .weight(1f) only makes sense once the Column has a bounded height
                // (the explicit .height(animatedFullHeight) above when isFull) —
                // without it, fillMaxSize() content collapses to zero height inside
                // the otherwise wrap-content Column.
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .then(if (isFull) Modifier.weight(1f) else Modifier)
                            .then(expandedBodyDragModifier),
                ) {
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
