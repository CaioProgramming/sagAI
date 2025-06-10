package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre

fun Genre.iconPrompt(description: String) =
    """
    Subject: $description
    ${GenrePrompts.bannerStyle(this)}
    """

fun Genre.avatarPrompt(description: String) =
    """
    Subject: $description
    ${GenrePrompts.iconStyle(this)}
    """

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
    sagaData: SagaData,
    messages: List<String>,
    chapters: List<Chapter>,
) = SagaPrompts.chapterGeneration(sagaData, messages, chapters)

fun Chapter.coverPrompt(
    genre: Genre,
    characters: List<Character>,
): String =
    """
    Create a illustration cover for the chapter "$title".
    Use the context of the chapter overview: "$overview".
    Emphasizing the chapter description.
    The cover should be in the theme of "${genre.name}".
    Include the characters in a dynamic poses, with a background that reflects the theme ${genre.name}.
    ${CharacterPrompts.charactersOverview(characters)}
    High resolution, suitable for printing.
    Style: Flat colour anime style, clear image, highly detailed image, realistic body anatomy, clean illustration, no text in it.
    Refinement: Medium depth of field, rule of thirds, high aesthetic quality, smooth shading, detailed textures, clean lines, sharp edges, minimalistic composition.
    Photography: Action photography, character focus.
    Background: Minimalist, contrasting with the character.
    Lighting: Dramatic lighting, emphasizing the character's features and the mood of the scene. 
    """

fun SagaData.characterPrompt(): String =
    """
    Write a character description for the main character of the story.
    Story Details: $title : $description
    Story theme: ${genre.name}
    The character description should include:
    1.  The character's name.
    2.  A brief backstory that explains the character's motivations and goals.
    3.  A description of the character's appearance, including clothing and accessories.
    4.  A description of the character's personality, including strengths and weaknesses.
    5.  Any special abilities or skills the character possesses.
    6.  Keywords for the character, like their role or nicknames.
    Target a description length of 50 words, ensuring it captures the essence of a playable RPG experience.
    The description should be set in the context of the story and the character's role in it.
    """
