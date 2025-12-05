package com.ilustris.sagai.core.media.model

data class PlaybackMetadata(
    val sagaId: Int = 0,
    val sagaTitle: String = "",
    val sagaIcon: String? = null,
    val currentActNumber: Int = 0,
    val totalActs: Int = 1,
    val timelineObjective: String = "",
    val mediaFilePath: String = "",
    val color: Int = 0,
)
