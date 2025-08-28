package com.ilustris.sagai.features.characters.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.timeline.data.model.Timeline // Assuming this is your Timeline entity
import java.util.Calendar

@Entity(
    tableName = "character_events",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Timeline::class, // Make sure this import points to your actual Timeline @Entity class
            parentColumns = ["id"],
            childColumns = ["gameTimelineId"], // Corrected to match the field name
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CharacterEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(index = true)
    val characterId: Int,

    @ColumnInfo(name = "gameTimelineId", index = true)
    val gameTimelineId: Int,

    val title: String,
    val summary: String,
    val createdAt: Long = Calendar.getInstance().timeInMillis
)
