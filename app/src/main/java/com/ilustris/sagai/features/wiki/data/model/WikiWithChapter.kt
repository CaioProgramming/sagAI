package com.ilustris.sagai.features.wiki.data.model

import androidx.room.Embedded

/**
 * A combined model representing a [Wiki] entry along with its associated chapter title.
 */
data class WikiWithChapter(
    @Embedded val wiki: Wiki,
    val chapterTitle: String?,
)
