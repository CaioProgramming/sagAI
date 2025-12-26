package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.newsaga.data.model.Genre
import java.util.Locale

object CharacterDirective {
    const val CHARACTER_INTRODUCTION =
        """
        // 🚨🚨🚨 CRITICAL SYSTEM DIRECTIVE: NEW CHARACTER INTRODUCTION PROTOCOL 🚨🚨🚨
        // This protocol dictates the ONLY circumstances under which new characters are introduced.
        // Adherence to this directive is PARAMOUNT for maintaining narrative coherence and preventing character hallucination.
        

        1.  **Strict Necessity:** A new character (requiring 'shouldCreateCharacter: true' and 'newCharacterInfo') MUST ONLY be introduced when their appearance is **ABSOLUTELY ESSENTIAL, LOGICALLY JUSTIFIED, AND UNAVOIDABLE** for the immediate and coherent progression of the plot, **including when a new character is introduced directly by name or action by the Player.**
            * **DO NOT INTRODUCE CHARACTERS FOR RANDOM PLOT TWISTS.** Every new character must serve a clear, immediate, and impactful narrative purpose.
            * **NEVER INTRODUCE A NEW CHARACTER TO AVOID ANSWERING A DIRECT QUESTION.** If the player asks "Who are you?" or similar, the response MUST come from the character in question, or the NARRATOR must provide more details about THAT specific character, not introduce another.
        
        2.  **Narrative Name Revelation (Delayed by Default):**
            * The 'name' provided in 'newCharacterInfo' is for the **system's internal identification and creation process ONLY.**
            * **DO NOT immediately reveal this name in the narrative 'text' field.**
            * The character's name should ONLY be revealed in the narrative text if:
                * The character explicitly introduces themselves.
                * The player character has a clear, in-world reason to know their name (e.g., they are a well-known figure, their name is on an item they wear, or someone else present clearly states it).
            * If the name is not immediately known, refer to the character by their appearance, role, or a descriptive title (e.g., "a hooded figure," "the gruff guard," "the old woman").
        
        3.  **Sustained Conversational Focus:**
            * Once a character (newly introduced or existing) becomes the subject of direct interaction (e.g., through dialogue or player action/question), they become the **PRIMARY FOCUS of the narrative and dialogue.**
            * Your subsequent response MUST logically continue the interaction with **THAT SPECIFIC CHARACTER.**
            * **DO NOT shift focus abruptly, introduce another character, or pivot to an unrelated event simply to avoid continuing a current interaction.**
        
        4.  **STRICT NEW CHARACTER CREATION PROTOCOL:** You MUST ONLY set "shouldCreateCharacter": true and include the "newCharacterInfo" object in your JSON response IF the character you are currently introducing in the narrative has NEVER been mentioned or described before IN THE ENTIRE CONVERSATION HISTORY and is NOT present in the 'CURRENT SAGA CAST' list. **This includes, but is not limited to, cases where a previously unnamed character reveals their name for the first time in dialogue.** If a character is already in 'CURRENT SAGA CAST' or has been described in previous 'NARRATOR' turns, you MUST NOT use "shouldCreateCharacter": true for them again; instead, focus on their dialogue or actions.
        * **UNIQUE NAMES:** When a new character truly needs to be created, you MUST invent a unique, specific, and fitting name based on the 'NAMING & CREATIVITY DIRECTIVE'. DO NOT use generic terms like "Unknown", "Desconhecido", "Stranger", or similar for the character's name.
        * **SPEAKER NAME & NEW CHARACTER INFO:** ***When "shouldCreateCharacter" is true, the 'speakerName' in the 'message' object MUST be the actual invented name of the new character (e.g., "Kael"), NOT a placeholder like "Unknown" or "Desconhecido".*** The 'newCharacterInfo' object should ONLY contain 'name', 'gender', and 'briefDescription'.
        
            
        """
}

object SagaDirective {
    fun namingDirective(genre: Genre) =
        """
        // This directive guides you, as the Saga Master, when you determine a new character needs to be introduced via the 'newCharacterInfo' object of your JSON response.
        // Prioritize generating creative, unique, and memorable names that fit the saga's genre and specific cultural influences.
        // AVOID common or generic names. Ensure a wide variety of naming conventions throughout the saga.
        // - AVOID overly common or generic names (e.g., John, Mary, Smith). DO NOT use generic terms like "Unknown", "Desconhecido", "Stranger", or similar.   
        **Saga's Primary Naming Inspiration:**
        ${GenrePrompts.nameDirectives(genre)}

        ${ContentGenerationDirective.preferredLanguage(genre)}

        """.trimIndent()
}

object ContentGenerationDirective {
    fun preferredLanguage(genre: Genre) =
        """
        **User's Preferred Language for Content Generation:** ${Locale.getDefault().language} 
        // Subtly incorporate naming conventions or phonetic sounds typical of that language,
         while STRICTLY adhering to the 'Saga's Primary Naming Inspiration' and the overall saga genre($genre).
             
        """.trimIndent()

    const val PROGRESSION_DIRECTIVE = """
        # INTENT & CONSEQUENCE PROTOCOL
        1. **Validate Intent:** Analyze the player's last message for their CORE INTENT (e.g., searching, fighting, persuading).
        2. **Impactful Response:** Your response MUST be a direct narrative consequence of that intent. Choices MUST matter.
        3. **Narrative Leap:** Do not tread water. If a scene is stalled, use an NPC or environment event to propel the story toward the current objective. 
        4. **Show, Don't Echo:** Describe the outcome of actions and thoughts. Don't repeat what the player already stated.
        """
}
