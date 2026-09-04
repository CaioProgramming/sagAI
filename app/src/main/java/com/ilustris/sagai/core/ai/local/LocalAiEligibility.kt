package com.ilustris.sagai.core.ai.local

import com.ilustris.sagai.core.ai.GeminiSyncGenerationParams
import com.ilustris.sagai.core.ai.model.ImageReference

object LocalAiEligibility {
    fun isEligible(
        params: GeminiSyncGenerationParams,
        config: LocalAiConfig,
    ): Boolean {
        if (!config.enabled) return false
        if (params.requirement !in config.tiers) return false
        if (params.references.any { it != null }) return false
        val promptLength =
            params.taskPrompt.length +
                params.references.filterNotNull().sumOf { it.description.length }
        return promptLength <= config.maxPromptChars
    }

    @Suppress("unused")
    fun referencesAreEmpty(references: List<ImageReference?>): Boolean = references.none { it != null }
}
