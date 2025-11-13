package com.ilustris.sagai.features.act.`data`.source

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
import com.ilustris.sagai.core.database.converters.IntListConverter
import com.ilustris.sagai.features.act.`data`.model.Act
import com.ilustris.sagai.features.act.`data`.model.ActContent
import com.ilustris.sagai.features.chapter.`data`.model.Chapter
import com.ilustris.sagai.features.chapter.`data`.model.ChapterContent
import com.ilustris.sagai.features.characters.`data`.model.Abilities
import com.ilustris.sagai.features.characters.`data`.model.BodyFeatures
import com.ilustris.sagai.features.characters.`data`.model.Character
import com.ilustris.sagai.features.characters.`data`.model.CharacterProfile
import com.ilustris.sagai.features.characters.`data`.model.Clothing
import com.ilustris.sagai.features.characters.`data`.model.Details
import com.ilustris.sagai.features.characters.`data`.model.FacialFeatures
import com.ilustris.sagai.features.characters.`data`.model.PhysicalTraits
import com.ilustris.sagai.features.characters.events.`data`.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.`data`.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.relations.`data`.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.`data`.model.RelationshipContent
import com.ilustris.sagai.features.characters.relations.`data`.model.RelationshipUpdateEvent
import com.ilustris.sagai.features.saga.chat.`data`.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.`data`.model.Message
import com.ilustris.sagai.features.saga.chat.`data`.model.MessageContent
import com.ilustris.sagai.features.saga.chat.`data`.model.Reaction
import com.ilustris.sagai.features.saga.chat.`data`.model.SenderType
import com.ilustris.sagai.features.timeline.`data`.model.Timeline
import com.ilustris.sagai.features.timeline.`data`.model.TimelineContent
import com.ilustris.sagai.features.wiki.`data`.model.Wiki
import com.ilustris.sagai.features.wiki.`data`.model.WikiType
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
public class ActDao_Impl(
  __db: RoomDatabase,
) : ActDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAct: EntityInsertAdapter<Act>

  private val __deleteAdapterOfAct: EntityDeleteOrUpdateAdapter<Act>

  private val __updateAdapterOfAct: EntityDeleteOrUpdateAdapter<Act>
  init {
    this.__db = __db
    this.__insertAdapterOfAct = object : EntityInsertAdapter<Act>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `acts` (`id`,`title`,`content`,`introduction`,`emotionalReview`,`sagaId`,`currentChapterId`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Act) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        statement.bindText(4, entity.introduction)
        val _tmpEmotionalReview: String? = entity.emotionalReview
        if (_tmpEmotionalReview == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpEmotionalReview)
        }
        val _tmpSagaId: Int? = entity.sagaId
        if (_tmpSagaId == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpSagaId.toLong())
        }
        val _tmpCurrentChapterId: Int? = entity.currentChapterId
        if (_tmpCurrentChapterId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCurrentChapterId.toLong())
        }
      }
    }
    this.__deleteAdapterOfAct = object : EntityDeleteOrUpdateAdapter<Act>() {
      protected override fun createQuery(): String = "DELETE FROM `acts` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Act) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfAct = object : EntityDeleteOrUpdateAdapter<Act>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `acts` SET `id` = ?,`title` = ?,`content` = ?,`introduction` = ?,`emotionalReview` = ?,`sagaId` = ?,`currentChapterId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Act) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        statement.bindText(4, entity.introduction)
        val _tmpEmotionalReview: String? = entity.emotionalReview
        if (_tmpEmotionalReview == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpEmotionalReview)
        }
        val _tmpSagaId: Int? = entity.sagaId
        if (_tmpSagaId == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpSagaId.toLong())
        }
        val _tmpCurrentChapterId: Int? = entity.currentChapterId
        if (_tmpCurrentChapterId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCurrentChapterId.toLong())
        }
        statement.bindLong(8, entity.id.toLong())
      }
    }
  }

  public override suspend fun insert(act: Act): Long = performSuspending(__db, false, true) {
      _connection ->
    val _result: Long = __insertAdapterOfAct.insertAndReturnId(_connection, act)
    _result
  }

  public override suspend fun insertAll(acts: List<Act>): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfAct.insert(_connection, acts)
  }

  public override suspend fun delete(act: Act): Unit = performSuspending(__db, false, true) {
      _connection ->
    __deleteAdapterOfAct.handle(_connection, act)
  }

  public override suspend fun update(act: Act): Unit = performSuspending(__db, false, true) {
      _connection ->
    __updateAdapterOfAct.handle(_connection, act)
  }

  public override fun getActsForSaga(sagaId: Int): Flow<List<Act>> {
    val _sql: String = "SELECT * FROM acts WHERE sagaId = ? ORDER BY title ASC"
    return createFlow(__db, false, arrayOf("acts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sagaId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfIntroduction: Int = getColumnIndexOrThrow(_stmt, "introduction")
        val _columnIndexOfEmotionalReview: Int = getColumnIndexOrThrow(_stmt, "emotionalReview")
        val _columnIndexOfSagaId: Int = getColumnIndexOrThrow(_stmt, "sagaId")
        val _columnIndexOfCurrentChapterId: Int = getColumnIndexOrThrow(_stmt, "currentChapterId")
        val _result: MutableList<Act> = mutableListOf()
        while (_stmt.step()) {
          val _item: Act
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpIntroduction: String
          _tmpIntroduction = _stmt.getText(_columnIndexOfIntroduction)
          val _tmpEmotionalReview: String?
          if (_stmt.isNull(_columnIndexOfEmotionalReview)) {
            _tmpEmotionalReview = null
          } else {
            _tmpEmotionalReview = _stmt.getText(_columnIndexOfEmotionalReview)
          }
          val _tmpSagaId: Int?
          if (_stmt.isNull(_columnIndexOfSagaId)) {
            _tmpSagaId = null
          } else {
            _tmpSagaId = _stmt.getLong(_columnIndexOfSagaId).toInt()
          }
          val _tmpCurrentChapterId: Int?
          if (_stmt.isNull(_columnIndexOfCurrentChapterId)) {
            _tmpCurrentChapterId = null
          } else {
            _tmpCurrentChapterId = _stmt.getLong(_columnIndexOfCurrentChapterId).toInt()
          }
          _item =
              Act(_tmpId,_tmpTitle,_tmpContent,_tmpIntroduction,_tmpEmotionalReview,_tmpSagaId,_tmpCurrentChapterId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getActContentsForSaga(sagaId: Int): Flow<List<ActContent>> {
    val _sql: String = "SELECT * FROM acts WHERE sagaId = ? ORDER BY title ASC"
    return createFlow(__db, true, arrayOf("Characters", "reactions", "messages", "timelines",
        "character_events", "wikis", "relationship_update_events", "character_relations", "Chapter",
        "acts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sagaId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfIntroduction: Int = getColumnIndexOrThrow(_stmt, "introduction")
        val _columnIndexOfEmotionalReview: Int = getColumnIndexOrThrow(_stmt, "emotionalReview")
        val _columnIndexOfSagaId: Int = getColumnIndexOrThrow(_stmt, "sagaId")
        val _columnIndexOfCurrentChapterId: Int = getColumnIndexOrThrow(_stmt, "currentChapterId")
        val _collectionCurrentChapterInfo: LongSparseArray<ChapterContent?> =
            LongSparseArray<ChapterContent?>()
        val _collectionChapters: LongSparseArray<MutableList<ChapterContent>> =
            LongSparseArray<MutableList<ChapterContent>>()
        while (_stmt.step()) {
          val _tmpKey: Long?
          if (_stmt.isNull(_columnIndexOfCurrentChapterId)) {
            _tmpKey = null
          } else {
            _tmpKey = _stmt.getLong(_columnIndexOfCurrentChapterId)
          }
          if (_tmpKey != null) {
            _collectionCurrentChapterInfo.put(_tmpKey, null)
          }
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
          if (!_collectionChapters.containsKey(_tmpKey_1)) {
            _collectionChapters.put(_tmpKey_1, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipChapterAscomIlustrisSagaiFeaturesChapterDataModelChapterContent(_connection,
            _collectionCurrentChapterInfo)
        __fetchRelationshipChapterAscomIlustrisSagaiFeaturesChapterDataModelChapterContent_1(_connection,
            _collectionChapters)
        val _result: MutableList<ActContent> = mutableListOf()
        while (_stmt.step()) {
          val _item: ActContent
          val _tmpData: Act
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpIntroduction: String
          _tmpIntroduction = _stmt.getText(_columnIndexOfIntroduction)
          val _tmpEmotionalReview: String?
          if (_stmt.isNull(_columnIndexOfEmotionalReview)) {
            _tmpEmotionalReview = null
          } else {
            _tmpEmotionalReview = _stmt.getText(_columnIndexOfEmotionalReview)
          }
          val _tmpSagaId: Int?
          if (_stmt.isNull(_columnIndexOfSagaId)) {
            _tmpSagaId = null
          } else {
            _tmpSagaId = _stmt.getLong(_columnIndexOfSagaId).toInt()
          }
          val _tmpCurrentChapterId: Int?
          if (_stmt.isNull(_columnIndexOfCurrentChapterId)) {
            _tmpCurrentChapterId = null
          } else {
            _tmpCurrentChapterId = _stmt.getLong(_columnIndexOfCurrentChapterId).toInt()
          }
          _tmpData =
              Act(_tmpId,_tmpTitle,_tmpContent,_tmpIntroduction,_tmpEmotionalReview,_tmpSagaId,_tmpCurrentChapterId)
          val _tmpCurrentChapterInfo: ChapterContent?
          val _tmpKey_2: Long?
          if (_stmt.isNull(_columnIndexOfCurrentChapterId)) {
            _tmpKey_2 = null
          } else {
            _tmpKey_2 = _stmt.getLong(_columnIndexOfCurrentChapterId)
          }
          if (_tmpKey_2 != null) {
            _tmpCurrentChapterInfo = _collectionCurrentChapterInfo.get(_tmpKey_2)
          } else {
            _tmpCurrentChapterInfo = null
          }
          val _tmpChaptersCollection: MutableList<ChapterContent>
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfId)
          _tmpChaptersCollection = checkNotNull(_collectionChapters.get(_tmpKey_3))
          _item = ActContent(_tmpData,_tmpCurrentChapterInfo,_tmpChaptersCollection)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getActContent(actId: Int): Flow<ActContent?> {
    val _sql: String = "SELECT * FROM acts WHERE id = ?"
    return createFlow(__db, true, arrayOf("Characters", "reactions", "messages", "timelines",
        "character_events", "wikis", "relationship_update_events", "character_relations", "Chapter",
        "acts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, actId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfIntroduction: Int = getColumnIndexOrThrow(_stmt, "introduction")
        val _columnIndexOfEmotionalReview: Int = getColumnIndexOrThrow(_stmt, "emotionalReview")
        val _columnIndexOfSagaId: Int = getColumnIndexOrThrow(_stmt, "sagaId")
        val _columnIndexOfCurrentChapterId: Int = getColumnIndexOrThrow(_stmt, "currentChapterId")
        val _collectionCurrentChapterInfo: LongSparseArray<ChapterContent?> =
            LongSparseArray<ChapterContent?>()
        val _collectionChapters: LongSparseArray<MutableList<ChapterContent>> =
            LongSparseArray<MutableList<ChapterContent>>()
        while (_stmt.step()) {
          val _tmpKey: Long?
          if (_stmt.isNull(_columnIndexOfCurrentChapterId)) {
            _tmpKey = null
          } else {
            _tmpKey = _stmt.getLong(_columnIndexOfCurrentChapterId)
          }
          if (_tmpKey != null) {
            _collectionCurrentChapterInfo.put(_tmpKey, null)
          }
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
          if (!_collectionChapters.containsKey(_tmpKey_1)) {
            _collectionChapters.put(_tmpKey_1, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipChapterAscomIlustrisSagaiFeaturesChapterDataModelChapterContent(_connection,
            _collectionCurrentChapterInfo)
        __fetchRelationshipChapterAscomIlustrisSagaiFeaturesChapterDataModelChapterContent_1(_connection,
            _collectionChapters)
        val _result: ActContent?
        if (_stmt.step()) {
          val _tmpData: Act
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpIntroduction: String
          _tmpIntroduction = _stmt.getText(_columnIndexOfIntroduction)
          val _tmpEmotionalReview: String?
          if (_stmt.isNull(_columnIndexOfEmotionalReview)) {
            _tmpEmotionalReview = null
          } else {
            _tmpEmotionalReview = _stmt.getText(_columnIndexOfEmotionalReview)
          }
          val _tmpSagaId: Int?
          if (_stmt.isNull(_columnIndexOfSagaId)) {
            _tmpSagaId = null
          } else {
            _tmpSagaId = _stmt.getLong(_columnIndexOfSagaId).toInt()
          }
          val _tmpCurrentChapterId: Int?
          if (_stmt.isNull(_columnIndexOfCurrentChapterId)) {
            _tmpCurrentChapterId = null
          } else {
            _tmpCurrentChapterId = _stmt.getLong(_columnIndexOfCurrentChapterId).toInt()
          }
          _tmpData =
              Act(_tmpId,_tmpTitle,_tmpContent,_tmpIntroduction,_tmpEmotionalReview,_tmpSagaId,_tmpCurrentChapterId)
          val _tmpCurrentChapterInfo: ChapterContent?
          val _tmpKey_2: Long?
          if (_stmt.isNull(_columnIndexOfCurrentChapterId)) {
            _tmpKey_2 = null
          } else {
            _tmpKey_2 = _stmt.getLong(_columnIndexOfCurrentChapterId)
          }
          if (_tmpKey_2 != null) {
            _tmpCurrentChapterInfo = _collectionCurrentChapterInfo.get(_tmpKey_2)
          } else {
            _tmpCurrentChapterInfo = null
          }
          val _tmpChaptersCollection: MutableList<ChapterContent>
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfId)
          _tmpChaptersCollection = checkNotNull(_collectionChapters.get(_tmpKey_3))
          _result = ActContent(_tmpData,_tmpCurrentChapterInfo,_tmpChaptersCollection)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteActsForSaga(sagaId: Int) {
    val _sql: String = "DELETE FROM acts WHERE sagaId = ?"
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

  private
      fun __fetchRelationshipmessagesAscomIlustrisSagaiFeaturesSagaChatDataModelMessageContent(_connection: SQLiteConnection,
      _map: LongSparseArray<MutableList<MessageContent>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipmessagesAscomIlustrisSagaiFeaturesSagaChatDataModelMessageContent(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`text`,`timestamp`,`senderType`,`speakerName`,`sagaId`,`characterId`,`timelineId`,`emotionalTone`,`status` FROM `messages` WHERE `timelineId` IN (")
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
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "timelineId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfText: Int = 1
      val _columnIndexOfTimestamp: Int = 2
      val _columnIndexOfSenderType: Int = 3
      val _columnIndexOfSpeakerName: Int = 4
      val _columnIndexOfSagaId: Int = 5
      val _columnIndexOfCharacterId: Int = 6
      val _columnIndexOfTimelineId: Int = 7
      val _columnIndexOfEmotionalTone: Int = 8
      val _columnIndexOfStatus: Int = 9
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
      while (_stmt.step()) {
        val _tmpKey_2: Long
        _tmpKey_2 = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<MessageContent>? = _map.get(_tmpKey_2)
        if (_tmpRelation != null) {
          val _item_1: MessageContent
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
          val _tmpKey_3: Long?
          if (_stmt.isNull(_columnIndexOfCharacterId)) {
            _tmpKey_3 = null
          } else {
            _tmpKey_3 = _stmt.getLong(_columnIndexOfCharacterId)
          }
          if (_tmpKey_3 != null) {
            _tmpCharacter = _collectionCharacter.get(_tmpKey_3)
          } else {
            _tmpCharacter = null
          }
          val _tmpReactionsCollection: MutableList<ReactionContent>
          val _tmpKey_4: Long
          _tmpKey_4 = _stmt.getLong(_columnIndexOfId)
          _tmpReactionsCollection = checkNotNull(_collectionReactions.get(_tmpKey_4))
          _item_1 = MessageContent(_tmpMessage,_tmpCharacter,_tmpReactionsCollection)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimeline(_connection: SQLiteConnection,
      _map: LongSparseArray<Timeline?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimeline(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`title`,`content`,`currentObjective`,`emotionalReview`,`createdAt`,`chapterId` FROM `timelines` WHERE `id` IN (")
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
      val _columnIndexOfTitle: Int = 1
      val _columnIndexOfContent: Int = 2
      val _columnIndexOfCurrentObjective: Int = 3
      val _columnIndexOfEmotionalReview: Int = 4
      val _columnIndexOfCreatedAt: Int = 5
      val _columnIndexOfChapterId: Int = 6
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey)) {
          val _item_1: Timeline?
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
          _item_1 =
              Timeline(_tmpId,_tmpTitle,_tmpContent,_tmpCurrentObjective,_tmpEmotionalReview,_tmpCreatedAt,_tmpChapterId)
          _map.put(_tmpKey, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshipcharacterEventsAscomIlustrisSagaiFeaturesCharactersEventsDataModelCharacterEventDetails(_connection: SQLiteConnection,
      _map: LongSparseArray<MutableList<CharacterEventDetails>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipcharacterEventsAscomIlustrisSagaiFeaturesCharactersEventsDataModelCharacterEventDetails(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`characterId`,`gameTimelineId`,`title`,`summary`,`createdAt` FROM `character_events` WHERE `gameTimelineId` IN (")
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
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "gameTimelineId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfCharacterId: Int = 1
      val _columnIndexOfGameTimelineId: Int = 2
      val _columnIndexOfTitle: Int = 3
      val _columnIndexOfSummary: Int = 4
      val _columnIndexOfCreatedAt: Int = 5
      val _collectionCharacter: LongSparseArray<Character?> = LongSparseArray<Character?>()
      val _collectionTimeline: LongSparseArray<Timeline?> = LongSparseArray<Timeline?>()
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_columnIndexOfCharacterId)
        _collectionCharacter.put(_tmpKey, null)
        val _tmpKey_1: Long
        _tmpKey_1 = _stmt.getLong(_columnIndexOfGameTimelineId)
        _collectionTimeline.put(_tmpKey_1, null)
      }
      _stmt.reset()
      __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter_1(_connection,
          _collectionCharacter)
      __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimeline(_connection,
          _collectionTimeline)
      while (_stmt.step()) {
        val _tmpKey_2: Long
        _tmpKey_2 = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<CharacterEventDetails>? = _map.get(_tmpKey_2)
        if (_tmpRelation != null) {
          val _item_1: CharacterEventDetails
          val _tmpEvent: CharacterEvent
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpCharacterId: Int
          _tmpCharacterId = _stmt.getLong(_columnIndexOfCharacterId).toInt()
          val _tmpGameTimelineId: Int
          _tmpGameTimelineId = _stmt.getLong(_columnIndexOfGameTimelineId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSummary: String
          _tmpSummary = _stmt.getText(_columnIndexOfSummary)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _tmpEvent =
              CharacterEvent(_tmpId,_tmpCharacterId,_tmpGameTimelineId,_tmpTitle,_tmpSummary,_tmpCreatedAt)
          val _tmpCharacter: Character?
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfCharacterId)
          _tmpCharacter = _collectionCharacter.get(_tmpKey_3)
          if (_tmpCharacter == null) {
            error("Relationship item 'character' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'characterId' and entityColumn named 'id'.")
          }
          val _tmpTimeline: Timeline?
          val _tmpKey_4: Long
          _tmpKey_4 = _stmt.getLong(_columnIndexOfGameTimelineId)
          _tmpTimeline = _collectionTimeline.get(_tmpKey_4)
          _item_1 = CharacterEventDetails(_tmpEvent,_tmpCharacter,_tmpTimeline)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
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

  private
      fun __fetchRelationshipwikisAscomIlustrisSagaiFeaturesWikiDataModelWiki(_connection: SQLiteConnection,
      _map: LongSparseArray<MutableList<Wiki>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipwikisAscomIlustrisSagaiFeaturesWikiDataModelWiki(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`title`,`content`,`type`,`emojiTag`,`createdAt`,`sagaId`,`timelineId` FROM `wikis` WHERE `timelineId` IN (")
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
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "timelineId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfTitle: Int = 1
      val _columnIndexOfContent: Int = 2
      val _columnIndexOfType: Int = 3
      val _columnIndexOfEmojiTag: Int = 4
      val _columnIndexOfCreatedAt: Int = 5
      val _columnIndexOfSagaId: Int = 6
      val _columnIndexOfTimelineId: Int = 7
      while (_stmt.step()) {
        val _tmpKey: Long?
        if (_stmt.isNull(_itemKeyIndex)) {
          _tmpKey = null
        } else {
          _tmpKey = _stmt.getLong(_itemKeyIndex)
        }
        if (_tmpKey != null) {
          val _tmpRelation: MutableList<Wiki>? = _map.get(_tmpKey)
          if (_tmpRelation != null) {
            val _item_1: Wiki
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
            _item_1 =
                Wiki(_tmpId,_tmpTitle,_tmpContent,_tmpType,_tmpEmojiTag,_tmpCreatedAt,_tmpSagaId,_tmpTimelineId)
            _tmpRelation.add(_item_1)
          }
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshiprelationshipUpdateEventsAscomIlustrisSagaiFeaturesCharactersRelationsDataModelRelationshipUpdateEvent(_connection: SQLiteConnection,
      _map: LongSparseArray<MutableList<RelationshipUpdateEvent>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshiprelationshipUpdateEventsAscomIlustrisSagaiFeaturesCharactersRelationsDataModelRelationshipUpdateEvent(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`relationId`,`timelineId`,`title`,`description`,`emoji`,`timestamp` FROM `relationship_update_events` WHERE `relationId` IN (")
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
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "relationId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfRelationId: Int = 1
      val _columnIndexOfTimelineId: Int = 2
      val _columnIndexOfTitle: Int = 3
      val _columnIndexOfDescription: Int = 4
      val _columnIndexOfEmoji: Int = 5
      val _columnIndexOfTimestamp: Int = 6
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<RelationshipUpdateEvent>? = _map.get(_tmpKey)
        if (_tmpRelation != null) {
          val _item_1: RelationshipUpdateEvent
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpRelationId: Int
          _tmpRelationId = _stmt.getLong(_columnIndexOfRelationId).toInt()
          val _tmpTimelineId: Int
          _tmpTimelineId = _stmt.getLong(_columnIndexOfTimelineId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item_1 =
              RelationshipUpdateEvent(_tmpId,_tmpRelationId,_tmpTimelineId,_tmpTitle,_tmpDescription,_tmpEmoji,_tmpTimestamp)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshipcharacterRelationsAscomIlustrisSagaiFeaturesCharactersRelationsDataModelRelationshipContent(_connection: SQLiteConnection,
      _map: LongSparseArray<MutableList<RelationshipContent>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipcharacterRelationsAscomIlustrisSagaiFeaturesCharactersRelationsDataModelRelationshipContent(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `character_relations`.`id` AS `id`,`character_relations`.`characterOneId` AS `characterOneId`,`character_relations`.`characterTwoId` AS `characterTwoId`,`character_relations`.`sagaId` AS `sagaId`,`character_relations`.`title` AS `title`,`character_relations`.`description` AS `description`,`character_relations`.`emoji` AS `emoji`,`character_relations`.`lastUpdated` AS `lastUpdated`,_junction.`timelineId` FROM `relationship_update_events` AS _junction INNER JOIN `character_relations` ON (_junction.`relationId` = `character_relations`.`id`) WHERE _junction.`timelineId` IN (")
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
      // _junction.timelineId
      val _itemKeyIndex: Int = 8
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfCharacterOneId: Int = 1
      val _columnIndexOfCharacterTwoId: Int = 2
      val _columnIndexOfSagaId: Int = 3
      val _columnIndexOfTitle: Int = 4
      val _columnIndexOfDescription: Int = 5
      val _columnIndexOfEmoji: Int = 6
      val _columnIndexOfLastUpdated: Int = 7
      val _collectionCharacterOne: LongSparseArray<Character?> = LongSparseArray<Character?>()
      val _collectionCharacterTwo: LongSparseArray<Character?> = LongSparseArray<Character?>()
      val _collectionRelationshipEvents: LongSparseArray<MutableList<RelationshipUpdateEvent>> =
          LongSparseArray<MutableList<RelationshipUpdateEvent>>()
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_columnIndexOfCharacterOneId)
        _collectionCharacterOne.put(_tmpKey, null)
        val _tmpKey_1: Long
        _tmpKey_1 = _stmt.getLong(_columnIndexOfCharacterTwoId)
        _collectionCharacterTwo.put(_tmpKey_1, null)
        val _tmpKey_2: Long
        _tmpKey_2 = _stmt.getLong(_columnIndexOfId)
        if (!_collectionRelationshipEvents.containsKey(_tmpKey_2)) {
          _collectionRelationshipEvents.put(_tmpKey_2, mutableListOf())
        }
      }
      _stmt.reset()
      __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter_1(_connection,
          _collectionCharacterOne)
      __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter_1(_connection,
          _collectionCharacterTwo)
      __fetchRelationshiprelationshipUpdateEventsAscomIlustrisSagaiFeaturesCharactersRelationsDataModelRelationshipUpdateEvent(_connection,
          _collectionRelationshipEvents)
      while (_stmt.step()) {
        val _tmpKey_3: Long
        _tmpKey_3 = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<RelationshipContent>? = _map.get(_tmpKey_3)
        if (_tmpRelation != null) {
          val _item_1: RelationshipContent
          val _tmpData: CharacterRelation
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpCharacterOneId: Int
          _tmpCharacterOneId = _stmt.getLong(_columnIndexOfCharacterOneId).toInt()
          val _tmpCharacterTwoId: Int
          _tmpCharacterTwoId = _stmt.getLong(_columnIndexOfCharacterTwoId).toInt()
          val _tmpSagaId: Int
          _tmpSagaId = _stmt.getLong(_columnIndexOfSagaId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpLastUpdated: Long
          _tmpLastUpdated = _stmt.getLong(_columnIndexOfLastUpdated)
          _tmpData =
              CharacterRelation(_tmpId,_tmpCharacterOneId,_tmpCharacterTwoId,_tmpSagaId,_tmpTitle,_tmpDescription,_tmpEmoji,_tmpLastUpdated)
          val _tmpCharacterOne: Character?
          val _tmpKey_4: Long
          _tmpKey_4 = _stmt.getLong(_columnIndexOfCharacterOneId)
          _tmpCharacterOne = _collectionCharacterOne.get(_tmpKey_4)
          if (_tmpCharacterOne == null) {
            error("Relationship item 'characterOne' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'characterOneId' and entityColumn named 'id'.")
          }
          val _tmpCharacterTwo: Character?
          val _tmpKey_5: Long
          _tmpKey_5 = _stmt.getLong(_columnIndexOfCharacterTwoId)
          _tmpCharacterTwo = _collectionCharacterTwo.get(_tmpKey_5)
          if (_tmpCharacterTwo == null) {
            error("Relationship item 'characterTwo' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'characterTwoId' and entityColumn named 'id'.")
          }
          val _tmpRelationshipEventsCollection: MutableList<RelationshipUpdateEvent>
          val _tmpKey_6: Long
          _tmpKey_6 = _stmt.getLong(_columnIndexOfId)
          _tmpRelationshipEventsCollection =
              checkNotNull(_collectionRelationshipEvents.get(_tmpKey_6))
          _item_1 =
              RelationshipContent(_tmpData,_tmpCharacterOne,_tmpCharacterTwo,_tmpRelationshipEventsCollection)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter_2(_connection: SQLiteConnection,
      _map: LongSparseArray<MutableList<Character>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter_2(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`name`,`backstory`,`image`,`hexColor`,`sagaId`,`joinedAt`,`firstSceneId`,`emojified`,`race`,`gender`,`ethnicity`,`height`,`weight`,`hair`,`eyes`,`mouth`,`distinctiveMarks`,`jawline`,`buildAndPosture`,`skinAppearance`,`distinguishFeatures`,`outfitDescription`,`accessories`,`carriedItems`,`skillsAndProficiencies`,`uniqueOrSignatureTalents`,`occupation`,`personality` FROM `Characters` WHERE `firstSceneId` IN (")
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
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "firstSceneId")
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
        val _tmpKey: Long?
        if (_stmt.isNull(_itemKeyIndex)) {
          _tmpKey = null
        } else {
          _tmpKey = _stmt.getLong(_itemKeyIndex)
        }
        if (_tmpKey != null) {
          val _tmpRelation: MutableList<Character>? = _map.get(_tmpKey)
          if (_tmpRelation != null) {
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
            _tmpRelation.add(_item_1)
          }
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimelineContent(_connection: SQLiteConnection,
      _map: LongSparseArray<MutableList<TimelineContent>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimelineContent(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`title`,`content`,`currentObjective`,`emotionalReview`,`createdAt`,`chapterId` FROM `timelines` WHERE `chapterId` IN (")
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
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "chapterId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfTitle: Int = 1
      val _columnIndexOfContent: Int = 2
      val _columnIndexOfCurrentObjective: Int = 3
      val _columnIndexOfEmotionalReview: Int = 4
      val _columnIndexOfCreatedAt: Int = 5
      val _columnIndexOfChapterId: Int = 6
      val _collectionMessages: LongSparseArray<MutableList<MessageContent>> =
          LongSparseArray<MutableList<MessageContent>>()
      val _collectionCharacterEventDetails: LongSparseArray<MutableList<CharacterEventDetails>> =
          LongSparseArray<MutableList<CharacterEventDetails>>()
      val _collectionUpdatedWikis: LongSparseArray<MutableList<Wiki>> =
          LongSparseArray<MutableList<Wiki>>()
      val _collectionUpdatedRelationshipDetails: LongSparseArray<MutableList<RelationshipContent>> =
          LongSparseArray<MutableList<RelationshipContent>>()
      val _collectionNewlyAppearedCharacters: LongSparseArray<MutableList<Character>> =
          LongSparseArray<MutableList<Character>>()
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_columnIndexOfId)
        if (!_collectionMessages.containsKey(_tmpKey)) {
          _collectionMessages.put(_tmpKey, mutableListOf())
        }
        val _tmpKey_1: Long
        _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
        if (!_collectionCharacterEventDetails.containsKey(_tmpKey_1)) {
          _collectionCharacterEventDetails.put(_tmpKey_1, mutableListOf())
        }
        val _tmpKey_2: Long
        _tmpKey_2 = _stmt.getLong(_columnIndexOfId)
        if (!_collectionUpdatedWikis.containsKey(_tmpKey_2)) {
          _collectionUpdatedWikis.put(_tmpKey_2, mutableListOf())
        }
        val _tmpKey_3: Long
        _tmpKey_3 = _stmt.getLong(_columnIndexOfId)
        if (!_collectionUpdatedRelationshipDetails.containsKey(_tmpKey_3)) {
          _collectionUpdatedRelationshipDetails.put(_tmpKey_3, mutableListOf())
        }
        val _tmpKey_4: Long
        _tmpKey_4 = _stmt.getLong(_columnIndexOfId)
        if (!_collectionNewlyAppearedCharacters.containsKey(_tmpKey_4)) {
          _collectionNewlyAppearedCharacters.put(_tmpKey_4, mutableListOf())
        }
      }
      _stmt.reset()
      __fetchRelationshipmessagesAscomIlustrisSagaiFeaturesSagaChatDataModelMessageContent(_connection,
          _collectionMessages)
      __fetchRelationshipcharacterEventsAscomIlustrisSagaiFeaturesCharactersEventsDataModelCharacterEventDetails(_connection,
          _collectionCharacterEventDetails)
      __fetchRelationshipwikisAscomIlustrisSagaiFeaturesWikiDataModelWiki(_connection,
          _collectionUpdatedWikis)
      __fetchRelationshipcharacterRelationsAscomIlustrisSagaiFeaturesCharactersRelationsDataModelRelationshipContent(_connection,
          _collectionUpdatedRelationshipDetails)
      __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter_2(_connection,
          _collectionNewlyAppearedCharacters)
      while (_stmt.step()) {
        val _tmpKey_5: Long
        _tmpKey_5 = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<TimelineContent>? = _map.get(_tmpKey_5)
        if (_tmpRelation != null) {
          val _item_1: TimelineContent
          val _tmpData: Timeline
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
          _tmpData =
              Timeline(_tmpId,_tmpTitle,_tmpContent,_tmpCurrentObjective,_tmpEmotionalReview,_tmpCreatedAt,_tmpChapterId)
          val _tmpMessagesCollection: MutableList<MessageContent>
          val _tmpKey_6: Long
          _tmpKey_6 = _stmt.getLong(_columnIndexOfId)
          _tmpMessagesCollection = checkNotNull(_collectionMessages.get(_tmpKey_6))
          val _tmpCharacterEventDetailsCollection: MutableList<CharacterEventDetails>
          val _tmpKey_7: Long
          _tmpKey_7 = _stmt.getLong(_columnIndexOfId)
          _tmpCharacterEventDetailsCollection =
              checkNotNull(_collectionCharacterEventDetails.get(_tmpKey_7))
          val _tmpUpdatedWikisCollection: MutableList<Wiki>
          val _tmpKey_8: Long
          _tmpKey_8 = _stmt.getLong(_columnIndexOfId)
          _tmpUpdatedWikisCollection = checkNotNull(_collectionUpdatedWikis.get(_tmpKey_8))
          val _tmpUpdatedRelationshipDetailsCollection: MutableList<RelationshipContent>
          val _tmpKey_9: Long
          _tmpKey_9 = _stmt.getLong(_columnIndexOfId)
          _tmpUpdatedRelationshipDetailsCollection =
              checkNotNull(_collectionUpdatedRelationshipDetails.get(_tmpKey_9))
          val _tmpNewlyAppearedCharactersCollection: MutableList<Character>
          val _tmpKey_10: Long
          _tmpKey_10 = _stmt.getLong(_columnIndexOfId)
          _tmpNewlyAppearedCharactersCollection =
              checkNotNull(_collectionNewlyAppearedCharacters.get(_tmpKey_10))
          _item_1 =
              TimelineContent(_tmpData,_tmpMessagesCollection,_tmpCharacterEventDetailsCollection,_tmpUpdatedWikisCollection,_tmpUpdatedRelationshipDetailsCollection,_tmpNewlyAppearedCharactersCollection)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimelineContent_1(_connection: SQLiteConnection,
      _map: LongSparseArray<TimelineContent?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimelineContent_1(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`title`,`content`,`currentObjective`,`emotionalReview`,`createdAt`,`chapterId` FROM `timelines` WHERE `id` IN (")
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
      val _columnIndexOfTitle: Int = 1
      val _columnIndexOfContent: Int = 2
      val _columnIndexOfCurrentObjective: Int = 3
      val _columnIndexOfEmotionalReview: Int = 4
      val _columnIndexOfCreatedAt: Int = 5
      val _columnIndexOfChapterId: Int = 6
      val _collectionMessages: LongSparseArray<MutableList<MessageContent>> =
          LongSparseArray<MutableList<MessageContent>>()
      val _collectionCharacterEventDetails: LongSparseArray<MutableList<CharacterEventDetails>> =
          LongSparseArray<MutableList<CharacterEventDetails>>()
      val _collectionUpdatedWikis: LongSparseArray<MutableList<Wiki>> =
          LongSparseArray<MutableList<Wiki>>()
      val _collectionUpdatedRelationshipDetails: LongSparseArray<MutableList<RelationshipContent>> =
          LongSparseArray<MutableList<RelationshipContent>>()
      val _collectionNewlyAppearedCharacters: LongSparseArray<MutableList<Character>> =
          LongSparseArray<MutableList<Character>>()
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_columnIndexOfId)
        if (!_collectionMessages.containsKey(_tmpKey)) {
          _collectionMessages.put(_tmpKey, mutableListOf())
        }
        val _tmpKey_1: Long
        _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
        if (!_collectionCharacterEventDetails.containsKey(_tmpKey_1)) {
          _collectionCharacterEventDetails.put(_tmpKey_1, mutableListOf())
        }
        val _tmpKey_2: Long
        _tmpKey_2 = _stmt.getLong(_columnIndexOfId)
        if (!_collectionUpdatedWikis.containsKey(_tmpKey_2)) {
          _collectionUpdatedWikis.put(_tmpKey_2, mutableListOf())
        }
        val _tmpKey_3: Long
        _tmpKey_3 = _stmt.getLong(_columnIndexOfId)
        if (!_collectionUpdatedRelationshipDetails.containsKey(_tmpKey_3)) {
          _collectionUpdatedRelationshipDetails.put(_tmpKey_3, mutableListOf())
        }
        val _tmpKey_4: Long
        _tmpKey_4 = _stmt.getLong(_columnIndexOfId)
        if (!_collectionNewlyAppearedCharacters.containsKey(_tmpKey_4)) {
          _collectionNewlyAppearedCharacters.put(_tmpKey_4, mutableListOf())
        }
      }
      _stmt.reset()
      __fetchRelationshipmessagesAscomIlustrisSagaiFeaturesSagaChatDataModelMessageContent(_connection,
          _collectionMessages)
      __fetchRelationshipcharacterEventsAscomIlustrisSagaiFeaturesCharactersEventsDataModelCharacterEventDetails(_connection,
          _collectionCharacterEventDetails)
      __fetchRelationshipwikisAscomIlustrisSagaiFeaturesWikiDataModelWiki(_connection,
          _collectionUpdatedWikis)
      __fetchRelationshipcharacterRelationsAscomIlustrisSagaiFeaturesCharactersRelationsDataModelRelationshipContent(_connection,
          _collectionUpdatedRelationshipDetails)
      __fetchRelationshipCharactersAscomIlustrisSagaiFeaturesCharactersDataModelCharacter_2(_connection,
          _collectionNewlyAppearedCharacters)
      while (_stmt.step()) {
        val _tmpKey_5: Long
        _tmpKey_5 = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey_5)) {
          val _item_1: TimelineContent?
          val _tmpData: Timeline
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
          _tmpData =
              Timeline(_tmpId,_tmpTitle,_tmpContent,_tmpCurrentObjective,_tmpEmotionalReview,_tmpCreatedAt,_tmpChapterId)
          val _tmpMessagesCollection: MutableList<MessageContent>
          val _tmpKey_6: Long
          _tmpKey_6 = _stmt.getLong(_columnIndexOfId)
          _tmpMessagesCollection = checkNotNull(_collectionMessages.get(_tmpKey_6))
          val _tmpCharacterEventDetailsCollection: MutableList<CharacterEventDetails>
          val _tmpKey_7: Long
          _tmpKey_7 = _stmt.getLong(_columnIndexOfId)
          _tmpCharacterEventDetailsCollection =
              checkNotNull(_collectionCharacterEventDetails.get(_tmpKey_7))
          val _tmpUpdatedWikisCollection: MutableList<Wiki>
          val _tmpKey_8: Long
          _tmpKey_8 = _stmt.getLong(_columnIndexOfId)
          _tmpUpdatedWikisCollection = checkNotNull(_collectionUpdatedWikis.get(_tmpKey_8))
          val _tmpUpdatedRelationshipDetailsCollection: MutableList<RelationshipContent>
          val _tmpKey_9: Long
          _tmpKey_9 = _stmt.getLong(_columnIndexOfId)
          _tmpUpdatedRelationshipDetailsCollection =
              checkNotNull(_collectionUpdatedRelationshipDetails.get(_tmpKey_9))
          val _tmpNewlyAppearedCharactersCollection: MutableList<Character>
          val _tmpKey_10: Long
          _tmpKey_10 = _stmt.getLong(_columnIndexOfId)
          _tmpNewlyAppearedCharactersCollection =
              checkNotNull(_collectionNewlyAppearedCharacters.get(_tmpKey_10))
          _item_1 =
              TimelineContent(_tmpData,_tmpMessagesCollection,_tmpCharacterEventDetailsCollection,_tmpUpdatedWikisCollection,_tmpUpdatedRelationshipDetailsCollection,_tmpNewlyAppearedCharactersCollection)
          _map.put(_tmpKey_5, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshipChapterAscomIlustrisSagaiFeaturesChapterDataModelChapterContent(_connection: SQLiteConnection,
      _map: LongSparseArray<ChapterContent?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshipChapterAscomIlustrisSagaiFeaturesChapterDataModelChapterContent(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`title`,`overview`,`introduction`,`currentEventId`,`coverImage`,`emotionalReview`,`createdAt`,`actId`,`featuredCharacters` FROM `Chapter` WHERE `id` IN (")
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
      val _columnIndexOfTitle: Int = 1
      val _columnIndexOfOverview: Int = 2
      val _columnIndexOfIntroduction: Int = 3
      val _columnIndexOfCurrentEventId: Int = 4
      val _columnIndexOfCoverImage: Int = 5
      val _columnIndexOfEmotionalReview: Int = 6
      val _columnIndexOfCreatedAt: Int = 7
      val _columnIndexOfActId: Int = 8
      val _columnIndexOfFeaturedCharacters: Int = 9
      val _collectionEvents: LongSparseArray<MutableList<TimelineContent>> =
          LongSparseArray<MutableList<TimelineContent>>()
      val _collectionCurrentEventInfo: LongSparseArray<TimelineContent?> =
          LongSparseArray<TimelineContent?>()
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_columnIndexOfId)
        if (!_collectionEvents.containsKey(_tmpKey)) {
          _collectionEvents.put(_tmpKey, mutableListOf())
        }
        val _tmpKey_1: Long?
        if (_stmt.isNull(_columnIndexOfCurrentEventId)) {
          _tmpKey_1 = null
        } else {
          _tmpKey_1 = _stmt.getLong(_columnIndexOfCurrentEventId)
        }
        if (_tmpKey_1 != null) {
          _collectionCurrentEventInfo.put(_tmpKey_1, null)
        }
      }
      _stmt.reset()
      __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimelineContent(_connection,
          _collectionEvents)
      __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimelineContent_1(_connection,
          _collectionCurrentEventInfo)
      while (_stmt.step()) {
        val _tmpKey_2: Long
        _tmpKey_2 = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey_2)) {
          val _item_1: ChapterContent?
          val _tmpData: Chapter
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpOverview: String
          _tmpOverview = _stmt.getText(_columnIndexOfOverview)
          val _tmpIntroduction: String
          _tmpIntroduction = _stmt.getText(_columnIndexOfIntroduction)
          val _tmpCurrentEventId: Int?
          if (_stmt.isNull(_columnIndexOfCurrentEventId)) {
            _tmpCurrentEventId = null
          } else {
            _tmpCurrentEventId = _stmt.getLong(_columnIndexOfCurrentEventId).toInt()
          }
          val _tmpCoverImage: String
          _tmpCoverImage = _stmt.getText(_columnIndexOfCoverImage)
          val _tmpEmotionalReview: String?
          if (_stmt.isNull(_columnIndexOfEmotionalReview)) {
            _tmpEmotionalReview = null
          } else {
            _tmpEmotionalReview = _stmt.getText(_columnIndexOfEmotionalReview)
          }
          val _tmpCreatedAt: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmpCreatedAt = null
          } else {
            _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmpActId: Int
          _tmpActId = _stmt.getLong(_columnIndexOfActId).toInt()
          val _tmpFeaturedCharacters: List<Int>
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfFeaturedCharacters)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfFeaturedCharacters)
          }
          val _tmp_1: List<Int>? = IntListConverter.fromString(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'kotlin.collections.List<kotlin.Int>', but it was NULL.")
          } else {
            _tmpFeaturedCharacters = _tmp_1
          }
          _tmpData =
              Chapter(_tmpId,_tmpTitle,_tmpOverview,_tmpIntroduction,_tmpCurrentEventId,_tmpCoverImage,_tmpEmotionalReview,_tmpCreatedAt,_tmpActId,_tmpFeaturedCharacters)
          val _tmpEventsCollection: MutableList<TimelineContent>
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfId)
          _tmpEventsCollection = checkNotNull(_collectionEvents.get(_tmpKey_3))
          val _tmpCurrentEventInfo: TimelineContent?
          val _tmpKey_4: Long?
          if (_stmt.isNull(_columnIndexOfCurrentEventId)) {
            _tmpKey_4 = null
          } else {
            _tmpKey_4 = _stmt.getLong(_columnIndexOfCurrentEventId)
          }
          if (_tmpKey_4 != null) {
            _tmpCurrentEventInfo = _collectionCurrentEventInfo.get(_tmpKey_4)
          } else {
            _tmpCurrentEventInfo = null
          }
          _item_1 = ChapterContent(_tmpData,_tmpEventsCollection,_tmpCurrentEventInfo)
          _map.put(_tmpKey_2, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshipChapterAscomIlustrisSagaiFeaturesChapterDataModelChapterContent_1(_connection: SQLiteConnection,
      _map: LongSparseArray<MutableList<ChapterContent>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipChapterAscomIlustrisSagaiFeaturesChapterDataModelChapterContent_1(_connection,
            _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`title`,`overview`,`introduction`,`currentEventId`,`coverImage`,`emotionalReview`,`createdAt`,`actId`,`featuredCharacters` FROM `Chapter` WHERE `actId` IN (")
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
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "actId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfTitle: Int = 1
      val _columnIndexOfOverview: Int = 2
      val _columnIndexOfIntroduction: Int = 3
      val _columnIndexOfCurrentEventId: Int = 4
      val _columnIndexOfCoverImage: Int = 5
      val _columnIndexOfEmotionalReview: Int = 6
      val _columnIndexOfCreatedAt: Int = 7
      val _columnIndexOfActId: Int = 8
      val _columnIndexOfFeaturedCharacters: Int = 9
      val _collectionEvents: LongSparseArray<MutableList<TimelineContent>> =
          LongSparseArray<MutableList<TimelineContent>>()
      val _collectionCurrentEventInfo: LongSparseArray<TimelineContent?> =
          LongSparseArray<TimelineContent?>()
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_columnIndexOfId)
        if (!_collectionEvents.containsKey(_tmpKey)) {
          _collectionEvents.put(_tmpKey, mutableListOf())
        }
        val _tmpKey_1: Long?
        if (_stmt.isNull(_columnIndexOfCurrentEventId)) {
          _tmpKey_1 = null
        } else {
          _tmpKey_1 = _stmt.getLong(_columnIndexOfCurrentEventId)
        }
        if (_tmpKey_1 != null) {
          _collectionCurrentEventInfo.put(_tmpKey_1, null)
        }
      }
      _stmt.reset()
      __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimelineContent(_connection,
          _collectionEvents)
      __fetchRelationshiptimelinesAscomIlustrisSagaiFeaturesTimelineDataModelTimelineContent_1(_connection,
          _collectionCurrentEventInfo)
      while (_stmt.step()) {
        val _tmpKey_2: Long
        _tmpKey_2 = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<ChapterContent>? = _map.get(_tmpKey_2)
        if (_tmpRelation != null) {
          val _item_1: ChapterContent
          val _tmpData: Chapter
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpOverview: String
          _tmpOverview = _stmt.getText(_columnIndexOfOverview)
          val _tmpIntroduction: String
          _tmpIntroduction = _stmt.getText(_columnIndexOfIntroduction)
          val _tmpCurrentEventId: Int?
          if (_stmt.isNull(_columnIndexOfCurrentEventId)) {
            _tmpCurrentEventId = null
          } else {
            _tmpCurrentEventId = _stmt.getLong(_columnIndexOfCurrentEventId).toInt()
          }
          val _tmpCoverImage: String
          _tmpCoverImage = _stmt.getText(_columnIndexOfCoverImage)
          val _tmpEmotionalReview: String?
          if (_stmt.isNull(_columnIndexOfEmotionalReview)) {
            _tmpEmotionalReview = null
          } else {
            _tmpEmotionalReview = _stmt.getText(_columnIndexOfEmotionalReview)
          }
          val _tmpCreatedAt: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmpCreatedAt = null
          } else {
            _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmpActId: Int
          _tmpActId = _stmt.getLong(_columnIndexOfActId).toInt()
          val _tmpFeaturedCharacters: List<Int>
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfFeaturedCharacters)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfFeaturedCharacters)
          }
          val _tmp_1: List<Int>? = IntListConverter.fromString(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'kotlin.collections.List<kotlin.Int>', but it was NULL.")
          } else {
            _tmpFeaturedCharacters = _tmp_1
          }
          _tmpData =
              Chapter(_tmpId,_tmpTitle,_tmpOverview,_tmpIntroduction,_tmpCurrentEventId,_tmpCoverImage,_tmpEmotionalReview,_tmpCreatedAt,_tmpActId,_tmpFeaturedCharacters)
          val _tmpEventsCollection: MutableList<TimelineContent>
          val _tmpKey_3: Long
          _tmpKey_3 = _stmt.getLong(_columnIndexOfId)
          _tmpEventsCollection = checkNotNull(_collectionEvents.get(_tmpKey_3))
          val _tmpCurrentEventInfo: TimelineContent?
          val _tmpKey_4: Long?
          if (_stmt.isNull(_columnIndexOfCurrentEventId)) {
            _tmpKey_4 = null
          } else {
            _tmpKey_4 = _stmt.getLong(_columnIndexOfCurrentEventId)
          }
          if (_tmpKey_4 != null) {
            _tmpCurrentEventInfo = _collectionCurrentEventInfo.get(_tmpKey_4)
          } else {
            _tmpCurrentEventInfo = null
          }
          _item_1 = ChapterContent(_tmpData,_tmpEventsCollection,_tmpCurrentEventInfo)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
