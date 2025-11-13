package com.ilustris.sagai.features.characters.relations.`data`.source

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.ilustris.sagai.features.characters.relations.`data`.model.CharacterRelation
import javax.`annotation`.processing.Generated
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CharacterRelationDao_Impl(
  __db: RoomDatabase,
) : CharacterRelationDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCharacterRelation: EntityInsertAdapter<CharacterRelation>
  init {
    this.__db = __db
    this.__insertAdapterOfCharacterRelation = object : EntityInsertAdapter<CharacterRelation>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `character_relations` (`id`,`characterOneId`,`characterTwoId`,`sagaId`,`title`,`description`,`emoji`,`lastUpdated`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CharacterRelation) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.characterOneId.toLong())
        statement.bindLong(3, entity.characterTwoId.toLong())
        statement.bindLong(4, entity.sagaId.toLong())
        statement.bindText(5, entity.title)
        statement.bindText(6, entity.description)
        statement.bindText(7, entity.emoji)
        statement.bindLong(8, entity.lastUpdated)
      }
    }
  }

  public override suspend fun insertRelation(characterRelation: CharacterRelation): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfCharacterRelation.insertAndReturnId(_connection,
        characterRelation)
    _result
  }

  public override suspend fun insertRelations(characterRelations: List<CharacterRelation>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCharacterRelation.insert(_connection, characterRelations)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
