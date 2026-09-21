package com.ilustris.sagai.features.act.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.narrative.data.model.ContinuitySummary
import com.ilustris.sagai.features.narrative.data.model.LocationCheckpoint

@Entity(
    tableName = "acts",
    foreignKeys = [
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["currentChapterId"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true,
        ),
    ],
)
data class Act(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    @ColumnInfo(defaultValue = "")
    val introduction: String = "",
    @ColumnInfo(defaultValue = "")
    val emotionalReview: String? = null,
    @ColumnInfo(index = true)
    val sagaId: Int? = null,
    @ColumnInfo(index = true)
    val currentChapterId: Int? = null,
    @ColumnInfo(defaultValue = "")
    val narrativeGuide: String? = null,
    @Embedded(prefix = "continuity_")
    val continuitySummary: ContinuitySummary? = null,
    /** Where/when this act begins — the saga's anchor location for Act 1, or the previous act's [closingCheckpoint]. */
    @Embedded(prefix = "opening_")
    val openingCheckpoint: LocationCheckpoint? = null,
    /** Where/when this act ends — populated by act synthesis, becomes the next act's [openingCheckpoint]. */
    @Embedded(prefix = "closing_")
    val closingCheckpoint: LocationCheckpoint? = null,
)
