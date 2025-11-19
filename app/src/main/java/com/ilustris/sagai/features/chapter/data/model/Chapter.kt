package com.ilustris.sagai.features.chapter.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val overview: String = "",
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
) {
    companion object {
        val CHAPTER_EXCLUSIONS =
            listOf("id", "currentEventId", "coverImage", "createdAt", "actId", "featuredCharacters")
    }
}

data class ChapterGeneration(
    val title: String,
    val overview: String,
    val featuredCharacters: List<String>,
)
