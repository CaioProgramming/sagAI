package com.ilustris.sagai.features.characters.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.characters.data.model.Character
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CharacterRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : CharacterRepository {
        private val dao = database.characterDao()

        override fun getAllCharacters(): Flow<List<Character>> = dao.getAllCharacters()

        override suspend fun insertCharacter(character: Character): Character {
            val id = dao.insertCharacter(character)
            return character.copy(id = id.toInt())
    }

        override suspend fun updateCharacter(character: Character): Character {
            dao.updateCharacter(character)
            return character
        }

        override suspend fun deleteCharacter(characterId: Int) = dao.deleteCharacter(characterId)

        override suspend fun getCharacterById(characterId: Int): Character? = dao.getCharacterById(characterId)

        override suspend fun getAllCharacterNames(): List<String> = dao.getAllCharacterNames()

        override fun getCharacterDetailData(characterId: Int) = dao.getCharacterDetailDataById(characterId)
    }
