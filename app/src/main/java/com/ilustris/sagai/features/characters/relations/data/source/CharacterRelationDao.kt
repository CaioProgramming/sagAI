package com.ilustris.sagai.features.characters.relations.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation

@Dao
interface CharacterRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(characterRelation: CharacterRelation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelations(characterRelations: List<CharacterRelation>)
}
