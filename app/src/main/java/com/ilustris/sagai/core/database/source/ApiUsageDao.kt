package com.ilustris.sagai.core.database.source

import androidx.room.Dao
import androidx.room.Query
import com.ilustris.sagai.core.database.model.ApiUsageDay
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiUsageDao {
    @Query("SELECT * FROM api_usage_days WHERE day = :day ORDER BY requests DESC")
    fun observeDay(day: String): Flow<List<ApiUsageDay>>

    /**
     * Adds one call to the running totals, inserting the row on first use.
     *
     * Written as an upsert in SQL rather than read-modify-write in Kotlin so concurrent
     * generations against different tiers cannot lose each other's increments.
     */
    @Query(
        """
        INSERT INTO api_usage_days (day, model, requests, promptTokens, candidatesTokens, thoughtsTokens)
        VALUES (:day, :model, 1, :promptTokens, :candidatesTokens, :thoughtsTokens)
        ON CONFLICT(day, model) DO UPDATE SET
            requests = requests + 1,
            promptTokens = promptTokens + :promptTokens,
            candidatesTokens = candidatesTokens + :candidatesTokens,
            thoughtsTokens = thoughtsTokens + :thoughtsTokens
        """,
    )
    suspend fun record(
        day: String,
        model: String,
        promptTokens: Long,
        candidatesTokens: Long,
        thoughtsTokens: Long,
    )

    /** Keeps the table from growing forever; a week is plenty to show yesterday for context. */
    @Query("DELETE FROM api_usage_days WHERE day < :oldestDayToKeep")
    suspend fun prune(oldestDayToKeep: String)
}
