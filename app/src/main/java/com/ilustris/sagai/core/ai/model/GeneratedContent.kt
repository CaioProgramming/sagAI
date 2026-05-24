package com.ilustris.sagai.core.ai.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GeneratedContent<T>(
    @SerializedName("data")
    val data: T,
    @SerializedName("finalMessage")
    val finalMessage: String,
)
