package com.ilustris.sagai.features.chat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val text: String,
    val timestamp: Long,
    val senderType: SenderType,
    val sagaId: Int
)

enum class SenderType {
    USER,
    BOT,
}
