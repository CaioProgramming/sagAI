package com.ilustris.sagai.features.progression.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.timeline.data.model.Timeline

/**
 * One moment of an event that changed something for a character, as judged by the AI and weighed by
 * code. The level is never stored: it is derived from `baseLevel` plus the sum of [xp].
 *
 * Re-running an event replaces its feats: the `(characterId, timelineId, ordinal)` triple is unique.
 *
 * @property kind One of BOND, SACRIFICE, VULNERABILITY, MORAL_CHOICE, DISCOVERY, OVERCOMING, LEADERSHIP.
 * @property changed What is different afterwards; for a tier 0 feat, why nothing moved.
 * @property cost What it cost the character; required from tier 3.
 * @property tier Impact from 0 to 5, already clamped by the guards.
 * @property xp Frozen when written, so a later curve change shifts the pace but never the log.
 */
@Entity(
    tableName = "character_feats",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Timeline::class,
            parentColumns = ["id"],
            childColumns = ["timelineId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["characterId", "timelineId", "ordinal"], unique = true),
        Index(value = ["timelineId"]),
        Index(value = ["sagaId"]),
    ],
)
data class CharacterFeat(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val characterId: Int,
    val sagaId: Int,
    val timelineId: Int,
    val ordinal: Int = 0,
    val kind: String,
    val title: String,
    val changed: String = "",
    val cost: String? = null,
    val tier: Int,
    val xp: Int,
    val relationshipUpdateEventId: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
