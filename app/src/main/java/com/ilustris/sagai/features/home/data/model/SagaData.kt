package com.ilustris.sagai.features.home.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre

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
data class SagaData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val icon: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val genre: Genre,
    @ColumnInfo(index = true)
    val mainCharacterId: Int?,
)
