package com.ilustris.sagai.core.ai.local

enum class LocalAiSidebackStep {
    TRY_LOCAL,
    TRIGGER_DOWNLOAD_AND_CLOUD,
    CLOUD_ONLY,
}

object LocalAiSidebackRouting {
    fun resolveStep(availability: LocalAiAvailability): LocalAiSidebackStep =
        when (availability) {
            LocalAiAvailability.AVAILABLE -> LocalAiSidebackStep.TRY_LOCAL

            LocalAiAvailability.DOWNLOADABLE -> LocalAiSidebackStep.TRIGGER_DOWNLOAD_AND_CLOUD

            LocalAiAvailability.DOWNLOADING,
            LocalAiAvailability.UNAVAILABLE,
            -> LocalAiSidebackStep.CLOUD_ONLY
        }
}
