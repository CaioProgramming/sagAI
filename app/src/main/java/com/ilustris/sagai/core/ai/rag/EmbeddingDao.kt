package com.ilustris.sagai.core.ai.rag

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EmbeddingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: EmbeddingEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<EmbeddingEntry>)

    /** All indexed facts for [sagaId] of any of [types] — the candidate pool a search ranks over. */
    @Query("SELECT * FROM embedding_entries WHERE sagaId = :sagaId AND sourceType IN (:types)")
    suspend fun findBySagaAndTypes(
        sagaId: Int,
        types: List<EmbeddingSourceType>,
    ): List<EmbeddingEntry>

    /**
     * Removes every fact previously indexed under [sourceKeyPrefix] — either the single row keyed
     * exactly by it (e.g. `"wiki:5"`), or every row one level under it (e.g. a chapter's
     * `"chapter:42:continuity:establishedFacts:0"`, `":1"`, ... under prefix
     * `"chapter:42:continuity:establishedFacts"`).
     *
     * Matching requires a `:` right after the prefix, not a bare `LIKE prefix || '%'` — otherwise
     * removing `"wiki:5"` would also match `"wiki:50"`, `"wiki:512"`, and so on.
     */
    @Query(
        "DELETE FROM embedding_entries WHERE sagaId = :sagaId " +
            "AND (sourceKey = :sourceKeyPrefix OR sourceKey LIKE :sourceKeyPrefix || ':%')",
    )
    suspend fun deleteBySourcePrefix(
        sagaId: Int,
        sourceKeyPrefix: String,
    )

    @Query("DELETE FROM embedding_entries WHERE sagaId = :sagaId")
    suspend fun deleteBySaga(sagaId: Int)
}
