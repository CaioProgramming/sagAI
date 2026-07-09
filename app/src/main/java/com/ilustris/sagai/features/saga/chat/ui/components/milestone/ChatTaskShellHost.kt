package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.vibrate
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.saga.chat.presentation.ChatUiAction
import com.ilustris.sagai.features.saga.chat.presentation.ChatUiState
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpansion
import com.ilustris.sagai.ui.components.taskshell.TaskShellLayout
import com.ilustris.sagai.ui.components.taskshell.TaskShellSlotState

@Composable
fun ChatTaskShellHost(
    uiState: ChatUiState,
    sagaContent: SagaMetadata,
    progress: Float,
    onAction: (ChatUiAction) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NarrativeAdvanceShellViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val narrativeState = uiState.narrativeUiState
    val reasoning by viewModel.reasoning.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    var topExpansion by remember { mutableStateOf(TaskShellExpansion.Collapsed) }
    var bottomExpansion by remember { mutableStateOf(TaskShellExpansion.Collapsed) }
    var advanceDragProgress by remember { mutableFloatStateOf(0f) }

    val objectiveText = sagaContent.getCurrentTimeLine()?.data?.displayObjective()
    val showObjectiveShell = !objectiveText.isNullOrBlank()

    val showAdvanceShell =
        narrativeState.showAdvanceTrigger &&
            narrativeState.displayAdvanceAction != null &&
            uiState.onboardingType == null &&
            !uiState.selectionState.isSelectionMode

    LaunchedEffect(showObjectiveShell) {
        if (!showObjectiveShell) {
            topExpansion = TaskShellExpansion.Collapsed
        }
    }

    LaunchedEffect(showAdvanceShell, isProcessing) {
        if (!showAdvanceShell || isProcessing) {
            bottomExpansion = TaskShellExpansion.Collapsed
            advanceDragProgress = 0f
        }
    }

    val topSlot =
        if (showObjectiveShell) {
            TaskShellSlotState(
                content =
                    ObjectiveShellContent(
                        title = stringResource(R.string.current_objective),
                        objective = objectiveText.orEmpty(),
                        progress = progress,
                        isLoading = isProcessing,
                    ),
                expansion = topExpansion,
                onExpansionChange = { topExpansion = it },
            )
        } else {
            null
        }

    val bottomSlot =
        if (showAdvanceShell) {
            val action = narrativeState.displayAdvanceAction!!
            TaskShellSlotState(
                content =
                    NarrativeAdvanceShellContent(
                        action = action,
                        reasoning = reasoning,
                        isProcessing = isProcessing,
                        dragProgress = advanceDragProgress,
                    ),
                expansion = bottomExpansion,
                onExpansionChange = { expansion ->
                    if (expansion == TaskShellExpansion.Full && !isProcessing) {
                        context.vibrate(longArrayOf(0, 400))
                        viewModel.advanceNarrative()
                        bottomExpansion = TaskShellExpansion.Collapsed
                    } else {
                        bottomExpansion = expansion
                        advanceDragProgress =
                            when (expansion) {
                                TaskShellExpansion.Full -> 1f
                                TaskShellExpansion.Expanded -> 0.5f
                                TaskShellExpansion.Collapsed -> 0f
                            }
                    }
                },
            )
        } else {
            null
        }

    TaskShellLayout(
        modifier =
            modifier
                .navigationBarsPadding()
                .statusBarsPadding()
                .imePadding(),
        topSlot = topSlot,
        bottomSlot = bottomSlot,
        background = { top, bottom ->
            val isActive =
                (top != null && top.expansion != TaskShellExpansion.Collapsed) ||
                    (bottom != null && bottom.expansion != TaskShellExpansion.Collapsed)
            val backgroundColor by animateColorAsState(
                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(backgroundColor),
                    )
        },
        content = content,
    )
}
