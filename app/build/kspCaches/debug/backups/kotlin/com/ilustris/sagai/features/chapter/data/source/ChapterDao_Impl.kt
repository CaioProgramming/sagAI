package com.ilustris.sagai.features.chapter.`data`.source

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.performBlocking
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.ilustris.sagai.core.database.converters.IntListConverter
import com.ilustris.sagai.features.chapter.`data`.model.Chapter
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ChapterDao_Impl(
  __db: RoomDatabase,
) : ChapterDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfChapter: EntityInsertAdapter<Chapter>

  private val __deleteAdapterOfChapter: EntityDeleteOrUpdateAdapter<Chapter>

  private val __updateAdapterOfChapter: EntityDeleteOrUpdateAdapter<Chapter>
  init {
    this.__db = __db
    this.__insertAdapterOfChapter = object : EntityInsertAdapter<Chapter>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `Chapter` (`id`,`title`,`overview`,`introduction`,`currentEventId`,`coverImage`,`emotionalReview`,`createdAt`,`actId`,`featuredCharacters`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Chapter) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.overview)
        statement.bindText(4, entity.introduction)
        val _tmpCurrentEventId: Int? = entity.currentEventId
        if (_tmpCurrentEventId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpCurrentEventId.toLong())
        }
        statement.bindText(6, entity.coverImage)
        val _tmpEmotionalReview: String? = entity.emotionalReview
        if (_tmpEmotionalReview == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpEmotionalReview)
        }
        val _tmpCreatedAt: Long? = entity.createdAt
        if (_tmpCreatedAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpCreatedAt)
        }
        statement.bindLong(9, entity.actId.toLong())
        val _tmp: String? = IntListConverter.fromList(entity.featuredCharacters)
        if (_tmp == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmp)
        }
      }
    }
    this.__deleteAdapterOfChapter = object : EntityDeleteOrUpdateAdapter<Chapter>() {
      protected override fun createQuery(): String = "DELETE FROM `Chapter` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Chapter) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfChapter = object : EntityDeleteOrUpdateAdapter<Chapter>() {
      protected override fun createQuery(): String =
          "UPDATE OR REPLACE `Chapter` SET `id` = ?,`title` = ?,`overview` = ?,`introduction` = ?,`currentEventId` = ?,`coverImage` = ?,`emotionalReview` = ?,`createdAt` = ?,`actId` = ?,`featuredCharacters` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Chapter) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.overview)
        statement.bindText(4, entity.introduction)
        val _tmpCurrentEventId: Int? = entity.currentEventId
        if (_tmpCurrentEventId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpCurrentEventId.toLong())
        }
        statement.bindText(6, entity.coverImage)
        val _tmpEmotionalReview: String? = entity.emotionalReview
        if (_tmpEmotionalReview == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpEmotionalReview)
        }
        val _tmpCreatedAt: Long? = entity.createdAt
        if (_tmpCreatedAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpCreatedAt)
        }
        statement.bindLong(9, entity.actId.toLong())
        val _tmp: String? = IntListConverter.fromList(entity.featuredCharacters)
        if (_tmp == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmp)
        }
        statement.bindLong(11, entity.id.toLong())
      }
    }
  }

  public override suspend fun saveChapter(chapter: Chapter): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfChapter.insertAndReturnId(_connection, chapter)
    _result
  }

  public override suspend fun deleteChapter(chapter: Chapter): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfChapter.handle(_connection, chapter)
  }

  public override fun updateChapter(chapter: Chapter): Int = performBlocking(__db, false, true) {
      _connection ->
    var _result: Int = 0
    _result += __updateAdapterOfChapter.handle(_connection, chapter)
    _result
  }

  public override suspend fun deleteChapterById(chapterId: Int) {
    val _sql: String = "DELETE FROM Chapter WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, chapterId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllChapters() {
    val _sql: String = "DELETE FROM Chapter"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
