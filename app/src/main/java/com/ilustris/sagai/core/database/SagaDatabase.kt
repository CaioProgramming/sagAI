package com.ilustris.sagai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.source.ChapterDao
import com.ilustris.sagai.features.chat.data.MessageDao
import com.ilustris.sagai.features.chat.data.SagaDao
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.home.data.model.SagaData

@Database(entities = [SagaData::class, Message::class, Chapter::class], version = 5, exportSchema = true)
abstract class SagaDatabase : RoomDatabase() {
    abstract fun sagaDao(): SagaDao

    abstract fun messageDao(): MessageDao

    abstract fun chapterDao(): ChapterDao
}
