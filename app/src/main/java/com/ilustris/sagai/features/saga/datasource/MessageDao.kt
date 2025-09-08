package com.ilustris.sagai.features.saga.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Transaction
    @Query("SELECT * FROM messages WHERE sagaId = :sagaId ORDER BY timestamp ASC")
    fun getMessages(sagaId: Int): Flow<List<MessageContent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMessage(message: Message): Long?

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("DELETE FROM messages WHERE  sagaId= :sagaId")
    suspend fun deleteMessages(sagaId: String)

    @Update
    suspend fun updateMessage(message: Message)

    @Query("SELECT * FROM messages WHERE sagaId = :sagaId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(sagaId: Int): Message?
}
