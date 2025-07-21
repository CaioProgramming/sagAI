package com.ilustris.sagai.features.chapter.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterGen
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import kotlinx.coroutines.flow.Flow

interface ChapterUseCase {
    fun getChaptersBySagaId(sagaId: Int): Flow<List<Chapter>>

    suspend fun getChapterBySagaAndMessageId(
        sagaId: Int,
        messageId: Int,
    ): Chapter?

    suspend fun saveChapter(chapter: Chapter): Chapter

    suspend fun deleteChapter(chapter: Chapter)

    suspend fun updateChapter(chapter: Chapter): Chapter

    suspend fun deleteChapterById(chapterId: Int)

    suspend fun deleteAllChapters()

    suspend fun generateChapterCover(
        chapter: Chapter,
        saga: Saga,
        characters: List<Character>,
    ): RequestResult<Exception, Chapter>

    suspend fun generateChapter(
        saga: SagaContent,
        lastAddedEvents: List<Timeline>,
    ): RequestResult<Exception, ChapterGen>
}
