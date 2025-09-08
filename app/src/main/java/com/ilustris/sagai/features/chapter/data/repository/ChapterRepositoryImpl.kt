package com.ilustris.sagai.features.chapter.data.repository

import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.source.ChapterDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class ChapterRepositoryImpl
    @Inject
    constructor(
        private val database: SagaDatabase,
    ) : ChapterRepository {
        private val chapterDao: ChapterDao by lazy {
            database.chapterDao()
        }


        override suspend fun saveChapter(chapter: Chapter) =
            chapter.copy(
                id = chapterDao.saveChapter(chapter).toInt(),
                createdAt = Calendar.getInstance().timeInMillis,
            )

        override suspend fun updateChapter(chapter: Chapter): Chapter {
            chapterDao.updateChapter(chapter)
            return chapter
        }

        override suspend fun deleteChapter(chapter: Chapter) = chapterDao.deleteChapter(chapter)

        override suspend fun deleteChapterById(chapterId: Int) = chapterDao.deleteChapterById(chapterId)

        override suspend fun deleteAllChapters() = chapterDao.deleteAllChapters()


    }
