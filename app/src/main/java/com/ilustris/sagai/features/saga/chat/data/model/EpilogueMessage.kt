package com.ilustris.sagai.features.saga.chat.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.characters.data.model.Character

/**
 * AI response for the epilogue chat — deliberately not [AIReply]: this conversation never
 * advances the story, so it carries none of [AIReply]'s scene-summary/new-character fields.
 */
data class EpilogueReply(
    val text: String = "",
    val emotionalTone: EmotionalTone? = null,
)

/**
 * A single turn in an epilogue chat, persisted per character so the conversation (and what the
 * character comes to know about the player, see [CharacterKnowledge]) survives leaving the screen.
 * Only the last few turns ever reach the prompt; older ones stay here for the player to scroll and
 * for the knowledge compaction to fold in.
 *
 * @property audioPath The spoken version of this line, when it was said or voiced in live mode.
 */
@Entity(
    tableName = "epilogue_messages",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["characterId", "timestamp"])],
)
data class EpilogueMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sagaId: Int,
    val characterId: Int,
    val text: String,
    val isUser: Boolean,
    val emotionalTone: EmotionalTone? = null,
    val inputMode: InputMode? = null,
    val audioPath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)
