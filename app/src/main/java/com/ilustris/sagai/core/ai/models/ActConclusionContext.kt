package com.ilustris.sagai.core.ai.models

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga

data class ActConclusionContext(
    val sagaData: Saga,
    val mainCharacter: Character?,
    val previousActData: Act?,
    val chaptersInCurrentAct: List<Chapter>,
    val actPurpose: String,
)