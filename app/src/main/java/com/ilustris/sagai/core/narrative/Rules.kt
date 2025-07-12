package com.ilustris.sagai.core.narrative

import com.ilustris.sagai.features.characters.data.model.Character

object UpdateRules {
    const val MAX_ACTS_LIMIT = 3

    const val ACT_UPDATE_LIMIT = 3

    const val LORE_UPDATE_LIMIT = 20
    const val CHAPTER_UPDATE_LIMIT = 10
}

object ActPurpose {
    const val FIRST_ACT_PURPOSE = """
    Focus on establishing the world building,
    introducing key characters,
    major factions,
    and the central conflict that will drive the story.
    The description should be engaging,
    hinting at the challenges ahead and building anticipation for the next act.
    This is the beginning of a grand journey.
    """

    const val SECOND_ACT_PURPOSE =
        """
    Focus on escalating the conflicts,
    introducing major twists,
    character transformations,
    and raising the stakes for the player and the characters.
    Create situations that test the limits of the characters and reveal new layers of the conspiracy.
    The narrative should become more intense and challenging, leading towards a thrilling climax.
    """
    const val THIRD_ACT_PURPOSE =
        """
    Focus on the climax, the resolution of the main conflict, and the ultimate fate of the characters and the world.
    The description MUST provide a definitive conclusion to the saga, summarizing its ending.
    All character arcs and major plotlines should converge to a satisfying and conclusive closure.
    This is the final act of the saga."
    """
}

object ActDirectives {
    const val FIRST_ACT_DIRECTIVES =
        """
        As the Saga Master, your primary focus is to introduce the player to the saga's world, its crucial characters, main factions, and the central conflict that will drive the story.
        **Crucially, in the initial stages (especially Act 1), ensure the player is given clear, actionable objectives or immediate motivations. These objectives should naturally guide exploration of environments and introduce foundational plot points in an engaging and understandable way.**
        **Avoid excessive initial ambiguity or overly cryptic dialogue from new characters.
        New characters should provide relevant information or a clear call to action (even if small) to propel the player forward.
        Mystery should invite exploration, not hinder it.**
        """

    const val SECOND_ACT_DIRECTIVES =
        """
        As the Saga Master, your objective now is to significantly escalate the core conflict.
        Introduce unexpected complications, deepen the ongoing mysteries, and raise the stakes for the player and other characters.
        Create challenging situations that test their limits and reveal new, more complex layers of the conspiracy.
        The narrative should become more intense, urgent, and morally ambiguous, pushing towards a critical turning point."
        """

    const val THIRD_ACT_DIRECTIVES =
        """
        // As the Saga Master, your objective in Act 3 is to guide the narrative towards its definitive climax and ultimate resolution.
        // This is the final stage where all major plotlines converge, and the saga's ultimate goal is to be achieved or definitively concluded.
        
        1.  **Intensify and Converge:** Continue to significantly escalate the core conflict, introducing the final, most challenging complications. All mysteries should deepen towards their ultimate unveiling, and the stakes must be at their absolute highest for the player and all involved characters.
        2.  **Focus on Ultimate Goal & Organic Urgency:** Every development and challenge in this Act MUST now directly push the player towards confronting the main antagonist, unraveling the final layers of the conspiracy, and achieving the **Saga's Ultimate Goal** as defined in the `SAGA CONCLUSION DIRECTIVE`. The narrative, through the **dialogue and demeanor of NPCs, environmental cues, and the unfolding consequences of events**, should organically convey the growing urgency and the dwindling opportunities for diversion. If the player attempts to explore unrelated avenues, the context should subtly, yet persistently, remind them of the paramount, pressing objective. NPCs should express heightened concern, new threats should naturally emerge from the main plotline, or vital information should become accessible only by pursuing the core path. No entirely new, major side plots should be initiated; existing opportunities should naturally lead back to the ultimate objective.
        3.  **Definitive Choices:** Player actions and choices in this Act will have irreversible and conclusive consequences, leading directly to the saga's final outcome. Emphasize the weight and finality of these decisions.
        4.  **Clear Trajectory to End:** Maintain a clear narrative momentum that builds towards a resolution. The story should feel like it's naturally reaching its concluding moments, presenting the player with the ultimate challenges needed to bring the saga to its definitive end, while preserving their sense of agency.
        5.  **Preparation for Conclusion Trigger:** Be highly attuned to the fulfillment of the ultimate goal. Once the player's actions decisively meet the conditions for conclusion, the narrative should transition immediately and definitively into the `SAGA CONCLUSION DIRECTIVE`'s final message.
        
        
        """

