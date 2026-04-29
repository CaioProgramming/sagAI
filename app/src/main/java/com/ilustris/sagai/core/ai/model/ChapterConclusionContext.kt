package com.ilustris.sagai.core.ai.model

data class ChapterConclusionContext(
    val sagaData: String,
    val mainCharacter: String?,
    val eventsOfThisChapter: String,
    val previousActData: String?,
    val previousChaptersInCurrentAct: String,
)
