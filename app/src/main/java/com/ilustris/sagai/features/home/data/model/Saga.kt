package com.ilustris.sagai.features.home.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.data.model.Review

@Entity(
    tableName = "sagas",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["mainCharacterId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class Saga(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val icon: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val genre: Genre = Genre.entries.first(),
    @ColumnInfo(index = true)
    val mainCharacterId: Int? = null,
    @ColumnInfo(index = true)
    val currentActId: Int? = null,
    val isEnded: Boolean = false,
    val endedAt: Long = 0L,
    @ColumnInfo(defaultValue = "false")
    val isDebug: Boolean = false,
    @ColumnInfo(defaultValue = "")
    val endMessage: String = "",
    @Embedded
    val review: Review? = null,
    @ColumnInfo(defaultValue = "")
    val emotionalReview: String? = null,
    @ColumnInfo(defaultValue = "0")
    val playTimeMs: Long = 0L,
)
