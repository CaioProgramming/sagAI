package com.ilustris.sagai.features.chapter.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaData
import kotlinx.coroutines.flow.Flow

interface ChapterUseCase {
    fun getChaptersBySagaId(sagaId: Int): Flow<List<Chapter>>

    suspend fun getChapterBySagaAndMessageId(
        sagaId: Int,
        messageId: Int,
    ): Chapter?

    suspend fun saveChapter(chapter: Chapter): Chapter

    suspend fun deleteChapter(chapter: Chapter)

    suspend fun updateChapter(chapter: Chapter): Int

    suspend fun deleteChapterById(chapterId: Int)

    suspend fun deleteAllChapters()

    suspend fun generateChapter(
        saga: SagaData,
        messageId: Int,
        messages: List<Pair<String, String>>,
        chapters: List<Chapter>,
        characters: List<Character>,
    ): RequestResult<Exception, Chapter>
}
