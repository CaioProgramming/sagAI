package com.ilustris.sagai.core.ai.model

import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class AIGeneration<T>(
    @SerializedName("reasoning")
    val reasoning: String,
    @SerializedName("data")
    val data: T,
)
