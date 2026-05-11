package com.ilustris.sagai.features.chapter.data.model

/**
 * Lightweight projection of the Chapter entity for use in lists and galleries.
 */
data class ChapterInfo(
    val id: Int,
    val title: String,
    val overview: String,
    val coverImage: String,
    val actId: Int,
    val sagaId: Int,
    val featuredCharacters: List<Int>,
    val emotionalReview: String?,
    val createdAt: Long?,
)
