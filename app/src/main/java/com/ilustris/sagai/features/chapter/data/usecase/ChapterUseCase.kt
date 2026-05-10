package com.ilustris.sagai.features.chapter.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent

interface ChapterUseCase {
    suspend fun generateChapterIntroduction(
        saga: SagaContent,
        chapterContent: Chapter,
        act: ActContent,
    ): RequestResult<GeneratedContent<Chapter>>

    suspend fun saveChapter(chapter: Chapter): Chapter

    suspend fun deleteChapter(chapter: Chapter)

    suspend fun updateChapter(chapter: Chapter): Chapter

    suspend fun deleteChapterById(chapterId: Int)

    suspend fun deleteAllChapters()

    fun getChaptersInfoBySaga(sagaId: Int): kotlinx.coroutines.flow.Flow<List<com.ilustris.sagai.features.chapter.data.model.ChapterInfo>>

    suspend fun generateChapterCover(
        chapter: ChapterContent,
        saga: SagaContent,
    ): RequestResult<Chapter>

    suspend fun generateChapterCoverStream(
        chapter: ChapterContent,
        saga: SagaContent,
    ): kotlinx.coroutines.flow.Flow<StreamingState<GeneratedContent<Chapter>>>

    suspend fun generateChapter(
        saga: SagaContent,
        chapterContent: ChapterContent,
    ): RequestResult<Chapter>

    suspend fun generateChapterStream(
        saga: SagaContent,
        chapterContent: ChapterContent,
    ): kotlinx.coroutines.flow.Flow<StreamingState<GeneratedContent<Chapter>>>

    suspend fun reviewChapter(
        saga: SagaContent,
        chapterContent: ChapterContent,
    ): RequestResult<Chapter>

    suspend fun generateChapterIntroductionStream(
        saga: SagaContent,
        chapterContent: Chapter,
        act: ActContent,
    ): kotlinx.coroutines.flow.Flow<StreamingState<GeneratedContent<Chapter>>>

    fun synthesizeChapterEvolutionStream(
        saga: SagaContent,
        chapterContent: ChapterContent,
    ): kotlinx.coroutines.flow.Flow<StreamingState<GeneratedContent<Chapter>>>
}
