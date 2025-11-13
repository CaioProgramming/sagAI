package com.ilustris.sagai.features.saga.datasource

import MessageStatus
import ReactionContent
import androidx.collection.LongSparseArray
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchLongSparseArray
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import com.ilustris.sagai.features.characters.`data`.model.Abilities
import com.ilustris.sagai.features.characters.`data`.model.BodyFeatures
import com.ilustris.sagai.features.characters.`data`.model.Character
import com.ilustris.sagai.features.characters.`data`.model.CharacterProfile
import com.ilustris.sagai.features.characters.`data`.model.Clothing
import com.ilustris.sagai.features.characters.`data`.model.Details
import com.ilustris.sagai.features.characters.`data`.model.FacialFeatures
import com.ilustris.sagai.features.characters.`data`.model.PhysicalTraits
import com.ilustris.sagai.features.saga.chat.`data`.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.`data`.model.Message
import com.ilustris.sagai.features.saga.chat.`data`.model.MessageContent
import com.ilustris.sagai.features.saga.chat.`data`.model.Reaction
import com.ilustris.sagai.features.saga.chat.`data`.model.SenderType
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
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
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MessageDao_Impl(
  __db: RoomDatabase,
) : MessageDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMessage: EntityInsertAdapter<Message>

  private val __updateAdapterOfMessage: EntityDeleteOrUpdateAdapter<Message>
  init {
    this.__db = __db
    this.__insertAdapterOfMessage = object : EntityInsertAdapter<Message>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `messages` (`id`,`text`,`timestamp`,`senderType`,`speakerName`,`sagaId`,`characterId`,`timelineId`,`emotionalTone`,`status`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Message) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.text)
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, __SenderType_enumToString(entity.senderType))
        val _tmpSpeakerName: String? = entity.speakerName
        if (_tmpSpeakerName == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpSpeakerName)
        }
        statement.bindLong(6, entity.sagaId.toLong())
        val _tmpCharacterId: Int? = entity.characterId
        if (_tmpCharacterId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCharacterId.toLong())
        }
        statement.bindLong(8, entity.timelineId.toLong())
        val _tmpEmotionalTone: EmotionalTone? = entity.emotionalTone
        if (_tmpEmotionalTone == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, __EmotionalTone_enumToString(_tmpEmotionalTone))
        }
        val _tmpStatus: MessageStatus? = entity.status
        if (_tmpStatus == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, __MessageStatus_enumToString(_tmpStatus))
        }
      }
    }
    this.__updateAdapterOfMessage = object : EntityDeleteOrUpdateAdapter<Message>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `messages` SET `id` = ?,`text` = ?,`timestamp` = ?,`senderType` = ?,`speakerName` = ?,`sagaId` = ?,`characterId` = ?,`timelineId` = ?,`emotionalTone` = ?,`status` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Message) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.text)
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, __SenderType_enumToString(entity.senderType))
        val _tmpSpeakerName: String? = entity.speakerName
        if (_tmpSpeakerName == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpSpeakerName)
        }
        statement.bindLong(6, entity.sagaId.toLong())
        val _tmpCharacterId: Int? = entity.characterId
        if (_tmpCharacterId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCharacterId.toLong())
        }
        statement.bindLong(8, entity.timelineId.toLong())
        val _tmpEmotionalTone: EmotionalTone? = entity.emotionalTone
        if (_tmpEmotionalTone == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, __EmotionalTone_enumToString(_tmpEmotionalTone))
        }
        val _tmpStatus: MessageStatus? = entity.status
        if (_tmpStatus == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, __MessageStatus_enumToString(_tmpStatus))
        }
        statement.bindLong(11, entity.id.toLong())
      }
    }
  }

  public override suspend fun saveMessage(message: Message): Long? = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long? = __insertAdapterOfMessage.insertAndReturnId(_connection, message)
    _result
  }

  public override suspend fun updateMessage(message: Message): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfMessage.handle(_connection, message)
  }

  public override fun getMessages(sagaId: Int): Flow<List<MessageContent>> {
    val _sql: String = "SELECT * FROM messages WHERE sagaId = ? ORDER BY timestamp ASC"
    return createFlow(__db, true, arrayOf("Characters", "reactions", "messages")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sagaId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSenderType: Int = getColumnIndexOrThrow(_stmt, "senderType")
        val _columnIndexOfSpeakerName: Int = getColumnIndexOrThrow(_stmt, "speakerName")
        val _columnIndexOfSagaId: Int = getColumnIndexOrThrow(_stmt, "sagaId")
        val _columnIndexOfCharacterId: Int = getColumnIndexOrThrow(_stmt, "characterId")
        val _columnIndexOfTimelineId: Int = getColumnIndexOrThrow(_stmt, "timelineId")
        val _columnIndexOfEmotionalTone: Int = getColumnIndexOrThrow(_stmt, "emotionalTone")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _collectionCharacter: LongSparseArray<Character?> = LongSparseArray<Character?>()
        val _collectionReactions: LongSparseArray<MutableList<ReactionContent>> =
            LongSparseArray<MutableList<ReactionContent>>()
        while (_stmt.step()) {
          val _tmpKey: Long?
          if (_stmt.isNull(_columnIndexOfCharacterId)) {
            _tmpKey = null
          } else {
            _tmpKey = _stmt.getLong(_columnIndexOfCharacterId)
          }
          if (_tmpKey != null) {
            _collectionCharacter.put(_tmpKey, null)
          }
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
          if (!_collectionReactions.containsKey(_tmpKey_1)) {
            _collectionReactions.put(_tmpKey_1, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter(_connection,
            _collectionCharacter)
        __fetchRelationshipreactionsAsReactionContent(_connection, _collectionReactions)
        val _result: MutableList<MessageContent> = mutableListOf()
        while (_stmt.step()) {
          val _item: MessageContent
          val _tmpMessage: Message
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSenderType: SenderType
          _tmpSenderType = __SenderType_stringToEnum(_stmt.getText(_columnIndexOfSenderType))
          val _tmpSpeakerName: String?
          if (_stmt.isNull(_columnIndexOfSpeakerName)) {
            _tmpSpeakerName = null
          } else {
            _tmpSpeakerName = _stmt.getText(_columnIndexOfSpeakerName)
          }
          val _tmpSagaId: Int
          _tmpSagaId = _stmt.getLong(_columnIndexOfSagaId).toInt()
          val _tmpCharacterId: Int?
          if (_stmt.isNull(_columnIndexOfCharacterId)) {
            _tmpCharacterId = null
          } else {
            _tmpCharacterId = _stmt.getLong(_columnIndexOfCharacterId).toInt()
          }
          val _tmpTimelineId: Int
          _tmpTimelineId = _stmt.getLong(_columnIndexOfTimelineId).toInt()
          val _tmpEmotionalTone: EmotionalTone?
          if (_stmt.isNull(_columnIndexOfEmotionalTone)) {
            _tmpEmotionalTone = null
          } else {
            _tmpEmotionalTone =
                __EmotionalTone_stringToEnum(_stmt.getText(_columnIndexOfEmotionalTone))
          }
          val _tmpStatus: MessageStatus?
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null
          } else {
            _tmpStatus = __MessageStatus_stringToEnum(_stmt.getText(_columnIndexOfStatus))
          }
          _tmpMessage =
              Message(_tmpId,_tmpText,_tmpTimestamp,_tmpSenderType,_tmpSpeakerName,_tmpSagaId,_tmpCharacterId,_tmpTimelineId,_tmpEmotionalTone,_tmpStatus)
          val _tmpCharacter: Character?
          val _tmpKey_2: Long?
          if (_stmt.isNull(_columnIndexOfCharacterId)) {
            _tmpKey_2 = null
          } else {
            _tmpKey_2 = _stmt.getLong(_columnIndexOfCharacterId)
          }
          if (_tmpKey_2 != null) {
            _tmpCharacter = _collectionCharacter.get(_tmpKey_2)
          } else {
            _tmpCharacter = null
          }
          val _tmpReactionsCollection: MutableList<ReactionContent>
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfId)
          _tmpReactionsCollection = checkNotNull(_collectionReactions.get(_tmpKey_3))
          _item = MessageContent(_tmpMessage,_tmpCharacter,_tmpReactionsCollection)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLastMessage(sagaId: Int): Message? {
    val _sql: String = "SELECT * FROM messages WHERE sagaId = ? ORDER BY timestamp DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sagaId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSenderType: Int = getColumnIndexOrThrow(_stmt, "senderType")
        val _columnIndexOfSpeakerName: Int = getColumnIndexOrThrow(_stmt, "speakerName")
        val _columnIndexOfSagaId: Int = getColumnIndexOrThrow(_stmt, "sagaId")
        val _columnIndexOfCharacterId: Int = getColumnIndexOrThrow(_stmt, "characterId")
        val _columnIndexOfTimelineId: Int = getColumnIndexOrThrow(_stmt, "timelineId")
        val _columnIndexOfEmotionalTone: Int = getColumnIndexOrThrow(_stmt, "emotionalTone")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: Message?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSenderType: SenderType
          _tmpSenderType = __SenderType_stringToEnum(_stmt.getText(_columnIndexOfSenderType))
          val _tmpSpeakerName: String?
          if (_stmt.isNull(_columnIndexOfSpeakerName)) {
            _tmpSpeakerName = null
          } else {
            _tmpSpeakerName = _stmt.getText(_columnIndexOfSpeakerName)
          }
          val _tmpSagaId: Int
          _tmpSagaId = _stmt.getLong(_columnIndexOfSagaId).toInt()
          val _tmpCharacterId: Int?
          if (_stmt.isNull(_columnIndexOfCharacterId)) {
            _tmpCharacterId = null
          } else {
            _tmpCharacterId = _stmt.getLong(_columnIndexOfCharacterId).toInt()
          }
          val _tmpTimelineId: Int
          _tmpTimelineId = _stmt.getLong(_columnIndexOfTimelineId).toInt()
          val _tmpEmotionalTone: EmotionalTone?
          if (_stmt.isNull(_columnIndexOfEmotionalTone)) {
            _tmpEmotionalTone = null
          } else {
            _tmpEmotionalTone =
                __EmotionalTone_stringToEnum(_stmt.getText(_columnIndexOfEmotionalTone))
          }
          val _tmpStatus: MessageStatus?
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null
          } else {
            _tmpStatus = __MessageStatus_stringToEnum(_stmt.getText(_columnIndexOfStatus))
          }
          _result =
              Message(_tmpId,_tmpText,_tmpTimestamp,_tmpSenderType,_tmpSpeakerName,_tmpSagaId,_tmpCharacterId,_tmpTimelineId,_tmpEmotionalTone,_tmpStatus)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteMessage(messageId: Long) {
    val _sql: String = "DELETE FROM messages WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, messageId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteMessages(sagaId: String) {
    val _sql: String = "DELETE FROM messages WHERE  sagaId= ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sagaId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __SenderType_enumToString(_value: SenderType): String = when (_value) {
    SenderType.USER -> "USER"
    SenderType.CHARACTER -> "CHARACTER"
    SenderType.THOUGHT -> "THOUGHT"
    SenderType.ACTION -> "ACTION"
    SenderType.NARRATOR -> "NARRATOR"
  }

  private fun __EmotionalTone_enumToString(_value: EmotionalTone): String = when (_value) {
    EmotionalTone.NEUTRAL -> "NEUTRAL"
    EmotionalTone.CALM -> "CALM"
    EmotionalTone.CURIOUS -> "CURIOUS"
    EmotionalTone.HOPEFUL -> "HOPEFUL"
    EmotionalTone.DETERMINED -> "DETERMINED"
    EmotionalTone.EMPATHETIC -> "EMPATHETIC"
    EmotionalTone.JOYFUL -> "JOYFUL"
    EmotionalTone.CONCERNED -> "CONCERNED"
    EmotionalTone.ANXIOUS -> "ANXIOUS"
    EmotionalTone.FRUSTRATED -> "FRUSTRATED"
    EmotionalTone.ANGRY -> "ANGRY"
    EmotionalTone.SAD -> "SAD"
    EmotionalTone.MELANCHOLIC -> "MELANCHOLIC"
    EmotionalTone.CYNICAL -> "CYNICAL"
  }

  private fun __MessageStatus_enumToString(_value: MessageStatus): String = when (_value) {
    MessageStatus.OK -> "OK"
    MessageStatus.ERROR -> "ERROR"
  }

  private
      fun __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter(_connection: SQLiteConnection,
      _map: LongSparseArray<Character?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`name`,`backstory`,`image`,`hexColor`,`sagaId`,`joinedAt`,`firstSceneId`,`emojified`,`race`,`gender`,`ethnicity`,`height`,`weight`,`hair`,`eyes`,`mouth`,`distinctiveMarks`,`jawline`,`buildAndPosture`,`skinAppearance`,`distinguishFeatures`,`outfitDescription`,`accessories`,`carriedItems`,`skillsAndProficiencies`,`uniqueOrSignatureTalents`,`occupation`,`personality` FROM `Characters` WHERE `id` IN (")
    val _inputSize: Int = _map.size()
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (i in 0 until _map.size()) {
      val _item: Long = _map.keyAt(i)
      _stmt.bindLong(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "id")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfName: Int = 1
      val _columnIndexOfBackstory: Int = 2
      val _columnIndexOfImage: Int = 3
      val _columnIndexOfHexColor: Int = 4
      val _columnIndexOfSagaId: Int = 5
      val _columnIndexOfJoinedAt: Int = 6
      val _columnIndexOfFirstSceneId: Int = 7
      val _columnIndexOfEmojified: Int = 8
      val _columnIndexOfRace: Int = 9
      val _columnIndexOfGender: Int = 10
      val _columnIndexOfEthnicity: Int = 11
      val _columnIndexOfHeight: Int = 12
      val _columnIndexOfWeight: Int = 13
      val _columnIndexOfHair: Int = 14
      val _columnIndexOfEyes: Int = 15
      val _columnIndexOfMouth: Int = 16
      val _columnIndexOfDistinctiveMarks: Int = 17
      val _columnIndexOfJawline: Int = 18
      val _columnIndexOfBuildAndPosture: Int = 19
      val _columnIndexOfSkinAppearance: Int = 20
      val _columnIndexOfDistinguishFeatures: Int = 21
      val _columnIndexOfOutfitDescription: Int = 22
      val _columnIndexOfAccessories: Int = 23
      val _columnIndexOfCarriedItems: Int = 24
      val _columnIndexOfSkillsAndProficiencies: Int = 25
      val _columnIndexOfUniqueOrSignatureTalents: Int = 26
      val _columnIndexOfOccupation: Int = 27
      val _columnIndexOfPersonality: Int = 28
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey)) {
          val _item_1: Character?
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBackstory: String
          _tmpBackstory = _stmt.getText(_columnIndexOfBackstory)
          val _tmpImage: String
          _tmpImage = _stmt.getText(_columnIndexOfImage)
          val _tmpHexColor: String
          _tmpHexColor = _stmt.getText(_columnIndexOfHexColor)
          val _tmpSagaId: Int
          _tmpSagaId = _stmt.getLong(_columnIndexOfSagaId).toInt()
          val _tmpJoinedAt: Long
          _tmpJoinedAt = _stmt.getLong(_columnIndexOfJoinedAt)
          val _tmpFirstSceneId: Int?
          if (_stmt.isNull(_columnIndexOfFirstSceneId)) {
            _tmpFirstSceneId = null
          } else {
            _tmpFirstSceneId = _stmt.getLong(_columnIndexOfFirstSceneId).toInt()
          }
          val _tmpEmojified: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfEmojified).toInt()
          _tmpEmojified = _tmp != 0
          val _tmpDetails: Details
          val _tmpPhysicalTraits: PhysicalTraits
          val _tmpRace: String
          _tmpRace = _stmt.getText(_columnIndexOfRace)
          val _tmpGender: String
          _tmpGender = _stmt.getText(_columnIndexOfGender)
          val _tmpEthnicity: String
          _tmpEthnicity = _stmt.getText(_columnIndexOfEthnicity)
          val _tmpHeight: Double
          _tmpHeight = _stmt.getDouble(_columnIndexOfHeight)
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpFacialDetails: FacialFeatures
          val _tmpHair: String
          _tmpHair = _stmt.getText(_columnIndexOfHair)
          val _tmpEyes: String
          _tmpEyes = _stmt.getText(_columnIndexOfEyes)
          val _tmpMouth: String
          _tmpMouth = _stmt.getText(_columnIndexOfMouth)
          val _tmpDistinctiveMarks: String
          _tmpDistinctiveMarks = _stmt.getText(_columnIndexOfDistinctiveMarks)
          val _tmpJawline: String
          _tmpJawline = _stmt.getText(_columnIndexOfJawline)
          _tmpFacialDetails =
              FacialFeatures(_tmpHair,_tmpEyes,_tmpMouth,_tmpDistinctiveMarks,_tmpJawline)
          val _tmpBodyFeatures: BodyFeatures
          val _tmpBuildAndPosture: String
          _tmpBuildAndPosture = _stmt.getText(_columnIndexOfBuildAndPosture)
          val _tmpSkinAppearance: String
          _tmpSkinAppearance = _stmt.getText(_columnIndexOfSkinAppearance)
          val _tmpDistinguishFeatures: String
          _tmpDistinguishFeatures = _stmt.getText(_columnIndexOfDistinguishFeatures)
          _tmpBodyFeatures =
              BodyFeatures(_tmpBuildAndPosture,_tmpSkinAppearance,_tmpDistinguishFeatures)
          _tmpPhysicalTraits =
              PhysicalTraits(_tmpRace,_tmpGender,_tmpEthnicity,_tmpHeight,_tmpWeight,_tmpFacialDetails,_tmpBodyFeatures)
          val _tmpClothing: Clothing
          val _tmpOutfitDescription: String
          _tmpOutfitDescription = _stmt.getText(_columnIndexOfOutfitDescription)
          val _tmpAccessories: String
          _tmpAccessories = _stmt.getText(_columnIndexOfAccessories)
          val _tmpCarriedItems: String
          _tmpCarriedItems = _stmt.getText(_columnIndexOfCarriedItems)
          _tmpClothing = Clothing(_tmpOutfitDescription,_tmpAccessories,_tmpCarriedItems)
          val _tmpAbilities: Abilities
          val _tmpSkillsAndProficiencies: String
          _tmpSkillsAndProficiencies = _stmt.getText(_columnIndexOfSkillsAndProficiencies)
          val _tmpUniqueOrSignatureTalents: String
          _tmpUniqueOrSignatureTalents = _stmt.getText(_columnIndexOfUniqueOrSignatureTalents)
          _tmpAbilities = Abilities(_tmpSkillsAndProficiencies,_tmpUniqueOrSignatureTalents)
          _tmpDetails = Details(_tmpPhysicalTraits,_tmpClothing,_tmpAbilities)
          val _tmpProfile: CharacterProfile
          val _tmpOccupation: String
          _tmpOccupation = _stmt.getText(_columnIndexOfOccupation)
          val _tmpPersonality: String
          _tmpPersonality = _stmt.getText(_columnIndexOfPersonality)
          _tmpProfile = CharacterProfile(_tmpOccupation,_tmpPersonality)
          _item_1 =
              Character(_tmpId,_tmpName,_tmpBackstory,_tmpImage,_tmpHexColor,_tmpSagaId,_tmpDetails,_tmpProfile,_tmpJoinedAt,_tmpFirstSceneId,_tmpEmojified)
          _map.put(_tmpKey, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter_1(_connection: SQLiteConnection,
      _map: LongSparseArray<Character?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter_1(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`name`,`backstory`,`image`,`hexColor`,`sagaId`,`joinedAt`,`firstSceneId`,`emojified`,`race`,`gender`,`ethnicity`,`height`,`weight`,`hair`,`eyes`,`mouth`,`distinctiveMarks`,`jawline`,`buildAndPosture`,`skinAppearance`,`distinguishFeatures`,`outfitDescription`,`accessories`,`carriedItems`,`skillsAndProficiencies`,`uniqueOrSignatureTalents`,`occupation`,`personality` FROM `Characters` WHERE `id` IN (")
    val _inputSize: Int = _map.size()
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (i in 0 until _map.size()) {
      val _item: Long = _map.keyAt(i)
      _stmt.bindLong(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "id")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfName: Int = 1
      val _columnIndexOfBackstory: Int = 2
      val _columnIndexOfImage: Int = 3
      val _columnIndexOfHexColor: Int = 4
      val _columnIndexOfSagaId: Int = 5
      val _columnIndexOfJoinedAt: Int = 6
      val _columnIndexOfFirstSceneId: Int = 7
      val _columnIndexOfEmojified: Int = 8
      val _columnIndexOfRace: Int = 9
      val _columnIndexOfGender: Int = 10
      val _columnIndexOfEthnicity: Int = 11
      val _columnIndexOfHeight: Int = 12
      val _columnIndexOfWeight: Int = 13
      val _columnIndexOfHair: Int = 14
      val _columnIndexOfEyes: Int = 15
      val _columnIndexOfMouth: Int = 16
      val _columnIndexOfDistinctiveMarks: Int = 17
      val _columnIndexOfJawline: Int = 18
      val _columnIndexOfBuildAndPosture: Int = 19
      val _columnIndexOfSkinAppearance: Int = 20
      val _columnIndexOfDistinguishFeatures: Int = 21
      val _columnIndexOfOutfitDescription: Int = 22
      val _columnIndexOfAccessories: Int = 23
      val _columnIndexOfCarriedItems: Int = 24
      val _columnIndexOfSkillsAndProficiencies: Int = 25
      val _columnIndexOfUniqueOrSignatureTalents: Int = 26
      val _columnIndexOfOccupation: Int = 27
      val _columnIndexOfPersonality: Int = 28
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey)) {
          val _item_1: Character
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBackstory: String
          _tmpBackstory = _stmt.getText(_columnIndexOfBackstory)
          val _tmpImage: String
          _tmpImage = _stmt.getText(_columnIndexOfImage)
          val _tmpHexColor: String
          _tmpHexColor = _stmt.getText(_columnIndexOfHexColor)
          val _tmpSagaId: Int
          _tmpSagaId = _stmt.getLong(_columnIndexOfSagaId).toInt()
          val _tmpJoinedAt: Long
          _tmpJoinedAt = _stmt.getLong(_columnIndexOfJoinedAt)
          val _tmpFirstSceneId: Int?
          if (_stmt.isNull(_columnIndexOfFirstSceneId)) {
            _tmpFirstSceneId = null
          } else {
            _tmpFirstSceneId = _stmt.getLong(_columnIndexOfFirstSceneId).toInt()
          }
          val _tmpEmojified: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfEmojified).toInt()
          _tmpEmojified = _tmp != 0
          val _tmpDetails: Details
          val _tmpPhysicalTraits: PhysicalTraits
          val _tmpRace: String
          _tmpRace = _stmt.getText(_columnIndexOfRace)
          val _tmpGender: String
          _tmpGender = _stmt.getText(_columnIndexOfGender)
          val _tmpEthnicity: String
          _tmpEthnicity = _stmt.getText(_columnIndexOfEthnicity)
          val _tmpHeight: Double
          _tmpHeight = _stmt.getDouble(_columnIndexOfHeight)
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpFacialDetails: FacialFeatures
          val _tmpHair: String
          _tmpHair = _stmt.getText(_columnIndexOfHair)
          val _tmpEyes: String
          _tmpEyes = _stmt.getText(_columnIndexOfEyes)
          val _tmpMouth: String
          _tmpMouth = _stmt.getText(_columnIndexOfMouth)
          val _tmpDistinctiveMarks: String
          _tmpDistinctiveMarks = _stmt.getText(_columnIndexOfDistinctiveMarks)
          val _tmpJawline: String
          _tmpJawline = _stmt.getText(_columnIndexOfJawline)
          _tmpFacialDetails =
              FacialFeatures(_tmpHair,_tmpEyes,_tmpMouth,_tmpDistinctiveMarks,_tmpJawline)
          val _tmpBodyFeatures: BodyFeatures
          val _tmpBuildAndPosture: String
          _tmpBuildAndPosture = _stmt.getText(_columnIndexOfBuildAndPosture)
          val _tmpSkinAppearance: String
          _tmpSkinAppearance = _stmt.getText(_columnIndexOfSkinAppearance)
          val _tmpDistinguishFeatures: String
          _tmpDistinguishFeatures = _stmt.getText(_columnIndexOfDistinguishFeatures)
          _tmpBodyFeatures =
              BodyFeatures(_tmpBuildAndPosture,_tmpSkinAppearance,_tmpDistinguishFeatures)
          _tmpPhysicalTraits =
              PhysicalTraits(_tmpRace,_tmpGender,_tmpEthnicity,_tmpHeight,_tmpWeight,_tmpFacialDetails,_tmpBodyFeatures)
          val _tmpClothing: Clothing
          val _tmpOutfitDescription: String
          _tmpOutfitDescription = _stmt.getText(_columnIndexOfOutfitDescription)
          val _tmpAccessories: String
          _tmpAccessories = _stmt.getText(_columnIndexOfAccessories)
          val _tmpCarriedItems: String
          _tmpCarriedItems = _stmt.getText(_columnIndexOfCarriedItems)
          _tmpClothing = Clothing(_tmpOutfitDescription,_tmpAccessories,_tmpCarriedItems)
          val _tmpAbilities: Abilities
          val _tmpSkillsAndProficiencies: String
          _tmpSkillsAndProficiencies = _stmt.getText(_columnIndexOfSkillsAndProficiencies)
          val _tmpUniqueOrSignatureTalents: String
          _tmpUniqueOrSignatureTalents = _stmt.getText(_columnIndexOfUniqueOrSignatureTalents)
          _tmpAbilities = Abilities(_tmpSkillsAndProficiencies,_tmpUniqueOrSignatureTalents)
          _tmpDetails = Details(_tmpPhysicalTraits,_tmpClothing,_tmpAbilities)
          val _tmpProfile: CharacterProfile
          val _tmpOccupation: String
          _tmpOccupation = _stmt.getText(_columnIndexOfOccupation)
          val _tmpPersonality: String
          _tmpPersonality = _stmt.getText(_columnIndexOfPersonality)
          _tmpProfile = CharacterProfile(_tmpOccupation,_tmpPersonality)
          _item_1 =
              Character(_tmpId,_tmpName,_tmpBackstory,_tmpImage,_tmpHexColor,_tmpSagaId,_tmpDetails,_tmpProfile,_tmpJoinedAt,_tmpFirstSceneId,_tmpEmojified)
          _map.put(_tmpKey, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private fun __fetchRelationshipreactionsAsReactionContent(_connection: SQLiteConnection,
      _map: LongSparseArray<MutableList<ReactionContent>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipreactionsAsReactionContent(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`messageId`,`characterId`,`emoji`,`timestamp` FROM `reactions` WHERE `messageId` IN (")
    val _inputSize: Int = _map.size()
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (i in 0 until _map.size()) {
      val _item: Long = _map.keyAt(i)
      _stmt.bindLong(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "messageId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfMessageId: Int = 1
      val _columnIndexOfCharacterId: Int = 2
      val _columnIndexOfEmoji: Int = 3
      val _columnIndexOfTimestamp: Int = 4
      val _collectionCharacter: LongSparseArray<Character?> = LongSparseArray<Character?>()
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_columnIndexOfCharacterId)
        _collectionCharacter.put(_tmpKey, null)
      }
      _stmt.reset()
      __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter_1(_connection,
          _collectionCharacter)
      while (_stmt.step()) {
        val _tmpKey_1: Long
        _tmpKey_1 = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<ReactionContent>? = _map.get(_tmpKey_1)
        if (_tmpRelation != null) {
          val _item_1: ReactionContent
          val _tmpData: Reaction
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpMessageId: Int
          _tmpMessageId = _stmt.getLong(_columnIndexOfMessageId).toInt()
          val _tmpCharacterId: Int
          _tmpCharacterId = _stmt.getLong(_columnIndexOfCharacterId).toInt()
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _tmpData = Reaction(_tmpId,_tmpMessageId,_tmpCharacterId,_tmpEmoji,_tmpTimestamp)
          val _tmpCharacter: Character?
          val _tmpKey_2: Long
          _tmpKey_2 = _stmt.getLong(_columnIndexOfCharacterId)
          _tmpCharacter = _collectionCharacter.get(_tmpKey_2)
          if (_tmpCharacter == null) {
            error("Relationship item 'character' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'characterId' and entityColumn named 'id'.")
          }
          _item_1 = ReactionContent(_tmpData,_tmpCharacter)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private fun __SenderType_stringToEnum(_value: String): SenderType = when (_value) {
    "USER" -> SenderType.USER
    "CHARACTER" -> SenderType.CHARACTER
    "THOUGHT" -> SenderType.THOUGHT
    "ACTION" -> SenderType.ACTION
    "NARRATOR" -> SenderType.NARRATOR
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  private fun __EmotionalTone_stringToEnum(_value: String): EmotionalTone = when (_value) {
    "NEUTRAL" -> EmotionalTone.NEUTRAL
    "CALM" -> EmotionalTone.CALM
    "CURIOUS" -> EmotionalTone.CURIOUS
    "HOPEFUL" -> EmotionalTone.HOPEFUL
    "DETERMINED" -> EmotionalTone.DETERMINED
    "EMPATHETIC" -> EmotionalTone.EMPATHETIC
    "JOYFUL" -> EmotionalTone.JOYFUL
    "CONCERNED" -> EmotionalTone.CONCERNED
    "ANXIOUS" -> EmotionalTone.ANXIOUS
    "FRUSTRATED" -> EmotionalTone.FRUSTRATED
    "ANGRY" -> EmotionalTone.ANGRY
    "SAD" -> EmotionalTone.SAD
    "MELANCHOLIC" -> EmotionalTone.MELANCHOLIC
    "CYNICAL" -> EmotionalTone.CYNICAL
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  private fun __MessageStatus_stringToEnum(_value: String): MessageStatus = when (_value) {
    "OK" -> MessageStatus.OK
    "ERROR" -> MessageStatus.ERROR
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
