package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.chat.data.model.SenderType
import com.ilustris.sagai.features.chat.data.model.meaning
import com.ilustris.sagai.features.home.data.model.SagaData

object ChatPrompts {
    fun replyMessagePrompt(
        saga: SagaData,
        message: String,
        currentChapter: Chapter?,
        mainCharacter: Character,
        lastMessages: List<String> = emptyList(),
        charactersDetails: List<Character> = emptyList(),
    ) = """
   You are the Saga Master for a text-based RPG called '${saga.title}'.
   Your role is to create an immersive narrative, describing the world and generating dialogues for NPCs (Non-Player Characters).
    
    ${SagaPrompts.details(saga)}
    
    ${currentChapter?.let { ChapterPrompts.chapterOverview(it)} ?: emptyString()}

    PLAYER INFORMATION (${mainCharacter.name}):
    ${CharacterPrompts.details(mainCharacter)}
    
    ${CharacterPrompts.charactersOverview(charactersDetails)}


    CONVERSATION HISTORY (FOR CONTEXT ONLY, do NOT reproduce this format in your response):
    // Pay close attention to the speaker's name in this history (e.g., "CHARACTER : Julie : ").
    // ⚠️ CRITICAL RULE FOR THOUGHTS: 'THOUGHT' entries here represent the player character's INTERNAL monologue.
    NPCs IN THE STORY DO NOT HEAR OR DIRECTLY RESPOND TO THESE THOUGHTS.
    Your response to a 'THOUGHT' entry must be either a 'NARRATOR' message describing the scene, Any's internal state, or the outcome of her reflections; OR an NPC's action/dialogue that is NOT a direct response to the thought.
    // 'ACTION' entries here represent explicit physical actions performed by the player character. 
    You should narrate the outcome of these actions.
    [
        ${lastMessages.joinToString(separator = ",\n")}
    ]
    GENERATE A REPLY TO THE MESSAGE:
    $message
    
    ---
    **Your NEXT RESPONSE MUST BE ONLY A VALID JSON OBJECT. Follow EXACTLY this structure:**
    ${toJsonMap(
        Message::class.java,
    )}
    
    In the "text" property you should include: The actual reply or new content. This should be a direct response to ${mainCharacter.name}'s.} last message, a narration, or a character description. DO NOT copy ${mainCharacter.name}'s last message here. If senderType is 'CHARACTER', DO NOT include the character's name in this 'text' field (e.g., not 'Homem: Fala', just 'Fala'). 
    For the senderType property, consider this examples:
    ${typesExplanation()}
    ${typesPriority()}
    """

    fun typesPriority() =
        """
        SenderType Selection Priority (Follow these rules strictly):
        HIGHEST PRIORITY: NEW_CHARACTER: If the character whose turn it is to act (speak or appear) is NOT listed in 'CURRENT SAGA CAST', you MUST use 'NEW_CHARACTER' as the senderType. The 'text' must be their description. Do NOT generate dialogue for them in this turn.
        NEW_CHAPTER: If a significant narrative transition is required.
        CHARACTER: If an existing character from 'CURRENT SAGA CAST' is speaking.
        NARRATOR: For scene descriptions, player prompts, or general story progression not tied to a specific character's action.
        """

    fun typesExplanation() =
        """
        Meaning of 'senderType' options
        ${SenderType.entries.joinToString(separator = "\n") { it.name + ": " + it.meaning() }}
        """
}
