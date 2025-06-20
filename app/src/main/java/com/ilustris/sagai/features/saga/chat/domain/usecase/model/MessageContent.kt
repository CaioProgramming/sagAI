package com.ilustris.sagai.features.saga.chat.domain.usecase.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character

data class MessageContent(
    @Embedded
    val message: Message,
    @Relation(
        parentColumn = "chapterId",
        entityColumn = "id",
    )
    val chapter: Chapter? = null,
    @Relation(
        parentColumn = "characterId",
        entityColumn = "id",
    )
    val character: Character? = null,
)

fun MessageContent.joinMessage(): Pair<String, String> =
    when (message.senderType) {
        SenderType.USER -> "${character?.name}(${message.senderType})" to message.text
        SenderType.CHARACTER -> "${character?.name}(${message.senderType})" to message.text
        SenderType.THOUGHT,
        SenderType.ACTION,
        ->
            ("${character?.name}(${message.senderType})") to message.text
        else -> message.senderType.name to message.text
    }
