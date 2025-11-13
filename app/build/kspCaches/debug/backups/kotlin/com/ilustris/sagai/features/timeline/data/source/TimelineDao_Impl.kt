package com.ilustris.sagai.features.timeline.`data`.source

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.ilustris.sagai.features.timeline.`data`.model.Timeline
import javax.`annotation`.processing.Generated
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
public class TimelineDao_Impl(
  __db: RoomDatabase,
) : TimelineDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTimeline: EntityInsertAdapter<Timeline>

  private val __deleteAdapterOfTimeline: EntityDeleteOrUpdateAdapter<Timeline>

  private val __updateAdapterOfTimeline: EntityDeleteOrUpdateAdapter<Timeline>
  init {
    this.__db = __db
    this.__insertAdapterOfTimeline = object : EntityInsertAdapter<Timeline>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `timelines` (`id`,`title`,`content`,`currentObjective`,`emotionalReview`,`createdAt`,`chapterId`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Timeline) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        val _tmpCurrentObjective: String? = entity.currentObjective
        if (_tmpCurrentObjective == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpCurrentObjective)
        }
        val _tmpEmotionalReview: String? = entity.emotionalReview
        if (_tmpEmotionalReview == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpEmotionalReview)
        }
        statement.bindLong(6, entity.createdAt)
        statement.bindLong(7, entity.chapterId.toLong())
      }
    }
    this.__deleteAdapterOfTimeline = object : EntityDeleteOrUpdateAdapter<Timeline>() {
      protected override fun createQuery(): String = "DELETE FROM `timelines` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Timeline) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfTimeline = object : EntityDeleteOrUpdateAdapter<Timeline>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `timelines` SET `id` = ?,`title` = ?,`content` = ?,`currentObjective` = ?,`emotionalReview` = ?,`createdAt` = ?,`chapterId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Timeline) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        val _tmpCurrentObjective: String? = entity.currentObjective
        if (_tmpCurrentObjective == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpCurrentObjective)
        }
        val _tmpEmotionalReview: String? = entity.emotionalReview
        if (_tmpEmotionalReview == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpEmotionalReview)
        }
        statement.bindLong(6, entity.createdAt)
        statement.bindLong(7, entity.chapterId.toLong())
        statement.bindLong(8, entity.id.toLong())
      }
    }
  }

  public override suspend fun saveTimeline(timeline: Timeline): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfTimeline.insertAndReturnId(_connection, timeline)
    _result
  }

  public override suspend fun deleteTimeline(timeline: Timeline): Unit = performSuspending(__db,
      false, true) { _connection ->
    __deleteAdapterOfTimeline.handle(_connection, timeline)
  }

  public override suspend fun updateTimeline(timeline: Timeline): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfTimeline.handle(_connection, timeline)
  }

  public override fun getTimeline(id: String): Flow<Timeline> {
    val _sql: String = "SELECT * FROM timelines WHERE id = ?"
    return createFlow(__db, false, arrayOf("timelines")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfCurrentObjective: Int = getColumnIndexOrThrow(_stmt, "currentObjective")
        val _columnIndexOfEmotionalReview: Int = getColumnIndexOrThrow(_stmt, "emotionalReview")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfChapterId: Int = getColumnIndexOrThrow(_stmt, "chapterId")
        val _result: Timeline
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpCurrentObjective: String?
          if (_stmt.isNull(_columnIndexOfCurrentObjective)) {
            _tmpCurrentObjective = null
          } else {
            _tmpCurrentObjective = _stmt.getText(_columnIndexOfCurrentObjective)
          }
          val _tmpEmotionalReview: String?
          if (_stmt.isNull(_columnIndexOfEmotionalReview)) {
            _tmpEmotionalReview = null
          } else {
            _tmpEmotionalReview = _stmt.getText(_columnIndexOfEmotionalReview)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpChapterId: Int
          _tmpChapterId = _stmt.getLong(_columnIndexOfChapterId).toInt()
          _result =
              Timeline(_tmpId,_tmpTitle,_tmpContent,_tmpCurrentObjective,_tmpEmotionalReview,_tmpCreatedAt,_tmpChapterId)
        } else {
          error("The query result was empty, but expected a single row to return a NON-NULL object of type <com.ilustris.sagai.features.timeline.`data`.model.Timeline>.")
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllTimelines(): Flow<List<Timeline>> {
    val _sql: String = "SELECT * FROM timelines"
    return createFlow(__db, false, arrayOf("timelines")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfCurrentObjective: Int = getColumnIndexOrThrow(_stmt, "currentObjective")
        val _columnIndexOfEmotionalReview: Int = getColumnIndexOrThrow(_stmt, "emotionalReview")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfChapterId: Int = getColumnIndexOrThrow(_stmt, "chapterId")
        val _result: MutableList<Timeline> = mutableListOf()
        while (_stmt.step()) {
          val _item: Timeline
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpCurrentObjective: String?
          if (_stmt.isNull(_columnIndexOfCurrentObjective)) {
            _tmpCurrentObjective = null
          } else {
            _tmpCurrentObjective = _stmt.getText(_columnIndexOfCurrentObjective)
          }
          val _tmpEmotionalReview: String?
          if (_stmt.isNull(_columnIndexOfEmotionalReview)) {
            _tmpEmotionalReview = null
          } else {
            _tmpEmotionalReview = _stmt.getText(_columnIndexOfEmotionalReview)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpChapterId: Int
          _tmpChapterId = _stmt.getLong(_columnIndexOfChapterId).toInt()
          _item =
              Timeline(_tmpId,_tmpTitle,_tmpContent,_tmpCurrentObjective,_tmpEmotionalReview,_tmpCreatedAt,_tmpChapterId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
