package com.ilustris.sagai.features.progression.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.characters.data.model.Character

enum class HonorKind {
    /** A single deed, granted at the end of a chapter. */
    MEDAL,

    /** A character's legend, granted at the end of an act. */
    TITLE,
}

/**
 * A medal (per chapter) or a legacy title (per act). The AI judges which feats deserve one, the code
 * only validates it: every id in [featIds] must be a feat of this character, within the caps.
 *
 * @property scopeId The chapter id for a medal, the act id for a title.
 * @property emoji The glyph of a medal; null for a title.
 * @property description The reason for a medal, the legend for a title.
 * @property featIds The [CharacterFeat] ids that justify it, stored as a JSON list.
 * @property posthumous Copied from the character's status when granted.
 * @property announced False until the island has told the player, so a backfill can write it silent.
 */
@Entity(
    tableName = "character_honors",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["characterId"]),
        Index(value = ["sagaId", "kind", "scopeId"]),
    ],
)
data class CharacterHonor(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val characterId: Int,
    val sagaId: Int,
    val kind: HonorKind,
    val scopeId: Int,
    val name: String,
    val emoji: String? = null,
    val description: String = "",
    val featIds: List<Int> = emptyList(),
    val posthumous: Boolean = false,
    val announced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
