package com.ilustris.sagai.features.characters.events.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterEventDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertCharacterEvent(characterEvent: CharacterEvent): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertCharacterEvents(characterEvents: List<CharacterEvent>)
}
