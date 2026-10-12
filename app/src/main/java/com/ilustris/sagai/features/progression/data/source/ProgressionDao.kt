package com.ilustris.sagai.features.progression.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ilustris.sagai.features.progression.data.model.CharacterFeat
import com.ilustris.sagai.features.progression.data.model.CharacterHonor
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressionDao {
    @Query("SELECT * FROM character_feats WHERE characterId = :characterId ORDER BY createdAt, id")
    fun observeFeats(characterId: Int): Flow<List<CharacterFeat>>

    @Query("SELECT * FROM character_feats WHERE sagaId = :sagaId")
    fun observeFeatsBySaga(sagaId: Int): Flow<List<CharacterFeat>>

    @Query("SELECT * FROM character_honors WHERE sagaId = :sagaId ORDER BY createdAt, id")
    fun observeHonorsBySaga(sagaId: Int): Flow<List<CharacterHonor>>

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

    @Update
    suspend fun updateFeats(feats: List<CharacterFeat>)

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

    @Query("SELECT * FROM character_honors WHERE sagaId = :sagaId ORDER BY createdAt, id")
    suspend fun getHonorsBySaga(sagaId: Int): List<CharacterHonor>

    /** Honors of one chapter or act with who earned them, for the story screens. */
    @Query(
        "SELECT h.*, c.name AS characterName FROM character_honors h JOIN Characters c ON c.id = h.characterId " +
            "WHERE h.kind = :kind AND h.scopeId = :scopeId ORDER BY h.createdAt, h.id",
    )
    fun observeHonorsOfScope(
        kind: String,
        scopeId: Int,
    ): Flow<List<HonorWithCharacter>>

    @Query("SELECT * FROM character_feats WHERE id IN (:ids)")
    suspend fun getFeatsByIds(ids: List<Int>): List<CharacterFeat>

    @Query("SELECT * FROM character_honors WHERE sagaId = :sagaId AND announced = 0")
    suspend fun getUnannouncedHonors(sagaId: Int): List<CharacterHonor>

    @Query("UPDATE character_honors SET announced = 1 WHERE id IN (:ids)")
    suspend fun markHonorsAnnounced(ids: List<Int>)

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

data class HonorWithCharacter(
    @androidx.room.Embedded val honor: CharacterHonor,
    val characterName: String,
)
