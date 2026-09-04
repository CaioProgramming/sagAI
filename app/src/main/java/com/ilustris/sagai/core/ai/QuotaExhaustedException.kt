package com.ilustris.sagai.core.ai

/**
 * Refused before leaving the device: the user's key has no daily quota left.
 *
 * Thrown by the pre-flight check rather than at each call site because generation has far more
 * entry points than the obvious two — book export, image generation, character creation, milestone
 * beats, the epilogue chat. Gating them one screen at a time would leave every path nobody
 * remembered failing mutely, so the guard sits on the single road they all take.
 */
class QuotaExhaustedException(
    val until: Long,
    val model: String,
) : Exception("Daily Gemini quota is exhausted for $model until $until")
