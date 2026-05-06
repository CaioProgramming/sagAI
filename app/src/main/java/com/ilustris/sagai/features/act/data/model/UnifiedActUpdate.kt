package com.ilustris.sagai.features.act.data.model

import com.ilustris.sagai.features.chapter.data.model.GeneratedCharacterArc
import com.ilustris.sagai.features.timeline.data.model.GeneratedWikiUpdate

data class UnifiedActUpdate(
    val actTitle: String = "",
    val actIntroduction: String = "",
    val actContent: String = "",
    val narrativeGuide: String? = null,
    val characterArcs: List<GeneratedCharacterArc> = emptyList(),
    val landmarkWikis: List<GeneratedWikiUpdate> = emptyList(),
    val finalWorldState: String? = null,
    val emotionalReview: String? = null,
)
