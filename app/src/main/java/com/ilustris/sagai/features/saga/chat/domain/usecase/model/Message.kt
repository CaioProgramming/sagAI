package com.ilustris.sagai.features.saga.chat.domain.usecase.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import java.util.Calendar

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = SagaData::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
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
    val chapterId: Int? = null,
    @ColumnInfo(index = true)
    val characterId: Int? = null,
)

enum class SenderType {
    USER,
    THOUGHT,

    ACTION,
    NARRATOR,
    NEW_CHAPTER,
    NEW_CHARACTER,
    CHARACTER,
}

fun SenderType.isCharacter() = this == SenderType.CHARACTER

fun SenderType.meaning() =
    when (this) {
        SenderType.USER ->
            """
            Represents a message sent by the player (Jeni). You, as the AI, must NEVER generate a message with this 'senderType'. This type is only for input from the player.
            """
        SenderType.CHARACTER ->
            """
            Use for dialogue spoken by an existing NPC (Non-Player Character) 
            who is already established in the story or listed in 'CURRENT SAGA CAST'.'
            """
        SenderType.NARRATOR ->
            """
             Use for general story narration,
             scene descriptions, or prompting the player for action."
             You can't speak for another character in the story
             """
        SenderType.NEW_CHAPTER ->
            """
            Use ONLY when introducing a brand new, significant NPC for the very first time. 
            If a character is mentioned or appears and is NOT in the Story,
            you MUST use this type for their first message. 
            The 'text' field for this type MUST contain a natural language description of this new character (including their name, key physical traits, and initial demeanor). 
            """
        SenderType.NEW_CHARACTER ->
            """
            Use when introducing a brand new, significant NPC for the very first time.
            The 'text' field for this type MUST contain a natural language description of this new character (including their name, key physical traits, and initial demeanor).
            """

        SenderType.THOUGHT ->
            """
            Use when Character its thinking and not talking directly with other NPC.
            """
        SenderType.ACTION ->
            """
            Use to describe a character action
            """
    }
