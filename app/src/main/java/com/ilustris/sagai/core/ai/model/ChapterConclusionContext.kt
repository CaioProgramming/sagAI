package com.ilustris.sagai.core.ai.model

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.timeline.data.model.Timeline

data class ChapterConclusionContext(
    val sagaData: Saga,
    val mainCharacter: Character?,
    val eventsOfThisChapter: List<Timeline>,
    val previousActData: Act?,
    val previousChaptersInCurrentAct: List<Chapter>,
)
