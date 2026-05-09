package com.ilustris.sagai.features.characters.data.model

import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * Lightweight projection of [Saga] containing only the fields needed by the character details page.
 * Avoids loading the full Saga entity (review, emotionalProfile, worldState, etc.).
 */
data class CharacterSagaInfo(
    val id: Int,
    val genre: Genre,
    val variationId: String?,
    val title: String = "",
) {
    /**
     * Creates a minimal [Saga] instance for backward-compatible APIs that still require the full type.
     * Only the projected fields carry real data; all other fields use their defaults.
     */
    fun toSaga() = Saga(id = id, genre = genre, variationId = variationId, title = title)
}
