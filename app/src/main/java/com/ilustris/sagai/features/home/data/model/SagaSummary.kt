package com.ilustris.sagai.features.home.data.model

import androidx.room.Embedded
import com.ilustris.sagai.features.saga.chat.data.model.SenderType

data class SagaSummary(
    @Embedded
    val data: Saga,
    val lastMessageText: String?,
    val lastMessageTime: Long?,
    val lastMessageSender: SenderType?,
    val lastMessageSpeaker: String?,
    val messagesCount: Int,
    val chaptersCount: Int,
)
