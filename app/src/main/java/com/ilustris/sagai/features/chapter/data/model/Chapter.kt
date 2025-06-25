package com.ilustris.sagai.features.chapter.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.ai.type.Schema
import com.ilustris.sagai.core.utils.toFirebaseSchema
import com.ilustris.sagai.features.wiki.data.model.Wiki

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
    val wikiUpdates: List<Wiki>,
) {
    companion object {
        fun toSchema() =
            Schema.obj(
                mapOf(
                    "chapter" to toFirebaseSchema(Chapter::class.java),
                    "wikiUpdates" to
                        Schema.array(
                            items = toFirebaseSchema(Wiki::class.java),
                            description = "New world building information about the saga eg(locations, factions, etc.)",
                        ),
                ),
            )
    }
}
