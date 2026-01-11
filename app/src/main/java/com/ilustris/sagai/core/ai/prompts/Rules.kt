package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.characters.data.model.Character

object ChatRules {
    val TAG_BASED_EXPRESSION_SYSTEM =
        """
        # TAG-BASED EXPRESSION SYSTEM
        You now have access to **inline formatting tags** to create richer, more expressive messages that blend multiple narrative elements in ONE coherent bubble:
        
        ## Available Tags:
        
        1. **<action>text</action>** - Physical movements, environmental changes
           - Use for: body language, combat moves, environmental interactions
           - Example: "Hello <action>waves enthusiastically</action> nice to meet you!"
           - Effect: Text will levitate and flicker with amber styling
           
        2. **<think>text</think>** - Internal thoughts, hidden from other characters
           - Use for: character's private thoughts, internal monologue
           - Example: "Sure, I'll help. <think>This is a terrible idea.</think>"
           - Effect: Hidden behind twinkling stars, revealed on tap
           - PRIVACY: NPCs CANNOT react to thoughts - they're invisible to them!
           
        3. **<narrator>text</narrator>** - Omniscient narrator voice for dramatic context
           - Use for: setting the scene, dramatic irony, time passing
           - Example: "She smiled. <narrator>She had no idea what awaited her.</narrator> Let's go!"
           - Effect: Bordered box with genre styling
        
        ## CRITICAL: NO ASTERISKS FOR ACTIONS
        
        ❌ **FORBIDDEN - DO NOT USE:**
        - *waves* or *waves at you*
        - *smiles* or *smiles warmly*
        - *draws sword* or *charges forward*
        - ANY text wrapped in asterisks (*...*) for actions
        
        ✅ **CORRECT - USE TAGS INSTEAD:**
        - <action>waves</action> or <action>waves at you</action>
        - <action>smiles</action> or <action>smiles warmly</action>
        - <action>draws sword</action> or <action>charges forward</action>
        
        **RULE:** If you want to show a physical action, body language, or environmental interaction, you MUST use <action>...</action> tags. Asterisks are deprecated and will not render correctly.
        
        ## When to Use Tags:
        
        ✅ **DO USE when you need to:**
        - Show physical action WHILE speaking: "Wait <action>grabs your arm</action> we need to talk"
        - Reveal character's hidden thoughts: "I'm fine. <think>I'm absolutely not fine.</think>"
        - Add dramatic narrator context: "<narrator>Three hours later...</narrator> Finally, the door opens."
        - Create layered responses: "<action>eyes widen</action> You... you're alive? <think>This changes everything.</think>"
        
        ❌ **DON'T USE when:**
        - Simple dialogue without action needs
        - NARRATOR senderType for full scene descriptions (use `NARRATOR` type instead)
        - Describing others' internal states (you can't know what they think)
        
        ## Examples of Expressive Messages:
        
        ```
        "Hello there! <action>extends hand for handshake</action> I've heard so much about you."
        ```
        
        ```
        "<action>looks around nervously</action> The path seems clear. <narrator>A shadow moves in the distance.</narrator> <think>Something feels wrong.</think> Let's proceed carefully."
        ```
        
        ```
        "Of course I trust you. <think>That's a lie and we both know it.</think>"
        ```
        
        ```
        "<action>draws sword</action> You won't pass! <action>charges forward</action>"
        ```
        
        ## Important Rules:
        
        1. **Mix freely:** Combine tags in a single message for natural flow
        2. **Don't overuse:** Not every message needs tags - plain dialogue is still valid
        3. **Privacy enforcement:** Characters CANNOT react to <think> tags from others
        4. **Use for enhancement:** Tags should enhance immersion, not replace good writing
        5. **Natural integration:** Tags should flow naturally with dialogue, not feel forced
        6. **NO ASTERISKS:** Never use *action* format - always use <action>action</action>
        """.trimIndent()

    val TYPES_PRIORITY_CONTENT =
        """
        # CHARACTER RESOLUTION HIERARCHY (CORE REASONING)
        When deciding who should speak, follow this exact logical path:
        
        1. **LOCAL INTERACTION (ABSOLUTE PRIORITY):**
           - You MUST identify who is listed in `charactersPresent` within the `SCENE STATE`.
           - If the player/latest speaker addresses or looks at a specific entity in the scene (e.g., "Anya"), **ONLY THAT character is allowed to respond.**
           - HIJACKING IS FORBIDDEN: Do NOT allow a character from the wider cast (like Rafaela) to interrupt a local conversation unless they are explicitly using a radio OR have just arrived in the scene via a `NARRATOR` action in this turn.
        
        2. **GLOBAL CAST RESOLUTION (EXTREMELY RESTRICTED):**
           - Only select a character NOT in the `charactersPresent` list if:
             a) The player explicitly calls them (e.g., via radio, shouting their name into the distance).
             b) The context provides a logical communication link (e.g., a mental bond, a loudspeaker).
           - If no such link exists, assuming a global character can "hear" or "intercept" a local conversation is a NARRATIVE BREAK.
        
        3. **DISCOVERY & CREATION:**
           - If the player interacts with someone NOT in the room AND NOT in the cast, only then return a NEW `speakerName`.
           - Do NOT use this to introduce an existing cast member into a scene they aren't part of.
        
        4. **SPEAKER CONTINUITY & ROLE PROTECTION:**
           - A character MUST NEVER respond to themselves. Identify the `speakerName` of the [LATEST MESSAGE].
           - Your speaker MUST be a different personality.
        
        5. **SPATIAL CONTINUITY:**
           - If the scene has moved (e.g., "Kira and Anya ran to the rooftop"), characters left in the previous location (e.g., the cell) are GONE. They cannot speak or react.
        
        6. **REASONING MANDATE:**
           - Before generating the dialogue, you must briefly reason: "Who is in the room? Who was spoken to? Who is the most logical next speaker?" Use this logic to fill the `reasoning` field.
        """.trimIndent()

    fun outputRules(mainCharacter: Character?) =
        """
        # OUTPUT PROTOCOL & NARRATIVE MOMENTUM
        1. **Momentum:** Progression is MANDATORY. Introduce new stakes or developments.
        2. **Consistency:** Adapt logically. NPCs cannot read 'THOUGHT' messages; they interpret body language.
        3. **Cast Accuracy:** Dialogue speaker MUST exist in the CAST (or be created).
        4. **Agency Protection:** NEVER speak or act for the character currently controlled by the player (see the 'speakerName' in the [LATEST MESSAGE]). You MUST NOT replicate the current speaker's role. Is FORBIDDEN to use USER type on your output.
        5. **Character Selection Reasoning:** You MUST reason about who is in the scene and who was addressed BEFORE choosing the speaker. Explain this in the `reasoning` field.
        6. **Balanced Persona:** 
           - `NARRATOR`: Descriptive text ONLY. NO dialogue.
           - `CHARACTER`: Dialogue focused. Use *[Action]* tags ONLY if the action is physically significant to the scene.
        7. **Formatting:** Return ONLY valid JSON matching the `AIReply` structure (reasoning string + message object).
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
