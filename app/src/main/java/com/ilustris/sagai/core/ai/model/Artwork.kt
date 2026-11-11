package com.ilustris.sagai.core.ai.model

/**
 * The cover concept for a saga or chapter, produced by the cover curator before any image is
 * generated. Field names are the whole schema the model sees, so they carry their own meaning:
 * [characterIds] are the characters the cover must show (chosen from the story, not a fixed
 * count) and [elements] are the concrete things from the story that the image draws on.
 * [noCharacters] is true only when the curator decides the cover's subject is an object, a place or a
 * trace and shows no character; artworks that predate it stay false and keep the character fallbacks.
 */
data class Artwork(
    val concept: String = "",
    val characterIds: List<Int> = emptyList(),
    val elements: List<String> = emptyList(),
    val noCharacters: Boolean = false,
) {
    fun isBlank() = concept.isBlank()
}

/**
 * Most characters a cover may show; the curator picks as many as the story needs, up to this.
 * Top level on purpose: a constant inside [Artwork] would be reflected into the output schema the model sees.
 */
const val MAX_COVER_CHARACTERS = 4
