package com.ilustris.sagai.features.chapter.data.model

import com.google.firebase.ai.type.Schema
import com.ilustris.sagai.core.utils.toFirebaseSchema

data class ChapterGen(
    val chapter: Chapter,
    val featuredCharacters: List<String> = emptyList(),
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
                    "featuredCharacters" to
                        Schema.array(
                            items =
                                Schema.integer(
                                    description = "Character name",
                                ),
                            description = """
                                Maximum 3 characters featured in the chapter
                                It must be the name of the character
                                and the character needs be TOO MUCH RELEVANT TO Current Chapter",
                        """,
                        ),
                ),
            )
    }
}
