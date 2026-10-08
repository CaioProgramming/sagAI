package com.ilustris.sagai.features.progression.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ilustris.sagai.features.progression.data.model.CharacterFeat
import com.ilustris.sagai.features.progression.data.model.CharacterHonor
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressionDao {
    @Query("SELECT * FROM character_feats WHERE characterId = :characterId ORDER BY createdAt, id")
    fun observeFeats(characterId: Int): Flow<List<CharacterFeat>>

    @Query("SELECT * FROM character_feats WHERE sagaId = :sagaId ORDER BY timelineId, id")
    suspend fun getFeatsBySaga(sagaId: Int): List<CharacterFeat>

    @Query("SELECT * FROM character_feats WHERE timelineId = :timelineId")
    suspend fun getFeatsByTimeline(timelineId: Int): List<CharacterFeat>

    /** XP a character has earned so far; the level is derived from this, never cached. */
    @Query("SELECT COALESCE(SUM(xp), 0) FROM character_feats WHERE characterId = :characterId")
    fun observeEarnedXp(characterId: Int): Flow<Int>

    /** Earned XP of every character of a saga in one read, for the per-turn power context. */
    @Query("SELECT characterId, SUM(xp) AS xp FROM character_feats WHERE sagaId = :sagaId GROUP BY characterId")
    suspend fun getEarnedXpBySaga(sagaId: Int): List<CharacterXp>

    @Query("SELECT COALESCE(SUM(xp), 0) FROM character_feats WHERE characterId = :characterId")
    suspend fun getEarnedXp(characterId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeats(feats: List<CharacterFeat>): List<Long>

    @Query("DELETE FROM character_feats WHERE timelineId = :timelineId")
    suspend fun deleteFeatsByTimeline(timelineId: Int)

    /** Re-running an event replaces what it gave, so the ledger never double counts. */
    @Transaction
    suspend fun replaceFeats(
        timelineId: Int,
        feats: List<CharacterFeat>,
    ): List<Long> {
        deleteFeatsByTimeline(timelineId)
        return insertFeats(feats)
    }

    @Query("SELECT * FROM character_honors WHERE characterId = :characterId ORDER BY createdAt, id")
    fun observeHonors(characterId: Int): Flow<List<CharacterHonor>>

    @Query("SELECT * FROM character_honors WHERE sagaId = :sagaId AND kind = :kind AND scopeId = :scopeId")
    suspend fun getHonors(
        sagaId: Int,
        kind: String,
        scopeId: Int,
    ): List<CharacterHonor>

    @Query("SELECT * FROM character_honors WHERE sagaId = :sagaId AND announced = 0")
    suspend fun getUnannouncedHonors(sagaId: Int): List<CharacterHonor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHonors(honors: List<CharacterHonor>): List<Long>

    @Query("DELETE FROM character_honors WHERE sagaId = :sagaId AND kind = :kind AND scopeId = :scopeId")
    suspend fun deleteHonors(
        sagaId: Int,
        kind: String,
        scopeId: Int,
    )

    /** Wipes the whole ledger of a saga, for the debug reset before a re-run. */
    @Query("DELETE FROM character_feats WHERE sagaId = :sagaId")
    suspend fun deleteFeatsBySaga(sagaId: Int)

    @Query("DELETE FROM character_honors WHERE sagaId = :sagaId")
    suspend fun deleteHonorsBySaga(sagaId: Int)
}

data class CharacterXp(
    val characterId: Int,
    val xp: Int,
)
