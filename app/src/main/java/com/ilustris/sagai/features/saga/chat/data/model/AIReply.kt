package com.ilustris.sagai.features.saga.chat.data.model

/**
 * Structured reply from the chat AI: the message itself, the scene state it leaves behind, and
 * [NewCharacterDiscovery] when a genuinely new character enters.
 *
 * Reactions and the notification hook deliberately live in [ReplyFallout] instead — they are
 * responses to a turn that already happened, and keeping them here made every reply prompt carry
 * their schema and rules. [sceneSummary] stays: the model that just wrote the scene is the one best
 * placed to say where it left off, and continuity depends on that being right. Both tones stay too
 * — they cost an enum each and feed the review's expressiveness pages.
 */
data class AIReply(
    val message: Message,
    val sceneSummary: SceneSummary? = null,
    val newCharacter: NewCharacterDiscovery? = null,
    val userTone: EmotionalTone? = null,
)
