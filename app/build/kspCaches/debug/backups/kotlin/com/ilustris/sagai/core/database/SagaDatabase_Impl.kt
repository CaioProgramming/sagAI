package com.ilustris.sagai.core.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.ilustris.sagai.features.act.`data`.source.ActDao
import com.ilustris.sagai.features.act.`data`.source.ActDao_Impl
import com.ilustris.sagai.features.chapter.`data`.source.ChapterDao
import com.ilustris.sagai.features.chapter.`data`.source.ChapterDao_Impl
import com.ilustris.sagai.features.characters.`data`.source.CharacterDao
import com.ilustris.sagai.features.characters.`data`.source.CharacterDao_Impl
import com.ilustris.sagai.features.characters.events.`data`.source.CharacterEventDao
import com.ilustris.sagai.features.characters.events.`data`.source.CharacterEventDao_Impl
import com.ilustris.sagai.features.characters.relations.`data`.source.CharacterRelationDao
import com.ilustris.sagai.features.characters.relations.`data`.source.CharacterRelationDao_Impl
import com.ilustris.sagai.features.characters.relations.`data`.source.RelationshipUpdateEventDao
import com.ilustris.sagai.features.characters.relations.`data`.source.RelationshipUpdateEventDao_Impl
import com.ilustris.sagai.features.saga.datasource.MessageDao
import com.ilustris.sagai.features.saga.datasource.MessageDao_Impl
import com.ilustris.sagai.features.saga.datasource.ReactionDao
import com.ilustris.sagai.features.saga.datasource.ReactionDao_Impl
import com.ilustris.sagai.features.saga.datasource.SagaDao
import com.ilustris.sagai.features.saga.datasource.SagaDao_Impl
import com.ilustris.sagai.features.timeline.`data`.source.TimelineDao
import com.ilustris.sagai.features.timeline.`data`.source.TimelineDao_Impl
import com.ilustris.sagai.features.wiki.`data`.source.WikiDao
import com.ilustris.sagai.features.wiki.`data`.source.WikiDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SagaDatabase_Impl : SagaDatabase() {
  private val _sagaDao: Lazy<SagaDao> = lazy {
    SagaDao_Impl(this)
  }

  private val _messageDao: Lazy<MessageDao> = lazy {
    MessageDao_Impl(this)
  }

  private val _chapterDao: Lazy<ChapterDao> = lazy {
    ChapterDao_Impl(this)
  }

  private val _characterDao: Lazy<CharacterDao> = lazy {
    CharacterDao_Impl(this)
  }

  private val _wikiDao: Lazy<WikiDao> = lazy {
    WikiDao_Impl(this)
  }

  private val _timelineDao: Lazy<TimelineDao> = lazy {
    TimelineDao_Impl(this)
  }

  private val _actDao: Lazy<ActDao> = lazy {
    ActDao_Impl(this)
  }

  private val _characterEventDao: Lazy<CharacterEventDao> = lazy {
    CharacterEventDao_Impl(this)
  }

  private val _characterRelationDao: Lazy<CharacterRelationDao> = lazy {
    CharacterRelationDao_Impl(this)
  }

  private val _relationshipUpdateEventDao: Lazy<RelationshipUpdateEventDao> = lazy {
    RelationshipUpdateEventDao_Impl(this)
  }

  private val _reactionDao: Lazy<ReactionDao> = lazy {
    ReactionDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3,
        "ec4e16d3d7a0aafe208a7263e681012f", "80afc5551256ca6de2e0856d20f948ae") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sagas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `icon` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `genre` TEXT NOT NULL, `mainCharacterId` INTEGER, `currentActId` INTEGER, `isEnded` INTEGER NOT NULL, `endedAt` INTEGER NOT NULL, `isDebug` INTEGER NOT NULL DEFAULT false, `endMessage` TEXT NOT NULL DEFAULT '', `emotionalReview` TEXT DEFAULT '', `introduction` TEXT DEFAULT '', `playstyle` TEXT DEFAULT '', `topCharacters` TEXT DEFAULT '', `actsInsight` TEXT DEFAULT '', `conclusion` TEXT DEFAULT '', FOREIGN KEY(`mainCharacterId`) REFERENCES `Characters`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sagas_mainCharacterId` ON `sagas` (`mainCharacterId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sagas_currentActId` ON `sagas` (`currentActId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `senderType` TEXT NOT NULL, `speakerName` TEXT, `sagaId` INTEGER NOT NULL, `characterId` INTEGER, `timelineId` INTEGER NOT NULL, `emotionalTone` TEXT, `status` TEXT, FOREIGN KEY(`sagaId`) REFERENCES `sagas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`characterId`) REFERENCES `Characters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`timelineId`) REFERENCES `timelines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_sagaId` ON `messages` (`sagaId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_characterId` ON `messages` (`characterId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_timelineId` ON `messages` (`timelineId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `Chapter` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `overview` TEXT NOT NULL, `introduction` TEXT NOT NULL DEFAULT '', `currentEventId` INTEGER, `coverImage` TEXT NOT NULL, `emotionalReview` TEXT, `createdAt` INTEGER, `actId` INTEGER NOT NULL, `featuredCharacters` TEXT NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_Chapter_actId` ON `Chapter` (`actId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `Characters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `backstory` TEXT NOT NULL, `image` TEXT NOT NULL, `hexColor` TEXT NOT NULL, `sagaId` INTEGER NOT NULL, `joinedAt` INTEGER NOT NULL, `firstSceneId` INTEGER, `emojified` INTEGER NOT NULL, `race` TEXT NOT NULL, `gender` TEXT NOT NULL, `ethnicity` TEXT NOT NULL, `height` REAL NOT NULL, `weight` REAL NOT NULL, `hair` TEXT NOT NULL, `eyes` TEXT NOT NULL, `mouth` TEXT NOT NULL, `distinctiveMarks` TEXT NOT NULL, `jawline` TEXT NOT NULL, `buildAndPosture` TEXT NOT NULL, `skinAppearance` TEXT NOT NULL, `distinguishFeatures` TEXT NOT NULL, `outfitDescription` TEXT NOT NULL, `accessories` TEXT NOT NULL, `carriedItems` TEXT NOT NULL, `skillsAndProficiencies` TEXT NOT NULL, `uniqueOrSignatureTalents` TEXT NOT NULL, `occupation` TEXT NOT NULL, `personality` TEXT NOT NULL, FOREIGN KEY(`sagaId`) REFERENCES `sagas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_Characters_sagaId` ON `Characters` (`sagaId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_Characters_firstSceneId` ON `Characters` (`firstSceneId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `wikis` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `type` TEXT, `emojiTag` TEXT, `createdAt` INTEGER NOT NULL, `sagaId` INTEGER NOT NULL, `timelineId` INTEGER, FOREIGN KEY(`sagaId`) REFERENCES `sagas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_wikis_sagaId` ON `wikis` (`sagaId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `timelines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `currentObjective` TEXT, `emotionalReview` TEXT, `createdAt` INTEGER NOT NULL, `chapterId` INTEGER NOT NULL, FOREIGN KEY(`chapterId`) REFERENCES `Chapter`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_timelines_chapterId` ON `timelines` (`chapterId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `acts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `introduction` TEXT NOT NULL DEFAULT '', `emotionalReview` TEXT DEFAULT '', `sagaId` INTEGER, `currentChapterId` INTEGER, FOREIGN KEY(`currentChapterId`) REFERENCES `Chapter`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_acts_sagaId` ON `acts` (`sagaId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_acts_currentChapterId` ON `acts` (`currentChapterId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `character_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `characterId` INTEGER NOT NULL, `gameTimelineId` INTEGER NOT NULL, `title` TEXT NOT NULL, `summary` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`characterId`) REFERENCES `Characters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`gameTimelineId`) REFERENCES `timelines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_character_events_characterId` ON `character_events` (`characterId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_character_events_gameTimelineId` ON `character_events` (`gameTimelineId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `character_relations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `characterOneId` INTEGER NOT NULL, `characterTwoId` INTEGER NOT NULL, `sagaId` INTEGER NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `emoji` TEXT NOT NULL, `lastUpdated` INTEGER NOT NULL, FOREIGN KEY(`characterOneId`) REFERENCES `Characters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`characterTwoId`) REFERENCES `Characters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_character_relations_characterOneId_characterTwoId_sagaId` ON `character_relations` (`characterOneId`, `characterTwoId`, `sagaId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_character_relations_characterOneId` ON `character_relations` (`characterOneId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_character_relations_characterTwoId` ON `character_relations` (`characterTwoId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_character_relations_sagaId` ON `character_relations` (`sagaId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `relationship_update_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `relationId` INTEGER NOT NULL, `timelineId` INTEGER NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `emoji` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`relationId`) REFERENCES `character_relations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`timelineId`) REFERENCES `timelines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_relationship_update_events_relationId` ON `relationship_update_events` (`relationId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_relationship_update_events_timelineId` ON `relationship_update_events` (`timelineId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `reactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `messageId` INTEGER NOT NULL, `characterId` INTEGER NOT NULL, `emoji` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`messageId`) REFERENCES `messages`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`characterId`) REFERENCES `Characters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_reactions_messageId` ON `reactions` (`messageId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_reactions_characterId` ON `reactions` (`characterId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ec4e16d3d7a0aafe208a7263e681012f')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `sagas`")
        connection.execSQL("DROP TABLE IF EXISTS `messages`")
        connection.execSQL("DROP TABLE IF EXISTS `Chapter`")
        connection.execSQL("DROP TABLE IF EXISTS `Characters`")
        connection.execSQL("DROP TABLE IF EXISTS `wikis`")
        connection.execSQL("DROP TABLE IF EXISTS `timelines`")
        connection.execSQL("DROP TABLE IF EXISTS `acts`")
        connection.execSQL("DROP TABLE IF EXISTS `character_events`")
        connection.execSQL("DROP TABLE IF EXISTS `character_relations`")
        connection.execSQL("DROP TABLE IF EXISTS `relationship_update_events`")
        connection.execSQL("DROP TABLE IF EXISTS `reactions`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsSagas: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSagas.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("description", TableInfo.Column("description", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("icon", TableInfo.Column("icon", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("genre", TableInfo.Column("genre", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("mainCharacterId", TableInfo.Column("mainCharacterId", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("currentActId", TableInfo.Column("currentActId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("isEnded", TableInfo.Column("isEnded", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("endedAt", TableInfo.Column("endedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("isDebug", TableInfo.Column("isDebug", "INTEGER", true, 0, "false",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("endMessage", TableInfo.Column("endMessage", "TEXT", true, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("emotionalReview", TableInfo.Column("emotionalReview", "TEXT", false, 0,
            "''", TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("introduction", TableInfo.Column("introduction", "TEXT", false, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("playstyle", TableInfo.Column("playstyle", "TEXT", false, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("topCharacters", TableInfo.Column("topCharacters", "TEXT", false, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("actsInsight", TableInfo.Column("actsInsight", "TEXT", false, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSagas.put("conclusion", TableInfo.Column("conclusion", "TEXT", false, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSagas: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSagas.add(TableInfo.ForeignKey("Characters", "SET NULL", "NO ACTION",
            listOf("mainCharacterId"), listOf("id")))
        val _indicesSagas: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSagas.add(TableInfo.Index("index_sagas_mainCharacterId", false,
            listOf("mainCharacterId"), listOf("ASC")))
        _indicesSagas.add(TableInfo.Index("index_sagas_currentActId", false, listOf("currentActId"),
            listOf("ASC")))
        val _infoSagas: TableInfo = TableInfo("sagas", _columnsSagas, _foreignKeysSagas,
            _indicesSagas)
        val _existingSagas: TableInfo = read(connection, "sagas")
        if (!_infoSagas.equals(_existingSagas)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sagas(com.ilustris.sagai.features.home.data.model.Saga).
              | Expected:
              |""".trimMargin() + _infoSagas + """
              |
              | Found:
              |""".trimMargin() + _existingSagas)
        }
        val _columnsMessages: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMessages.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("text", TableInfo.Column("text", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("senderType", TableInfo.Column("senderType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("speakerName", TableInfo.Column("speakerName", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("sagaId", TableInfo.Column("sagaId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("characterId", TableInfo.Column("characterId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("timelineId", TableInfo.Column("timelineId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("emotionalTone", TableInfo.Column("emotionalTone", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("status", TableInfo.Column("status", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMessages: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysMessages.add(TableInfo.ForeignKey("sagas", "CASCADE", "NO ACTION",
            listOf("sagaId"), listOf("id")))
        _foreignKeysMessages.add(TableInfo.ForeignKey("Characters", "CASCADE", "NO ACTION",
            listOf("characterId"), listOf("id")))
        _foreignKeysMessages.add(TableInfo.ForeignKey("timelines", "CASCADE", "NO ACTION",
            listOf("timelineId"), listOf("id")))
        val _indicesMessages: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesMessages.add(TableInfo.Index("index_messages_sagaId", false, listOf("sagaId"),
            listOf("ASC")))
        _indicesMessages.add(TableInfo.Index("index_messages_characterId", false,
            listOf("characterId"), listOf("ASC")))
        _indicesMessages.add(TableInfo.Index("index_messages_timelineId", false,
            listOf("timelineId"), listOf("ASC")))
        val _infoMessages: TableInfo = TableInfo("messages", _columnsMessages, _foreignKeysMessages,
            _indicesMessages)
        val _existingMessages: TableInfo = read(connection, "messages")
        if (!_infoMessages.equals(_existingMessages)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |messages(com.ilustris.sagai.features.saga.chat.data.model.Message).
              | Expected:
              |""".trimMargin() + _infoMessages + """
              |
              | Found:
              |""".trimMargin() + _existingMessages)
        }
        val _columnsChapter: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChapter.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChapter.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChapter.put("overview", TableInfo.Column("overview", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChapter.put("introduction", TableInfo.Column("introduction", "TEXT", true, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChapter.put("currentEventId", TableInfo.Column("currentEventId", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapter.put("coverImage", TableInfo.Column("coverImage", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChapter.put("emotionalReview", TableInfo.Column("emotionalReview", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapter.put("createdAt", TableInfo.Column("createdAt", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChapter.put("actId", TableInfo.Column("actId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChapter.put("featuredCharacters", TableInfo.Column("featuredCharacters", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChapter: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesChapter: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesChapter.add(TableInfo.Index("index_Chapter_actId", false, listOf("actId"),
            listOf("ASC")))
        val _infoChapter: TableInfo = TableInfo("Chapter", _columnsChapter, _foreignKeysChapter,
            _indicesChapter)
        val _existingChapter: TableInfo = read(connection, "Chapter")
        if (!_infoChapter.equals(_existingChapter)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |Chapter(com.ilustris.sagai.features.chapter.data.model.Chapter).
              | Expected:
              |""".trimMargin() + _infoChapter + """
              |
              | Found:
              |""".trimMargin() + _existingChapter)
        }
        val _columnsCharacters: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCharacters.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("backstory", TableInfo.Column("backstory", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("image", TableInfo.Column("image", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("hexColor", TableInfo.Column("hexColor", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("sagaId", TableInfo.Column("sagaId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("joinedAt", TableInfo.Column("joinedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("firstSceneId", TableInfo.Column("firstSceneId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("emojified", TableInfo.Column("emojified", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("race", TableInfo.Column("race", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("gender", TableInfo.Column("gender", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("ethnicity", TableInfo.Column("ethnicity", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("height", TableInfo.Column("height", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("weight", TableInfo.Column("weight", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("hair", TableInfo.Column("hair", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("eyes", TableInfo.Column("eyes", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("mouth", TableInfo.Column("mouth", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("distinctiveMarks", TableInfo.Column("distinctiveMarks", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("jawline", TableInfo.Column("jawline", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("buildAndPosture", TableInfo.Column("buildAndPosture", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("skinAppearance", TableInfo.Column("skinAppearance", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("distinguishFeatures", TableInfo.Column("distinguishFeatures",
            "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("outfitDescription", TableInfo.Column("outfitDescription", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("accessories", TableInfo.Column("accessories", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("carriedItems", TableInfo.Column("carriedItems", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("skillsAndProficiencies", TableInfo.Column("skillsAndProficiencies",
            "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("uniqueOrSignatureTalents",
            TableInfo.Column("uniqueOrSignatureTalents", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("occupation", TableInfo.Column("occupation", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacters.put("personality", TableInfo.Column("personality", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCharacters: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysCharacters.add(TableInfo.ForeignKey("sagas", "CASCADE", "NO ACTION",
            listOf("sagaId"), listOf("id")))
        val _indicesCharacters: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCharacters.add(TableInfo.Index("index_Characters_sagaId", false, listOf("sagaId"),
            listOf("ASC")))
        _indicesCharacters.add(TableInfo.Index("index_Characters_firstSceneId", false,
            listOf("firstSceneId"), listOf("ASC")))
        val _infoCharacters: TableInfo = TableInfo("Characters", _columnsCharacters,
            _foreignKeysCharacters, _indicesCharacters)
        val _existingCharacters: TableInfo = read(connection, "Characters")
        if (!_infoCharacters.equals(_existingCharacters)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |Characters(com.ilustris.sagai.features.characters.data.model.Character).
              | Expected:
              |""".trimMargin() + _infoCharacters + """
              |
              | Found:
              |""".trimMargin() + _existingCharacters)
        }
        val _columnsWikis: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWikis.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWikis.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWikis.put("content", TableInfo.Column("content", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWikis.put("type", TableInfo.Column("type", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWikis.put("emojiTag", TableInfo.Column("emojiTag", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWikis.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWikis.put("sagaId", TableInfo.Column("sagaId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWikis.put("timelineId", TableInfo.Column("timelineId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWikis: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysWikis.add(TableInfo.ForeignKey("sagas", "CASCADE", "NO ACTION",
            listOf("sagaId"), listOf("id")))
        val _indicesWikis: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWikis.add(TableInfo.Index("index_wikis_sagaId", false, listOf("sagaId"),
            listOf("ASC")))
        val _infoWikis: TableInfo = TableInfo("wikis", _columnsWikis, _foreignKeysWikis,
            _indicesWikis)
        val _existingWikis: TableInfo = read(connection, "wikis")
        if (!_infoWikis.equals(_existingWikis)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |wikis(com.ilustris.sagai.features.wiki.data.model.Wiki).
              | Expected:
              |""".trimMargin() + _infoWikis + """
              |
              | Found:
              |""".trimMargin() + _existingWikis)
        }
        val _columnsTimelines: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTimelines.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTimelines.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTimelines.put("content", TableInfo.Column("content", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTimelines.put("currentObjective", TableInfo.Column("currentObjective", "TEXT",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTimelines.put("emotionalReview", TableInfo.Column("emotionalReview", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTimelines.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTimelines.put("chapterId", TableInfo.Column("chapterId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTimelines: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysTimelines.add(TableInfo.ForeignKey("Chapter", "CASCADE", "NO ACTION",
            listOf("chapterId"), listOf("id")))
        val _indicesTimelines: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTimelines.add(TableInfo.Index("index_timelines_chapterId", false,
            listOf("chapterId"), listOf("ASC")))
        val _infoTimelines: TableInfo = TableInfo("timelines", _columnsTimelines,
            _foreignKeysTimelines, _indicesTimelines)
        val _existingTimelines: TableInfo = read(connection, "timelines")
        if (!_infoTimelines.equals(_existingTimelines)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |timelines(com.ilustris.sagai.features.timeline.data.model.Timeline).
              | Expected:
              |""".trimMargin() + _infoTimelines + """
              |
              | Found:
              |""".trimMargin() + _existingTimelines)
        }
        val _columnsActs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsActs.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActs.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActs.put("content", TableInfo.Column("content", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActs.put("introduction", TableInfo.Column("introduction", "TEXT", true, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActs.put("emotionalReview", TableInfo.Column("emotionalReview", "TEXT", false, 0,
            "''", TableInfo.CREATED_FROM_ENTITY))
        _columnsActs.put("sagaId", TableInfo.Column("sagaId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActs.put("currentChapterId", TableInfo.Column("currentChapterId", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysActs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysActs.add(TableInfo.ForeignKey("Chapter", "SET NULL", "NO ACTION",
            listOf("currentChapterId"), listOf("id")))
        val _indicesActs: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesActs.add(TableInfo.Index("index_acts_sagaId", false, listOf("sagaId"),
            listOf("ASC")))
        _indicesActs.add(TableInfo.Index("index_acts_currentChapterId", false,
            listOf("currentChapterId"), listOf("ASC")))
        val _infoActs: TableInfo = TableInfo("acts", _columnsActs, _foreignKeysActs, _indicesActs)
        val _existingActs: TableInfo = read(connection, "acts")
        if (!_infoActs.equals(_existingActs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |acts(com.ilustris.sagai.features.act.data.model.Act).
              | Expected:
              |""".trimMargin() + _infoActs + """
              |
              | Found:
              |""".trimMargin() + _existingActs)
        }
        val _columnsCharacterEvents: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCharacterEvents.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterEvents.put("characterId", TableInfo.Column("characterId", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterEvents.put("gameTimelineId", TableInfo.Column("gameTimelineId", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterEvents.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterEvents.put("summary", TableInfo.Column("summary", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterEvents.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCharacterEvents: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysCharacterEvents.add(TableInfo.ForeignKey("Characters", "CASCADE", "NO ACTION",
            listOf("characterId"), listOf("id")))
        _foreignKeysCharacterEvents.add(TableInfo.ForeignKey("timelines", "CASCADE", "NO ACTION",
            listOf("gameTimelineId"), listOf("id")))
        val _indicesCharacterEvents: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCharacterEvents.add(TableInfo.Index("index_character_events_characterId", false,
            listOf("characterId"), listOf("ASC")))
        _indicesCharacterEvents.add(TableInfo.Index("index_character_events_gameTimelineId", false,
            listOf("gameTimelineId"), listOf("ASC")))
        val _infoCharacterEvents: TableInfo = TableInfo("character_events", _columnsCharacterEvents,
            _foreignKeysCharacterEvents, _indicesCharacterEvents)
        val _existingCharacterEvents: TableInfo = read(connection, "character_events")
        if (!_infoCharacterEvents.equals(_existingCharacterEvents)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |character_events(com.ilustris.sagai.features.characters.events.data.model.CharacterEvent).
              | Expected:
              |""".trimMargin() + _infoCharacterEvents + """
              |
              | Found:
              |""".trimMargin() + _existingCharacterEvents)
        }
        val _columnsCharacterRelations: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCharacterRelations.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterRelations.put("characterOneId", TableInfo.Column("characterOneId",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterRelations.put("characterTwoId", TableInfo.Column("characterTwoId",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterRelations.put("sagaId", TableInfo.Column("sagaId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterRelations.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterRelations.put("description", TableInfo.Column("description", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterRelations.put("emoji", TableInfo.Column("emoji", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCharacterRelations.put("lastUpdated", TableInfo.Column("lastUpdated", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCharacterRelations: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysCharacterRelations.add(TableInfo.ForeignKey("Characters", "CASCADE",
            "NO ACTION", listOf("characterOneId"), listOf("id")))
        _foreignKeysCharacterRelations.add(TableInfo.ForeignKey("Characters", "CASCADE",
            "NO ACTION", listOf("characterTwoId"), listOf("id")))
        val _indicesCharacterRelations: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCharacterRelations.add(TableInfo.Index("index_character_relations_characterOneId_characterTwoId_sagaId",
            true, listOf("characterOneId", "characterTwoId", "sagaId"), listOf("ASC", "ASC",
            "ASC")))
        _indicesCharacterRelations.add(TableInfo.Index("index_character_relations_characterOneId",
            false, listOf("characterOneId"), listOf("ASC")))
        _indicesCharacterRelations.add(TableInfo.Index("index_character_relations_characterTwoId",
            false, listOf("characterTwoId"), listOf("ASC")))
        _indicesCharacterRelations.add(TableInfo.Index("index_character_relations_sagaId", false,
            listOf("sagaId"), listOf("ASC")))
        val _infoCharacterRelations: TableInfo = TableInfo("character_relations",
            _columnsCharacterRelations, _foreignKeysCharacterRelations, _indicesCharacterRelations)
        val _existingCharacterRelations: TableInfo = read(connection, "character_relations")
        if (!_infoCharacterRelations.equals(_existingCharacterRelations)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |character_relations(com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation).
              | Expected:
              |""".trimMargin() + _infoCharacterRelations + """
              |
              | Found:
              |""".trimMargin() + _existingCharacterRelations)
        }
        val _columnsRelationshipUpdateEvents: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRelationshipUpdateEvents.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRelationshipUpdateEvents.put("relationId", TableInfo.Column("relationId", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRelationshipUpdateEvents.put("timelineId", TableInfo.Column("timelineId", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRelationshipUpdateEvents.put("title", TableInfo.Column("title", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRelationshipUpdateEvents.put("description", TableInfo.Column("description", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRelationshipUpdateEvents.put("emoji", TableInfo.Column("emoji", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRelationshipUpdateEvents.put("timestamp", TableInfo.Column("timestamp", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRelationshipUpdateEvents: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysRelationshipUpdateEvents.add(TableInfo.ForeignKey("character_relations",
            "CASCADE", "NO ACTION", listOf("relationId"), listOf("id")))
        _foreignKeysRelationshipUpdateEvents.add(TableInfo.ForeignKey("timelines", "CASCADE",
            "NO ACTION", listOf("timelineId"), listOf("id")))
        val _indicesRelationshipUpdateEvents: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesRelationshipUpdateEvents.add(TableInfo.Index("index_relationship_update_events_relationId",
            false, listOf("relationId"), listOf("ASC")))
        _indicesRelationshipUpdateEvents.add(TableInfo.Index("index_relationship_update_events_timelineId",
            false, listOf("timelineId"), listOf("ASC")))
        val _infoRelationshipUpdateEvents: TableInfo = TableInfo("relationship_update_events",
            _columnsRelationshipUpdateEvents, _foreignKeysRelationshipUpdateEvents,
            _indicesRelationshipUpdateEvents)
        val _existingRelationshipUpdateEvents: TableInfo = read(connection,
            "relationship_update_events")
        if (!_infoRelationshipUpdateEvents.equals(_existingRelationshipUpdateEvents)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |relationship_update_events(com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent).
              | Expected:
              |""".trimMargin() + _infoRelationshipUpdateEvents + """
              |
              | Found:
              |""".trimMargin() + _existingRelationshipUpdateEvents)
        }
        val _columnsReactions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsReactions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReactions.put("messageId", TableInfo.Column("messageId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReactions.put("characterId", TableInfo.Column("characterId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReactions.put("emoji", TableInfo.Column("emoji", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReactions.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysReactions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysReactions.add(TableInfo.ForeignKey("messages", "CASCADE", "NO ACTION",
            listOf("messageId"), listOf("id")))
        _foreignKeysReactions.add(TableInfo.ForeignKey("Characters", "CASCADE", "NO ACTION",
            listOf("characterId"), listOf("id")))
        val _indicesReactions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesReactions.add(TableInfo.Index("index_reactions_messageId", false,
            listOf("messageId"), listOf("ASC")))
        _indicesReactions.add(TableInfo.Index("index_reactions_characterId", false,
            listOf("characterId"), listOf("ASC")))
        val _infoReactions: TableInfo = TableInfo("reactions", _columnsReactions,
            _foreignKeysReactions, _indicesReactions)
        val _existingReactions: TableInfo = read(connection, "reactions")
        if (!_infoReactions.equals(_existingReactions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |reactions(com.ilustris.sagai.features.saga.chat.data.model.Reaction).
              | Expected:
              |""".trimMargin() + _infoReactions + """
              |
              | Found:
              |""".trimMargin() + _existingReactions)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "sagas", "messages", "Chapter",
        "Characters", "wikis", "timelines", "acts", "character_events", "character_relations",
        "relationship_update_events", "reactions")
  }

  public override fun clearAllTables() {
    super.performClear(true, "sagas", "messages", "Chapter", "Characters", "wikis", "timelines",
        "acts", "character_events", "character_relations", "relationship_update_events",
        "reactions")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(SagaDao::class, SagaDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MessageDao::class, MessageDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ChapterDao::class, ChapterDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CharacterDao::class, CharacterDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(WikiDao::class, WikiDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TimelineDao::class, TimelineDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ActDao::class, ActDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CharacterEventDao::class, CharacterEventDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CharacterRelationDao::class,
        CharacterRelationDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(RelationshipUpdateEventDao::class,
        RelationshipUpdateEventDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ReactionDao::class, ReactionDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun sagaDao(): SagaDao = _sagaDao.value

  public override fun messageDao(): MessageDao = _messageDao.value

  public override fun chapterDao(): ChapterDao = _chapterDao.value

  public override fun characterDao(): CharacterDao = _characterDao.value

  public override fun wikiDao(): WikiDao = _wikiDao.value

  public override fun timelineDao(): TimelineDao = _timelineDao.value

  public override fun actDao(): ActDao = _actDao.value

  public override fun characterEventDao(): CharacterEventDao = _characterEventDao.value

  public override fun characterRelationDao(): CharacterRelationDao = _characterRelationDao.value

  public override fun relationshipUpdateEventDao(): RelationshipUpdateEventDao =
      _relationshipUpdateEventDao.value

  public override fun reactionDao(): ReactionDao = _reactionDao.value
}
