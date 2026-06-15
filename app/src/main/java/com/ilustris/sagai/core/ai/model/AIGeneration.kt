package com.ilustris.sagai.core.ai.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AIGeneration<T>(
    @SerializedName("data")
    val data: T?,
    @SerializedName("error")
    val error: AIError? = null,
)
