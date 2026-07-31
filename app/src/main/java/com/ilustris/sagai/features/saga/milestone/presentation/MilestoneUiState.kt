package com.ilustris.sagai.features.saga.milestone.presentation

import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

/** What the Milestone screen shows right now. Closures are stepped ("2 de 3"); introductions
 * and the loading state between chain steps are not — see the milestone redesign discussion
 * for why those two tones are kept visually separate. */
sealed class MilestoneUiState {
    data class Loading(
        val reasoning: String? = null,
    ) : MilestoneUiState()

    data class ClosureStep(
        val milestone: SagaMilestone,
        val stepIndex: Int,
        val stepTotal: Int,
    ) : MilestoneUiState()

    data class IntroductionStep(
        val milestone: SagaMilestone.Introduction,
    ) : MilestoneUiState()
}
