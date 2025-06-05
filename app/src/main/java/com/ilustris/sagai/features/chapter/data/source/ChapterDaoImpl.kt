package com.ilustris.sagai.features.chapter.data.source

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChapterDaoImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : ChapterDao {
        private val chapterDao by lazy {
            database.chapterDao()
        }

        override fun getChaptersBySagaId(sagaId: Int): Flow<List<Chapter>> = chapterDao.getChaptersBySagaId(sagaId)

        override suspend fun saveChapter(chapter: Chapter): Long = chapterDao.saveChapter(chapter)

        override fun updateChapter(chapter: Chapter) = chapterDao.updateChapter(chapter)

        override suspend fun deleteChapter(chapter: Chapter) {
            chapterDao.deleteChapter(chapter)
        }

        override suspend fun deleteChapterById(chapterId: Int) {
            chapterDao.deleteChapterById(chapterId)
        }

        override suspend fun deleteAllChapters() {
            chapterDao.deleteAllChapters()
        }

        override suspend fun getChapterBySagaAndMessageId(
            chapterId: Int,
            messageId: Int,
        ): Chapter? =
            chapterDao.getChapterBySagaAndMessageId(
                chapterId,
                messageId,
            )
    }
