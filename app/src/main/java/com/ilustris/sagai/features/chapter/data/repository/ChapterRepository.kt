package com.ilustris.sagai.features.chapter.data.repository

import com.ilustris.sagai.features.chapter.data.model.Chapter
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {
    fun getChaptersBySagaId(sagaId: Int): Flow<List<Chapter>>

    suspend fun saveChapter(chapter: Chapter): Chapter

    suspend fun updateChapter(chapter: Chapter): Chapter

    suspend fun deleteChapter(chapter: Chapter)

    suspend fun deleteChapterById(chapterId: Int)

    suspend fun deleteAllChapters()

    suspend fun getChapterBySagaAndMessageId(
        chapterId: Int,
        messageId: Int,
    ): Chapter?
}
