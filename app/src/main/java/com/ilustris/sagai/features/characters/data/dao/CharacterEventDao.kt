package com.ilustris.sagai.features.characters.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ilustris.sagai.features.characters.data.model.CharacterEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacterEvent(characterEvent: CharacterEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacterEvents(characterEvents: List<CharacterEvent>)

    @Query("SELECT * FROM character_events WHERE characterId = :characterId ORDER BY createdAt ASC")
    fun getEventsForCharacter(characterId: Int): Flow<List<CharacterEvent>>

    @Query("SELECT * FROM character_events WHERE gameTimelineId = :gameTimelineId ORDER BY createdAt ASC")
    fun getEventsForSagaTimeline(gameTimelineId: Int): Flow<List<CharacterEvent>>

    @Query("SELECT * FROM character_events WHERE id = :eventId")
    suspend fun getCharacterEventById(eventId: Int): CharacterEvent?

    @Query("DELETE FROM character_events WHERE id = :eventId")
    suspend fun deleteCharacterEventById(eventId: Int)

    @Query("DELETE FROM character_events WHERE characterId = :characterId")
    suspend fun deleteAllEventsForCharacter(characterId: Int)
}
