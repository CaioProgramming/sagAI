package com.ilustris.sagai.features.saga.chat.domain.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NarrativeCoordinator
    @Inject
    constructor() {
        private val _uiState = MutableStateFlow(NarrativeUiState())
        val uiState: StateFlow<NarrativeUiState> = _uiState.asStateFlow()

        private var pendingReevaluation = false
        private var lastCompletedAction: NarrativeAction? = null

        fun reevaluate(
            nextResolvedAction: NarrativeAction?,
            context: NarrativeEvaluationContext,
        ): NarrativeUiState {
            if (context.isOnboardingVisible) {
                return _uiState.value
            }

            if (context.isNarrativeProcessing) {
                return _uiState.value
            }

            if (context.hasActiveMilestoneOverlay && !context.isMilestoneActive) {
                return _uiState.value
            }

            if (_uiState.value.phase is NarrativePhase.Processing ||
                _uiState.value.phase is NarrativePhase.BackgroundProcessing
            ) {
                pendingReevaluation = true
                return _uiState.value
            }

            if (context.isMilestoneActive || context.hasActiveMilestoneOverlay) {
                _uiState.update {
                    it.copy(
                        phase = NarrativePhase.MilestoneBlocking,
                        pendingAction = null,
                        backgroundTask = null,
                        lastError = null,
                    )
                }
                return _uiState.value
            }

            val newState =
                when (val nextAction = nextResolvedAction) {
                    null -> {
                        NarrativeUiState(
                            phase = NarrativePhase.Playing,
                            pendingAction = null,
                            backgroundTask = null,
                            lastError = null,
                            isProcessing = false,
                        )
                    }

                    is NarrativeAction.CloseTimeline -> {
                        NarrativeUiState(
                            phase = NarrativePhase.BackgroundProcessing(BackgroundTask.ClosingScene),
                            pendingAction = null,
                            backgroundTask = BackgroundTask.ClosingScene,
                            lastError = null,
                            isProcessing = false,
                        )
                    }

                    else -> {
                        NarrativeUiState(
                            phase = NarrativePhase.AwaitingAdvance(nextAction),
                            pendingAction = nextAction,
                            backgroundTask = null,
                            lastError = null,
                            isProcessing = false,
                        )
                    }
                }

            _uiState.value = newState
            pendingReevaluation = false
            return newState
        }

        fun onUserAdvanceRequested(action: NarrativeAction): NarrativeUiState {
            _uiState.update {
                it.copy(
                    phase = NarrativePhase.Processing(action),
                    pendingAction = null,
                    isProcessing = true,
                    lastError = null,
                )
            }
            return _uiState.value
        }

        fun onBackgroundTaskStarted(task: BackgroundTask): NarrativeUiState {
            _uiState.update {
                it.copy(
                    phase = NarrativePhase.BackgroundProcessing(task),
                    backgroundTask = task,
                    pendingAction = null,
                    isProcessing = true,
                )
            }
            return _uiState.value
        }

        fun onActionCompleted(
            action: NarrativeAction,
            result: NarrativeExecutionResult,
        ): NarrativeUiState {
            lastCompletedAction = action
            when (result) {
                is NarrativeExecutionResult.Success -> {
                    _uiState.update {
                        it.copy(
                            phase = NarrativePhase.Playing,
                            pendingAction = null,
                            backgroundTask = null,
                            isProcessing = false,
                            lastError = null,
                        )
                    }
                }

                is NarrativeExecutionResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            phase =
                                if (action.executionMode() == NarrativeExecutionMode.UserTriggered) {
                                    NarrativePhase.AwaitingAdvance(action)
                                } else {
                                    NarrativePhase.Playing
                                },
                            pendingAction =
                                if (action.executionMode() == NarrativeExecutionMode.UserTriggered) {
                                    action
                                } else {
                                    null
                                },
                            backgroundTask = null,
                            isProcessing = false,
                            lastError =
                                NarrativeError(
                                    action = action,
                                    message = result.message,
                                    canRetry = result.canRetry,
                                ),
                        )
                    }
                }
            }
            return _uiState.value
        }

        fun markProcessing(isProcessing: Boolean) {
            _uiState.update { it.copy(isProcessing = isProcessing) }
        }

        fun markMilestoneActive() {
            _uiState.update {
                it.copy(
                    phase = NarrativePhase.MilestoneBlocking,
                    pendingAction = null,
                    backgroundTask = null,
                )
            }
        }

        fun markMilestoneDismissed() {
            if (_uiState.value.phase is NarrativePhase.MilestoneBlocking) {
                _uiState.update {
                    it.copy(phase = NarrativePhase.Playing)
                }
            }
        }

        fun schedulePendingReevaluation() {
            pendingReevaluation = true
        }

        fun consumePendingReevaluation(): Boolean {
            val pending = pendingReevaluation
            pendingReevaluation = false
            return pending
        }

        fun clearError() {
            _uiState.update { it.copy(lastError = null) }
        }

        fun reset() {
            pendingReevaluation = false
            lastCompletedAction = null
            _uiState.value = NarrativeUiState()
        }
    }

fun NarrativeAction.executionMode(): NarrativeExecutionMode =
    when (this) {
        is NarrativeAction.CloseTimeline -> NarrativeExecutionMode.Automatic
        else -> NarrativeExecutionMode.UserTriggered
    }
