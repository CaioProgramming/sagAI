package com.ilustris.sagai.features.chapter.data.model

import com.ilustris.sagai.core.ai.model.GeneratedChapter
import com.ilustris.sagai.features.narrative.data.model.ContinuitySummary
import com.ilustris.sagai.features.narrative.data.model.GeneratedLocationCheckpoint
import com.ilustris.sagai.features.timeline.data.model.GeneratedWikiUpdate

data class UnifiedChapterUpdate(
    val chapter: GeneratedChapter,
    val characterArcs: List<GeneratedCharacterArc> = emptyList(),
    val landmarkWikis: List<GeneratedWikiUpdate> = emptyList(),
    val worldStateUpdate: String? = null,
    val continuitySummary: ContinuitySummary? = null,
    /** Where/when this chapter ends — becomes the next chapter's opening checkpoint. */
    val closingCheckpoint: GeneratedLocationCheckpoint? = null,
)

data class GeneratedCharacterArc(
    val characterName: String,
    val arcTitle: String,
    val arcContent: String,
)
