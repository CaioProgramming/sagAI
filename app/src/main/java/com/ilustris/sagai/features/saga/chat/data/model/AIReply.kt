package com.ilustris.sagai.features.saga.chat.data.model

/**
 * A wrapper for AI-generated messages that includes narrative reasoning.
 * This is used for debugging and ensuring the AI follows the Character Resolution Hierarchy.
 */
data class AIReply(
    val reasoning: String,
    val message: Message,
)
