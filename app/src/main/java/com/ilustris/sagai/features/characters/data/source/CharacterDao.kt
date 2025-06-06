package com.ilustris.sagai.features.characters.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ilustris.sagai.features.characters.data.model.Character
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM Characters ORDER BY id ASC")
    fun getAllCharacters(): Flow<List<Character>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertCharacter(character: Character): Long

    @Update
    suspend fun updateCharacter(character: Character)

    @Query("DELETE FROM Characters WHERE id = :characterId")
    suspend fun deleteCharacter(characterId: Int)

    @Query("SELECT * FROM Characters WHERE id = :characterId LIMIT 1")
    suspend fun getCharacterById(characterId: Int): Character?
}
