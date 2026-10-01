package com.ilustris.sagai.features.chapter.data.source

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import javax.inject.Inject

class ChapterDaoImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : ChapterDao {
        private val chapterDao by lazy {
            database.chapterDao()
        }

        override suspend fun saveChapter(chapter: Chapter): Long = chapterDao.saveChapter(chapter)

        override suspend fun updateChapter(chapter: Chapter) = chapterDao.updateChapter(chapter)

        override suspend fun deleteChapter(chapter: Chapter) {
            chapterDao.deleteChapter(chapter)
        }

        override suspend fun deleteChapterById(chapterId: Int) {
            chapterDao.deleteChapterById(chapterId)
        }

        override fun getChaptersInfoBySaga(sagaId: Int) = chapterDao.getChaptersInfoBySaga(sagaId)

        override suspend fun deleteAllChapters() {
            chapterDao.deleteAllChapters()
        }

        override fun getChaptersCount(sagaId: Int) = chapterDao.getChaptersCount(sagaId)

        override fun getChaptersBySaga(sagaId: Int) = chapterDao.getChaptersBySaga(sagaId)

        override suspend fun getChapterContentById(chapterId: Int) = chapterDao.getChapterContentById(chapterId)

        override suspend fun getChapterById(chapterId: Int) = chapterDao.getChapterById(chapterId)
    }
