package com.ilustris.sagai.features.characters.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterDetailData
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.timeline.data.model.CharacterUpdates
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
    ): Flow<StreamingState<GeneratedContent<Pair<Character, String>>>>

    suspend fun generateCharacter(
        sagaContent: SagaContent,
        description: String,
        sceneSummary: SceneSummary? = null,
    ): RequestResult<Character>

    suspend fun generateCharacterStream(
        sagaContent: SagaContent,
        description: String,
        sceneSummary: SceneSummary? = null,
    ): Flow<StreamingState<GeneratedContent<Character>>>

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

    suspend fun applyCharacterUpdates(
        saga: SagaContent,
        timelineId: Int,
        character: Character,
        update: CharacterUpdates,
    ): RequestResult<Character>

    suspend fun updateCharacterKnowledge(
        character: Character,
        knowledgeUpdate: List<String>,
    ): RequestResult<Unit>

    suspend fun enrichCharacter(
        character: CharacterContent,
        saga: SagaContent,
    ): RequestResult<com.ilustris.sagai.features.characters.ui.CharacterDetailState>

    suspend fun insertCharacterEvent(characterEvent: CharacterEvent): CharacterEvent

    suspend fun insertCharacterEvents(characterEvents: List<CharacterEvent>)

    suspend fun insertCharacterArc(characterArc: CharacterArc)

    fun getCharacterArcs(characterId: Int): Flow<List<CharacterArc>>

    fun getCharacterDetailData(characterId: Int): Flow<CharacterDetailData?>

    fun getCharactersBySaga(sagaId: Int): Flow<List<CharacterContent>>

    fun getTopCharacters(
        sagaId: Int,
        limit: Int,
    ): Flow<List<CharacterContent>>
}
