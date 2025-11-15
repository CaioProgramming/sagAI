package com.ilustris.sagai.features.saga.chat.data.model

data class ReactionGen(
    val reactions: List<AIReaction>,
)

data class AIReaction(
    val character: String,
    val reaction: String,
    val thought: String? = null,
)
