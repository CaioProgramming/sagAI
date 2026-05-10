package com.ilustris.sagai.features.characters.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterSagaInfo
import com.ilustris.sagai.features.characters.data.model.CharacterWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM Characters ORDER BY id ASC")
    fun getAllCharacters(): Flow<List<Character>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character): Long

    @Update
    suspend fun updateCharacter(character: Character)

    @Query("DELETE FROM Characters WHERE id = :characterId")
    suspend fun deleteCharacter(characterId: Int)

    @Query("SELECT * FROM Characters WHERE id = :characterId LIMIT 1")
    suspend fun getCharacterById(characterId: Int): Character?

    @Query("SELECT * FROM Characters WHERE name LIKE :name AND sagaId = :sagaId LIMIT 1")
    fun getCharacterByName(
        name: String,
        sagaId: Int,
    ): Character?

    @Query("SELECT TRIM(name || ' ' || IFNULL(lastName, '')) FROM Characters")
    suspend fun getAllCharacterNames(): List<String>

    /**
     * Fetches a [Character] with its events, relationships, and message count.
     * Does NOT load the parent Saga — use [getSagaInfoForCharacter] for the lightweight projection.
     */
    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(*) FROM messages WHERE characterId = Characters.id) as messageCount FROM Characters WHERE id = :characterId LIMIT 1",
    )
    fun getCharacterWithRelations(characterId: Int): Flow<CharacterWithRelations?>

    /**
     * Lightweight projection of the Saga entity, loading only the fields needed by the character details page.
     */
    @Query("SELECT id, genre, variationId, title, icon FROM sagas WHERE id = :sagaId LIMIT 1")
    suspend fun getSagaInfoForCharacter(sagaId: Int): CharacterSagaInfo?
}
