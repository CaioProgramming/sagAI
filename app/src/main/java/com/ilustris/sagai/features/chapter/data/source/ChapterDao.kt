package com.ilustris.sagai.features.chapter.data.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ilustris.sagai.features.chapter.data.model.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM Chapter WHERE sagaId = :sagaId")
    fun getChaptersBySagaId(sagaId: Int): Flow<List<Chapter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChapter(chapter: Chapter): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateChapter(chapter: Chapter): Int

    @Delete
    suspend fun deleteChapter(chapter: Chapter)

    @Query("DELETE FROM Chapter WHERE id = :chapterId")
    suspend fun deleteChapterById(chapterId: Int)

    @Query("DELETE FROM Chapter")
    suspend fun deleteAllChapters()

    @Query("SELECT * FROM Chapter WHERE sagaId = :sagaId AND messageReference = :messageId")
    suspend fun getChapterBySagaAndMessageId(
        sagaId: Int,
        messageId: Int,
    ): Chapter?
}
