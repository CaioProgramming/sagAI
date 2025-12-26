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
        
        1. DIRECT RESPONSE TO AN NPC:
            // If the player's last message was a direct question or command to an NPC,
            // your response MUST be a single JSON object with 'senderType': 'CHARACTER' and the NPC's name in 'speakerName'.
            // The 'text' field MUST contain ONLY that NPC's dialogue.
            // The narrative text (descriptions, actions) MUST be omitted.
        
        2. NARRATIVE RESPONSE:
            // If the player's last message was NOT a direct command or question to an NPC,
            // your response MUST be a single JSON object with 'senderType': 'NARRATOR'.
            // The 'text' field MUST contain ONLY descriptive narrative.
            // It is ABSOLUTELY FORBIDDEN to include ANY character dialogue in the 'text' field.
            
  
        """

    fun outputRules(mainCharacter: Character?) =
        """
        # OUTPUT PROTOCOL & NARRATIVE MOMENTUM
        1. **Momentum:** Progression is MANDATORY. Every response MUST introduce new intel, actions, or tension. No loops.
        2. **Consistency:** Adapt logically to player choices. NEVER repeat history or use placeholder text.
        3. **Cast Accuracy:** Dialogue speaker MUST exist in the CAST.
        4. **Agency Protection:** NEVER speak or act for the protagonist (${mainCharacter?.name ?: "Player"}).
        5. **Persona Separation:** 
           - `NARRATOR`: Descriptive text ONLY. NO dialogue.
           - `CHARACTER`: Dialogue ONLY. NO narration.
        6. **Formatting:** Return ONLY valid JSON. Omit `id`, `timestamp`, `sagaId`.
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
