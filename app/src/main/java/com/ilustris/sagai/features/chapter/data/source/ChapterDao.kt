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
}
