package com.ilustris.sagai.features.brain.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.ui.theme.components.MorphingThemeIcon
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

fun BrainNode.showsSubtitleInSheet(): Boolean =
    when (type) {
        BrainNodeType.CHARACTER, BrainNodeType.RELATION -> subtitle.isNotBlank()
        else -> false
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainDetailSheet(
    displayNode: BrainNode?,
    pagerNodes: List<BrainNode>,
    selectedNodeId: String?,
    sceneFocusId: String?,
    sheetState: SheetState,
    onPagerNodeSelected: (String) -> Unit,
    onRecenter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (displayNode == null || pagerNodes.isEmpty()) return

    val isExpanded by remember {
        derivedStateOf { sheetState.currentValue == SheetValue.Expanded }
    }

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
                            onPagerNodeSelected(pathNode.id)
                        }
                    }
                }
        }

        Column(modifier = modifier) {
            if (showingSatellite) {
                BrainDetailSheetPage(
                    node = displayNode,
                    isExpanded = isExpanded,
                    onRecenter = onRecenter,
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    userScrollEnabled = pagerNodes.size > 1 && isExpanded,
                ) { page ->
                    BrainDetailSheetPage(
                        node = pagerNodes[page],
                        isExpanded = isExpanded,
                        onRecenter = onRecenter,
                    )
                }
            }
        }
    }
}

@Composable
private fun BrainDetailSheetPage(
    node: BrainNode,
    isExpanded: Boolean,
    onRecenter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (isExpanded) {
                        Modifier.verticalScroll(rememberScrollState())
                    } else {
                        Modifier.wrapContentSize()
                    },
                )
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SharedTransitionLayout {
            AnimatedContent(isExpanded) {
                if (it) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = onRecenter, enabled = isExpanded) {
                            MorphingThemeIcon(
                                modifier =
                                    Modifier
                                        .size(24.dp)
                                        .sharedElement(
                                            rememberSharedContentState(key = "brain_recenter"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                        ),
                                tint = MaterialTheme.colorScheme.primary,
                                glowIntensity = 0.35f,
                            )
                        }

                        Text(
                            text = node.label,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            maxLines = if (isExpanded) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .sharedElement(
                                        rememberSharedContentState(key = "brain_title"),
                                        animatedVisibilityScope = this@AnimatedContent,
                                    ),
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        IconButton(onClick = onRecenter, enabled = isExpanded) {
                            MorphingThemeIcon(
                                modifier =
                                    Modifier
                                        .size(24.dp)
                                        .sharedElement(
                                            rememberSharedContentState(key = "brain_recenter"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                        ),
                                tint = MaterialTheme.colorScheme.primary,
                                glowIntensity = 0.35f,
                            )
                        }

                        Text(
                            text = node.label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .sharedElement(
                                        rememberSharedContentState(key = "brain_title"),
                                        animatedVisibilityScope = this@AnimatedContent,
                                    ),
                        )
                    }
                }
            }
        }

        if (node.showsSubtitleInSheet()) {
            Text(
                text = node.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
            )
        }

        if (node.detailBody.isNotBlank()) {
            Text(
                text = node.detailBody,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
