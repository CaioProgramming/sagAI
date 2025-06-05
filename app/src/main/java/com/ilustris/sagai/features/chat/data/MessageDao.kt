package com.ilustris.sagai.features.chat.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ilustris.sagai.features.chat.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM Message WHERE sagaId = :sagaId ORDER BY timestamp ASC")
    fun getMessages(sagaId: Int): Flow<List<Message>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMessage(message: Message): Long
    @Query("DELETE FROM Message WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)
    @Query("DELETE FROM Message WHERE  sagaId= :sagaId")
    suspend fun deleteMessages(sagaId: String)
    @Update
    suspend fun updateMessage(message: Message)
}