package com.ilustris.sagai.features.characters.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * What one character has come to know about the player through their epilogue conversations —
 * the character's own memory of them, not a transcript. Rewritten (never appended) every few turns
 * by the knowledge compaction, so it stays small no matter how long the friendship runs, and fed
 * back into every epilogue turn so the character keeps knowing the player after old messages fall
 * out of the prompt.
 *
 * Its own table rather than more fields on [Character], which is already large and belongs to the
 * story itself; this belongs to the time after it.
 *
 * @property impression How the character sees the player now, in their own voice.
 * @property sharedMoments Things the player told or did in these conversations that stuck.
 * @property openThreads Promises, questions and topics left hanging, to pick up later.
 * @property compactedThroughMessageId The last epilogue message already folded into this.
 */
@Entity(
    tableName = "character_knowledge",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CharacterKnowledge(
    @PrimaryKey
    val characterId: Int,
    val sagaId: Int,
    val impression: String = "",
    val sharedMoments: List<String> = emptyList(),
    val openThreads: List<String> = emptyList(),
    val compactedThroughMessageId: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)

/** Model output of the knowledge compaction — several real fields, so the model can't flatten it. */
data class GeneratedCharacterKnowledge(
    val impression: String = "",
    val sharedMoments: List<String> = emptyList(),
    val openThreads: List<String> = emptyList(),
)
