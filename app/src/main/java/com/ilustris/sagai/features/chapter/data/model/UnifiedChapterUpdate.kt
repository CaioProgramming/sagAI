package com.ilustris.sagai.features.chapter.data.model

import com.ilustris.sagai.features.timeline.data.model.GeneratedWikiUpdate

data class UnifiedChapterUpdate(
    val chapterTitle: String = "",
    val chapterIntroduction: String = "",
    val chapterOverview: String = "",
    val narrativeGuide: String? = null,
    val characterArcs: List<GeneratedCharacterArc> = emptyList(),
    val landmarkWikis: List<GeneratedWikiUpdate> = emptyList(),
    val worldStateUpdate: String? = null,
    val emotionalReview: String? = null,
)

data class GeneratedCharacterArc(
    val characterName: String,
    val arcTitle: String,
    val arcContent: String,
)
