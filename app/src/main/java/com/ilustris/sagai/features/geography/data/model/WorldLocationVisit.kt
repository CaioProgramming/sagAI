package com.ilustris.sagai.features.geography.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.timeline.data.model.Timeline
import java.util.Calendar

/**
 * Log of every time a [WorldLocation] was visited/mentioned during the story, tied to the
 * chapter/timeline it happened in — the raw data behind a future "map of the journey" view.
 * No uniqueness constraint: revisiting the same place is expected and is the interesting signal.
 */
@Entity(
    tableName = "world_location_visits",
    foreignKeys = [
        ForeignKey(
            entity = WorldLocation::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true,
        ),
        ForeignKey(
            entity = Timeline::class,
            parentColumns = ["id"],
            childColumns = ["timelineId"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true,
        ),
    ],
)
data class WorldLocationVisit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    val locationId: Int,
    @ColumnInfo(index = true)
    val chapterId: Int? = null,
    val timelineId: Int? = null,
    val visitedAt: Long = Calendar.getInstance().timeInMillis,
)
