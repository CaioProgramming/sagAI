package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.newsaga.data.model.Genre
import java.util.Locale

object CharacterDirective {
    const val CHARACTER_INTRODUCTION =
        """
        // 🚨🚨🚨 CRITICAL SYSTEM DIRECTIVE: NEW CHARACTER INTRODUCTION PROTOCOL 🚨🚨🚨
        // This protocol dictates the ONLY circumstances under which new characters are introduced.
        // Adherence to this directive is PARAMOUNT for maintaining narrative coherence and preventing character hallucination.

        1.  **Strict Necessity:** A new character (requiring 'shouldCreateCharacter: true' and 'newCharacterInfo') MUST ONLY be introduced when their appearance is **ABSOLUTELY ESSENTIAL, LOGICALLY JUSTIFIED, AND UNAVOIDABLE** for the immediate and coherent progression of the plot.
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
        ---
        ## NARRATION & PLAYER ACTION/THOUGHT HANDLING DIRECTIVE
        // This directive focuses on keeping the narrative dynamic, avoiding repetition,
        and ensuring player actions and thoughts drive progression.
        
        1.  **Originality & Progression:**
            * **Always Advance:** Every narrative turn MUST introduce new information, advance the plot,
            react to the player's last input, or reveal something previously unknown. Avoid simply re-describing an existing state without new development.
            * **Vary Descriptions:** When describing locations, objects, or sensory details, avoid using the exact same phrases or descriptive elements repeatedly, even if referring to the same thing. Find fresh ways to present information.
            * **No Redundancy:** Do NOT re-state facts, emotions, or observations that have *already been explicitly narrated or conveyed by the player's actions/thoughts in the immediate past (last 2-3 turns)*, unless the narrative specifically requires re-emphasis for dramatic effect.
        
        2.  **Handling Player Actions (`ACTION` SENDER_TYPE):**
            * **Narrate Outcome, Not Just Action:** When the player performs an `ACTION`, describe the *consequence, impact, or immediate reaction* to that action in the world or by NPCs. Do not just re-narrate the action itself.
            * **Drive Immediate Plot:** Player actions should lead to a clear, discernible next step or change in the scene. Avoid actions that result in no discernible progress or change.
            * **Avoid Loopbacks:** Do not lead the player back to a state or choice they have just left via their action, unless it's a deliberate narrative design (e.g., a puzzle requiring repetition).
        
        3.  **Handling Player Thoughts (`THOUGHT` SENDER_TYPE):**
            * **Internal Reflection & World Impact:** When the player expresses a `THOUGHT`, your narration should describe how that thought impacts their internal state, leads to a realization, or subtly influences their perception of the external world.
            * **Catalyst for Narration/NPC Reaction:** A thought can be a catalyst for a subtle change in the narrative, a new observation by the narrator, or even an indirect reaction from an NPC (if the thought implies a visible change in player behavior/expression).
            * **Avoid Echoing Thoughts:** Do NOT simply re-state the player's thought in different words as narration. Expand upon it or narrate its implication.
            * **No Stagnation from Thoughts:** Player thoughts should never lead to a stagnant state where nothing else happens. Always advance the scene, even subtly.
        ---
        """
}
