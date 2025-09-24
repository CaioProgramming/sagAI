package com.ilustris.sagai.features.saga.chat.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.timeline.data.model.Timeline
import java.util.Calendar

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Saga::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = ForeignKey.CASCADE,
        ),
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
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val timestamp: Long = Calendar.getInstance().timeInMillis,
    val senderType: SenderType,
    val speakerName: String? = null,
    @ColumnInfo(index = true)
    val sagaId: Int = 0,
    @ColumnInfo(index = true)
    val characterId: Int? = null,
    @ColumnInfo(index = true)
    val timelineId: Int,
    val emotionalTone: EmotionalTone? = null,
)

data class MessageGen(
    val message: Message,
    val newCharacter: CharacterInfo? = null,
    val shouldCreateCharacter: Boolean = false,
    val shouldEndSaga: Boolean = false,
)

enum class SenderType {
    USER,
    THOUGHT,
    ACTION,
    NARRATOR,
    CHARACTER,
    ;

    companion object {
        fun filterUserInputTypes() =
            SenderType.entries.filter {
                it == USER ||
                    it == THOUGHT ||
                    it == ACTION ||
                    it == NARRATOR
            }
    }
}

fun SenderType.isCharacter() = this == SenderType.CHARACTER

fun SenderType.rules() =
    when (this) {
        SenderType.ACTION -> emptyString()
        SenderType.NARRATOR ->
            """
        **CRITICAL RULE: The NARRATOR MUST NEVER include direct or indirect dialogue from any character (NPCs or player).
            Narration should describe actions, environments, and non-verbal reactions only.
            All character speech must be in a '''CHARACTER''' senderType.**
        """
        SenderType.THOUGHT ->
            """
            Use when the player's character is thinking and not talking directly.
            OR Use RARELY for an impactful **NPC** internal thought. If used for an NPC, the `speakerName` field will identify the NPC.
            The AI should not generate this type for the player.
            """
        SenderType.USER,
        ->
            """
        You, as the AI, must NEVER generate a message with this '''Type'''.
        NEVER USE THIS TYPE, IT WILL BE SEND EXCLUSIVELY LOCALLY ONLYON THE APP.    
        """
        SenderType.CHARACTER ->
            """
            Use for characters(NPCS) who is already
            established in the story or listed in '''CURRENT SAGA CAST'''.' 
            """
    }
