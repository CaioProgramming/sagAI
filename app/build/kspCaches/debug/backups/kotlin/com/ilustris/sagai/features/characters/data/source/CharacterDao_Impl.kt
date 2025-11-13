package com.ilustris.sagai.features.characters.`data`.source

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performBlocking
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.ilustris.sagai.features.characters.`data`.model.Abilities
import com.ilustris.sagai.features.characters.`data`.model.BodyFeatures
import com.ilustris.sagai.features.characters.`data`.model.Character
import com.ilustris.sagai.features.characters.`data`.model.CharacterProfile
import com.ilustris.sagai.features.characters.`data`.model.Clothing
import com.ilustris.sagai.features.characters.`data`.model.Details
import com.ilustris.sagai.features.characters.`data`.model.FacialFeatures
import com.ilustris.sagai.features.characters.`data`.model.PhysicalTraits
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
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
public class CharacterDao_Impl(
  __db: RoomDatabase,
) : CharacterDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCharacter: EntityInsertAdapter<Character>

  private val __updateAdapterOfCharacter: EntityDeleteOrUpdateAdapter<Character>
  init {
    this.__db = __db
    this.__insertAdapterOfCharacter = object : EntityInsertAdapter<Character>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `Characters` (`id`,`name`,`backstory`,`image`,`hexColor`,`sagaId`,`joinedAt`,`firstSceneId`,`emojified`,`race`,`gender`,`ethnicity`,`height`,`weight`,`hair`,`eyes`,`mouth`,`distinctiveMarks`,`jawline`,`buildAndPosture`,`skinAppearance`,`distinguishFeatures`,`outfitDescription`,`accessories`,`carriedItems`,`skillsAndProficiencies`,`uniqueOrSignatureTalents`,`occupation`,`personality`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Character) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.backstory)
        statement.bindText(4, entity.image)
        statement.bindText(5, entity.hexColor)
        statement.bindLong(6, entity.sagaId.toLong())
        statement.bindLong(7, entity.joinedAt)
        val _tmpFirstSceneId: Int? = entity.firstSceneId
        if (_tmpFirstSceneId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpFirstSceneId.toLong())
        }
        val _tmp: Int = if (entity.emojified) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmpDetails: Details = entity.details
        val _tmpPhysicalTraits: PhysicalTraits = _tmpDetails.physicalTraits
        statement.bindText(10, _tmpPhysicalTraits.race)
        statement.bindText(11, _tmpPhysicalTraits.gender)
        statement.bindText(12, _tmpPhysicalTraits.ethnicity)
        statement.bindDouble(13, _tmpPhysicalTraits.height)
        statement.bindDouble(14, _tmpPhysicalTraits.weight)
        val _tmpFacialDetails: FacialFeatures = _tmpPhysicalTraits.facialDetails
        statement.bindText(15, _tmpFacialDetails.hair)
        statement.bindText(16, _tmpFacialDetails.eyes)
        statement.bindText(17, _tmpFacialDetails.mouth)
        statement.bindText(18, _tmpFacialDetails.distinctiveMarks)
        statement.bindText(19, _tmpFacialDetails.jawline)
        val _tmpBodyFeatures: BodyFeatures = _tmpPhysicalTraits.bodyFeatures
        statement.bindText(20, _tmpBodyFeatures.buildAndPosture)
        statement.bindText(21, _tmpBodyFeatures.skinAppearance)
        statement.bindText(22, _tmpBodyFeatures.distinguishFeatures)
        val _tmpClothing: Clothing = _tmpDetails.clothing
        statement.bindText(23, _tmpClothing.outfitDescription)
        statement.bindText(24, _tmpClothing.accessories)
        statement.bindText(25, _tmpClothing.carriedItems)
        val _tmpAbilities: Abilities = _tmpDetails.abilities
        statement.bindText(26, _tmpAbilities.skillsAndProficiencies)
        statement.bindText(27, _tmpAbilities.uniqueOrSignatureTalents)
        val _tmpProfile: CharacterProfile = entity.profile
        statement.bindText(28, _tmpProfile.occupation)
        statement.bindText(29, _tmpProfile.personality)
      }
    }
    this.__updateAdapterOfCharacter = object : EntityDeleteOrUpdateAdapter<Character>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `Characters` SET `id` = ?,`name` = ?,`backstory` = ?,`image` = ?,`hexColor` = ?,`sagaId` = ?,`joinedAt` = ?,`firstSceneId` = ?,`emojified` = ?,`race` = ?,`gender` = ?,`ethnicity` = ?,`height` = ?,`weight` = ?,`hair` = ?,`eyes` = ?,`mouth` = ?,`distinctiveMarks` = ?,`jawline` = ?,`buildAndPosture` = ?,`skinAppearance` = ?,`distinguishFeatures` = ?,`outfitDescription` = ?,`accessories` = ?,`carriedItems` = ?,`skillsAndProficiencies` = ?,`uniqueOrSignatureTalents` = ?,`occupation` = ?,`personality` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Character) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.backstory)
        statement.bindText(4, entity.image)
        statement.bindText(5, entity.hexColor)
        statement.bindLong(6, entity.sagaId.toLong())
        statement.bindLong(7, entity.joinedAt)
        val _tmpFirstSceneId: Int? = entity.firstSceneId
        if (_tmpFirstSceneId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpFirstSceneId.toLong())
        }
        val _tmp: Int = if (entity.emojified) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmpDetails: Details = entity.details
        val _tmpPhysicalTraits: PhysicalTraits = _tmpDetails.physicalTraits
        statement.bindText(10, _tmpPhysicalTraits.race)
        statement.bindText(11, _tmpPhysicalTraits.gender)
        statement.bindText(12, _tmpPhysicalTraits.ethnicity)
        statement.bindDouble(13, _tmpPhysicalTraits.height)
        statement.bindDouble(14, _tmpPhysicalTraits.weight)
        val _tmpFacialDetails: FacialFeatures = _tmpPhysicalTraits.facialDetails
        statement.bindText(15, _tmpFacialDetails.hair)
        statement.bindText(16, _tmpFacialDetails.eyes)
        statement.bindText(17, _tmpFacialDetails.mouth)
        statement.bindText(18, _tmpFacialDetails.distinctiveMarks)
        statement.bindText(19, _tmpFacialDetails.jawline)
        val _tmpBodyFeatures: BodyFeatures = _tmpPhysicalTraits.bodyFeatures
        statement.bindText(20, _tmpBodyFeatures.buildAndPosture)
        statement.bindText(21, _tmpBodyFeatures.skinAppearance)
        statement.bindText(22, _tmpBodyFeatures.distinguishFeatures)
        val _tmpClothing: Clothing = _tmpDetails.clothing
        statement.bindText(23, _tmpClothing.outfitDescription)
        statement.bindText(24, _tmpClothing.accessories)
        statement.bindText(25, _tmpClothing.carriedItems)
        val _tmpAbilities: Abilities = _tmpDetails.abilities
        statement.bindText(26, _tmpAbilities.skillsAndProficiencies)
        statement.bindText(27, _tmpAbilities.uniqueOrSignatureTalents)
        val _tmpProfile: CharacterProfile = entity.profile
        statement.bindText(28, _tmpProfile.occupation)
        statement.bindText(29, _tmpProfile.personality)
        statement.bindLong(30, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertCharacter(character: Character): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfCharacter.insertAndReturnId(_connection, character)
    _result
  }

  public override suspend fun updateCharacter(character: Character): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfCharacter.handle(_connection, character)
  }

  public override fun getAllCharacters(): Flow<List<Character>> {
    val _sql: String = "SELECT * FROM Characters ORDER BY id ASC"
    return createFlow(__db, false, arrayOf("Characters")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBackstory: Int = getColumnIndexOrThrow(_stmt, "backstory")
        val _columnIndexOfImage: Int = getColumnIndexOrThrow(_stmt, "image")
        val _columnIndexOfHexColor: Int = getColumnIndexOrThrow(_stmt, "hexColor")
        val _columnIndexOfSagaId: Int = getColumnIndexOrThrow(_stmt, "sagaId")
        val _columnIndexOfJoinedAt: Int = getColumnIndexOrThrow(_stmt, "joinedAt")
        val _columnIndexOfFirstSceneId: Int = getColumnIndexOrThrow(_stmt, "firstSceneId")
        val _columnIndexOfEmojified: Int = getColumnIndexOrThrow(_stmt, "emojified")
        val _columnIndexOfRace: Int = getColumnIndexOrThrow(_stmt, "race")
        val _columnIndexOfGender: Int = getColumnIndexOrThrow(_stmt, "gender")
        val _columnIndexOfEthnicity: Int = getColumnIndexOrThrow(_stmt, "ethnicity")
        val _columnIndexOfHeight: Int = getColumnIndexOrThrow(_stmt, "height")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfHair: Int = getColumnIndexOrThrow(_stmt, "hair")
        val _columnIndexOfEyes: Int = getColumnIndexOrThrow(_stmt, "eyes")
        val _columnIndexOfMouth: Int = getColumnIndexOrThrow(_stmt, "mouth")
        val _columnIndexOfDistinctiveMarks: Int = getColumnIndexOrThrow(_stmt, "distinctiveMarks")
        val _columnIndexOfJawline: Int = getColumnIndexOrThrow(_stmt, "jawline")
        val _columnIndexOfBuildAndPosture: Int = getColumnIndexOrThrow(_stmt, "buildAndPosture")
        val _columnIndexOfSkinAppearance: Int = getColumnIndexOrThrow(_stmt, "skinAppearance")
        val _columnIndexOfDistinguishFeatures: Int = getColumnIndexOrThrow(_stmt,
            "distinguishFeatures")
        val _columnIndexOfOutfitDescription: Int = getColumnIndexOrThrow(_stmt, "outfitDescription")
        val _columnIndexOfAccessories: Int = getColumnIndexOrThrow(_stmt, "accessories")
        val _columnIndexOfCarriedItems: Int = getColumnIndexOrThrow(_stmt, "carriedItems")
        val _columnIndexOfSkillsAndProficiencies: Int = getColumnIndexOrThrow(_stmt,
            "skillsAndProficiencies")
        val _columnIndexOfUniqueOrSignatureTalents: Int = getColumnIndexOrThrow(_stmt,
            "uniqueOrSignatureTalents")
        val _columnIndexOfOccupation: Int = getColumnIndexOrThrow(_stmt, "occupation")
        val _columnIndexOfPersonality: Int = getColumnIndexOrThrow(_stmt, "personality")
        val _result: MutableList<Character> = mutableListOf()
        while (_stmt.step()) {
          val _item: Character
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
          _item =
              Character(_tmpId,_tmpName,_tmpBackstory,_tmpImage,_tmpHexColor,_tmpSagaId,_tmpDetails,_tmpProfile,_tmpJoinedAt,_tmpFirstSceneId,_tmpEmojified)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCharacterById(characterId: Int): Character? {
    val _sql: String = "SELECT * FROM Characters WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, characterId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBackstory: Int = getColumnIndexOrThrow(_stmt, "backstory")
        val _columnIndexOfImage: Int = getColumnIndexOrThrow(_stmt, "image")
        val _columnIndexOfHexColor: Int = getColumnIndexOrThrow(_stmt, "hexColor")
        val _columnIndexOfSagaId: Int = getColumnIndexOrThrow(_stmt, "sagaId")
        val _columnIndexOfJoinedAt: Int = getColumnIndexOrThrow(_stmt, "joinedAt")
        val _columnIndexOfFirstSceneId: Int = getColumnIndexOrThrow(_stmt, "firstSceneId")
        val _columnIndexOfEmojified: Int = getColumnIndexOrThrow(_stmt, "emojified")
        val _columnIndexOfRace: Int = getColumnIndexOrThrow(_stmt, "race")
        val _columnIndexOfGender: Int = getColumnIndexOrThrow(_stmt, "gender")
        val _columnIndexOfEthnicity: Int = getColumnIndexOrThrow(_stmt, "ethnicity")
        val _columnIndexOfHeight: Int = getColumnIndexOrThrow(_stmt, "height")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfHair: Int = getColumnIndexOrThrow(_stmt, "hair")
        val _columnIndexOfEyes: Int = getColumnIndexOrThrow(_stmt, "eyes")
        val _columnIndexOfMouth: Int = getColumnIndexOrThrow(_stmt, "mouth")
        val _columnIndexOfDistinctiveMarks: Int = getColumnIndexOrThrow(_stmt, "distinctiveMarks")
        val _columnIndexOfJawline: Int = getColumnIndexOrThrow(_stmt, "jawline")
        val _columnIndexOfBuildAndPosture: Int = getColumnIndexOrThrow(_stmt, "buildAndPosture")
        val _columnIndexOfSkinAppearance: Int = getColumnIndexOrThrow(_stmt, "skinAppearance")
        val _columnIndexOfDistinguishFeatures: Int = getColumnIndexOrThrow(_stmt,
            "distinguishFeatures")
        val _columnIndexOfOutfitDescription: Int = getColumnIndexOrThrow(_stmt, "outfitDescription")
        val _columnIndexOfAccessories: Int = getColumnIndexOrThrow(_stmt, "accessories")
        val _columnIndexOfCarriedItems: Int = getColumnIndexOrThrow(_stmt, "carriedItems")
        val _columnIndexOfSkillsAndProficiencies: Int = getColumnIndexOrThrow(_stmt,
            "skillsAndProficiencies")
        val _columnIndexOfUniqueOrSignatureTalents: Int = getColumnIndexOrThrow(_stmt,
            "uniqueOrSignatureTalents")
        val _columnIndexOfOccupation: Int = getColumnIndexOrThrow(_stmt, "occupation")
        val _columnIndexOfPersonality: Int = getColumnIndexOrThrow(_stmt, "personality")
        val _result: Character?
        if (_stmt.step()) {
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
          _result =
              Character(_tmpId,_tmpName,_tmpBackstory,_tmpImage,_tmpHexColor,_tmpSagaId,_tmpDetails,_tmpProfile,_tmpJoinedAt,_tmpFirstSceneId,_tmpEmojified)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getCharacterByName(name: String, sagaId: Int): Character? {
    val _sql: String = "SELECT * FROM Characters WHERE name LIKE ? AND sagaId = ? LIMIT 1"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        _argIndex = 2
        _stmt.bindLong(_argIndex, sagaId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBackstory: Int = getColumnIndexOrThrow(_stmt, "backstory")
        val _columnIndexOfImage: Int = getColumnIndexOrThrow(_stmt, "image")
        val _columnIndexOfHexColor: Int = getColumnIndexOrThrow(_stmt, "hexColor")
        val _columnIndexOfSagaId: Int = getColumnIndexOrThrow(_stmt, "sagaId")
        val _columnIndexOfJoinedAt: Int = getColumnIndexOrThrow(_stmt, "joinedAt")
        val _columnIndexOfFirstSceneId: Int = getColumnIndexOrThrow(_stmt, "firstSceneId")
        val _columnIndexOfEmojified: Int = getColumnIndexOrThrow(_stmt, "emojified")
        val _columnIndexOfRace: Int = getColumnIndexOrThrow(_stmt, "race")
        val _columnIndexOfGender: Int = getColumnIndexOrThrow(_stmt, "gender")
        val _columnIndexOfEthnicity: Int = getColumnIndexOrThrow(_stmt, "ethnicity")
        val _columnIndexOfHeight: Int = getColumnIndexOrThrow(_stmt, "height")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfHair: Int = getColumnIndexOrThrow(_stmt, "hair")
        val _columnIndexOfEyes: Int = getColumnIndexOrThrow(_stmt, "eyes")
        val _columnIndexOfMouth: Int = getColumnIndexOrThrow(_stmt, "mouth")
        val _columnIndexOfDistinctiveMarks: Int = getColumnIndexOrThrow(_stmt, "distinctiveMarks")
        val _columnIndexOfJawline: Int = getColumnIndexOrThrow(_stmt, "jawline")
        val _columnIndexOfBuildAndPosture: Int = getColumnIndexOrThrow(_stmt, "buildAndPosture")
        val _columnIndexOfSkinAppearance: Int = getColumnIndexOrThrow(_stmt, "skinAppearance")
        val _columnIndexOfDistinguishFeatures: Int = getColumnIndexOrThrow(_stmt,
            "distinguishFeatures")
        val _columnIndexOfOutfitDescription: Int = getColumnIndexOrThrow(_stmt, "outfitDescription")
        val _columnIndexOfAccessories: Int = getColumnIndexOrThrow(_stmt, "accessories")
        val _columnIndexOfCarriedItems: Int = getColumnIndexOrThrow(_stmt, "carriedItems")
        val _columnIndexOfSkillsAndProficiencies: Int = getColumnIndexOrThrow(_stmt,
            "skillsAndProficiencies")
        val _columnIndexOfUniqueOrSignatureTalents: Int = getColumnIndexOrThrow(_stmt,
            "uniqueOrSignatureTalents")
        val _columnIndexOfOccupation: Int = getColumnIndexOrThrow(_stmt, "occupation")
        val _columnIndexOfPersonality: Int = getColumnIndexOrThrow(_stmt, "personality")
        val _result: Character?
        if (_stmt.step()) {
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
          _result =
              Character(_tmpId,_tmpName,_tmpBackstory,_tmpImage,_tmpHexColor,_tmpSagaId,_tmpDetails,_tmpProfile,_tmpJoinedAt,_tmpFirstSceneId,_tmpEmojified)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCharacter(characterId: Int) {
    val _sql: String = "DELETE FROM Characters WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, characterId.toLong())
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