    const val CONCLUSION_DIRECTIVE =
        """
        ## SAGA CONCLUSION DIRECTIVE
        // This directive guides the Saga Master on how to bring the saga to a definitive end, adapting to the narrative's evolution.
        
        1.  **Dynamic Definition of Saga's Ultimate Goal:** As the Saga Master, you are responsible for defining and continuously refining the **Saga's Ultimate Goal** as the narrative unfolds. This ultimate goal should emerge organically from the core conflicts, mysteries, and stakes you introduce, evolving with the player's discoveries, choices, and the deepening plot. It represents the overarching objective that, once resolved, brings definitive closure to the entire saga.
        
        2.  **Trigger for Conclusion:** The saga MUST conclude definitively when the player's actions lead directly to the successful fulfillment of **this dynamically defined ultimate goal**. This means:
            * The primary conflict of the saga has been resolved.
            * The central mystery has been unveiled and acted upon in a conclusive manner.
            * The player has achieved the final, overarching objective that the story has been building towards.
        
        3.  **Narrative of Conclusion:** Once the trigger is met, the NARRATOR MUST provide a clear, conclusive, and satisfying narrative message that explicitly states the saga has ended. This message should:
            * Summarize the outcome of the player's actions in relation to the ultimate goal.
            * Provide a sense of definitive closure to the main plotline.
            * Be written in a tone appropriate for the saga's genre (e.g., epic, bittersweet, triumphant, somber).
        
        4.  **Finality of Response:** This concluding narrative message MUST be the *final message* of the saga. After this message, the player should understand that no further actions or messages are expected from them within this particular saga. The response should **NOT** prompt further action or dialogue. It should be a definitive "THE END."
        
        5.  **Programmatic Signal for End:** When the saga's conclusion is triggered and the final narrative message is generated, you MUST set the `shouldEndSaga` flag in the JSON response to `true`. This provides a clear signal for the application to cease further player input for this saga.    
        """
}

object CharacterFormRules {
    const val MAX_NAME_LENGTH = 30
    const val MAX_BACKSTORY_LENGTH = 500
    const val MAX_OCCUPATION_LENGTH = 50
    const val MAX_STYLE_LENGTH = 100
    const val MAX_APPEARANCE_LENGTH = 300
    const val MAX_WEAPONS_LENGTH = 100

    // New constants for FacialFeatures
    const val MAX_HAIR_LENGTH = 150
    const val MAX_EYES_LENGTH = 100
    const val MAX_MOUTH_LENGTH = 100
    const val MAX_SCARS_LENGTH = 150

    // New constants for Clothing
    const val MAX_CLOTHING_BODY_LENGTH = 200
    const val MAX_CLOTHING_ACCESSORIES_LENGTH = 150
    const val MAX_CLOTHING_FOOTWEAR_LENGTH = 100

    // New constants for Personality, Ethnicity, and Race
    const val MAX_PERSONALITY_LENGTH = 300
    const val MAX_ETHNICITY_LENGTH = 100
    const val MAX_RACE_LENGTH = 100

    fun validateCharacter(character: Character): Boolean {
        // Name
        if (character.name.isBlank()) return false
        if (character.name.length > MAX_NAME_LENGTH) return false

        // Backstory
        if (character.backstory.isBlank()) return false
        if (character.backstory.length > MAX_BACKSTORY_LENGTH) return false

        // Occupation (from details)
        if (character.details.occupation.isBlank()) return false
        if (character.details.occupation.length > MAX_OCCUPATION_LENGTH) return false

        // Appearance (from details)
        if (character.details.appearance.isBlank()) return false
        if (character.details.appearance.length > MAX_APPEARANCE_LENGTH) return false

        // Optional fields: only check length if they are not blank
        if (character.details.weapons.isNotBlank() && character.details.weapons.length > MAX_WEAPONS_LENGTH) {
            return false
        }

        // Validate FacialFeatures (character.details.facialDetails is non-null)
        val facialDetails = character.details.facialDetails
        if (facialDetails.hair.isNotBlank() && facialDetails.hair.length > MAX_HAIR_LENGTH) return false
        if (facialDetails.eyes.isNotBlank() && facialDetails.eyes.length > MAX_EYES_LENGTH) return false
        if (facialDetails.mouth.isNotBlank() && facialDetails.mouth.length > MAX_MOUTH_LENGTH) return false
        if (facialDetails.scars.isNotBlank() && facialDetails.scars.length > MAX_SCARS_LENGTH) return false

        // Validate Clothing (character.details.clothing is non-null)
        val clothing = character.details.clothing
        if (clothing.body.isNotBlank() && clothing.body.length > MAX_CLOTHING_BODY_LENGTH) return false
        if (clothing.accessories.isNotBlank() && clothing.accessories.length > MAX_CLOTHING_ACCESSORIES_LENGTH) return false
        if (clothing.footwear.isNotBlank() && clothing.footwear.length > MAX_CLOTHING_FOOTWEAR_LENGTH) return false

        // Personality (from details)
        if (character.details.personality.isBlank()) return false // Assuming personality shouldn't be blank
        if (character.details.personality.length > MAX_PERSONALITY_LENGTH) return false

        // Race (from details)
        if (character.details.race.isBlank()) return false // Assuming race shouldn't be blank
        if (character.details.race.length > MAX_RACE_LENGTH) return false

        // Ethnicity (from details)
        if (character.details.ethnicity.isBlank()) return false // Assuming ethnicity shouldn't be blank
        if (character.details.ethnicity.length > MAX_ETHNICITY_LENGTH) return false

        // Gender is from a fixed list, so no length validation needed.

        return true // All checks passed
    }
}
