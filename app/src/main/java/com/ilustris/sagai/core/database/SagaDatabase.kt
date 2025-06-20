package com.ilustris.sagai.core.database

import androidx.room.AutoMigration
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
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.source.TimelineDao
import com.ilustris.sagai.features.wiki.data.model.Wiki // Updated Wiki import
import com.ilustris.sagai.features.wiki.data.source.WikiDao // Added WikiDao import

@Database(
    entities = [
        SagaData::class,
        Message::class,
        Chapter::class,
        Character::class,
        Wiki::class,
        Timeline::class,
    ],
    version = 16,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 15, to = 16),
    ],
)
abstract class SagaDatabase : RoomDatabase() {
    abstract fun sagaDao(): SagaDao

    abstract fun messageDao(): MessageDao

    abstract fun chapterDao(): ChapterDao

    abstract fun characterDao(): CharacterDao

    abstract fun wikiDao(): WikiDao

    abstract fun timelineDao(): TimelineDao
}
