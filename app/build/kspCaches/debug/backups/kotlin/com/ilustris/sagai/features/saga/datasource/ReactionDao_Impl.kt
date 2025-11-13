package com.ilustris.sagai.features.saga.datasource

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.ilustris.sagai.features.saga.chat.`data`.model.Reaction
import javax.`annotation`.processing.Generated
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ReactionDao_Impl(
  __db: RoomDatabase,
) : ReactionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfReaction: EntityInsertAdapter<Reaction>

  private val __deleteAdapterOfReaction: EntityDeleteOrUpdateAdapter<Reaction>
  init {
    this.__db = __db
    this.__insertAdapterOfReaction = object : EntityInsertAdapter<Reaction>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `reactions` (`id`,`messageId`,`characterId`,`emoji`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Reaction) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.messageId.toLong())
        statement.bindLong(3, entity.characterId.toLong())
        statement.bindText(4, entity.emoji)
        statement.bindLong(5, entity.timestamp)
      }
    }
    this.__deleteAdapterOfReaction = object : EntityDeleteOrUpdateAdapter<Reaction>() {
      protected override fun createQuery(): String = "DELETE FROM `reactions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Reaction) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
  }

  public override suspend fun addReaction(reaction: Reaction): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfReaction.insertAndReturnId(_connection, reaction)
    _result
  }

  public override suspend fun removeReaction(reaction: Reaction): Unit = performSuspending(__db,
      false, true) { _connection ->
    __deleteAdapterOfReaction.handle(_connection, reaction)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
