package com.ilustris.sagai.features.saga.milestone.presentation

import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

/** What the Milestone screen shows right now. Closures are stepped ("2 de 3"); introductions
 * and the loading state between chain steps are not — see the milestone redesign discussion
 * for why those two tones are kept visually separate. */
sealed class MilestoneUiState {
    data class Loading(
        val reasoning: String? = null,
        // True while narrativeUiState.phase is Processing(isAutomatic = true) — currently only
        // CreateTimeline. There's no reasoning stream for it (no AI call involved), so the
        // screen would otherwise sit on the generic loading copy for that brief beat; this lets
        // it show its own "adjusting a few things" line instead, Duolingo-lesson-transition
        // style, without moving the actual execution off SagaContentManagerImpl (see the
        // narrative-progression architecture discussion for why it has to stay there).
        val isAutomaticStep: Boolean = false,
    ) : MilestoneUiState()

    data class ClosureStep(
        val milestone: SagaMilestone,
        val stepIndex: Int,
        val stepTotal: Int,
    ) : MilestoneUiState()

    data class IntroductionStep(
        val milestone: SagaMilestone.Introduction,
    ) : MilestoneUiState()

    /** Surfaced when the chain's current step has a [com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeError]
     * attached — i.e. it already failed once and is sitting in AwaitingAdvance again, not
     * mid-flight. Retrying is always an explicit tap now: auto-re-driving a persistently failing
     * action (e.g. no network overnight) used to retry silently forever, which looked from the
     * outside exactly like being stuck loading with no way out. */
    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : MilestoneUiState()
}
