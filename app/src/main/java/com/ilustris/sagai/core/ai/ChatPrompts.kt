package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData

object ChatPrompts {
    fun replyMessagePrompt(
        saga: SagaData,
        message: String,
        mainCharacter: Character,
        lastMessages: List<String> = emptyList(),
        charactersDetails: List<Character> = emptyList(),
    ) = """
     ${SagaPrompts.details(saga)}
     ${GenrePrompts.detail(saga.genre)}
     Main Character overview:
     ${CharacterPrompts.details(mainCharacter)}
     
     Conversation context:
     [
        ${lastMessages.joinToString(",") {
        """
        $it 
        """
    }}
     ]
     ${CharacterPrompts.charactersOverview(charactersDetails)}
     Reply the message from the main character:
     $message
   
     IGNORE THE NARRATOR OPTION.
     NEVER NARRATE, you need to reply as a character.
     Keep continuing the conversation and evolving the storyline.
     You can always introduce a new character if necessary
     Target a message with a maximum length of 100 words,
    """
}
