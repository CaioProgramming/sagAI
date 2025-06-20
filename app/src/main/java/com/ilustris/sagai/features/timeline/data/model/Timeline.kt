package com.ilustris.sagai.features.timeline.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.wiki.data.model.Wiki
import java.util.Calendar

@Entity(
    tableName = "timelines",
    foreignKeys = [
        ForeignKey(
            entity = SagaData::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = ForeignKey.Companion.CASCADE,
        ),
    ],
)
data class Timeline(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val messageReference: Int = 0,
    val createdAt: Long = Calendar.getInstance().timeInMillis,
    @ColumnInfo(index = true)
    val sagaId: Int,
)

data class LoreGen(
    val timeLine: Timeline,
    val newEntries: List<Wiki> = emptyList(),
    val updatedCharacters: List<Character> = emptyList(),
)
