package com.ilustris.sagai.features.act.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ilustris.sagai.features.act.data.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBook(book: Book): Long

    @Query("SELECT * FROM books WHERE actId = :actId")
    fun getBookForAct(actId: Int): Flow<Book?>

    @Query("SELECT * FROM books WHERE actId = :actId")
    suspend fun getBook(actId: Int): Book?

    @Query("DELETE FROM books WHERE actId = :actId")
    suspend fun deleteBookForAct(actId: Int)
}
