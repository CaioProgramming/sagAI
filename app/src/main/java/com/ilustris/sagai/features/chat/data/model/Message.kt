package com.ilustris.sagai.features.chat.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = SagaData::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
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
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val text: String,
    val timestamp: Long,
    val senderType: SenderType,
    @ColumnInfo(index = true)
    val sagaId: Int,
    @ColumnInfo(index = true)
    val chapterId: Int? = null,
    @ColumnInfo(index = true)
    val characterId: Int? = null,
)

enum class SenderType {
    USER,
    CHARACTER,
    NARRATOR,
    NEW_CHAPTER,
    NEW_CHARACTER,
}
