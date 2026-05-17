package com.ilustris.sagai.features.playthrough.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.home.data.model.Saga

data class SagaPlaythrough(
    @Embedded val data: Saga,
    @Relation(
        entity = Act::class,
        parentColumn = "id",
        entityColumn = "sagaId",
    )
    val acts: List<ActPlaythrough>,
)

data class ActPlaythrough(
    @Embedded val data: Act,
    @Relation(
        parentColumn = "id",
        entityColumn = "actId",
    )
    val chapters: List<Chapter>,
)
