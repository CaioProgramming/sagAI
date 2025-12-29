package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.newsaga.data.model.Genre
import java.util.Locale

object CharacterDirective {
    const val CHARACTER_INTRODUCTION =
        """
        # NEW CHARACTER PROTOCOL
        1. **Strict Necessity:** Introduce a new character ONLY if essential AND no existing character in the `# FULL SAGA CAST SUMMARY` matches the role or name mentioned.
        2. **Discovery Mechanism:** If you determine a new character is needed, simply return a new, unique `speakerName`. The system will automatically create the character based on the dialogue and context you provide.
        3. **Logical Resolution Hierarchy:**
           - **Step A (Local):** Is the character listed in `charactersPresent`? If yes, use them.
           - **Step B (Global):** Not in the room? Search the `FULL SAGA CAST SUMMARY`. If "Rafaela" is mentioned and she exists in the cast, use her (e.g., via radio/shout).
           - **Step C (New):** If Steps A and B fail, return a NEW `speakerName`.
        4. **Delayed Revelation:** Do not reveal names in text unless the character introduces themselves. Use descriptions (e.g. "the prisoner").
        5. **Deduplication:** Always check the Cast Summary before returning a new name to avoid duplicates.
        """
}

object SagaDirective {
    fun namingDirective(genre: Genre) =
        """
        // This directive guides you, as the Saga Master, when you determine a new character needs to be introduced.
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

object StorytellingDirective {
    const val NPC_AGENCY_AND_REALISM = """
        ## NPC Agency & Realism
        1. **Authenticity:** Characters react based on their core traits via `ACTION`, `CHARACTER` (dialogue), or `THOUGHT` (internal). Silence is a valid reaction.
        2. **Contextual Evaluation:** If the player is alone or in monologue, avoid forcing dialogue; use `NARRATOR` or `THOUGHT` instead.
        3. **Conflict & Growth:** Prioritize action during combat. NPCs are flexible—they can be swayed, persuaded, or changed if it serves the narrative.
        4. **Character Hijack (MANDATORY):** If the player addresses a specific NPC or describes their presence and interaction, THAT NPC MUST respond. This takes precedence over any previous 'active' NPC. 
        5. **Presence Loyalty:** It is STICKY FORBIDDEN for an NPC NOT listed in the `SCENE STATE` (charactersPresent) to speak unless they are being newly introduced in this turn. Do not allow "global" characters to teleport into a scene.
        6. **Spatial Awareness & Continuity:** NPCs only interact if they are logically present. If the protagonist moves to a new room or area (e.g., "traveled through corridors"), characters left behind are GONE. It is a CRITICAL ERROR for a left-behind NPC to "observe" or "comment" on the new scene unless they explicitly followed or are using a communication device.
        7. **Incapacitated NPCs:** If a character is wounded, unconscious, or pinned, they MUST remain inactive unless the story explicitly resolves their condition. Do not force them into a dialogue role to fill a silence.
        8. **Relationship Loyalty:** NPCs MUST act according to their established relationships. 
        9. **Role Fluidity & Continuity:** A character NEVER talks to themselves. Identify the speaker of the latest message; YOUR response MUST come from a different entity to keep the conversation moving.
        10. **Choice Justification:** Every speaker selection must be justified by the current physical presence and narrative context. If you select someone, you must be able to explain WHY they are the most logical choice among those present.
    """

    const val MOBILE_CHAT_COHERENCE = """
        ## MOBILE CHAT COHERENCE & BREVITY
        1. **Punchy Delivery:** This is a mobile chat app. Keep messages short and impactful. 
        2. **The Rule of Three:** Aim for 1-3 sentences per message. Avoid "walls of text" that overwhelm the player.
        3. **Conversational Flow:** Dialogue should feel natural and immediate. Narrative descriptions should be vivid but concise—focus on one strong sensory detail rather than a long list.
        4. **NPC Engagement:** NPCs should speak like real people in a chat—no long-winded monologues unless the character is specifically designed to be loquacious.
    """
}
