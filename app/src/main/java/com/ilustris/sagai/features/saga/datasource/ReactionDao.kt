package com.ilustris.sagai.features.saga.datasource

import ReactionContent
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ilustris.sagai.features.saga.chat.data.model.Reaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addReaction(reaction: Reaction): Long

    @Delete
    suspend fun removeReaction(reaction: Reaction)

    /** Reactions to [messageIds] created at or after [since] — the ones live mode animates as they land. */
    @Transaction
    @Query("SELECT * FROM reactions WHERE messageId IN (:messageIds) AND timestamp >= :since ORDER BY timestamp")
    fun observeReactions(
        messageIds: List<Int>,
        since: Long,
    ): Flow<List<ReactionContent>>
}
