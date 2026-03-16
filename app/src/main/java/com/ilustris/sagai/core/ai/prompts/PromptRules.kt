package com.ilustris.sagai.core.ai.prompts

data class PromptRules(
    val rules: Map<String, String> = emptyMap(),
) {
    fun get(key: String): String = rules[key] ?: ""

    // No specific hardcoded rules here for now as they are all feature-specific
    // and should be in the feature prompts themselves.
}
