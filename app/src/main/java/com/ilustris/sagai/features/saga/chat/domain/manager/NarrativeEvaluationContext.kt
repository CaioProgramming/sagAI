package com.ilustris.sagai.features.saga.chat.domain.manager

data class NarrativeEvaluationContext(
    val isMilestoneActive: Boolean = false,
    val isNarrativeProcessing: Boolean = false,
    val hasActiveMilestoneOverlay: Boolean = false,
)
