package com.ilustris.sagai.core.database

import androidx.room.Database
import androidx.room.RenameColumn // Ensure this import is present
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // Added for TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ilustris.sagai.core.database.converters.IntListConverter // Added import for our new converter
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.source.ActDao
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.source.ChapterDao
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.source.CharacterDao
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.datasource.MessageDao
import com.ilustris.sagai.features.saga.datasource.SagaDao
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.source.TimelineDao
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.source.WikiDao

@Database(
    entities = [
        Saga::class,
        Message::class,
        Chapter::class,
        Character::class,
        Wiki::class,
        Timeline::class,
        Act::class,
    ],
    version = 40,
    autoMigrations = [
        androidx.room.AutoMigration(from = 39, to = 40),
    ],
    exportSchema = true,
)
@TypeConverters(IntListConverter::class)
abstract class SagaDatabase : RoomDatabase() {
    abstract fun sagaDao(): SagaDao

    abstract fun messageDao(): MessageDao

    abstract fun chapterDao(): ChapterDao

    abstract fun characterDao(): CharacterDao

    abstract fun wikiDao(): WikiDao

    abstract fun timelineDao(): TimelineDao

    abstract fun actDao(): ActDao
}
