package com.ilustris.sagai.features.chapter.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChapterGeneration
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.timeline.data.model.Timeline
import kotlinx.coroutines.flow.Flow

interface ChapterUseCase {
    suspend fun generateChapterIntroduction(
        saga: SagaContent,
        chapterContent: Chapter,
        act: ActContent,
    ): RequestResult<Chapter>

    suspend fun saveChapter(chapter: Chapter): Chapter

    suspend fun deleteChapter(chapter: Chapter)

    suspend fun updateChapter(chapter: Chapter): Chapter

    suspend fun deleteChapterById(chapterId: Int)

    suspend fun deleteAllChapters()

    suspend fun generateChapterCover(
        chapter: ChapterContent,
        saga: SagaContent,
    ): RequestResult<Chapter>

    suspend fun generateChapter(
        saga: SagaContent,
        chapterContent: ChapterContent,
    ): RequestResult<ChapterGeneration>
}
