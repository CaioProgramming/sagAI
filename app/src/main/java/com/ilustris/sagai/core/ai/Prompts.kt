package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData

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
    saga: SagaData,
    characters: List<Character>,
): String =
    """
    Create a illustration cover for the chapter "$title".
    Use the context of the chapter overview: "$overview".
    Use this visuals to generate the cover:
    Color palette: ${saga.visuals.colorPalette},
    Lighting: ${saga.visuals.lightingDetails},
    Environment: ${saga.visuals.environmentDetails},
    Characters Pose: ${saga.visuals.characterPose},
    Characters expressions: ${saga.visuals.characterExpression},
    Use the artStyle of the genre "${saga.genre.name}":
    ${GenrePrompts.artStyle(saga.genre)}
    Don't write the title of the chapter, only the cover.
    ${CharacterPrompts.charactersOverview(characters)}
    """

fun SagaData.characterPrompt(description: String): String =
    """
    Generate the main character of the story. Following the user's description and the story theme.
    Story Details: $title : $description
    Story theme: ${genre.name}
    Description provided by the user:
    $description
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
