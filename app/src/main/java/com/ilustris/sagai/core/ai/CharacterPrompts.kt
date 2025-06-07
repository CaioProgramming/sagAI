package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre

object CharacterPrompts {
    fun details(character: Character?) = character?.toJsonFormat() ?: emptyString()

    fun generateImage(
        character: Character,
        genre: Genre,
    ) = """
        Generate a illustration of the character:
        ${details(character)}
        Following the style:
        ${GenrePrompts.iconStyle(genre)}
        """

    fun charactersOverview(characters: List<Character>): String =
        if (characters.isNotEmpty()) {
            """
            You can use the following characters to reply the main character.    
            Remember to use their personality to write your reply.
            Characters in the story:
            [
                ${characters.joinToString(",\n") { details(it) }}
            ]
            """
        } else {
            """
            Theres no characters in the story yet.
            Introduce a new one to keep the conversation evolving.    
            """
        }
}
