package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming
import com.ilustris.sagai.core.ai.prompts.ChapterPrompts.coverCompositions
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData

object ImagePrompts {
    fun wallpaperGeneration(
        saga: SagaData,
        mainCharacter: Character,
        description: String,
    ) = GenrePrompts
        .iconPrompt(
            genre = saga.genre,
            mainCharacter = mainCharacter,
            description = description,
        ).trimIndent()

    fun chapterCover(
        saga: SagaData,
        characters: List<Character>,
    ) = """
        ${GenrePrompts.artStyle(saga.genre)}
        ${CharacterFraming.MEDIUM_SHOT.description}
        Featuring: ${coverCompositions(characters)}
        ${GenrePrompts.coverComposition(saga.genre)}
        """

    fun generateImage(
        character: Character,
        saga: SagaData,
        description: String,
    ) = """
        ${GenrePrompts.artStyle(saga.genre)}
        ${GenrePrompts.portraitStyle(saga.genre)}
        
        ${CharacterRules.IMAGE_CRITICAL_RULE}
        $description
        ${character.details.race} character, realistic ${character.details.race} features.

        """.trimIndent()
}
