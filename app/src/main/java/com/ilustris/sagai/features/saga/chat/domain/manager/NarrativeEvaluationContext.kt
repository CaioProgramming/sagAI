package com.ilustris.sagai.features.saga.chat.domain.manager

data class NarrativeEvaluationContext(
    val isOnboardingVisible: Boolean = false,
    val isMilestoneActive: Boolean = false,
    val isNarrativeProcessing: Boolean = false,
    val hasActiveMilestoneOverlay: Boolean = false,
)
