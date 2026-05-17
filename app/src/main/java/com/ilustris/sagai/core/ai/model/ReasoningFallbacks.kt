package com.ilustris.sagai.core.ai.model

import androidx.annotation.Keep

@Keep
data class ReasoningFallbacks(
    val default: List<String> = emptyList(),
    val genres: Map<String, List<String>> = emptyMap(),
)
