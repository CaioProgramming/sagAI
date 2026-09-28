package com.ilustris.sagai.features.saga.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface EpilogueMessageDao {
    @Query("SELECT * FROM epilogue_messages WHERE characterId = :characterId ORDER BY timestamp ASC, id ASC")
    fun observeConversation(characterId: Int): Flow<List<EpilogueMessage>>

    @Query("SELECT * FROM epilogue_messages WHERE characterId = :characterId ORDER BY timestamp ASC, id ASC")
    suspend fun getConversation(characterId: Int): List<EpilogueMessage>

    @Query("SELECT * FROM epilogue_messages WHERE characterId = :characterId AND id > :afterId ORDER BY timestamp ASC, id ASC")
    suspend fun getMessagesAfter(
        characterId: Int,
        afterId: Int,
    ): List<EpilogueMessage>

    @Insert
    suspend fun insert(message: EpilogueMessage): Long

    @Update
    suspend fun update(message: EpilogueMessage)

    @Query("DELETE FROM epilogue_messages WHERE characterId = :characterId")
    suspend fun deleteConversation(characterId: Int)
}
