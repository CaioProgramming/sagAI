package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.vibrate
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import com.ilustris.sagai.features.milestone.presentation.MilestoneViewModel
import com.ilustris.sagai.features.saga.chat.presentation.ChatUiAction
import com.ilustris.sagai.features.saga.chat.presentation.ChatUiState
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.components.island.AdvanceIslandContent
import com.ilustris.sagai.ui.components.island.DynamicBottomIsland
import com.ilustris.sagai.ui.components.island.DynamicIslandOverlay
import com.ilustris.sagai.ui.components.island.IslandContent
import com.ilustris.sagai.ui.components.island.LoadingIslandContent
import com.ilustris.sagai.ui.components.island.ObjectiveIslandContent

@Composable
fun ChatTaskShellHost(
    uiState: ChatUiState,
    sagaContent: SagaMetadata,
    progress: Float,
    onAction: (ChatUiAction) -> Unit,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NarrativeAdvanceShellViewModel = hiltViewModel(),
    milestoneViewModel: MilestoneViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val narrativeState = uiState.narrativeUiState
    val reasoning by viewModel.reasoning.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    val milestone = uiState.milestone
    // Loading is now a compact-only bottom island (published below), not a full-screen slot.
    val milestoneLoading = milestone is SagaMilestone.Loading

    val objectiveText = sagaContent.getCurrentTimeLine()?.data?.displayObjective()
    val showObjectiveShell = !objectiveText.isNullOrBlank()

    val advanceAction = narrativeState.displayAdvanceAction
    val showAdvanceShell =
        narrativeState.showAdvanceTrigger &&
            advanceAction != null &&
            uiState.onboardingType == null &&
            !uiState.selectionState.isSelectionMode &&
            // Never offer "advance" while a reply is still generating — wait for it to land.
            !uiState.isGenerating

    // Bottom island: Loading (compact loader) takes precedence over the advance trigger, which is
    // itself gated by the chat's full context. Published while on screen; cleared on dispose.
    LaunchedEffect(milestoneLoading, showAdvanceShell, advanceAction, isProcessing, reasoning) {
        val content =
            when {
                milestoneLoading ->
                    LoadingIslandContent(
                        reasoning = reasoning ?: uiState.reasoningChunk,
                        genre = sagaContent.data.genre,
                    )

                showAdvanceShell && advanceAction != null ->
                    AdvanceIslandContent(
                        action = advanceAction,
                        reasoning = reasoning,
                        isProcessing = isProcessing,
                        genre = sagaContent.data.genre,
                        onAction = {
                            if (!isProcessing) {
                                context.vibrate(longArrayOf(0, 400))
                                viewModel.advanceNarrative()
                            }
                        },
                    )

                else -> null
            }
        viewModel.publishBottomIsland(content)
    }

    // Current objective is now a top island in the global overlay (Shell v2).
    LaunchedEffect(showObjectiveShell, objectiveText, progress) {
        val content =
            if (showObjectiveShell) {
                ObjectiveIslandContent(
                    titleRes = R.string.current_objective,
                    objective = objectiveText.orEmpty(),
                    genre = sagaContent.data.genre,
                )
            } else {
                null
            }
        viewModel.publishTopIsland(content)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.publishBottomIsland(null)
            viewModel.publishTopIsland(null)
        }
    }

    // TODO: Milestone full-screen reveals (Introduction, NewCharacter, NewEvent, etc.)
    // will be integrated here once MilestoneShellContent is refactored for IslandContent.
    // For now, milestones bypass the shell entirely (handled elsewhere).

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .imePadding(),
    ) {
        content()
    }
}
