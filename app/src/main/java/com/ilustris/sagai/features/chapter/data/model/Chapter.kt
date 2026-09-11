package com.ilustris.sagai.features.chapter.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.narrative.data.model.ContinuitySummary
import com.ilustris.sagai.features.narrative.data.model.LocationCheckpoint

@Entity
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    @ColumnInfo(defaultValue = "")
    val introduction: String = "",
    val currentEventId: Int? = null,
    val coverImage: String = "",
    val emotionalReview: String? = null,
    @ColumnInfo(defaultValue = "")
    val createdAt: Long? = 0L,
    @ColumnInfo(index = true)
    val actId: Int,
    val featuredCharacters: List<Int> = emptyList(),
    @ColumnInfo(defaultValue = "")
    val narrativeGuide: String? = null,
    @ColumnInfo(defaultValue = "")
    val artwork: String? = null,
    @Embedded(prefix = "continuity_")
    val continuitySummary: ContinuitySummary? = null,
    /** Where/when this chapter begins — carried forward from the previous chapter/act's [closingCheckpoint]. */
    @Embedded(prefix = "opening_")
    val openingCheckpoint: LocationCheckpoint? = null,
    /** Where/when this chapter ends — populated by chapter synthesis, becomes the next chapter's [openingCheckpoint]. */
    @Embedded(prefix = "closing_")
    val closingCheckpoint: LocationCheckpoint? = null,
) {
    fun isEmpty() = title.isEmpty() && content.isEmpty()
}
