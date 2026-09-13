package com.ilustris.sagai.features.act.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.act.data.model.BookChapterPages
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

    /** Only sealed volumes — incremental volumes still being written have an empty cover quote. */
    @Query(
        "SELECT * FROM books WHERE actId IN (SELECT id FROM acts WHERE sagaId = :sagaId) AND coverQuote != '' ORDER BY id ASC",
    )
    fun getBooksBySaga(sagaId: Int): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChapterPages(pages: BookChapterPages): Long

    @Query("SELECT * FROM book_chapter_pages WHERE chapterId = :chapterId")
    suspend fun getChapterPages(chapterId: Int): BookChapterPages?

    @Query("DELETE FROM book_chapter_pages WHERE chapterId = :chapterId")
    suspend fun deleteChapterPages(chapterId: Int)

    @Query("DELETE FROM book_chapter_pages WHERE chapterId IN (SELECT id FROM Chapter WHERE actId = :actId)")
    suspend fun deleteChapterPagesForAct(actId: Int)
}
