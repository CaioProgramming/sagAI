package com.ilustris.sagai.core.ai.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

// region Request Models

data class OpenRouterMessage(
    val role: String,
    val content: String,
)

data class OpenRouterReasoningConfig(
    val enabled: Boolean = true,
)

data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val temperature: Float?,
    val reasoning: OpenRouterReasoningConfig? = null,
)

// endregion

// region Response Models

@Keep
data class OpenRouterResponse(
    val model: String?,
    val choices: List<OpenRouterChoice>?,
    val usage: OpenRouterUsage?,
    val error: OpenRouterError?,
)

@Keep
data class OpenRouterChoice(
    val message: OpenRouterMessage?,
    @SerializedName("finish_reason")
    val finishReason: String?,
    val index: Int?,
)

@Keep
data class OpenRouterUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int?,
    @SerializedName("completion_tokens")
    val completionTokens: Int?,
    @SerializedName("total_tokens")
    val totalTokens: Int?,
)

@Keep
data class OpenRouterError(
    val code: Int?,
    val message: String?,
    val status: String?,
)

// endregion
