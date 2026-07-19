package com.ilustris.sagai.features.saga.chat.domain.manager

data class NarrativeUiState(
    val phase: NarrativePhase = NarrativePhase.Playing,
    val pendingAction: NarrativeAction? = null,
    val backgroundTask: BackgroundTask? = null,
    val lastError: NarrativeError? = null,
    val isProcessing: Boolean = false,
) {
    val displayAdvanceAction: NarrativeAction?
        get() =
            when (val currentPhase = phase) {
                is NarrativePhase.AwaitingAdvance -> pendingAction
                // Automatic actions (e.g. CreateTimeline) skip AwaitingAdvance entirely and jump
                // straight to Processing — they were never meant to surface a trigger, so this
                // must not expose the action for those, or the advance island shows a "ghost"
                // pill for a step the user never needed to confirm in the first place.
                is NarrativePhase.Processing -> currentPhase.action.takeIf { !currentPhase.isAutomatic }
                else -> null
            }

    val showAdvanceTrigger: Boolean
        get() =
            displayAdvanceAction != null &&
                (phase is NarrativePhase.AwaitingAdvance || phase is NarrativePhase.Processing)

    val showBackgroundBanner: Boolean
        get() = backgroundTask != null && phase is NarrativePhase.BackgroundProcessing
}

sealed class NarrativePhase {
    data object Playing : NarrativePhase()

    data class AwaitingAdvance(
        val action: NarrativeAction,
    ) : NarrativePhase()

    data class Processing(
        val action: NarrativeAction,
        val isAutomatic: Boolean = false,
    ) : NarrativePhase()

    data class BackgroundProcessing(
        val task: BackgroundTask,
    ) : NarrativePhase()

    data object MilestoneBlocking : NarrativePhase()
}

sealed class BackgroundTask {
    data object ClosingScene : BackgroundTask()
}

data class NarrativeError(
    val action: NarrativeAction,
    val message: String,
    val canRetry: Boolean = true,
)
