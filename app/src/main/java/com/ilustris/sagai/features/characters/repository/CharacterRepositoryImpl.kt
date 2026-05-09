package com.ilustris.sagai.features.characters.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterDetailData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

        override fun getCharacterDetailData(characterId: Int): Flow<CharacterDetailData?> =
            dao.getCharacterWithRelations(characterId).map { withRelations ->
                withRelations?.let {
                    val sagaInfo =
                        dao.getSagaInfoForCharacter(it.character.sagaId)
                            ?: return@map null
                    CharacterDetailData(
                        character = it.character,
                        sagaInfo = sagaInfo,
                        events = it.events,
                        relationshipsAsFirst = it.relationshipsAsFirst,
                        relationshipsAsSecond = it.relationshipsAsSecond,
                        messageCount = it.messageCount,
                )
            }
        }
    }
