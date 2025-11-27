package com.ilustris.sagai.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ilustris.sagai.core.database.converters.IntListConverter
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.source.ActDao
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.source.ChapterDao
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.source.CharacterDao
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.source.CharacterEventDao
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent
import com.ilustris.sagai.features.characters.relations.data.source.CharacterRelationDao
import com.ilustris.sagai.features.characters.relations.data.source.RelationshipUpdateEventDao
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.Reaction
import com.ilustris.sagai.features.saga.datasource.MessageDao
import com.ilustris.sagai.features.saga.datasource.ReactionDao
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
        CharacterEvent::class,
        CharacterRelation::class,
        RelationshipUpdateEvent::class,
        Reaction::class,
    ],
    version = 6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
    ],
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

    abstract fun characterEventDao(): CharacterEventDao

    abstract fun characterRelationDao(): CharacterRelationDao

    abstract fun relationshipUpdateEventDao(): RelationshipUpdateEventDao

    abstract fun reactionDao(): ReactionDao
}
