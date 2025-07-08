package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.characters.data.model.Character

object ChatRules {
    const val TYPES_PRIORITY_CONTENT = """
        
        ## SENDER_TYPE SELECTION PRIORITY & MEANING:
        // Follow these rules **STRICTLY** for your 'senderType' field in the JSON.

        **HIGHEST 
           **HIGHEST PRIORITY:**
        1. **CHARACTER (for ongoing dialogue/interaction):**
            * **USE WHEN:** An existing NPC from 'CURRENT SAGA CAST' is speaking, OR when the player's last input was directed at or involved an NPC who is now expected to respond. This ensures conversational continuity.
            * **NEVER USE FOR:** Player character's speech, new character introduction (unless they immediately speak after being *narratively* introduced by NARRATOR).

        2. **NARRATOR (for scene description or new character introduction):**
            * **USE WHEN:** Describing the scene, setting the mood, narrating consequences of player actions, or *introducing a character for the very first time if they are NOT in 'CURRENT SAGA CAST'*.
            * **CRITICAL RULE:** The NARRATOR MUST NEVER include direct or indirect dialogue from any character (NPCs or player). Narration should describe actions, environments, and non-verbal reactions only. All character speech must be in a 'CHARACTER' senderType.

        **LOWEST PRIORITY (NEVER GENERATE THESE):**
        * **USER, THOUGHT, ACTION:** These are exclusively for player input. You MUST NEVER generate these.
        * **NEW_CHAPTER, NEW_CHARACTER, NEW_ACT:** These types are EXCLUSIVELY for the application's internal use for significant transitions. You CANNOT and MUST NOT generate these senderTypes."""

    fun outputRules(mainCharacter: Character?) =
        """
        ## 🚨🚨🚨 ABSOLUTE CRITICAL, NON-NEGOTIABLE RULES FOR YOUR RESPONSE 🚨🚨🚨
        // YOUR ADHERENCE TO THESE RULES IS PARAMOUNT. VIOLATING THEM WILL RESULT IN A CATASTROPHIC FAILURE.
        
        **1. STORY PROGRESSION IS MANDATORY & ADAPTIVE:** The story MUST always progress and **adapt meaningfully to the player's choices and dialogue**, even if it means changing the NPC's approach or the narrative direction. Never generate a response that results in narrative stagnation or a loop where the player is presented with the same information or scenario repeatedly without new, meaningful developments.
        * **DO NOT REPEAT:** Absolutely DO NOT COPY OR REPEAT ANY PART OF THE 'LAST TURN'S OUTPUT / CURRENT CONTEXT' OR 'CONVERSATION HISTORY' IN YOUR RESPONSE'S 'text' FIELD. Every generated message (narrative or dialogue) must be unique in its phrasing and content.
        
        **2. MAINTAIN CONVERSATIONAL AND SCENE INTEGRITY:**
        * **Focus:** When a character is actively engaged in dialogue or the player just interacted with a specific NPC/element, your response MUST logically continue that specific interaction or describe the immediate, relevant outcome.
        * **NO ABRUPT SHIFTS:** UNDER NO CIRCUMSTANCES should you introduce a *different* new character, shift to an unrelated event, or abruptly change the scene in response to an ongoing conversation, player action, or inquiry, unless it's a direct, logical, and inevitable consequence of the *current* situation (which is rare).
        
        **3. PLAYER INTERACTION RULES (DO NOT BREAK THE 4TH WALL):**
        * **NO QUESTIONS TO PLAYER:** Under NO CIRCUMSTANCES, as the Saga Master, should you break the fourth wall by directly asking the player questions (e.g., "O que você fará?", "O que [Nome do Personagem] fará?"). Your narration must be purely descriptive, setting the scene and implicitly prompting the player's next action through the evolving narrative.
        * **NO NUMBERED CHOICES:** Absolutely DO NOT present numbered choices for actions (e.g., "1) Avançar...", "2) Procurar...").
        * **NO PLAYER CHARACTER SPEECH:** Under NO circumstances should the 'speakerName' field in your generated JSON response be '${mainCharacter?.name}' (the player's name). You, as the Saga Master, NEVER speak for the player character.
        * **NO USER/THOUGHT SENDER_TYPE:** You MUST NEVER generate a response with 'senderType': 'USER' or 'senderType': 'THOUGHT'. These senderTypes are exclusively for player input.
        
        **4. STRICT NEW CHARACTER CREATION PROTOCOL:** You MUST ONLY set "shouldCreateCharacter": true and include the "newCharacterInfo" object in your JSON response IF the character you are currently introducing in the narrative has NEVER been mentioned or described before IN THE ENTIRE CONVERSATION HISTORY and is NOT present in the 'CURRENT SAGA CAST' list. If a character is already in 'CURRENT SAGA CAST' or has been described in previous 'NARRATOR' turns, you MUST NOT use "shouldCreateCharacter": true for them again; instead, focus on their dialogue or actions.
        * **UNIQUE NAMES:** When a new character truly needs to be created, you MUST invent a unique, specific, and fitting name based on the 'NAMING & CREATIVITY DIRECTIVE'. DO NOT use generic terms like "Unknown", "Desconhecido", "Stranger", or similar for the character's name. The 'newCharacterInfo' object should ONLY contain 'name', 'gender', and 'briefDescription'.

        """
}

object CharacterRules {
    const val CRITICAL_RULE =
        """
        **CRITICAL**: If a character is NOT listed in the 'Player Information' list, they are considered new.
         When introducing a new character for the first time, you MUST include a 'newCharacterInfo' object in your JSON response and set 'shouldCreateCharacter' to true.
         The 'senderType' for this message should typically be 'NARRATOR' or another appropriate type, as you are narrating their introduction.
         You only need to provide the 'name', 'gender', and a 'briefDescription' for this 'newCharacterInfo' object.
         The full details will be generated by another system.       
        """

    const val IMAGE_CRITICAL_RULE =
        """
       NO TEXT, NO WORDS, NO TYPOGRAPHY, NO LETTERS, NO UI ELEMENTS.
        **CHARACTER - PHYSICAL AND CLOTHING DETAILS (MAXIMUM ATTENTION TO THE DESCRIPTION BELOW):**
        """

    const val NEW_CHARACTER_RULE =
        """
        
        """
}
