package com.ilustris.sagai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.source.ChapterDao
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.source.CharacterDao
import com.ilustris.sagai.features.chat.data.MessageDao
import com.ilustris.sagai.features.chat.data.SagaDao
import com.ilustris.sagai.features.chat.data.model.Message
import com.ilustris.sagai.features.home.data.model.SagaData

@Database(
    entities = [SagaData::class, Message::class, Chapter::class, Character::class],
    version = 8,
    exportSchema = true,
)
abstract class SagaDatabase : RoomDatabase() {
    abstract fun sagaDao(): SagaDao

    abstract fun messageDao(): MessageDao

    abstract fun chapterDao(): ChapterDao

    abstract fun characterDao(): CharacterDao
}
