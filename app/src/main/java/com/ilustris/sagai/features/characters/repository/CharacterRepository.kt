package com.ilustris.sagai.features.characters.repository

import com.ilustris.sagai.features.characters.data.model.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getAllCharacters(): Flow<List<Character>>

    suspend fun insertCharacter(character: Character): Character

    suspend fun updateCharacter(character: Character): Character

    suspend fun deleteCharacter(characterId: Int)

    suspend fun getCharacterById(characterId: Int): Character?

    suspend fun getCharacterByName(
        name: String,
        sagaId: Int,
    ): Character?

    suspend fun getAllCharacterNames(): List<String>
}
