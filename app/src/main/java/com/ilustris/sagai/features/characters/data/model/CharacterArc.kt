package com.ilustris.sagai.features.characters.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(
    tableName = "character_arcs",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CharacterArc(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    val characterId: Int,
    @ColumnInfo(index = true)
    val sourceId: Int,
    val sourceType: ArcSourceType,
    val title: String,
    val content: String,
    val createdAt: Long = Calendar.getInstance().timeInMillis,
)

enum class ArcSourceType {
    CHAPTER,
    ACT,
}
