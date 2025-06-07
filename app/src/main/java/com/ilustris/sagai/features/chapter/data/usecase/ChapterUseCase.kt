package com.ilustris.sagai.features.chapter.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.flow.Flow

interface ChapterUseCase {
    fun getChaptersBySagaId(sagaId: Int): Flow<List<Chapter>>

    suspend fun getChapterBySagaAndMessageId(
        sagaId: Int,
        messageId: Int,
    ): Chapter?

    suspend fun saveChapter(chapter: Chapter): Long

    suspend fun deleteChapter(chapter: Chapter)

    suspend fun updateChapter(chapter: Chapter): Int

    suspend fun deleteChapterById(chapterId: Int)

    suspend fun deleteAllChapters()

    suspend fun generateChapter(
        saga: SagaData,
        messages: List<Pair<String, String>>,
    ): RequestResult<Exception, Chapter>

    suspend fun generateChapterCover(
        chapter: Chapter,
        genre: Genre,
    ): RequestResult<Exception, ByteArray>
}
