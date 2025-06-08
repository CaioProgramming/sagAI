package com.ilustris.sagai.features.characters.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.source.CharacterDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CharacterRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : CharacterRepository {
        private val characterDao: CharacterDao by lazy { database.characterDao() }

        override fun getAllCharacters(): Flow<List<Character>> = characterDao.getAllCharacters()

        override suspend fun insertCharacter(character: Character): Character =
            character.copy(
                id = characterDao.insertCharacter(character.copy(id = 0)).toInt(),
            )

        override suspend fun updateCharacter(character: Character): Character {
            characterDao.updateCharacter(character)
            return character
        }

        override suspend fun deleteCharacter(characterId: Int) = characterDao.deleteCharacter(characterId)

        override suspend fun getCharacterById(characterId: Int): Character? = characterDao.getCharacterById(characterId)
    }
