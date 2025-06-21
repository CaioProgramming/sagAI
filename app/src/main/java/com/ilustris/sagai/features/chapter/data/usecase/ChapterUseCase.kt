package com.ilustris.sagai.features.chapter.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
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

    suspend fun generateChapter(
        saga: SagaContent,
        messageReference: Message,
        messages: List<MessageContent>,
    ): RequestResult<Exception, Chapter>
}
