package com.ilustris.sagai.features.saga.chat.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.characters.data.model.Character
import java.util.Calendar

@Entity(
    tableName = "reactions",
    foreignKeys = [
        ForeignKey(
            entity = Message::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Reaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    val messageId: Int,
    @ColumnInfo(index = true)
    val characterId: Int,
    val emoji: String,
    val thought: String? = null,
    val timestamp: Long = Calendar.getInstance().timeInMillis,
)
