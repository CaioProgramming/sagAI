package com.ilustris.sagai.features.wiki.`data`.source

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.ilustris.sagai.features.wiki.`data`.model.Wiki
import com.ilustris.sagai.features.wiki.`data`.model.WikiType
import javax.`annotation`.processing.Generated
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WikiDao_Impl(
  __db: RoomDatabase,
) : WikiDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWiki: EntityInsertAdapter<Wiki>

  private val __updateAdapterOfWiki: EntityDeleteOrUpdateAdapter<Wiki>
  init {
    this.__db = __db
    this.__insertAdapterOfWiki = object : EntityInsertAdapter<Wiki>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `wikis` (`id`,`title`,`content`,`type`,`emojiTag`,`createdAt`,`sagaId`,`timelineId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Wiki) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        val _tmpType: WikiType? = entity.type
        if (_tmpType == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, __WikiType_enumToString(_tmpType))
        }
        val _tmpEmojiTag: String? = entity.emojiTag
        if (_tmpEmojiTag == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpEmojiTag)
        }
        statement.bindLong(6, entity.createdAt)
        statement.bindLong(7, entity.sagaId.toLong())
        val _tmpTimelineId: Int? = entity.timelineId
        if (_tmpTimelineId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpTimelineId.toLong())
        }
      }
    }
    this.__updateAdapterOfWiki = object : EntityDeleteOrUpdateAdapter<Wiki>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `wikis` SET `id` = ?,`title` = ?,`content` = ?,`type` = ?,`emojiTag` = ?,`createdAt` = ?,`sagaId` = ?,`timelineId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Wiki) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        val _tmpType: WikiType? = entity.type
        if (_tmpType == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, __WikiType_enumToString(_tmpType))
        }
        val _tmpEmojiTag: String? = entity.emojiTag
        if (_tmpEmojiTag == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpEmojiTag)
        }
        statement.bindLong(6, entity.createdAt)
        statement.bindLong(7, entity.sagaId.toLong())
        val _tmpTimelineId: Int? = entity.timelineId
        if (_tmpTimelineId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpTimelineId.toLong())
        }
        statement.bindLong(9, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertWiki(wiki: Wiki): Long = performSuspending(__db, false, true) {
      _connection ->
    val _result: Long = __insertAdapterOfWiki.insertAndReturnId(_connection, wiki)
    _result
  }

  public override suspend fun updateWiki(wiki: Wiki): Unit = performSuspending(__db, false, true) {
      _connection ->
    __updateAdapterOfWiki.handle(_connection, wiki)
  }

  public override fun getWikisBySaga(sagaId: Int): Flow<List<Wiki>> {
    val _sql: String = "SELECT * FROM wikis WHERE sagaId = ? ORDER BY title ASC"
    return createFlow(__db, false, arrayOf("wikis")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sagaId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfEmojiTag: Int = getColumnIndexOrThrow(_stmt, "emojiTag")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSagaId: Int = getColumnIndexOrThrow(_stmt, "sagaId")
        val _columnIndexOfTimelineId: Int = getColumnIndexOrThrow(_stmt, "timelineId")
        val _result: MutableList<Wiki> = mutableListOf()
        while (_stmt.step()) {
          val _item: Wiki
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpType: WikiType?
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmpType = null
          } else {
            _tmpType = __WikiType_stringToEnum(_stmt.getText(_columnIndexOfType))
          }
          val _tmpEmojiTag: String?
          if (_stmt.isNull(_columnIndexOfEmojiTag)) {
            _tmpEmojiTag = null
          } else {
            _tmpEmojiTag = _stmt.getText(_columnIndexOfEmojiTag)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSagaId: Int
          _tmpSagaId = _stmt.getLong(_columnIndexOfSagaId).toInt()
          val _tmpTimelineId: Int?
          if (_stmt.isNull(_columnIndexOfTimelineId)) {
            _tmpTimelineId = null
          } else {
            _tmpTimelineId = _stmt.getLong(_columnIndexOfTimelineId).toInt()
          }
          _item =
              Wiki(_tmpId,_tmpTitle,_tmpContent,_tmpType,_tmpEmojiTag,_tmpCreatedAt,_tmpSagaId,_tmpTimelineId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getWikiById(wikiId: Int): Wiki? {
    val _sql: String = "SELECT * FROM wikis WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, wikiId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfEmojiTag: Int = getColumnIndexOrThrow(_stmt, "emojiTag")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSagaId: Int = getColumnIndexOrThrow(_stmt, "sagaId")
        val _columnIndexOfTimelineId: Int = getColumnIndexOrThrow(_stmt, "timelineId")
        val _result: Wiki?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpType: WikiType?
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmpType = null
          } else {
            _tmpType = __WikiType_stringToEnum(_stmt.getText(_columnIndexOfType))
          }
          val _tmpEmojiTag: String?
          if (_stmt.isNull(_columnIndexOfEmojiTag)) {
            _tmpEmojiTag = null
          } else {
            _tmpEmojiTag = _stmt.getText(_columnIndexOfEmojiTag)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSagaId: Int
          _tmpSagaId = _stmt.getLong(_columnIndexOfSagaId).toInt()
          val _tmpTimelineId: Int?
          if (_stmt.isNull(_columnIndexOfTimelineId)) {
            _tmpTimelineId = null
          } else {
            _tmpTimelineId = _stmt.getLong(_columnIndexOfTimelineId).toInt()
          }
          _result =
              Wiki(_tmpId,_tmpTitle,_tmpContent,_tmpType,_tmpEmojiTag,_tmpCreatedAt,_tmpSagaId,_tmpTimelineId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteWiki(wikiId: Int) {
    val _sql: String = "DELETE FROM wikis WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, wikiId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteWikisBySaga(sagaId: Int) {
    val _sql: String = "DELETE FROM wikis WHERE sagaId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sagaId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __WikiType_enumToString(_value: WikiType): String = when (_value) {
    WikiType.LOCATION -> "LOCATION"
    WikiType.FACTION -> "FACTION"
    WikiType.ITEM -> "ITEM"
    WikiType.CREATURE -> "CREATURE"
    WikiType.CONCEPT -> "CONCEPT"
    WikiType.ORGANIZATION -> "ORGANIZATION"
    WikiType.TECHNOLOGY -> "TECHNOLOGY"
    WikiType.MAGIC -> "MAGIC"
    WikiType.OTHER -> "OTHER"
  }

  private fun __WikiType_stringToEnum(_value: String): WikiType = when (_value) {
    "LOCATION" -> WikiType.LOCATION
    "FACTION" -> WikiType.FACTION
    "ITEM" -> WikiType.ITEM
    "CREATURE" -> WikiType.CREATURE
    "CONCEPT" -> WikiType.CONCEPT
    "ORGANIZATION" -> WikiType.ORGANIZATION
    "TECHNOLOGY" -> WikiType.TECHNOLOGY
    "MAGIC" -> WikiType.MAGIC
    "OTHER" -> WikiType.OTHER
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
