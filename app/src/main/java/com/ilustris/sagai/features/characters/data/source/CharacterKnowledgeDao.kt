package com.ilustris.sagai.features.characters.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ilustris.sagai.features.characters.data.model.CharacterKnowledge

@Dao
interface CharacterKnowledgeDao {
    @Query("SELECT * FROM character_knowledge WHERE characterId = :characterId")
    suspend fun get(characterId: Int): CharacterKnowledge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(knowledge: CharacterKnowledge)
}
