package com.ilustris.sagai.features.saga.chat.data.model

/**
 * AI response for the epilogue chat — deliberately not [AIReply]: this conversation never
 * advances the story, so it carries none of [AIReply]'s scene-summary/new-character fields.
 */
data class EpilogueReply(
    val text: String = "",
    val emotionalTone: EmotionalTone? = null,
)

/**
 * A single turn in an epilogue chat. In-memory only — never backed by Room, never written to
 * any DAO, and wiped whenever [id]'s holder (an [EpilogueChatViewModel] instance) is cleared.
 */
data class EpilogueMessage(
    val id: Long = System.nanoTime(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
)
