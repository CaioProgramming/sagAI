package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Expanded body for the objective island — just the objective text. The compact row above it
 * already carries the "Objetivo atual" label (swapped in via
 * [com.ilustris.sagai.ui.components.island.CompactIslandData.expandedLabelRes]) so this doesn't
 * repeat a title or icon of its own.
 */
@Composable
fun ObjectiveOverlay(
    objective: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    Text(
        text = objective,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
        modifier =
            modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clickable(onClick = onDismiss),
    )
}
