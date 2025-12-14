package com.ilustris.sagai.features.characters.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.features.home.data.model.Saga

class NicknameTypeConverter {
    @TypeConverter
    fun fromNicknameList(nicknames: List<String>): String = Gson().toJson(nicknames)

    @TypeConverter
    fun toNicknameList(nicknamesJson: String): List<String> {
        val typeToken = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(nicknamesJson, typeToken)
    }
}

@Entity(
    tableName = "Characters",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = Saga::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = CASCADE,
        ),
    ],
)
@TypeConverters(NicknameTypeConverter::class)
data class Character(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val lastName: String? = "",
    val nicknames: List<String>? = emptyList(),
    val backstory: String = "",
    val image: String = "",
    val hexColor: String = "#3d98f7",
    @ColumnInfo(index = true)
    val sagaId: Int = 0,
    @Embedded
    val details: Details,
    @Embedded
    val profile: CharacterProfile,
    val joinedAt: Long = 0L,
    @ColumnInfo(index = true)
    val firstSceneId: Int? = null,
    val emojified: Boolean = false,
    @Embedded(prefix = "zoom_")
    val smartZoom: SmartZoom? = null,
)

data class Abilities(
    val skillsAndProficiencies: String = "",
    val uniqueOrSignatureTalents: String = "",
)

data class Details(
    @Embedded
    val physicalTraits: PhysicalTraits = PhysicalTraits(),
    @Embedded
    val clothing: Clothing = Clothing(),
    @Embedded
    val abilities: Abilities = Abilities(),
)

data class CharacterProfile(
    val occupation: String = "",
    val personality: String = "",
)

data class PhysicalTraits(
    val race: String = "",
    val gender: String = "",
    val ethnicity: String = "",
    val height: Double = 0.0,
    val weight: Double = 0.0,
    @Embedded
    val facialDetails: FacialFeatures = FacialFeatures(),
    @Embedded
    val bodyFeatures: BodyFeatures = BodyFeatures(),
)

data class BodyFeatures(
    val buildAndPosture: String = "",
    val skinAppearance: String = "",
    val distinguishFeatures: String = "",
)

data class Clothing(
    val outfitDescription: String = "",
    val accessories: String = "",
    val carriedItems: String = "",
)

data class FacialFeatures(
    val hair: String = "",
    val eyes: String = "",
    val mouth: String = "",
    val distinctiveMarks: String = "",
    val jawline: String = "",
)

data class SmartZoom(
    val scale: Float = 1f,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val needsZoom: Boolean = false,
)
