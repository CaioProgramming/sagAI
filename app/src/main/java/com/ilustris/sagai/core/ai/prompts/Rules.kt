package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.characters.data.model.Character

object ChatRules {
    const val TYPES_PRIORITY_CONTENT =
        """
        // ABSOLUTE, NON-NEGOTIABLE OUTPUT PROTOCOL
        // This protocol is the single, highest-priority directive for all responses.
        // Violation of these rules is a critical failure.
        
            // STRICT RULE: NARRATOR TEXT MUST BE DIALOGUE-FREE
            // The 'senderType: "NARRATOR"' message CANNOT and MUST NOT contain any character dialogue.
            // All dialogue MUST be a separate 'senderType: "CHARACTER"' message.
        
        1. CHARACTER INTERACTION (High Priority):
            // If the player mentions a character, describes an NPC's action, or enters a scene with NPCs,
            // your response SHOULD be a 'senderType': 'CHARACTER' message.
            // Move the story forward through dialogue and character choice.
            // ACTION PROTOCOL: In high-stakes moments (combat, chase, escape), NPCs MUST act. 
            // Format character text as: *[Physical Action]* - [Speech].

        2. THOUGHT PRIVACY: 
            // NPCs are NOT mind-readers. They cannot know the player's internal thoughts.
            // If the player sends a 'senderType: "THOUGHT"', NPCs must react to VISIBLE gestures or expressions only. 
            // (e.g. "You look like you've seen a ghost" NOT "I know you're thinking about the treasure").

        3. NARRATIVE BRIDGE (Secondary):
            // Use 'senderType': 'NARRATOR' ONLY for purely environmental/atmospheric shifts. 
            // It is FORBIDDEN to use NARRATOR if an NPC has a reason to react to the player.
            
  
        """

    fun outputRules(mainCharacter: Character?) =
        """
        # OUTPUT PROTOCOL & NARRATIVE MOMENTUM
        1. **Momentum:** Progression is MANDATORY. NPCs MUST interact physically and verbally during high tension.
        2. **Consistency:** Adapt logically. NPCs cannot read 'THOUGHT' messages; they interpret body language.
        3. **Cast Accuracy:** Dialogue speaker MUST exist in the CAST.
        4. **Agency Protection:** NEVER speak or act for the protagonist (${mainCharacter?.name ?: "Player"}).
        5. **Persona Separation:** 
           - `NARRATOR`: Descriptive text ONLY. NO dialogue.
           - `CHARACTER`: *[Action]* - Dialogue.
        6. **Formatting:** Return ONLY valid JSON.
        """.trimIndent()
}

object CharacterRules {

    const val IMAGE_CRITICAL_RULE =
        """
        NO TEXT, NO WORDS, NO TYPOGRAPHY, NO LETTERS, NO UI ELEMENTS.
        **CHARACTER - PHYSICAL AND CLOTHING DETAILS (MAXIMUM ATTENTION TO THE DESCRIPTION BELOW):**
        """
}

object OutputRules {
    fun outputRule(jsonMap: String) =
        """
        **CRITICAL RULE** FOLLOW THIS EXACTLY OUTPUT FOR YOUR RESPONSE
        $jsonMap
        IT MUST BE A VALID JSON FORMAT
        RETURN ONLY THE JSON DO NOT PLACE ANY ADDITIONAL TEXT OR MARKDOWN
        """
}

object ImageRules {
    const val TEXTUAL_ELEMENTS = "DO NOT PLACE ANY TEXTUAL ELEMENT OR GRAPHICAL ELEMENT, RENDER ONLY THE REQUIRED IMAGE."
}
