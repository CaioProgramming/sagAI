package com.ilustris.sagai.features.characters.events.`data`.source

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.ilustris.sagai.features.characters.events.`data`.model.CharacterEvent
import javax.`annotation`.processing.Generated
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CharacterEventDao_Impl(
  __db: RoomDatabase,
) : CharacterEventDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCharacterEvent: EntityInsertAdapter<CharacterEvent>
  init {
    this.__db = __db
    this.__insertAdapterOfCharacterEvent = object : EntityInsertAdapter<CharacterEvent>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `character_events` (`id`,`characterId`,`gameTimelineId`,`title`,`summary`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CharacterEvent) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.characterId.toLong())
        statement.bindLong(3, entity.gameTimelineId.toLong())
        statement.bindText(4, entity.title)
        statement.bindText(5, entity.summary)
        statement.bindLong(6, entity.createdAt)
      }
    }
  }

  public override suspend fun insertCharacterEvent(characterEvent: CharacterEvent): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfCharacterEvent.insertAndReturnId(_connection,
        characterEvent)
    _result
  }

  public override suspend fun insertCharacterEvents(characterEvents: List<CharacterEvent>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCharacterEvent.insert(_connection, characterEvents)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
