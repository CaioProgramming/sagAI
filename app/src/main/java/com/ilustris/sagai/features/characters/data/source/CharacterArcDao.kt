package com.ilustris.sagai.features.characters.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterArcDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArc(arc: CharacterArc)

    @Query("SELECT * FROM character_arcs WHERE characterId = :characterId ORDER BY createdAt ASC")
    fun getArcsForCharacter(characterId: Int): Flow<List<CharacterArc>>

    @Query("SELECT * FROM character_arcs WHERE sourceId = :sourceId AND sourceType = :sourceType")
    suspend fun getArcsBySource(
        sourceId: Int,
        sourceType: String,
    ): List<CharacterArc>

    @Query("DELETE FROM character_arcs WHERE sourceId = :sourceId AND sourceType = :sourceType")
    suspend fun deleteArcsBySource(
        sourceId: Int,
        sourceType: String,
    )
}
