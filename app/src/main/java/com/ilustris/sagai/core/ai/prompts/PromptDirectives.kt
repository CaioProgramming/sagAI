package com.ilustris.sagai.core.ai.prompts

data class PromptDirectives(
    val directives: Map<String, String> = emptyMap(),
) {
    fun get(key: String): String = directives[key] ?: ""

    /**
     * Structural/Agnostic bricks reused across many templates.
     */
    val jsonStringIntegrity get() = get("JSON_STRING_INTEGRITY")
    val outputJsonDirective get() = get("OUTPUT_JSON_DIRECTIVE")
    val preferredLanguage get() = get("PREFERRED_LANGUAGE")

    // Dynamic UI-specific shorthand
    val conversationHistory get() = get("conversationHistory")
    val recentContext get() = get("recentContext")
}
