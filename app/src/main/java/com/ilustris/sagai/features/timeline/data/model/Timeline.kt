package com.ilustris.sagai.features.timeline.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.firebase.ai.type.Schema
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiType
import java.util.Calendar

@Entity(
    tableName = "timelines",
    foreignKeys = [
        ForeignKey(
            entity = SagaData::class,
            parentColumns = ["id"],
            childColumns = ["sagaId"],
            onDelete = ForeignKey.Companion.CASCADE,
        ),
    ],
)
data class Timeline(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val messageReference: Int = 0,
    val createdAt: Long = Calendar.getInstance().timeInMillis,
    @ColumnInfo(index = true)
    val sagaId: Int = 0,
)

data class LoreGen(
    val timeLine: Timeline,
    val wikiUpdates: List<Wiki> = emptyList(),
    val updatedCharacters: List<Character> = emptyList(),
) {
    companion object {
        fun toSchema(): Schema =
            Schema.obj(
                mapOf(
                    "timeLine" to
                        Schema.obj(
                            mapOf(
                                "title" to Schema.string(nullable = false, description = "Short title that describes the event"),
                                "content" to Schema.string(nullable = false),
                            ),
                        ),
                    "wikiUpdates" to
                        Schema.array(
                            description = "New updates for the saga world building.",
                            items =
                                Schema.obj(
                                    mapOf(
                                        "title" to Schema.string(nullable = false),
                                        "content" to Schema.string(nullable = false),
                                        "type" to Schema.enumeration(WikiType.entries.map { it.name }, nullable = false),
                                    ),
                                ),
                        ),
                    "updatedCharacters" to
                        Schema.array(
                            items =
                                Schema.obj(
                                    mapOf(
                                        "name" to Schema.string(nullable = false),
                                        "backstory" to Schema.string(nullable = false),
                                        "status" to Schema.string(nullable = false),
                                    ),
                                ),
                        ),
                ),
            )
    }
}
