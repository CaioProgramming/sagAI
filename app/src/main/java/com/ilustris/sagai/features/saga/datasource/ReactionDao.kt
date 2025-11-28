
package com.ilustris.sagai.features.saga.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ilustris.sagai.features.saga.chat.data.model.Reaction

@Dao
interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addReaction(reaction: Reaction): Long

    @Delete
    suspend fun removeReaction(reaction: Reaction)
}
