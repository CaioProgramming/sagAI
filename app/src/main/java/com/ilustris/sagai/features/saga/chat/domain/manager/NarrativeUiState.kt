package com.ilustris.sagai.features.saga.chat.domain.manager

data class NarrativeUiState(
    val phase: NarrativePhase = NarrativePhase.Playing,
    val pendingAction: NarrativeAction? = null,
    val backgroundTask: BackgroundTask? = null,
    val lastError: NarrativeError? = null,
    val isProcessing: Boolean = false,
) {
    val showAdvanceTrigger: Boolean
        get() = pendingAction != null && phase is NarrativePhase.AwaitingAdvance

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
