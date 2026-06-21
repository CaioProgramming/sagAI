package com.ilustris.sagai.features.brain.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.themeIcon
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private val SheetCollapsedHeight = 108.dp

@Composable
fun BrainDetailSheet(
    displayNode: BrainNode?,
    pagerNodes: List<BrainNode>,
    selectedNodeId: String?,
    sceneFocusId: String?,
    onPagerNodeSelected: (String) -> Unit,
    onRecenter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (displayNode == null || pagerNodes.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val sheetExpandedHeight = screenHeight * 0.52f

    val targetHeight = if (expanded) sheetExpandedHeight else SheetCollapsedHeight
    val sheetHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(320),
        label = "brainSheetHeight",
    )

    val pagerKey = remember(pagerNodes) { pagerNodes.joinToString("|") { it.id } }
    val pagerIds = remember(pagerNodes) { pagerNodes.map { it.id }.toSet() }
    val showingSatellite = displayNode.id !in pagerIds

    key(pagerKey) {
        val anchorId = selectedNodeId ?: sceneFocusId
        val initialPage =
            pagerNodes.indexOfFirst { it.id == anchorId }.coerceAtLeast(0)
        val pagerState = rememberPagerState(initialPage = initialPage) { pagerNodes.size }

        LaunchedEffect(selectedNodeId, pagerKey) {
            val target = pagerNodes.indexOfFirst { it.id == selectedNodeId }
            if (target >= 0 && target != pagerState.currentPage && !pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(target)
            }
        }

        LaunchedEffect(sceneFocusId, pagerKey) {
            if (selectedNodeId != null) return@LaunchedEffect
            val target = pagerNodes.indexOfFirst { it.id == sceneFocusId }
            if (target >= 0 && target != pagerState.currentPage && !pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(target)
            }
        }

        LaunchedEffect(pagerState, pagerKey) {
            snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
                .filter { !it.second }
                .distinctUntilChanged { old, new -> old.first == new.first }
                .collect { (page, _) ->
                    pagerNodes.getOrNull(page)?.let { pathNode ->
                        if (pathNode.id != selectedNodeId) {
                            expanded = true
                            onPagerNodeSelected(pathNode.id)
                        }
                    }
                }
        }

        Column(
            modifier =
                modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .navigationBarsPadding()
                    .clip(sagaShape())
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .pointerInput(expanded) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                when {
                                    dragOffset < -48f -> expanded = true
                                    dragOffset > 48f -> expanded = false
                                }
                                dragOffset = 0f
                            },
                            onVerticalDrag = { _, dragAmount ->
                                dragOffset += dragAmount
                            },
                        )
                    },
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onRecenter) {
                    Icon(
                        painter = themeIcon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            if (showingSatellite) {
                BrainDetailSheetPage(
                    node = displayNode,
                    expanded = expanded,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    userScrollEnabled = pagerNodes.size > 1,
                ) { page ->
                    BrainDetailSheetPage(
                        node = pagerNodes[page],
                        expanded = expanded,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun BrainDetailSheetPage(
    node: BrainNode,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp)
                .verticalScroll(scrollState, enabled = expanded),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = node.label,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = if (expanded) Int.MAX_VALUE else 1,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
        )
        if (node.subtitle.isNotBlank() && expanded) {
            Text(
                text = node.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
        }
        val body = node.detailBody.ifBlank { node.subtitle }
        if (body.isNotBlank()) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            )
        }
    }
}
