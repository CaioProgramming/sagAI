package com.ilustris.sagai.features.timeline.data.model

import com.google.firebase.ai.type.Schema
import com.ilustris.sagai.core.utils.toFirebaseSchema
import com.ilustris.sagai.features.characters.data.model.Character

data class LoreGen(
    val timeLine: Timeline,
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
                ),
            )
    }
}
