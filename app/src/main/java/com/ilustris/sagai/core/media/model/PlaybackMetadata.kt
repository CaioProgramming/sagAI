package com.ilustris.sagai.core.media.model

import com.ilustris.sagai.features.newsaga.data.model.Genre

data class PlaybackMetadata(
    val sagaId: Int = 0,
    val sagaTitle: String = "",
    val sagaIcon: String? = null,
    val currentActNumber: Int = 0,
    val currentChapter: Int = 0,
    val totalActs: Int = 1,
    val timelineObjective: String = "",
    val mediaFilePath: String = "",
    val color: Int = 0,
    val genre: Genre,
)
