package com.ilustris.sagai.features.saga.chat.data.model

import com.ilustris.sagai.features.saga.chat.data.model.SenderType

data class NextTurnDecision(
    val senderType: SenderType,
    val speakerName: String? = null,
    val reasoning: String,
    val relevantWikis: List<String> = emptyList(),
    val confidence: Float? = null,
)
