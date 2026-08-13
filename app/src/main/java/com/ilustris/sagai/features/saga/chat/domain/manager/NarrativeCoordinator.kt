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
            isAutomatic: Boolean = false,
        ): NarrativeUiState {
            // Deliberately not gated on context.isNarrativeProcessing (message/narrative generation
            // in flight) — that flag lives outside this coordinator and a scheduled reevaluation
            // could get stranded unconsumed until an unrelated event happened to ask again, leaving
            // the advance trigger stuck hidden until app restart or idle. The phase check right
            // below is the coordinator's own authoritative "something is actually running" signal
            // and is enough to avoid corrupting an in-flight Processing/BackgroundProcessing phase;
            // whether the user can *tap* the trigger while generation is still wrapping up is a
            // presentation concern already handled by disabling the button (see isProcessing on
            // AdvanceIslandContent), not something this function needs to hide state for.
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

                    else -> {
                        if (isAutomatic) {
                            NarrativeUiState(
                                phase = NarrativePhase.Processing(nextAction, isAutomatic = true),
                                pendingAction = null,
                                backgroundTask = null,
                                lastError = null,
                                isProcessing = true,
                            )
                        } else {
                            NarrativeUiState(
                                phase = NarrativePhase.AwaitingAdvance(nextAction),
                                pendingAction = nextAction,
                                backgroundTask = null,
                                lastError = null,
                                isProcessing = false,
                            )
                        }
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
                            phase = NarrativePhase.AwaitingAdvance(action),
                            pendingAction = action,
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

/** [NarrativeAction.CreateTimeline] is a pure local write (inherits the chapter's scene summary,
 * no AI call) — it runs automatically instead of waiting for a user tap. Every other action
 * involves a real generation request and stays user-triggered. */
fun NarrativeAction.executionMode(): NarrativeExecutionMode =
    when (this) {
        is NarrativeAction.CreateTimeline -> NarrativeExecutionMode.Automatic
        else -> NarrativeExecutionMode.UserTriggered
    }
