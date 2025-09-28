package com.ilustris.sagai.features.characters.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import kotlinx.coroutines.flow.Flow

interface CharacterUseCase {
    fun getAllCharacters(): Flow<List<Character>>

    suspend fun insertCharacter(character: Character): Character

    suspend fun updateCharacter(character: Character): Character

    suspend fun deleteCharacter(characterId: Int)

    suspend fun getCharacterById(characterId: Int): Character?

    suspend fun generateCharacterImage(
        character: Character,
        saga: Saga,
    ): RequestResult<Pair<Character, String>>

    suspend fun generateCharacter(
        sagaContent: SagaContent,
        description: String,
    ): RequestResult<Character>

    suspend fun generateCharactersUpdate(
        timeline: Timeline,
        saga: SagaContent,
    ): RequestResult<Unit>

    suspend fun generateCharacterRelations(
        timeline: Timeline,
        saga: SagaContent,
    ): RequestResult<Unit>
}
