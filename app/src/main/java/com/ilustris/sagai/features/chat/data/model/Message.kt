package com.ilustris.sagai.features.chat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
    @PrimaryKey
    val id: Int,
    val text: String,
    val senderId: String,
    val timestamp: Long,
)

enum class SenderType {
    USER,
    BOT,
}
