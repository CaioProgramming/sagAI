package com.ilustris.sagai.features.characters.relations.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.timeline.data.model.Timeline // Assuming this is the correct import for Timeline

@Entity(
    tableName = "relationship_update_events",
    foreignKeys = [
        ForeignKey(
            entity = CharacterRelation::class,
            parentColumns = ["id"],
            childColumns = ["relationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Timeline::class, // Make sure Timeline::class is resolved correctly
            parentColumns = ["id"],
            childColumns = ["timelineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Optional: Add indices for faster querying if needed
    // indices = [Index(value = ["relationId"]), Index(value = ["timelineId"])]
)
data class RelationshipUpdateEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val relationId: Int,
    val timelineId: Int,
    val title: String,
    val description: String,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
)
