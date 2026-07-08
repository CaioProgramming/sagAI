package com.ilustris.sagai.ui.components.taskshell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val CompactFallbackHeight = 56.dp
private val ExpandedRevealThresholdPx = 24f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskShellLayout(
    modifier: Modifier = Modifier,
    horizontalInset: Dp = 8.dp,
    shellBackground: Color = MaterialTheme.colorScheme.background,
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
    val scope = rememberCoroutineScope()

    var topOccupiedPx by remember { mutableFloatStateOf(0f) }
    var bottomCompactPx by remember { mutableFloatStateOf(with(density) { CompactFallbackHeight.toPx() }) }

    val contentPadding by animateDpAsState(
        if (topSlot != null || bottomSlot != null) 8.dp else 2.dp,
    )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(shellBackground)
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
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(contentPadding)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.background),
        ) {
            content()
        }

        bottomSlot?.let { slot ->
            val fullHeightPx = (screenHeightPx - topOccupiedPx).coerceAtLeast(bottomCompactPx)

            val anchors =
                remember(bottomCompactPx, fullHeightPx, slot.content.isDraggable) {
                    if (slot.content.isDraggable) {
                        DraggableAnchors {
                            TaskShellExpansion.Collapsed at bottomCompactPx
                            TaskShellExpansion.Full at fullHeightPx
                        }
                    } else {
                        DraggableAnchors {
                            TaskShellExpansion.Collapsed at bottomCompactPx
                        }
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
            val revealExpanded = sheetHeightPx > bottomCompactPx + ExpandedRevealThresholdPx

            androidx.compose.runtime.LaunchedEffect(slot.expansion) {
                if (draggableState.currentValue != slot.expansion) {
                    draggableState.animateTo(slot.expansion)
                }
            }

            androidx.compose.runtime.LaunchedEffect(draggableState.currentValue) {
                val mapped =
                    when (draggableState.currentValue) {
                        TaskShellExpansion.Collapsed -> TaskShellExpansion.Collapsed
                        TaskShellExpansion.Full -> TaskShellExpansion.Full
                        TaskShellExpansion.Expanded -> TaskShellExpansion.Expanded
                    }
                if (mapped != slot.expansion) {
                    slot.onExpansionChange(mapped)
                }
            }

            val shellScope =
                slot.rememberScope(
                    onMinimize = {
                        scope.launch {
                            draggableState.animateTo(TaskShellExpansion.Collapsed)
                        }
                    },
                    onToggle = {
                        scope.launch {
                            val target =
                                if (slot.expansion == TaskShellExpansion.Collapsed) {
                                    TaskShellExpansion.Full
                                } else {
                                    TaskShellExpansion.Collapsed
                                }
                            draggableState.animateTo(target)
                        }
                    },
                    onRequestFull = {
                        scope.launch {
                            draggableState.animateTo(TaskShellExpansion.Full)
                        }
                    },
                )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(with(density) { sheetHeightPx.toDp() })
                        .then(
                            if (slot.content.isDraggable) {
                                Modifier.anchoredDraggable(
                                    state = draggableState,
                                    orientation = Orientation.Vertical,
                                    reverseDirection = true,
                                )
                            } else {
                                Modifier
                            },
                        ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .onSizeChanged {
                                bottomCompactPx = it.height.toFloat()
                            },
                ) {
                    slot.content.Compact(shellScope)
                }

                if (revealExpanded && slot.content.isExpandable) {
                    val expandedAlpha by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = tween(280),
                        label = "bottomShellExpandedAlpha",
                    )
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .alpha(expandedAlpha),
                    ) {
                        Column(Modifier.fillMaxSize()) {
                            Box(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                            ) {
                                AnimatedContent(
                                    targetState = slot.expansion != TaskShellExpansion.Collapsed,
                                    transitionSpec = {
                                        fadeIn(tween(320)) togetherWith fadeOut(tween(240))
                                    },
                                    label = "bottomShellExpanded",
                                    modifier = Modifier.fillMaxSize(),
                                ) { showExpanded ->
                                    if (showExpanded) {
                                        slot.content.Expanded(shellScope)
                                    }
                                }
                            }
                        }
                    }
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
    val scope = rememberCoroutineScope()
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
                .animateContentSize(tween(320, easing = FastOutSlowInEasing)),
    ) {
        Box(Modifier.fillMaxWidth()) {
            slot.content.Compact(shellScope)
        }

        if (isExpanded) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    fadeIn(tween(280)) togetherWith fadeOut(tween(220))
                },
                label = "topShellExpanded",
            ) {
                if (it) {
                    slot.content.Expanded(shellScope)
                }
            }
            TaskShellMinimizeHandle(onMinimize = shellScope.onMinimize)
        }
    }
}
