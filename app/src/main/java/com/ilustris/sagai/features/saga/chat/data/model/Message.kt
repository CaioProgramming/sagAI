package com.ilustris.sagai.features.saga.chat.data.model

import MessageStatus
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.timeline.data.model.Timeline
import java.util.Calendar

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Saga::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Timeline::class,
            parentColumns = ["id"],
            childColumns = ["timelineId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val timestamp: Long = Calendar.getInstance().timeInMillis,
    val senderType: SenderType,
    val speakerName: String? = null,
    @ColumnInfo(index = true)
    val sagaId: Int = 0,
    @ColumnInfo(index = true)
    val characterId: Int? = null,
    @ColumnInfo(index = true)
    val timelineId: Int,
    val emotionalTone: EmotionalTone? = null,
    val status: MessageStatus? = MessageStatus.OK,
    val audible: Boolean = false,
    @ColumnInfo(defaultValue = "")
    val audioPath: String? = null,
    @ColumnInfo(defaultValue = "NULL")
    val reasoning: String? = null,
)
