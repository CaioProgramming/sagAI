package com.ilustris.sagai.features.wiki.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.home.data.model.Saga
import java.util.Calendar

@Entity(
    tableName = "wikis",
    foreignKeys = [
        ForeignKey(
            entity = Saga::class,
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
    val type: WikiType? = null,
    val emojiTag: String? = null,
    val createdAt: Long = Calendar.getInstance().timeInMillis,
    @ColumnInfo(index = true)
    val sagaId: Int,
    val timelineId: Int? = null,
)

enum class WikiType {
    LOCATION,
    FACTION,
    ITEM,
    CREATURE,
    CONCEPT,
    ORGANIZATION,
    TECHNOLOGY,
    MAGIC,
    OTHER,
}
