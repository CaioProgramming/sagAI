package com.ilustris.sagai.features.saga.chat.data.model

import ReactionContent
import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.characters.data.model.Character

data class MessageContent(
    @Embedded
    val message: Message,
    @Relation(
        parentColumn = "characterId",
        entityColumn = "id",
    )
    val character: Character? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId",
        entity = Reaction::class,
    )
    val reactions: List<ReactionContent>,
)
