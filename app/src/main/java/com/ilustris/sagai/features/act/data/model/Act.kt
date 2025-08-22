package com.ilustris.sagai.features.act.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.chapter.data.model.Chapter

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
    val title: String = emptyString(),
    val content: String = emptyString(),
    @ColumnInfo(index = true)
    val sagaId: Int? = null,
    @ColumnInfo(index = true)
    val currentChapterId: Int? = null,
)
