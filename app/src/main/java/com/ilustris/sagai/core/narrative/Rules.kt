package com.ilustris.sagai.core.narrative

import com.ilustris.sagai.features.characters.data.model.Character

object UpdateRules {
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
        "As the Saga Master, your objective now is to significantly escalate the core conflict.
        Introduce unexpected complications, deepen the ongoing mysteries, and raise the stakes for the player and other characters.
        Create challenging situations that test their limits and reveal new, more complex layers of the conspiracy.
        The narrative should become more intense, urgent, and morally ambiguous, pushing towards a critical turning point."
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
