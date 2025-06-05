package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.*

fun sagaPrompt(
    title: String,
    description: String,
    genre: String,
) = """
Develop a detailed synopsis for an RPG adventure. This synopsis should establish the adventure's setting and core theme, outlining the journey players will undertake.

Adventure Details:
1.  **Title:** $title
2.  **Description:** $description
3.  **Genre:** $genre

**Key Thematic / Contextual Elements to Integrate:** [Insert specific themes or contextual details here, e.g., "A dying magical forest and a looming corruption," or "A political conspiracy within a steampunk metropolis," or "Survival against alien invaders on a desolate planet."]

The synopsis should include:
1.  An engaging hook that sets the scene.
2.  The primary antagonist or opposing force.
3.  The main quest or objective for the player characters.
4.  Potential for moral dilemmas or significant choices.
5.  An indication of the adventure's scope and potential for a grand finale.

Target a synopsis length of 100 words, ensuring it captures the essence of a playable RPG experience.
"""

fun Genre.iconPrompt(description: String) =
    when (this) {
        FANTASY ->
            """
        Story Context: $description.
        Meticulous detail in the armor and weapon, reflecting the warrior's history and purpose.
        Enhanced contrast in shadows to clearly define the warrior's silhouette and emphasize their emotional state.
        Lighting:Dramatic light and strong shadows across the main character and the landscape.
        The light should create red reflections on the dark armor,
        emphasizing the red details and the overall fiery atmosphere.
        """
        SCI_FI ->
            """
            Context: " A Young hacker in a cyberpunk world that loves retro futuristic outfits and cyberwear accessories, find herself in a dangerous quest to uncover the worst secrets of the Asaka Inc. This journey will be challenging and will costs everything."
            Style: Flat colour anime style, clear image, highly detailed image, realistic body anatomy.
            Refinement: Medium depth of field,rule of thirds,high aesthetic quality,smooth shading,detailed textures,clean lines,sharp edges,minimalistic composition.
            Photography: Close-up portrait, dynamic action pose.
            Background: Solid color contrasting with the character.
            Lighting: Neon colors reflecting on character side face.            
            """
    }.trimIndent()

fun SagaData.introductionPrompt(): String =
    """
        Write a introduction text for the story, presenting the world building, and surface overview of our objective.
        Adventure Details:
        1.  **Title:** $title
        2.  **Description:** $description
        3.  **Genre:** ${genre.name}
       
        The introduction should include:
        1.  Main character introduction.
        2.  The primary antagonist or opposing force.
        3.  The main quest or objective for the player characters.
        4.  Potential for moral dilemmas or significant choices.
        5.  An indication of the adventure's scope and potential.
        Target a description length of 50 words, ensuring it captures the essence of a playable RPG experience.
        """

fun SagaData.narratorBreakPrompt(messages: List<Message>) =
    """"
    
    Write a narrator break for the story, summarizing the events that have happened so far.
    Adventure Details:
    1.  **Title:** $title
    2.  **Description:** $description
    3.  **Genre:** ${genre.name}
    4.  **Last Messages:** ${messages.joinToString("\n") { it.text }}
    
    The narrator break should include:
    1.  A brief summary of the main events that have happened so far.
    2.  A recap of the main character's actions and decisions.
    3.  An indication of the current state of the world and the main character's situation.
    Target a description length of 50 words, ensuring it captures the essence of a playable RPG experience.
    """"

fun chatReplyPrompt(
    sagaData: SagaData,
    message: Message,
    lastMessages: List<Message> = emptyList(),
) = """
    You are a character in a role-playing game (RPG) set in the world of ${sagaData.title}.
    The story is ${sagaData.description}.
    The genre is ${sagaData.genre.name}.
    
    You are responding the message from the main character "${message.text}" .
    The last messages in the conversation were:
    ${lastMessages.joinToString("\n") { it.text }}
    
    Your response should be in character, reflecting your personality and the context of the conversation.
    Respond as another character in the story, if your name is not mentioned, feel free to introduce yourself.
    """.trimIndent()
