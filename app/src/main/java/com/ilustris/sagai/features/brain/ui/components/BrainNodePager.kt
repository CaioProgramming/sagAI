package com.ilustris.sagai.features.brain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.brain.domain.model.BrainMode
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.brain.domain.model.glowColor
import com.ilustris.sagai.ui.theme.sagaShape
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun BrainNodePager(
    nodes: List<BrainNode>,
    selectedNodeId: String?,
    mode: BrainMode,
    accentColor: Color,
    onPageChanged: (String) -> Unit,
    onExpandDetail: (BrainNode) -> Unit,
    onOpenCharacterBrain: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (nodes.isEmpty()) return

    val initialPage = nodes.indexOfFirst { it.id == selectedNodeId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { nodes.size }

    LaunchedEffect(selectedNodeId, nodes) {
        val target = nodes.indexOfFirst { it.id == selectedNodeId }
        if (target >= 0 && target != pagerState.currentPage) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                nodes.getOrNull(page)?.let { onPageChanged(it.id) }
            }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(140.dp),
        ) { page ->
            BrainNodeCard(
                node = nodes[page],
                accentColor = accentColor,
                onExpand = { onExpandDetail(nodes[page]) },
                onOpenCharacter =
                    nodes[page].characterId?.let { charId ->
                        { onOpenCharacterBrain(charId) }
                    },
                isCharacterBrain = mode == BrainMode.CHARACTER,
                showCharacterAction = mode == BrainMode.STORY && nodes[page].type == BrainNodeType.CHARACTER,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            nodes.forEachIndexed { index, node ->
                Box(
                    modifier =
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (index == pagerState.currentPage) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) {
                                    node.glowColor()
                                } else {
                                    Color.White.copy(alpha = 0.25f)
                                },
                            ),
                )
            }
        }
    }
}

@Composable
fun BrainNodeCard(
    node: BrainNode,
    accentColor: Color,
    onExpand: () -> Unit,
    onOpenCharacter: (() -> Unit)?,
    showCharacterAction: Boolean,
    isCharacterBrain: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(sagaShape())
                .border(
                    width = 1.dp,
                    brush =
                        Brush.linearGradient(
                            listOf(
                                node.glowColor().copy(alpha = 0.7f),
                                accentColor.copy(alpha = 0.4f),
                            ),
                        ),
                    shape = sagaShape(),
                ).background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(node.glowColor()),
            )
            Text(
                text = nodeTypeLabel(node.type),
                style = MaterialTheme.typography.labelSmall,
                color = node.glowColor(),
            )
        }
        Text(
            text = node.label,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (node.subtitle.isNotBlank()) {
            Text(
                text = node.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (node.detailBody.isNotBlank()) {
            Text(
                text = node.detailBody,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (node.connectedNodeIds.isNotEmpty()) {
            Text(
                text =
                    stringResource(
                        R.string.saga_brain_connections_count,
                        node.connectedNodeIds.size,
                    ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                showCharacterAction && onOpenCharacter != null -> {
                    TextButton(onClick = onOpenCharacter) {
                        Text(stringResource(R.string.saga_brain_open_character))
                    }
                }

                isCharacterBrain -> {
                    TextButton(onClick = onExpand) {
                        Text(stringResource(R.string.saga_brain_expand))
                    }
                }

                else -> {
                    TextButton(onClick = onExpand) {
                        Text(stringResource(R.string.saga_brain_expand))
                    }
                }
            }
        }
    }
}

private fun nodeTypeLabel(type: BrainNodeType): String =
    when (type) {
        BrainNodeType.SAGA -> "Saga"
        BrainNodeType.ACT -> "Act"
        BrainNodeType.CHAPTER -> "Chapter"
        BrainNodeType.EVENT -> "Event"
        BrainNodeType.CHARACTER -> "Character"
        BrainNodeType.RELATION -> "Relation"
        BrainNodeType.WIKI -> "Lore"
    }
