package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts.coverCompositions
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga

object ImagePrompts {
    fun wallpaperGeneration(
        saga: Saga,
        description: String,
    ) = GenrePrompts
        .iconPrompt(
            genre = saga.genre,
            description = description,
        ).trimIndent()

    fun chapterCover(
        saga: Saga,
        characters: List<Character>,
    ) = """
        ${GenrePrompts.artStyle(saga.genre)}
        ${CharacterFraming.MEDIUM_SHOT.description}
        Featuring: ${coverCompositions(characters)}
        ${GenrePrompts.coverComposition(saga.genre)}
        """

    fun generateImage(
        character: Character,
        saga: Saga,
        description: String,
    ) = """
        ${GenrePrompts.artStyle(saga.genre)}
        ${GenrePrompts.portraitStyle(saga.genre)}
        
        ${CharacterRules.IMAGE_CRITICAL_RULE}
        $description
        ${character.details.race} character, realistic ${character.details.race} features.
        --ar 3:2
        """.trimIndent()
}
