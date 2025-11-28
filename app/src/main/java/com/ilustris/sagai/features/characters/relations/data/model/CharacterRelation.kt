package com.ilustris.sagai.features.characters.relations.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.characters.data.model.Character

@Entity(
    tableName = "character_relations",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterOneId"],
            onDelete = ForeignKey.Companion.CASCADE,
        ),
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterTwoId"],
            onDelete = ForeignKey.Companion.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["characterOneId", "characterTwoId", "sagaId"], unique = true),
        Index(value = ["characterOneId"]),
        Index(value = ["characterTwoId"]),
        Index(value = ["sagaId"]),
    ],
)
data class CharacterRelation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val characterOneId: Int,
    val characterTwoId: Int,
    val sagaId: Int,
    val title: String,
    val description: String,
    val emoji: String,
    val lastUpdated: Long = System.currentTimeMillis(),
) {
    companion object {
        fun create(
            char1Id: Int,
            char2Id: Int,
            sagaId: Int,
            title: String,
            description: String,
            emoji: String,
            lastUpdated: Long = System.currentTimeMillis(),
        ): CharacterRelation =
            if (char1Id < char2Id) {
                CharacterRelation(
                    characterOneId = char1Id,
                    characterTwoId = char2Id,
                    sagaId = sagaId,
                    title = title,
                    description = description,
                    emoji = emoji,
                    lastUpdated = lastUpdated,
                )
            } else {
                CharacterRelation(
                    characterOneId = char2Id,
                    characterTwoId = char1Id,
                    sagaId = sagaId,
                    title = title,
                    description = description,
                    emoji = emoji,
                    lastUpdated = lastUpdated,
                )
            }
    }
}
