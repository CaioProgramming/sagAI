package com.ilustris.sagai.features.chapter.data.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ilustris.sagai.features.chapter.data.model.Chapter

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChapter(chapter: Chapter): Long

    @Update
    suspend fun updateChapter(chapter: Chapter): Int

    @Delete
    suspend fun deleteChapter(chapter: Chapter)

    @Query("DELETE FROM Chapter WHERE id = :chapterId")
    suspend fun deleteChapterById(chapterId: Int)

    @Query(
        "SELECT id, title, overview, coverImage, actId, :sagaId as sagaId, featuredCharacters, emotionalReview, createdAt FROM Chapter WHERE actId IN (SELECT id FROM acts WHERE sagaId = :sagaId)",
    )
    fun getChaptersInfoBySaga(sagaId: Int): kotlinx.coroutines.flow.Flow<List<com.ilustris.sagai.features.chapter.data.model.ChapterInfo>>

    @Query("DELETE FROM Chapter")
    suspend fun deleteAllChapters()

    @Query("SELECT COUNT(*) FROM Chapter WHERE actId IN (SELECT id FROM acts WHERE sagaId = :sagaId)")
    fun getChaptersCount(sagaId: Int): kotlinx.coroutines.flow.Flow<Int>

    @androidx.room.Transaction
    @Query("SELECT * FROM Chapter WHERE actId IN (SELECT id FROM acts WHERE sagaId = :sagaId)")
    fun getChaptersBySaga(sagaId: Int): kotlinx.coroutines.flow.Flow<List<com.ilustris.sagai.features.chapter.data.model.ChapterContent>>

    @androidx.room.Transaction
    @Query("SELECT * FROM Chapter WHERE id = :chapterId")
    suspend fun getChapterContentById(chapterId: Int): com.ilustris.sagai.features.chapter.data.model.ChapterContent?

    @Query("SELECT * FROM Chapter WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: Int): Chapter?
}
