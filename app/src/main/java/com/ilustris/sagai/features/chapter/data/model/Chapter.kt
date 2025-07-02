package com.ilustris.sagai.features.chapter.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.ai.type.Schema
import com.ilustris.sagai.core.utils.toFirebaseSchema

@Entity
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sagaId: Int,
    val title: String,
    val overview: String,
    val messageReference: Int,
    val eventReference: Int? = null,
    val coverImage: String,
    @ColumnInfo(defaultValue = "")
    val visualDescription: String = "",
    val createdAt: Long? = 0L,
)

data class ChapterGen(
    val chapter: Chapter,
    val featuredCharacters: List<String> = emptyList(),
) {
    companion object {
        fun toSchema() =
            Schema.obj(
                mapOf(
                    "chapter" to toFirebaseSchema(Chapter::class.java),
                    "featuredCharacters" to
                        Schema.array(
                            items =
                                Schema.string(
                                    description = "Character name",
                                ),
                            description = """
                                Maximum 3 characters featured in the chapter +
                                It must be the name of the character
                                and the character needs be TOO MUCH RELEVANT TO Current Chapter",
                        """,
                        ),
                ),
            )
    }
}
