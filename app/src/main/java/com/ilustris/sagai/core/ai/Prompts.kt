package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData

fun SagaData.introductionPrompt(character: Character?): String = SagaPrompts.introductionGeneration(this, character)

fun SagaData.narratorBreakPrompt(messages: List<String>) =
    SagaPrompts
        .narratorGeneration(this, messages)

fun Chapter.coverPrompt(saga: SagaData): String =
    """
    ${GenrePrompts.artStyle(saga.genre)} 
    ${CharacterFraming.MEDIUM_SHOT.description}   
    $visualDescription
    """
