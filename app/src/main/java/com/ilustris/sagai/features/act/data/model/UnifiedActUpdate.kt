package com.ilustris.sagai.features.act.data.model

import com.ilustris.sagai.core.ai.model.GeneratedAct
import com.ilustris.sagai.features.chapter.data.model.GeneratedCharacterArc
import com.ilustris.sagai.features.timeline.data.model.GeneratedWikiUpdate

data class UnifiedActUpdate(
    val act: GeneratedAct,
    val characterArcs: List<GeneratedCharacterArc> = emptyList(),
    val landmarkWikis: List<GeneratedWikiUpdate> = emptyList(),
    val finalWorldState: String? = null,
)
