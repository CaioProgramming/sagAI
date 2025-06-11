package com.ilustris.sagai.features.home.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message

data class SagaContent(
    @Embedded
    val saga: SagaData,
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
)
