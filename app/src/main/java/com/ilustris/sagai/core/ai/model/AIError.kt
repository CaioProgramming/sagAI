package com.ilustris.sagai.core.ai.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AIError(
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: SafeGuard,
)
