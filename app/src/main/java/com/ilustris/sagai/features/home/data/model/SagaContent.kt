package com.ilustris.sagai.features.home.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki

data class SagaContent(
    @Embedded
    val data: SagaData,
    @Relation(
        parentColumn = "mainCharacterId",
        entityColumn = "id",
    )
    val mainCharacter: Character? = null,
    @Relation(
        entity = Act::class,
        parentColumn = "currentActId",
        entityColumn = "id",
    )
    val currentActInfo: ActContent? = null,
    @Relation(
        entity = Message::class,
        parentColumn = "id",
        entityColumn = "sagaId",
    )
    val messages: List<MessageContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
    )
    val chapters: List<Chapter> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
    )
    val characters: List<Character> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
    )
    val wikis: List<Wiki> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
    )
    val timelines: List<Timeline> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
    )
    val acts: List<Act> = emptyList(),
)
