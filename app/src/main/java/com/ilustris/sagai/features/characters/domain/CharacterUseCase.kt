package com.ilustris.sagai.features.characters.domain

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.flow.Flow

interface CharacterUseCase {
    fun getAllCharacters(): Flow<List<Character>>

    suspend fun insertCharacter(character: Character): Character

    suspend fun updateCharacter(character: Character): Character

    suspend fun deleteCharacter(characterId: Int)

    suspend fun getCharacterById(characterId: Int): Character?

    suspend fun generateCharacterPrompt(
        character: Character,
        guidelines: String,
        genre: Genre,
    ): RequestResult<Exception, String>

    suspend fun generateCharacterImage(
        character: Character,
        description: String,
        saga: SagaData,
    ): RequestResult<Exception, Character>

    suspend fun generateCharacter(
        sagaContent: SagaContent,
        description: String,
    ): RequestResult<Exception, Character>
}
