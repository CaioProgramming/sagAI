package com.ilustris.sagai.features.chat.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ilustris.sagai.features.home.data.model.ChatData
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM ChatData")
    fun getAllChats(): Flow<List<ChatData>>

    @Query("SELECT * FROM ChatData WHERE id = :chatId")
    fun getChat(chatId: String): Flow<ChatData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChatData(ChatData: ChatData) : Long

    @Delete
    suspend fun deleteChatData(ChatData: ChatData)

    @Query("DELETE FROM ChatData WHERE id = :chatId")
    suspend fun deleteChatData(chatId: String)

    @Query("DELETE FROM ChatData")
    suspend fun deleteAllChats()
}
