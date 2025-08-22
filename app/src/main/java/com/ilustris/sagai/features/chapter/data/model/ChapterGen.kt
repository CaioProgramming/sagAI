package com.ilustris.sagai.features.chapter.data.model

import com.google.firebase.ai.type.Schema
import com.ilustris.sagai.core.utils.toFirebaseSchema

data class ChapterGen(
    val chapter: Chapter,
) {
    companion object {
        fun toSchema() =
            Schema.obj(
                mapOf(
                    "chapter" to
                        toFirebaseSchema(
                            Chapter::class.java,
                            excludeFields = listOf("featuredCharacters"),
                        ),
                ),
            )
    }
}
