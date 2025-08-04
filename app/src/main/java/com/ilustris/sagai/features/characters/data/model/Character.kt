package com.ilustris.sagai.features.characters.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.home.data.model.Saga

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
)

fun exampleCharacter() =
    Character(
        id = 0,
        name = "Tess",
        backstory = "Noble warrior that fought in many battles, seeking redemption.",
        image = "https://example.com/tess.png",
        hexColor = "#A0522D",
        sagaId = 1,
        details =
            Details(
                appearance = "Tall and muscular with long braided brown hair.",
                personality = "Stoic but kind-hearted, fiercely loyal.",
                race = "Human",
                height = 1.8,
                weight = 75.0,
                gender = "Female",
                occupation = "Mercenary",
                ethnicity = "Nordic",
                facialDetails =
                    FacialFeatures(
                        hair = "Long, braided brown",
                        eyes = "Piercing blue",
                        mouth = "Firm, determined",
                        scars = "A faint scar above the left eyebrow",
                    ),
                clothing = Clothing(body = "Leather armor", accessories = "Silver amulet", footwear = "Sturdy boots"),
                weapons = "Longsword and shield",
            ),
        joinedAt = System.currentTimeMillis(),
    )

data class Details(
    val appearance: String = "",
    val personality: String = "",
    val race: String = "",
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val gender: String = "",
    val occupation: String = "",
    val ethnicity: String = "",
    @Embedded
    val facialDetails: FacialFeatures = FacialFeatures(),
    @Embedded
    val clothing: Clothing = Clothing(),
    val weapons: String = "",
)

data class Clothing(
    val body: String = "",
    val accessories: String = "",
    val footwear: String = "",
)

data class FacialFeatures(
    val hair: String = "",
    val eyes: String = "",
    val mouth: String = "",
    val scars: String = "",
)
