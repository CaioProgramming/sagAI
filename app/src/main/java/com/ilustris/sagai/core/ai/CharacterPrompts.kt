package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character

object CharacterPrompts {
    fun details(character: Character?) =
        character?.let {
            """
        Character Details:
        1.  **Name:** ${character.name}
        2.  **Backstory:** ${character.backstory}
        3.  **Appearance:** ${character.details.appearance}
        4.  **Personality:** ${character.details.personality}
        5.  **Abilities:** ${character.details.occupation}
        """
        } ?: emptyString()
}
