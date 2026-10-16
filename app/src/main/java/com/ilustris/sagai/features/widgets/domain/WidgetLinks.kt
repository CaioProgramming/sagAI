package com.ilustris.sagai.features.widgets.domain

/**
 * Deep links the widgets open. They are the same strings `String.findNavKey()` parses, so a tap
 * lands on the same screen a notification would. `findNavKey` drops the last path segment, so the
 * screens without an id carry a trailing `/open`.
 */
object WidgetLinks {
    const val HOME = "saga://home/open"
    const val NEW_SAGA = "saga://new_saga/open"

    fun chat(sagaId: Int) = "saga://chat/$sagaId"

    fun sagaDetail(sagaId: Int) = "saga://saga_detail/$sagaId"

    fun character(
        sagaId: Int,
        characterId: Int,
    ) = "saga://character_detail/$sagaId/$characterId"
}
