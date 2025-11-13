package com.ilustris.sagai.features.characters.relations.`data`.source

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.ilustris.sagai.features.characters.relations.`data`.model.RelationshipUpdateEvent
import javax.`annotation`.processing.Generated
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class RelationshipUpdateEventDao_Impl(
  __db: RoomDatabase,
) : RelationshipUpdateEventDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfRelationshipUpdateEvent: EntityInsertAdapter<RelationshipUpdateEvent>
  init {
    this.__db = __db
    this.__insertAdapterOfRelationshipUpdateEvent = object :
        EntityInsertAdapter<RelationshipUpdateEvent>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `relationship_update_events` (`id`,`relationId`,`timelineId`,`title`,`description`,`emoji`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RelationshipUpdateEvent) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.relationId.toLong())
        statement.bindLong(3, entity.timelineId.toLong())
        statement.bindText(4, entity.title)
        statement.bindText(5, entity.description)
        statement.bindText(6, entity.emoji)
        statement.bindLong(7, entity.timestamp)
      }
    }
  }

  public override suspend fun insertEvent(event: RelationshipUpdateEvent): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfRelationshipUpdateEvent.insertAndReturnId(_connection,
        event)
    _result
  }

  public override suspend fun insertEvents(events: List<RelationshipUpdateEvent>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfRelationshipUpdateEvent.insert(_connection, events)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
