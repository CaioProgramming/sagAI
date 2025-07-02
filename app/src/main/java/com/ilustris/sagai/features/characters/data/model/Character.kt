package com.ilustris.sagai.features.characters.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.home.data.model.SagaData

@Entity(
    tableName = "Characters",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = SagaData::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = CASCADE,
        ),
    ],
)
data class Character(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val backstory: String = "",
    val image: String = "",
    val hexColor: String = "#3d98f7",
    @ColumnInfo(index = true)
    val sagaId: Int = 0,
    @Embedded
    val details: Details,
    val joinedAt: Long = 0L,
    @ColumnInfo(defaultValue = "")
    val status: String = "",
)

data class Details(
    val appearance: String = "",
    val personality: String = "",
    val race: String = "",
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val style: String = "",
    val gender: String = "",
    val occupation: String = "",
    val ethnicity: String = "",
    val facialDetails: String? = "",
    val clothing: String? = "",
)

data class Clothing(
    val body: String = "",
    val accessories: String = "",
    val footwear: String = "",
)

data class FacialFeatures(
    val hair: String = "",
    val eyes: String = "",
    val ears: String = "",
    val nose: String = "",
    val mouth: String = "",
    val eyebrows: String = "",
    val scars: String = "",
)
