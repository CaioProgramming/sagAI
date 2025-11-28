package com.ilustris.sagai.features.chapter.data.repository

import android.util.Log
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.source.ChapterDao
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
            Log.i(javaClass.simpleName, "updateChapter: Updating to ->\n${chapter.toJsonFormat()}")
            chapterDao.updateChapter(chapter)
            return chapter
        }

        override suspend fun deleteChapter(chapter: Chapter) = chapterDao.deleteChapter(chapter)

        override suspend fun deleteChapterById(chapterId: Int) = chapterDao.deleteChapterById(chapterId)

        override suspend fun deleteAllChapters() = chapterDao.deleteAllChapters()
    }
