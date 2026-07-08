package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.presentation.model.toUi
import com.ilustris.sagai.ui.components.taskshell.TaskShellChevron
import com.ilustris.sagai.ui.components.taskshell.TaskShellCompactClick
import com.ilustris.sagai.ui.components.taskshell.TaskShellContent
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpansion
import com.ilustris.sagai.ui.components.taskshell.TaskShellScope
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.themePainter

class ObjectiveShellContent(
    private val title: String,
    private val objective: String,
    private val progress: Float,
    private val isLoading: Boolean = false,
) : TaskShellContent {
    override val isExpandable: Boolean = true
    override val isDraggable: Boolean = false
    override val compactClick: TaskShellCompactClick = TaskShellCompactClick.Toggle

    @Composable
    override fun Compact(scope: TaskShellScope) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
            Icon(
                themePainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .size(24.dp)
                        .reactiveShimmer(
                            isPlaying = isLoading,
                            shimmerColors = MaterialTheme.colorScheme.primary.shimmerize(),
                        ),
            )
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        ObjectiveOverlay(
            title = title,
            objective = objective,
            progress = progress,
            applyStatusBarsPadding = false,
            modifier = Modifier.fillMaxWidth(),
            onDismiss = scope.onMinimize,
        )
    }
}

class NarrativeAdvanceShellContent(
    private val action: NarrativeAction,
    private val reasoning: String?,
    private val isProcessing: Boolean,
    private val dragProgress: Float,
) : TaskShellContent {
    override val isExpandable: Boolean = true
    override val isDraggable: Boolean = !isProcessing
    override val compactClick: TaskShellCompactClick = TaskShellCompactClick.RequestFull

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val actionUi = action.toUi()
        val titleRes = if (isProcessing) actionUi.holdingTextRes else (actionUi.titleRes ?: R.string.continue_text)

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                themePainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!isProcessing) {
                    Text(
                        text = stringResource(R.string.advance_pull_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            TaskShellChevron(
                isExpanded = scope.expansion != TaskShellExpansion.Collapsed,
                onClick = scope.onRequestFull,
            )
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        val actionUi = action.toUi()
        val titleRes = if (isProcessing) actionUi.holdingTextRes else (actionUi.titleRes ?: R.string.continue_text)

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                themePainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )

            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )

            reasoning?.let { chunk ->
                Text(
                    text = chunk,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (!isProcessing) {
                Text(
                    text =
                        stringResource(
                            if (dragProgress > 0.85f) {
                                R.string.narrative_advance_shell_release
                            } else {
                                R.string.advance_pull_hint
                            },
                        ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
