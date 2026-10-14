package com.ilustris.sagai.features.brain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.brain.domain.model.glowColor

private fun BrainNodeType.labelRes(): Int =
    when (this) {
        BrainNodeType.SAGA -> R.string.saga_globe_type_saga
        BrainNodeType.ACT -> R.string.saga_globe_type_act
        BrainNodeType.CHAPTER -> R.string.saga_globe_type_chapter
        BrainNodeType.EVENT -> R.string.saga_globe_type_event
        BrainNodeType.CHARACTER -> R.string.saga_globe_type_character
        BrainNodeType.CHARACTER_EVENT -> R.string.saga_globe_type_character_event
        BrainNodeType.RELATION -> R.string.saga_globe_type_relation
        BrainNodeType.WIKI -> R.string.saga_globe_type_wiki
    }

/** Explains the selected globe node and lists what it is connected to. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrainGlobeCard(
    node: BrainNode,
    connections: List<BrainNode>,
    onClose: () -> Unit,
    onConnectionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenCharacter: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.94f),
    ) {
        Column(
            modifier =
                Modifier
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(node.glowColor().copy(alpha = 1f)),
                )
                Text(
                    text = stringResource(node.type.labelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClose) { Text(stringResource(R.string.saga_globe_close)) }
            }

            Text(
                text = node.label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (node.subtitle.isNotBlank()) {
                Text(
                    text = node.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
            }
            if (node.detailBody.isNotBlank()) {
                Text(
                    text = node.detailBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                )
            }
            if (onOpenCharacter != null) {
                TextButton(onClick = onOpenCharacter) { Text(stringResource(R.string.saga_brain_open_character)) }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

            if (connections.isEmpty()) {
                Text(
                    text = stringResource(R.string.saga_globe_no_connections),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            } else {
                Text(
                    text = stringResource(R.string.saga_brain_connections_count, connections.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    connections.take(MAX_CONNECTION_CHIPS).forEach { other ->
                        Row(
                            modifier =
                                Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    .clickable { onConnectionClick(other.id) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(other.glowColor().copy(alpha = 1f)),
                            )
                            Text(
                                text = other.label,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val MAX_CONNECTION_CHIPS = 14
