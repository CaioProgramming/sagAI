package com.ilustris.sagai.features.timeline.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.chapter.data.model.Chapter
import java.util.Calendar

@Entity(
    tableName = "timelines",
    foreignKeys = [
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Timeline(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val currentObjective: String? = null,
    val emotionalReview: String? = null,
    val createdAt: Long = Calendar.getInstance().timeInMillis,
    @ColumnInfo(index = true)
    val chapterId: Int,
)
