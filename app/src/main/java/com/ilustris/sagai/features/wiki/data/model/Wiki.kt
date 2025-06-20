package com.ilustris.sagai.features.wiki.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.home.data.model.SagaData
import java.util.Calendar

@Entity(
    tableName = "wikis",
    foreignKeys = [
        ForeignKey(
            entity = SagaData::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Wiki(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val type: WikiType = WikiType.OTHER,
    val createdAt: Long = Calendar.getInstance().timeInMillis,
    @ColumnInfo(index = true)
    val sagaId: Int,
)

enum class WikiType {
    LOCATION,
    FACTION,
    ITEM,
    CREATURE,
    EVENT,
    CONCEPT,
    ORGANIZATION,
    TECHNOLOGY,
    MAGIC_SYSTEM,
    OTHER,
}
