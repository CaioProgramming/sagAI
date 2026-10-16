package com.ilustris.sagai.features.widgets.domain

/**
 * Deep links the widgets open. They are the same strings `String.findNavKey()` parses, so a tap
 * lands on the same screen a notification would.
 */
object WidgetLinks {
    const val HOME = "saga://home"
    const val NEW_SAGA = "saga://new_saga"

    fun chat(sagaId: Int) = "saga://chat/$sagaId"

    fun sagaDetail(sagaId: Int) = "saga://saga_detail/$sagaId"

    fun character(
        sagaId: Int,
        characterId: Int,
    ) = "saga://character_detail/$sagaId/$characterId"
}
