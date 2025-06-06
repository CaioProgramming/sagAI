package com.ilustris.sagai.features.characters.domain

import com.ilustris.sagai.features.characters.data.model.Character
import kotlinx.coroutines.flow.Flow

interface CharacterUseCase {
    fun getAllCharacters(): Flow<List<Character>>

    suspend fun insertCharacter(character: Character): Long

    suspend fun updateCharacter(character: Character)

    suspend fun deleteCharacter(characterId: Int)

    suspend fun getCharacterById(characterId: Int): Character?
}
