package com.ilustris.sagai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.source.ChapterDao
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.source.CharacterDao
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.datasource.MessageDao
import com.ilustris.sagai.features.saga.datasource.SagaDao

@Database(
    entities = [SagaData::class, Message::class, Chapter::class, Character::class],
    version = 11,
    exportSchema = true,
)
abstract class SagaDatabase : RoomDatabase() {
    abstract fun sagaDao(): SagaDao

    abstract fun messageDao(): MessageDao

    abstract fun chapterDao(): ChapterDao

    abstract fun characterDao(): CharacterDao
}
