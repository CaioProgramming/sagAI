package com.ilustris.sagai.features.wiki.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ilustris.sagai.features.wiki.data.model.Wiki // Updated import
import kotlinx.coroutines.flow.Flow

@Dao
interface WikiDao {

    @Query("SELECT * FROM wikis WHERE sagaId = :sagaId ORDER BY title ASC")
    fun getWikisBySaga(sagaId: Int): Flow<List<Wiki>>

    @Query("SELECT * FROM wikis WHERE id = :wikiId LIMIT 1")
    suspend fun getWikiById(wikiId: Int): Wiki?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWiki(wiki: Wiki): Long

    @Update
    suspend fun updateWiki(wiki: Wiki)

    @Query("DELETE FROM wikis WHERE id = :wikiId")
    suspend fun deleteWiki(wikiId: Int)

    @Query("DELETE FROM wikis WHERE sagaId = :sagaId")
    suspend fun deleteWikisBySaga(sagaId: Int)
}