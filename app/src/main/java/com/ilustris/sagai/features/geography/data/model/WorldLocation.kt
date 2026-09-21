package com.ilustris.sagai.features.geography.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.home.data.model.Saga
import java.util.Calendar

@Entity(
    tableName = "world_locations",
    foreignKeys = [
        ForeignKey(
            entity = Saga::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorldLocation::class,
            parentColumns = ["id"],
            childColumns = ["parentLocationId"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true,
        ),
    ],
)
data class WorldLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    val sagaId: Int,
    val name: String,
    val history: String = "",
    @ColumnInfo(index = true)
    val parentLocationId: Int? = null,
    val emojiTag: String? = null,
    val createdAt: Long = Calendar.getInstance().timeInMillis,
    val originChapterId: Int? = null,
)
