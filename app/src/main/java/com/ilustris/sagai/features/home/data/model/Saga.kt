package com.ilustris.sagai.features.home.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
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
    val title: String = "",
    val description: String = "",
    val icon: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val genre: Genre = Genre.entries.first(),
    @ColumnInfo(index = true)
    val mainCharacterId: Int? = null,
    @ColumnInfo(index = true)
    val currentActId: Int? = null,
    @Embedded
    val visuals: IllustrationVisuals = IllustrationVisuals(),
    val lastLoreReference: Int = 0,
)

data class IllustrationVisuals(
    val characterPose: String = "",
    val characterExpression: String = "",
    val environmentDetails: String = "",
    val lightingDetails: String = "",
    val colorPalette: String = "",
    val foregroundElements: String? = "",
    val backgroundElements: String? = "",
    val overallMood: String = "",
)
