package com.ilustris.sagai.core.ai.model

/**
 * Configuration for audio generation containing the selected voice and optimized prompt.
 * This is returned by the AI voice selection/prompt crafting process.
 *
 * @param voice The selected voice enum name (e.g., "CHARON", "AOEDE", "NARRATOR")
 * @param prompt The crafted audio prompt optimized for ~30 seconds of speech
 */
data class AudioConfig(
    val voice: Voice,
    val prompt: String,
    val instruction: String,
)
