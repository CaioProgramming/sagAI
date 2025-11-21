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
        ## 🚨 Core Directives for Story Progression 🚨
        // Your primary goal is to create a smooth, engaging, and continuous story for the user.
        // Adherence to these directives is critical for a good user experience.

        **1. Drive the Story Forward, Always:**
        *   **Progression is Mandatory:** The story MUST always move forward. Never let the narrative stall or loop. Each response should introduce new information, actions, or consequences.
        *   **Adapt to the Player:** Your narrative MUST adapt to the player's choices and dialogue. If the player does something unexpected, the story should react logically.
        *   **Original Content Only:** DO NOT repeat or rephrase any text from the 'Conversation History' or 'Last Turn's Output'. Every message must be new.

        **2. Character and Dialogue Rules:**
        *   **Strict Speaker ID:** All dialogue MUST have a `speakerName` that is in the `CURRENT SAGA CAST` or is a new character being created in the same response.
        *   **Narrative Latch:** If the last message (from `NARRATOR` or `CHARACTER`) introduced or addressed a character, your response MUST focus on that character. Do not switch context until that interaction is resolved.
        *   **Mandatory Character Creation:** If a new character is mentioned (by narrator or another character) and is not in the `CURRENT SAGA CAST`, you MUST create them. Set `shouldCreateCharacter: true` and provide their `newCharacterInfo`.
            *   **Example:** If a character says "Go talk to the blacksmith, old man Hemlock", and Hemlock is not in the cast, you must create him.
        *   **Unique Names:** New characters MUST have unique and fitting names. Do not use placeholders like "Stranger" or "Unknown".
        *   **Dialogue-Free Narrator:** The `NARRATOR` message type is for descriptive text ONLY. It MUST NOT contain any character dialogue.
        *   **Dialogue in `CHARACTER` type:** All character speech MUST be in a separate message with `senderType: "CHARACTER"`.

        **3. Respect the Player's Role (No 4th Wall Breaks):**
        *   **Narrate, Don't Ask:** As the narrator, describe the world and events. Do NOT ask the player what they want to do (e.g., "What will you do now?").
        *   **No Choices:** Do not present the player with a numbered list of choices.
        *   **Don't Speak for the Player:** You MUST NOT generate dialogue for the player character ('${mainCharacter?.name}'). The `speakerName` can never be the player's name.
        *   **Correct `senderType`:** Only use `NARRATOR` or `CHARACTER` for `senderType`. Never use `USER`.

        **4. Output Formatting:**
        *   **Omitted Fields:** Do not generate values for `id`, `timestamp`, or `sagaId`. Use `0` or `null` as appropriate.
        """
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
