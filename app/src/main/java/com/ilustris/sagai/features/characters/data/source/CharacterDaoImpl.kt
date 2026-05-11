package com.ilustris.sagai.features.characters.data.source

import androidx.room.Query
import androidx.room.Transaction
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterSagaInfo
import com.ilustris.sagai.features.characters.data.model.CharacterWithRelations
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CharacterDaoImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : CharacterDao {
        private val characterDao: CharacterDao by lazy { database.characterDao() }

        @Query("SELECT * FROM Characters")
        override fun getAllCharacters(): Flow<List<Character>> = characterDao.getAllCharacters()

        override fun getCharactersBySaga(sagaId: Int): Flow<List<CharacterContent>> = characterDao.getCharactersBySaga(sagaId)

        @Transaction
        override suspend fun insertCharacter(character: Character): Long = characterDao.insertCharacter(character)

        @Transaction
        override suspend fun updateCharacter(character: Character) = characterDao.updateCharacter(character)

        @Transaction
        override suspend fun deleteCharacter(characterId: Int) = characterDao.deleteCharacter(characterId)

        @Query("SELECT * FROM Characters WHERE id = :characterId")
        override suspend fun getCharacterById(characterId: Int): Character? = characterDao.getCharacterById(characterId)

        override fun getCharacterByName(
            name: String,
            sagaId: Int,
        ) = characterDao.getCharacterByName(name, sagaId)

        override suspend fun getAllCharacterNames(): List<String> = characterDao.getAllCharacterNames()

        override fun getCharacterWithRelations(characterId: Int): Flow<CharacterWithRelations?> =
            characterDao.getCharacterWithRelations(characterId)

        override suspend fun getSagaInfoForCharacter(sagaId: Int): CharacterSagaInfo? = characterDao.getSagaInfoForCharacter(sagaId)

        override fun getTopCharacters(
            sagaId: Int,
            limit: Int,
        ) = characterDao.getTopCharacters(sagaId, limit)

        override fun getCharactersCount(sagaId: Int): Flow<Int> = characterDao.getCharactersCount(sagaId)
    }
