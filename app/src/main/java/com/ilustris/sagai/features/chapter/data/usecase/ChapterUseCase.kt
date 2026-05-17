package com.ilustris.sagai.features.chapter.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.Chapter

interface ChapterUseCase {
    suspend fun generateChapterIntroduction(
        sagaId: Int,
        chapterContent: Chapter,
    ): RequestResult<GeneratedContent<Chapter>>

    suspend fun saveChapter(chapter: Chapter): Chapter

    suspend fun deleteChapter(chapter: Chapter)

    suspend fun updateChapter(chapter: Chapter): Chapter

    suspend fun deleteChapterById(chapterId: Int)

    suspend fun deleteAllChapters()

    fun getChaptersInfoBySaga(sagaId: Int): kotlinx.coroutines.flow.Flow<List<com.ilustris.sagai.features.chapter.data.model.ChapterInfo>>

    suspend fun generateChapterCover(chapterId: Int): RequestResult<Chapter>

    suspend fun generateChapterCoverStream(chapterId: Int): kotlinx.coroutines.flow.Flow<StreamingState<GeneratedContent<Chapter>>>

    suspend fun generateChapter(chapterId: Int): RequestResult<Chapter>

    suspend fun generateChapterStream(chapterId: Int): kotlinx.coroutines.flow.Flow<StreamingState<GeneratedContent<Chapter>>>

    suspend fun reviewChapter(chapterId: Int): RequestResult<Chapter>

    suspend fun generateChapterIntroductionStream(chapterId: Int): kotlinx.coroutines.flow.Flow<StreamingState<GeneratedContent<Chapter>>>

    fun synthesizeChapterEvolutionStream(chapterId: Int): kotlinx.coroutines.flow.Flow<StreamingState<GeneratedContent<Chapter>>>
}
