package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.*

fun Genre.iconPrompt(description: String) =
    """
    Subject: $description
    ${GenrePrompts.bannerStyle(this)}
    """.trimIndent()

fun Genre.avatarPrompt(description: String) =
    """
    Subject: $description
    ${GenrePrompts.iconStyle(this)}
    """

fun SagaData.introductionPrompt(character: Character?): String =
    """
        Write a introduction text for the story,
        presenting the world building,
        and surface overview of our objective.
        The introduction should encourage the player to start the adventure.
        
        Adventure Details:
        1.  **Title:** $title
        2.  **Description:** $description
        3.  **Genre:** ${genre.name}
        
        ${CharacterPrompts.details(character)}
        
        The introduction should include:
        1.  Main character introduction.
        2.  The primary antagonist or opposing force.
        3.  The main quest or objective for the player characters.
        4.  Potential for moral dilemmas or significant choices.
        5.  An indication of the adventure's scope and potential.
        Target a description length of 50 words, ensuring it captures the essence of a playable RPG experience.
        """

fun SagaData.narratorBreakPrompt(messages: List<Message>) = SagaPrompts.narratorGeneration(this, messages)

fun chatReplyPrompt(
    sagaData: SagaData,
    message: String,
    lastMessages: List<String> = emptyList(),
) = """
    You are a character in a role-playing game (RPG) set in the world of ${sagaData.title}.
    The story is ${sagaData.description}.
    The genre is ${sagaData.genre.name}.
    
    You are responding the message from the main character "\n$message\n" .
    The last messages in the conversation were:
    \n
        ${lastMessages.joinToString("\n}") { it } }
    \n    
    ]
    \n
    Write a reply to the main character's message, continuing the adventure.
    If the main character's message is talking directly to another character, respond as that character.
    Your response should be in character, reflecting your personality and the context of the conversation.
    Respond as another character in the story, if your name have not been mentioned in the last messages,
    feel free to introduce yourself in a natural way.
    Target a message with a maximum length of 50 words, ensuring it captures the essence of a playable RPG experience.
    """.trimIndent()

fun chapterPrompt(
    sagaData: SagaData,
    messages: List<Message>,
) = """
    Write a new chapter to continue the adventure in a role-playing game (RPG) set in the world of ${sagaData.title}.
    The story is ${sagaData.description}.
    The genre is ${sagaData.genre.name}.
    
    Write a overview of what should be the next events connecting with the past events from the messages.
    The last messages in the conversation were:
    ${messages.joinToString("\n") { it.text }}
    
    Your summary should be in character, reflecting the context of the story and the events that have happened so far.
    The chapter should include:
    1.  A brief summary of the main events that have happened so far.
    2.  A recap of the main character's actions and decisions.
    3.  An indication of the current state of the world and the main character's situation.
    Target a description length of 100 words, ensuring it captures the essence of a playable RPG experience.
    """.trimIndent()

fun Chapter.coverPrompt(genre: Genre): String =
    """
    Create a illustration cover for the chapter "$title".
    Use the context of the chapter overview: "$overview".
    Emphasizing the chapter description.
    The cover should be in the theme of "${genre.name}".
    Include main characters in a dynamic poses, with a background that reflects the theme ${genre.name}.
    High resolution, suitable for printing.
    Style: Flat colour anime style, clear image, highly detailed image, realistic body anatomy, clean illustration, no text in it.
    Refinement: Medium depth of field, rule of thirds, high aesthetic quality, smooth shading, detailed textures, clean lines, sharp edges, minimalistic composition.
    Photography: Action photography, character focus.
    Background: Minimalist, contrasting with the character.
    Lighting: Dramatic lighting, emphasizing the character's features and the mood of the scene. 
    """.trimIndent()

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
