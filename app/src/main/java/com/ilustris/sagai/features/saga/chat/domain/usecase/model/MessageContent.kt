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

fun senderDescription(
    senderType: SenderType,
    showSender: Boolean,
) = if (showSender) {
    "(${senderType.name})"
} else {
    emptyString()
}
