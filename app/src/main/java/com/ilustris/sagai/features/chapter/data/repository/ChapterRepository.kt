package com.ilustris.sagai.features.chapter.data.repository

import com.ilustris.sagai.features.chapter.data.model.Chapter
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {
    suspend fun saveChapter(chapter: Chapter): Chapter

    suspend fun updateChapter(chapter: Chapter): Chapter

    suspend fun deleteChapter(chapter: Chapter)

    suspend fun deleteChapterById(chapterId: Int)

    fun getChaptersInfoBySaga(sagaId: Int): Flow<List<com.ilustris.sagai.features.chapter.data.model.ChapterInfo>>

    suspend fun getChapterContentById(chapterId: Int): com.ilustris.sagai.features.chapter.data.model.ChapterContent?

    suspend fun getChapterById(chapterId: Int): Chapter?

    suspend fun deleteAllChapters()
}
