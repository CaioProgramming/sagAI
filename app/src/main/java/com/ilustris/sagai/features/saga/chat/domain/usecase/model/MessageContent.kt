package com.ilustris.sagai.features.saga.chat.domain.usecase.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.act.data.model.Act
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
    @Relation(
        parentColumn = "actId",
        entityColumn = "id",
        entity = Act::class,
    )
    val act: Act? = null,
)

fun MessageContent.joinMessage(showSender: Boolean = true): Pair<String, String> =
    when (message.senderType) {
        SenderType.USER -> "${character?.name}(${message.senderType})" to message.text
        SenderType.CHARACTER -> "${(character?.name) ?: "Unknown"} ${senderDescription(message.senderType, showSender)}" to message.text
        SenderType.THOUGHT,
        SenderType.ACTION,
        ->
            ("${character?.name}${senderDescription(message.senderType, showSender)}") to message.text
        else -> message.senderType.name to message.text
    }

fun senderDescription(
    senderType: SenderType,
    showSender: Boolean,
) = if (showSender) {
    "(${senderType.name})"
} else {
    emptyString()
}

fun MessageContent.isUser(mainCharacter: Character?) : Boolean {
    if (message.senderType == SenderType.CHARACTER) return false
   return message.senderType == SenderType.USER ||
            message.characterId == mainCharacter?.id ||
            message.speakerName.equals(mainCharacter?.name, true)
}
