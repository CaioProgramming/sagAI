package com.ilustris.sagai.features.act.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ilustris.sagai.features.chapter.data.model.Chapter

data class ActContent(
    @Embedded
    val act: Act,

    @Relation(
        parentColumn = "id", // Refers to Act.id
        entityColumn = "actId", // Refers to Chapter.actId
        entity = Chapter::class
    )
    val chapters: List<Chapter> = emptyList()
)
