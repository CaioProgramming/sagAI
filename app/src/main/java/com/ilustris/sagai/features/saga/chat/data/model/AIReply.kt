package com.ilustris.sagai.features.saga.chat.data.model

/**
 * Structured reply from the chat AI, including optional scene update, reactions,
 * and [NewCharacterDiscovery] when a genuinely new character enters the scene.
 */
data class AIReply(
    val message: Message,
    val reactions: List<AIReaction>? = null,
    val sceneSummary: SceneSummary? = null,
    val newCharacter: NewCharacterDiscovery? = null,
)
