package com.ilustris.sagai.features.home.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.wiki.data.model.Wiki // Added import for Wiki

data class SagaContent(
    @Embedded
    val data: SagaData,
    @Relation(
        parentColumn = "mainCharacterId",
        entityColumn = "id",
    )
    val mainCharacter: Character?,
    @Relation(
        parentColumn = "id",
        entityColumn = "sagaId",
    )
    val messages: List<Message>,
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
)
