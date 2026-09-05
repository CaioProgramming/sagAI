package com.ilustris.sagai.core.ai.local

import com.ilustris.sagai.core.ai.GeminiGenerationAuditContext
import com.ilustris.sagai.core.ai.GeminiSyncGenerationParams
import com.ilustris.sagai.core.ai.ModelRequirement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAiEligibilityTest {
    private val baseParams =
        GeminiSyncGenerationParams(
            model = "gemini-test",
            requirement = ModelRequirement.LOW,
            logEnabled = false,
            taskPrompt = "short prompt",
            systemInstruction = "system",
            references = emptyList(),
            temperatureRandomness = 0.1f,
            thinkingLevel = null,
            audit =
                GeminiGenerationAuditContext(
                    dataType = "TestType",
                    systemInstruction = "system",
                ),
            promptForFailureLog = "short prompt",
        )

    private val enabledConfig =
        LocalAiConfig(
            enabled = true,
            tiers = setOf(ModelRequirement.MINIMAL, ModelRequirement.LOW),
        )

    @Test
    fun eligible_whenEnabledAndLowTier() {
        assertTrue(LocalAiEligibility.isEligible(baseParams, enabledConfig))
    }

    @Test
    fun notEligible_whenDisabled() {
        assertFalse(
            LocalAiEligibility.isEligible(
                baseParams,
                enabledConfig.copy(enabled = false),
            ),
        )
    }

    @Test
    fun notEligible_whenTierExcluded() {
        assertFalse(
            LocalAiEligibility.isEligible(
                baseParams,
                enabledConfig.copy(tiers = setOf(ModelRequirement.MINIMAL)),
            ),
        )
    }

    @Test
    fun notEligible_whenPromptTooLong() {
        assertFalse(
            LocalAiEligibility.isEligible(
                baseParams.copy(taskPrompt = "x".repeat(20_000)),
                enabledConfig.copy(maxPromptChars = 100),
            ),
        )
    }

    @Test
    fun referencesAreEmpty_whenOnlyNullSlots() {
        assertTrue(LocalAiEligibility.referencesAreEmpty(listOf(null, null)))
    }
}

class LocalAiSidebackRoutingTest {
    @Test
    fun routesAvailableToTryLocal() {
        assertEquals(
            LocalAiSidebackStep.TRY_LOCAL,
            LocalAiSidebackRouting.resolveStep(LocalAiAvailability.AVAILABLE),
        )
    }

    @Test
    fun routesDownloadableToTriggerDownloadAndCloud() {
        assertEquals(
            LocalAiSidebackStep.TRIGGER_DOWNLOAD_AND_CLOUD,
            LocalAiSidebackRouting.resolveStep(LocalAiAvailability.DOWNLOADABLE),
        )
    }

    @Test
    fun routesDownloadingToCloudOnly() {
        assertEquals(
            LocalAiSidebackStep.CLOUD_ONLY,
            LocalAiSidebackRouting.resolveStep(LocalAiAvailability.DOWNLOADING),
        )
    }

    @Test
    fun routesUnavailableToCloudOnly() {
        assertEquals(
            LocalAiSidebackStep.CLOUD_ONLY,
            LocalAiSidebackRouting.resolveStep(LocalAiAvailability.UNAVAILABLE),
        )
    }
}
