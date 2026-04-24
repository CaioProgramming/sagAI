package com.ilustris.sagai.core.ai.model

import androidx.annotation.Keep

@Keep
data class AIGeneration<T>(
    val reasoning: String,
    val data: T,
)
