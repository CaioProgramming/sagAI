package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterExpression
import com.ilustris.sagai.features.characters.data.model.CharacterPose
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent

fun SagaData.introductionPrompt(character: Character?): String = SagaPrompts.introductionGeneration(this, character)

fun SagaData.narratorBreakPrompt(messages: List<String>) =
    SagaPrompts
        .narratorGeneration(this, messages)

fun chatReplyPrompt(
    sagaData: SagaData,
    currentChapter: Chapter?,
    message: String,
    mainCharacter: Character,
    lastMessages: List<String> = emptyList(),
    characters: List<Character> = emptyList(),
) = ChatPrompts.replyMessagePrompt(
    sagaData,
    message,
    currentChapter,
    mainCharacter,
    lastMessages,
    characters,
)

fun chapterPrompt(
    sagaData: SagaContent,
    messages: List<MessageContent>,
) = SagaPrompts.chapterGeneration(sagaData, messages)

fun Chapter.coverPrompt(saga: SagaData): String =
    """
    ${GenrePrompts.artStyle(saga.genre)} 
    ${CharacterFraming.MEDIUM_SHOT.description}   
    Pose: ${CharacterPose.random().description}
    Expression ${CharacterExpression.random().description}
    ${this.visualDescription}
    """
