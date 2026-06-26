package com.ilustris.sagai.core.ai.local

import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.services.RemoteConfigService

data class LocalAiConfig(
    val enabled: Boolean = false,
    val tiers: Set<ModelRequirement> = DEFAULT_TIERS,
    val timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    val maxPromptChars: Int = DEFAULT_MAX_PROMPT_CHARS,
) {
    companion object {
        val DEFAULT_TIERS = setOf(ModelRequirement.MINIMAL, ModelRequirement.LOW)
        const val DEFAULT_TIMEOUT_MS = 4_000L
        const val DEFAULT_MAX_PROMPT_CHARS = 10_000
        const val LOCAL_MODEL_AUDIT_NAME = "gemini-nano-local"
        const val MAX_OUTPUT_TOKENS = 256
    }
}

class LocalAiConfigLoader(
    private val remoteConfigService: RemoteConfigService,
) {
    suspend fun load(): LocalAiConfig {
        val enabled = remoteConfigService.getBoolean(KEY_ENABLED) ?: false
        val tierNames =
            remoteConfigService.getJsonList(KEY_TIERS, String::class.java, logEnabled = false)
                ?: emptyList()
        val tiers =
            tierNames
                .mapNotNull { name ->
                    runCatching { ModelRequirement.valueOf(name.uppercase()) }.getOrNull()
                }.toSet()
                .ifEmpty { LocalAiConfig.DEFAULT_TIERS }
        val timeoutMs =
            remoteConfigService.getLong(KEY_TIMEOUT_MS)?.takeIf { it > 0 }
                ?: LocalAiConfig.DEFAULT_TIMEOUT_MS
        val maxPromptChars =
            remoteConfigService
                .getLong(KEY_MAX_PROMPT_CHARS)
                ?.toInt()
                ?.takeIf { it > 0 }
                ?: LocalAiConfig.DEFAULT_MAX_PROMPT_CHARS
        return LocalAiConfig(
            enabled = enabled,
            tiers = tiers,
            timeoutMs = timeoutMs,
            maxPromptChars = maxPromptChars,
        )
    }

    companion object {
        const val KEY_ENABLED = "local_ai_enabled"
        const val KEY_TIERS = "local_ai_tiers"
        const val KEY_TIMEOUT_MS = "local_ai_timeout_ms"
        const val KEY_MAX_PROMPT_CHARS = "local_ai_max_prompt_chars"
    }
}
