package com.ilustris.sagai.core.ai.prompts

data class PromptDirectives(
    val directives: Map<String, String> = emptyMap(),
) {
    fun get(key: String): String = directives[key] ?: ""

    val roleEmotionalReviewer get() = get("role_emotional_reviewer")
    val roleEmotionalCounselor get() = get("role_emotional_counselor")
    val characterResumeNoEvents get() = get("CHARACTER_RESUME_NO_EVENTS")
    val characterResumeNoRelationships get() = get("CHARACTER_RESUME_NO_RELATIONSHIPS")
}
