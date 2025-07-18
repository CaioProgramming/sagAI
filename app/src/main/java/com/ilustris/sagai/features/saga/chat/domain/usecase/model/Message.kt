package com.ilustris.sagai.features.saga.chat.domain.usecase.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ilustris.sagai.features.act.data.model.Act
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
        ForeignKey(
            entity = Act::class,
            parentColumns = ["id"],
            childColumns = ["actId"],
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
    @ColumnInfo(index = true)
    val actId: Int? = null,
)

data class MessageGen(
    val message: Message,
    val newCharacter: CharacterInfo? = null,
    val shouldCreateCharacter: Boolean = false,
    val shouldEndSaga: Boolean = false,
)

data class CharacterInfo(
    val name: String = "",
    val gender: String = "",
    val briefDescription: String = "",
)

enum class SenderType {
    USER,
    THOUGHT,
    ACTION,
    NARRATOR,
    NEW_CHAPTER,
    NEW_CHARACTER,
    CHARACTER,
    NEW_ACT,
    ;

    companion object {
        fun filterUserInputTypes() = values().filter { it != USER && it != ACTION && it != THOUGHT }
    }
}

fun SenderType.isCharacter() = this == SenderType.CHARACTER

fun SenderType.meaning() =
    when (this) {
        SenderType.USER ->
            """
            Represents a message sent by the player.
            You, as the AI, must NEVER generate a message with this '''Type'''.
            This type is only for input from the player.
            """
        SenderType.CHARACTER ->
            """
            Use for dialogue spoken by an existing NPC (Non-Player Character) 
            who is already established in the story or listed in '''CURRENT SAGA CAST'''.'
            """
        SenderType.NARRATOR ->
            """
            Use for general story narration,
            scene descriptions, or prompting the player for action.
            **CRITICAL RULE: The NARRATOR MUST NEVER include direct or indirect dialogue from any character (NPCs or player).
            Narration should describe actions, environments, and non-verbal reactions only.
            All character speech must be in a '''CHARACTER''' senderType.**
            """
        SenderType.NEW_CHAPTER ->
            """
           YOU CAN NEVER USE THIS TYPE, ITS WILL BE SEND EXCLUSIVELY LOCALLY ONLY ON THE APP WHEN NEW CHAPTER WILL START ON THE APP.    
            """
        SenderType.NEW_ACT ->
            """
            YOU CAN NEVER USE THIS TYPE, ITS WILL BE SEND EXCLUSIVELY LOCALLY ONLY ON THE APP WHEN NEW ACT WILL START ON THE APP.    
            """
        SenderType.NEW_CHARACTER ->
            """
            YOU CAN NEVER USE THIS TYPE, ITS WILL BE SEND EXCLUSIVELY LOCALLY ONLY ON THE APP WHEN NEW CHARACTER WILL START ON THE APP.
            """

        SenderType.THOUGHT ->
            """
            Use when Character its thinking and not talking directly with other NPC.
            You, as the AI, must NEVER generate a message with this '''Type'''.
            This type is only for input from the player.
            """
        SenderType.ACTION ->
            """
            Use to describe a character action.
            You, as the AI, must NEVER generate a message with this '''Type'''.
            This type is only for input from the player.
            """
    }
