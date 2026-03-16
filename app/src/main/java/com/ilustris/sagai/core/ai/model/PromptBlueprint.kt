package com.ilustris.sagai.core.ai.model

data class PromptBlueprint(
    val role: String = "",
    val template: String = "",
    val directives: Map<String, String> = emptyMap(),
    val rules: Map<String, String> = emptyMap(),
)
