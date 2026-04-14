package com.ilustris.sagai.features.characters.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
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

    suspend fun generateCharacterImageStream(
        character: Character,
        saga: Saga,
    ): Flow<com.ilustris.sagai.core.ai.StreamingState<com.ilustris.sagai.core.ai.model.GeneratedContent<Pair<Character, String>>>>

    suspend fun generateCharacter(
        sagaContent: SagaContent,
        description: String,
        sceneSummary: com.ilustris.sagai.features.saga.chat.data.model.SceneSummary? = null,
    ): RequestResult<Character>

    suspend fun generateCharacterStream(
        sagaContent: SagaContent,
        description: String,
        sceneSummary: com.ilustris.sagai.features.saga.chat.data.model.SceneSummary? = null,
    ): Flow<com.ilustris.sagai.core.ai.StreamingState<com.ilustris.sagai.core.ai.model.GeneratedContent<Character>>>

    suspend fun createSmartZoom(character: Character): RequestResult<Unit>

    suspend fun generateCharactersUpdate(
        timeline: Timeline,
        saga: SagaContent,
    ): RequestResult<Unit>

    suspend fun generateCharacterRelations(
        timeline: Timeline,
        saga: SagaContent,
    ): RequestResult<Unit>

    suspend fun findAndSuggestNicknames(
        saga: SagaContent,
        timelineContent: TimelineContent,
    ): RequestResult<Unit>

    suspend fun generateCharacterResume(
        character: CharacterContent,
        saga: SagaContent,
    ): RequestResult<String>

    suspend fun updateCharacterKnowledge(
        timeline: Timeline,
        saga: SagaContent,
    ): RequestResult<Unit>

    suspend fun insertCharacterEvent(
        characterEvent: com.ilustris.sagai.features.characters.events.data.model.CharacterEvent,
    ): com.ilustris.sagai.features.characters.events.data.model.CharacterEvent

    suspend fun insertCharacterEvents(characterEvents: List<com.ilustris.sagai.features.characters.events.data.model.CharacterEvent>)
}
